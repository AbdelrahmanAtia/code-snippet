package spring_boot._3_application_events._6_ordered_listeners;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

	private static final Logger logger = LoggerFactory.getLogger(RestAppEventListener.class);

	@EventListener(ContextRefreshedEvent.class)
	@Order(1)
	public void restAppHandler(ApplicationEvent applicationEvent) {

		logger.info("inside first listener..");

	}

	@EventListener(ContextRefreshedEvent.class)
	@Order(2)
	public void restAppHandler2(ApplicationEvent applicationEvent) {

		logger.info("inside second listener..");

	}

	@EventListener(ContextRefreshedEvent.class)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void restAppHandler3(ApplicationEvent applicationEvent) {

		logger.info("inside highest precedence listener..");

	}
}
