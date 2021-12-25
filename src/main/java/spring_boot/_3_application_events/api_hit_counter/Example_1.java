package spring_boot._3_application_events.api_hit_counter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

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

}

@RestController
@RequestMapping("/country")
class CountryController {

	@GetMapping("/all")
	public List<String> getAllCountries() {
		return Arrays.asList("Egypt", "Germany", "Austria");
	}
}

@Component
class CounterService {

	private MetricRegistry meterRegistry = new MetricRegistry();

	private Map<String, Counter> counters = new HashMap<>();

	public CounterService() {
		counters.put("url.country.all.hits", this.meterRegistry.counter("url.country.all.hits"));
	}

	public void increment(String counterName) {
		counters.get(counterName).inc();
	}
	
	public long getCount(String counterName) {
		return counters.get(counterName).getCount();
	}

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Log {

	boolean printParamsValues() default false;

	String callMethodWithNoParamsToString() default "toString";

}

@Aspect
@Component
class CodeLogger {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger(CodeLogger.class);

	@Pointcut("execution(@spring_boot._3_application_events.api_hit_counter.Log * spring_boot._3_application_events.api_hit_counter.*.*(..)) && @annotation(codeLog)")
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

@Component
class RestApiEventsListener implements ApplicationListener<ApplicationEvent> {

	private static final String ALL_COUNTRIES_REQUEST_URL = "/country/all";

	@Autowired
	private CounterService counterService;

	@Log(printParamsValues = true)
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ServletRequestHandledEvent) {
			if (((ServletRequestHandledEvent) event).getRequestUrl().equals(ALL_COUNTRIES_REQUEST_URL)) {
				counterService.increment("url.country.all.hits");
				
				/**
				 * note
				 * =======
				 * this counter "url.country.all.hits" should be exposed through the 
				 * actuator endpoint /actuator/metrics but it is not viewing
				 * any custom metrics it shows only the predefined ones..
				 * so, i'm just printing the counter value here to just validate that 
				 * it is working fine until i found a solution to view it 
				 * through the actuator metrics end point..
				 */
				
				System.out.println(counterService.getCount("url.country.all.hits"));
			}
		}
	}
}
