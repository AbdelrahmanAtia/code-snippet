package spring_boot._6_basic_features._3_create_beans_based_on_custom_conditions;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example1 {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("logging.level.org.springframework.web", "DEBUG");
		
		//these properties usually set inside the application.properties file 
		properties.put("spring.activemq.broker-url", "tcp://localhost:61616");
		properties.put("spring.activemq.in-memory", "true");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}
	
	/**
	 * there already exist some annotations such as:-
	 * @ConditionalOnProperty >> but it will not help u in some complex scenarios such as this example cause 
	 *  you can't make an OR operation between properties existence but u can make an AND operation
	 * @ConditionalOnExpression >> might be useful in complex scenarios but the expression that u will write
	 * will be more complex and hard to debug if it contains an error
	 * so in that cases, it's better to use custom conditions 
	 */
	
	//create the test bean when any of these properties 
	//["spring.activemq.broker-url" OR "spring.activemq.in-memory"] exist
	@Conditional(ActiveMqCondition.class)
	@Bean
	public TestBean testBean() {
		System.out.println("creating test bean...");
		return new TestBean();
	}

}

class TestBean {

}


class ActiveMqCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Environment env = context.getEnvironment();

		String activeMqUrl = env.getProperty("spring.activemq.broker-url");
		String inMemoryActiveMqEnabled = env.getProperty("spring.activemq.in-memory");

		System.out.println("===================================");
		System.out.println("activeMqUrl: " + activeMqUrl);
		System.out.println("inMemoryActiveMqEnabled: " + inMemoryActiveMqEnabled);
		System.out.println("===================================");

		return activeMqUrl != null || (inMemoryActiveMqEnabled != null && Boolean.parseBoolean(inMemoryActiveMqEnabled));
	}
}
