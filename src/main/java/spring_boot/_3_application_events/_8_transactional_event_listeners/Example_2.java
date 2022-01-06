package spring_boot._3_application_events._8_transactional_event_listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example_2 {
	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("spring.jpa.show-sql", true);
		properties.put("management.endpoints.web.exposure.include", "*");

		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example_2.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(CurrencyService service) {
		return (args) -> {
			service.saveRate(new Rate("EUR", 0.88857F, new Date()));
			service.saveRate(new Rate("JPY", 102.17F, new Date()));
			service.saveRate(new Rate("MXN", 19.232F, new Date()));
			service.saveRate(new Rate("GBP", 0.75705F, new Date()));
		};
	}
}

//======================================  Service ================================================================//

@Service
class CurrencyService {
	private RateRepository repository;
	private ApplicationEventPublisher publisher;

	public CurrencyService(RateRepository repository, ApplicationEventPublisher publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Transactional
	public void saveRate(Rate rate) {
		repository.save(new Rate(rate.getCode(), rate.getRate(), rate.getDate()));
		publisher.publishEvent(new CurrencyEvent(this, rate));
	}
}

//======================================  Repository ===========================================================//

interface RateRepository extends JpaRepository<Rate, String> {

}

//======================================  Domain ================================================================//

@Entity
class Rate {

	@Id
	private String code;
	private Float rate;

	@JsonIgnore
	@Temporal(TemporalType.DATE)
	private Date date;

	public Rate() {
	}

	public Rate(String base, Float rate, Date date) {
		super();
		this.code = base;
		this.rate = rate;
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String base) {
		this.code = base;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		String format = new SimpleDateFormat("yyyy-MM-dd").format(date);
		return "Rate [code=" + code + ", rate=" + rate + ", date=" + format + "]";
	}

}

//======================================  Event ================================================================//

class CurrencyEvent extends ApplicationEvent {

	private static final long serialVersionUID = 889202626288526113L;
	private Rate rate;

	public CurrencyEvent(Object source, Rate rate) {
		super(source);
		this.rate = rate;
	}

	public Rate getRate() {
		return this.rate;
	}
}

//======================================  Listener ================================================================//

@Component
class RateEventListener {

	// @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	@TransactionalEventListener
    @Log(printParamsValues=true,callMethodWithNoParamsToString="getRate")
	public void processEvent(CurrencyEvent event) {

	}
}

//======================================  Annotation ================================================================//

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Log {

	boolean printParamsValues() default false;

	String callMethodWithNoParamsToString() default "toString";

}

//======================================  Logger ================================================================//

@Aspect
@Component
class CodeLogger {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger(CodeLogger.class);

	@Pointcut("execution(@spring_boot._3_application_events._8_transactional_event_listeners.Log * spring_boot._3_application_events._8_transactional_event_listeners.*.*(..)) && @annotation(codeLog)")
	public void codeLogger(Log codeLog) {
		
	}

	@Before("codeLogger(codeLog)")
	public void doCodeLogger(JoinPoint jp, Log codeLog) {
		StringBuilder str = new StringBuilder(NEXT_LINE);
		str.append(DASH_LINE);
		str.append(NEXT_LINE);
		str.append(" Class: " + jp.getTarget().getClass().getSimpleName());
		str.append(NEXT_LINE);
		str.append("Method: " + jp.getSignature().getName());
		str.append(NEXT_LINE);
		if (codeLog.printParamsValues()) {
			Object[] args = jp.getArgs();
			str.append(NEXT_LINE);
			for (Object obj : args) {
				str.append(" Param: " + obj.getClass().getSimpleName());
				str.append(NEXT_LINE);

				try {
					String methodToCall = codeLog.callMethodWithNoParamsToString();

					if ("toString".equals(methodToCall))
						str.append(" Value: " + obj);
					else
						str.append(" Value: " + obj.getClass().getDeclaredMethod(methodToCall, new Class[] {})
								.invoke(obj, new Object[] {}));
				} catch (Exception e) {
					str.append(" Value: [ERROR]> " + e.getMessage());
				}
				str.append(NEXT_LINE);
			}
		}
		str.append(DASH_LINE);
		log.info(str.toString());
	}
}