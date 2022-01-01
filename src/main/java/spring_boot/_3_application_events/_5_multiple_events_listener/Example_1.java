package spring_boot._3_application_events._5_multiple_events_listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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

@Component
class RestAppEventListener {
	
	@EventListener({ContextRefreshedEvent.class, ServletWebServerInitializedEvent.class})
	@Log(printParamsValues = true)
	public void restAppHandler(ApplicationEvent applicationEvent) {

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

	@Pointcut("execution(@spring_boot._3_application_events._5_multiple_events_listener.Log * spring_boot._3_application_events._5_multiple_events_listener.*.*(..)) && @annotation(codeLog)")
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
