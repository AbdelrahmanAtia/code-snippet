package spring_boot._3_application_events.custom_events;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example_1 {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("spring.jpa.show-sql", true);
		properties.put("management.endpoints.web.exposure.include", "*");

		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example_1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(RateRepository repository) {
		return (args) -> {
			repository.save(new Rate("EUR", 0.88857F, new Date()));
			repository.save(new Rate("JPY", 102.17F, new Date()));
			repository.save(new Rate("MXN", 19.232F, new Date()));
			repository.save(new Rate("GBP", 0.75705F, new Date()));
		};
	}

}

////////////////================================Controller==================================///////

@RestController
@RequestMapping("/currency")
class CurrencyController {

	private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);

	@Autowired
	CurrencyConversionService conversionService;

	@RequestMapping("/{amount}/{base}/to/{code}")
	public ResponseEntity<CurrencyConversion> conversion(@PathVariable("amount") Float amount,
			@PathVariable("base") String base, @PathVariable("code") String code) {
		CurrencyConversion conversionResult = conversionService.convertFromTo(base, code, amount);
		return new ResponseEntity<CurrencyConversion>(conversionResult, HttpStatus.OK);
	}

}

//=========================================Service================================================================//

@Service
class CurrencyConversionService {

	@Autowired
	RateRepository repository;

	public CurrencyConversion convertFromTo(/* @ToUpper */ String base, /* @ToUpper */ String code, Float amount) {
		Rate baseRate = new Rate(CurrencyExchange.BASE_CODE, 1.0F, new Date());
		Rate codeRate = new Rate(CurrencyExchange.BASE_CODE, 1.0F, new Date());

		if (!CurrencyExchange.BASE_CODE.equals(base))
			baseRate = repository.findByDateAndCode(new Date(), base);

		if (!CurrencyExchange.BASE_CODE.equals(code))
			codeRate = repository.findByDateAndCode(new Date(), code);

		if (null == codeRate || null == baseRate)
			throw new BadCodeRuntimeException("Bad Code Base, unknown code: " + base,
					new CurrencyConversion(base, code, amount, -1F));

		return new CurrencyConversion(base, code, amount, (codeRate.getRate() / baseRate.getRate()) * amount);
	}

}

//=======================================  Repository  ======================================================//

interface RateRepository extends JpaRepository<Rate, String> {

	List<Rate> findByDate(Date date);

	Rate findByDateAndCode(Date date, String code);
}

//=========================================Domain================================================================//

class CurrencyConversion {

	private String base;
	private String code;
	private float amount;
	private float total;

	public CurrencyConversion() {
	}

	public CurrencyConversion(String base, String code, float amount, float total) {
		super();
		this.base = base;
		this.code = code;
		this.amount = amount;
		this.total = total;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getTotal() {
		return total;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "CurrencyConversion [base=" + base + ", code=" + code + ", amount=" + amount + ", total=" + total + "]";
	}
}

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

class CurrencyExchange {

	public static final String BASE_CODE = "USD";
	private String base;
	private String date;
	private Rate[] rates;

	public CurrencyExchange(String base, String date, Rate[] rates) {
		super();
		this.base = base;
		this.date = date;
		this.rates = rates;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Rate[] getRates() {
		return rates;
	}

	public void setRates(Rate[] rates) {
		this.rates = rates;
	}

	@Override
	public String toString() {
		return "CurrencyExchange [base=" + base + ", date=" + date + ", rates=" + Arrays.toString(rates) + "]";
	}
}

//======================================  Exception ================================================================//

class BadCodeRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -2411444965751028974L;
	private CurrencyConversion conversion;
	private Rate rate;

	public BadCodeRuntimeException(String message) {
		super(message);
	}

	public BadCodeRuntimeException(String message, CurrencyConversion conversion) {
		super(message);
		this.conversion = conversion;
	}

	public BadCodeRuntimeException(String message, Rate rate) {
		super(message);
		this.rate = rate;
	}

	public CurrencyConversion getConversion() {
		return conversion;
	}

	public Rate getRate() {
		return rate;
	}
}

//======================================  Event ================================================================//

class CurrencyConversionEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6218089386428601881L;

	private CurrencyConversion conversion;
	private String message;

	public CurrencyConversionEvent(Object source, CurrencyConversion conversion) {
		super(source);
		this.conversion = conversion;
	}

	public CurrencyConversionEvent(Object source, String message, CurrencyConversion conversion) {
		super(source);
		this.message = message;
		this.conversion = conversion;
	}

	public CurrencyConversion getConversion() {
		return conversion;
	}

	public String getMessage() {
		return message;
	}

}

//======================================  Listener ================================================================//

@Component
class CurrencyConversionEventListener implements ApplicationListener<CurrencyConversionEvent> {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger(CurrencyConversionEventListener.class);

	@Override
	public void onApplicationEvent(CurrencyConversionEvent event) {
		Object obj = event.getSource();
		StringBuilder str = new StringBuilder(NEXT_LINE);
		str.append(DASH_LINE);
		str.append(NEXT_LINE);
		str.append("  Class: " + obj.getClass().getSimpleName());
		str.append(NEXT_LINE);
		str.append("Message: " + event.getMessage());
		str.append(NEXT_LINE);
		str.append("  Value: " + event.getConversion());
		str.append(NEXT_LINE);
		str.append(DASH_LINE);
		log.error(str.toString());

	}

}

//======================================  Event Publisher ========================================//

@Aspect
@Component
class CurrencyConversionAudit {
	
	private ApplicationEventPublisher publisher;
	
	@Autowired
	public CurrencyConversionAudit(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	@Pointcut("execution(* spring_boot._3_application_events.custom_events.*Service.*(..))")
    public void exceptionPointcut() {}
	
	@AfterThrowing(pointcut="exceptionPointcut()", throwing="ex")
	public void badCodeException(JoinPoint jp, BadCodeRuntimeException ex){
		
		/*
		 * to publish an event, the system must throw a BadCodeRuntimeException
		 * to do that, call the following url:- 
		 * http://localhost:8080/currency/10/USDX/to/MX 
		 * 
		 */
		
		if(ex.getConversion()!=null){
			publisher.publishEvent(new CurrencyConversionEvent(jp.getTarget(), ex.getMessage(), ex.getConversion()));
		}
	}
	
}

