package spring_boot._4_jms.using_spring_boot_jms_template._1_consumer_without_annotations;

import java.lang.reflect.Parameter;
import java.util.stream.IntStream;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

//this commented annotation is used to scan any class annotated with @ConfigurationProperties and 
//makes it a managed bean in spring we commented it because we have another alternative which is 
//@EnableConfigurationProperties that annotates the listener configuration class

//@ConfigurationPropertiesScan 
@SpringBootApplication
public class JmsSenderApplication {
	public static void main(String[] args) {
		SpringApplication.run(JmsSenderApplication.class, args);
	}

	@Bean
	CommandLineRunner simple(JMSProperties props, SimpleSender sender) {
		return args -> {
			sender.sendMessage(props.getQueue(), "Hello World");
		};
	}
}

//======================================  Sender ==========================================//

@Component
class SimpleSender {

	private JmsTemplate jmsTemplate;

	@Autowired
	public SimpleSender(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendMessage(String destination, String message) {
		this.jmsTemplate.convertAndSend(destination, message);
	}
}

//======================================  Listener =========================================//

@Configuration
@EnableConfigurationProperties(JMSProperties.class)
class JMSConfig { // this class configures the listener..without it the listener will not listen
					// to any thing
	@Bean
	public DefaultMessageListenerContainer customMessageListenerContainer(ConnectionFactory connectionFactory,
			MessageListener queueListener, @Value("${apress.jms.queue}") final String destinationName) {
		DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
		listener.setConnectionFactory(connectionFactory);
		listener.setDestinationName(destinationName);
		listener.setMessageListener(queueListener);
		return listener;
	}
}

@Component
class QueueListener implements MessageListener {

	public void onMessage(Message message) {

	}
}

//======================================  Properties =========================================//

@ConfigurationProperties(prefix = "apress.jms")
class JMSProperties {

	private String queue;
	private String rateQueue;
	private String rateReplyQueue;
	private String topic;

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getRateQueue() {
		return rateQueue;
	}

	public void setRateQueue(String rateQueue) {
		this.rateQueue = rateQueue;
	}

	public String getRateReplyQueue() {
		return rateReplyQueue;
	}

	public void setRateReplyQueue(String rateReplyQueue) {
		this.rateReplyQueue = rateReplyQueue;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

}

//======================================  Auditing =========================================//

@Aspect
@Component
class JMSAudit {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger("JMSAudit");

	@Pointcut("execution(* spring_boot._4_jms.using_spring_boot_jms_template._1_consumer_without_annotations.*.*(..))")
	public void logJms() {
	};

	@Around("logJms()")
	public Object jmsAudit(ProceedingJoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();
		Parameter[] parameters = ((MethodSignature) pjp.getSignature()).getMethod().getParameters();

		StringBuilder builder = new StringBuilder(NEXT_LINE);
		builder.append(DASH_LINE);
		builder.append(NEXT_LINE);
		builder.append("[BEFORE]");
		builder.append(NEXT_LINE);
		builder.append("Method: ");
		builder.append(pjp.getSignature().getName());
		builder.append(NEXT_LINE);
		builder.append("Params: ");
		builder.append(NEXT_LINE);
		IntStream.range(0, args.length).forEach(index -> {
			builder.append("> ");
			builder.append(parameters[index].getName());
			builder.append(": ");
			builder.append(args[index]);
			builder.append(NEXT_LINE);
		});
		builder.append(DASH_LINE);
		log.info(builder.toString());

		Object object = pjp.proceed(args);

		// Some Extra logging [AFTER]

		return object;
	}
}
