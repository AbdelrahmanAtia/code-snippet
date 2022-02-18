package spring_boot._5_amqp._7_bolck_unblock_and_shutdown_events;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.stream.IntStream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import spring_boot._0_util.ExamplesUtil;

@SpringBootApplication
public class AmqpDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}

	// exchange is an empty string since there is no property apress.amqp.exchange in the properties file
	// routing key is similar to the queue name since we are using the default exchange type. this might change according to our needs.
	@Bean
	CommandLineRunner simple(@Value("${apress.amqp.exchange:}") String exchange,
			@Value("${apress.amqp.queue}") String routingKey, Producer producer) {
		return args -> {
			for(int i = 0; i < 100000; i++) {
				//System.out.println("sending message number " + i);
				
				//send a big size messages to consume the memory and 
				//to force the rabbitMQ to make queue enters the "flow" state
				String messageOfSize1MB = ExamplesUtil.createDataSize(1024);
				producer.sendMessage(exchange, routingKey, messageOfSize1MB);
			}
		};
	}
}

//======================================  Producer ==========================================//

@Component
class Producer {
	private RabbitTemplate template;

	@Autowired
	public Producer(RabbitTemplate template) {
		this.template = template;
	}

	public void sendMessage(String exchange, String routingKey, String message) {
		this.template.convertAndSend(exchange, routingKey, message);
	}
}

//======================================  Consumer ==========================================//

@Component
class AnnotatedConsumer {
	
	@RabbitListener(queues = "${apress.amqp.queue}")
	public void process(String message) throws InterruptedException {
		
		//delay the listener so that the messages not consumed fast 
		//and the queue enters the "flow" state
		Thread.sleep(5000L);
	}
}

//======================================  Configuration =========================================//

@Configuration
@EnableConfigurationProperties(AMQPProperties.class)
class AMQPConfig {
	
	
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		
		template.execute(new ChannelCallback<Object>() {

			@Override
			public Object doInRabbit(Channel channel) throws Exception {

				channel.getConnection().addBlockedListener(new BlockedListener() {
					
					//TODO: i tried to throttle the connection by sending many large messages
					//but the block & unblock events are never triggered.
					public void handleUnblocked() throws IOException {
						// Resume business logic
						System.out.println("UnBlocked");
					}

					public void handleBlocked(String reason) throws IOException {
						// FlowControl -> Logic to handle block
						System.out.println("Blocked");
					}
				});
				
				//when u stop the rabbitMQ service, this event will be triggered.
				channel.getConnection().addShutdownListener(new ShutdownListener() {
					public void shutdownCompleted(ShutdownSignalException cause) {
						
						System.out.println("Rabbit Shutdown");
					}
				});

				return null;
			}

		});
		
		return template;
	}
	
	/**
	 * >> u need to create the queue either manually through Rabbitmq web console
	 *    or programmatically through this bean.
	 * 
	 * >> if u didn't create the queue manually or programmatically, then an exception
	 *    will be thrown
	 *    
	 * >> if the queue already exist then this bean will not create a new queue
	 */
	//this bean is used to create the queue programmatically..
	@Bean
	public Queue queue(@Value("${apress.amqp.queue}") String queueName) {
		return new Queue(queueName, false);
	}
		
}

//======================================  Properties =========================================//

@ConfigurationProperties(prefix = "apress.amqp") // this will declare a custom properties that will have
													// the prefix apress.amqp
class AMQPProperties {

	private String queue;
	private String replyQueue;
	private String replyExchangeQueue;
	private String exchange = "";
	private String errorQueue;
	private String errorExchange = "";
	private String rateQueue;
	private String rateExchange = "";
	private String errorRoutingKey = "error";

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getReplyQueue() {
		return replyQueue;
	}

	public void setReplyQueue(String replyQueue) {
		this.replyQueue = replyQueue;
	}

	public String getReplyExchangeQueue() {
		return replyExchangeQueue;
	}

	public void setReplyExchangeQueue(String replyExchangeQueue) {
		this.replyExchangeQueue = replyExchangeQueue;
	}

	public String getRateQueue() {
		return rateQueue;
	}

	public void setRateQueue(String rateQueue) {
		this.rateQueue = rateQueue;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getRateExchange() {
		return rateExchange;
	}

	public void setRateExchange(String rateExchange) {
		this.rateExchange = rateExchange;
	}

	public String getErrorQueue() {
		return errorQueue;
	}

	public void setErrorQueue(String errorQueue) {
		this.errorQueue = errorQueue;
	}

	public String getErrorExchange() {
		return errorExchange;
	}

	public void setErrorExchange(String errorExchange) {
		this.errorExchange = errorExchange;
	}

	public String getErrorRoutingKey() {
		return errorRoutingKey;
	}

	public void setErrorRoutingKey(String errorRoutingKey) {
		this.errorRoutingKey = errorRoutingKey;
	}

}

//======================================  Auditing =========================================//

//disable auditing so that u can easily see the block and unblock 
//messages if they are printed.

//@Aspect
//@Component
class AMQPAudit {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger("AMQPAudit");

	@Pointcut("execution(* spring_boot._5_amqp._7_bolck_unblock_and_shutdown_events.*.*(..))")
	public void logAMQP() {
	};

	@Around("logAMQP()")
	public Object amqpAudit(ProceedingJoinPoint pjp) throws Throwable {
		StringBuilder builder = new StringBuilder(NEXT_LINE);
		printBefore(builder, pjp);

		// Method Execution
		Object object = pjp.proceed(pjp.getArgs());

		printAfter(builder, object);
		log.info(builder.toString());

		return object;
	}

	@Before("execution(* com.apress.messaging.listener.*.*(..))")
	public void auditListeners(JoinPoint joinPoint) {
		StringBuilder builder = new StringBuilder(NEXT_LINE);
		printBefore(builder, joinPoint);
		builder.append(NEXT_LINE);
		builder.append(DASH_LINE);
		log.info(builder.toString());
	}

	private void printBefore(StringBuilder builder, JoinPoint jp) {
		Object[] args = jp.getArgs();
		Parameter[] parameters = ((MethodSignature) jp.getSignature()).getMethod().getParameters();

		builder.append(DASH_LINE);
		builder.append(NEXT_LINE);
		builder.append("[BEFORE]");
		builder.append(NEXT_LINE);
		builder.append(" Class: ");
		builder.append(jp.getTarget().getClass().getName());
		builder.append(NEXT_LINE);
		builder.append("Method: ");
		builder.append(jp.getSignature().getName());
		builder.append(NEXT_LINE);
		builder.append("Params: ");
		builder.append(NEXT_LINE);

		if (null != args && null != parameters) {
			IntStream.range(0, args.length).forEach(index -> {
				builder.append("> ");
				builder.append(parameters[index].getName());
				builder.append(": ");
				builder.append(args[index]);
				builder.append(NEXT_LINE);
			});
		}
	}

	private void printAfter(StringBuilder builder, Object object) {
		builder.append(NEXT_LINE);
		builder.append("[AFTER]");
		builder.append(NEXT_LINE);
		builder.append("Return: ");
		builder.append(object == null ? "void/null" : object);
		builder.append(NEXT_LINE);
		builder.append(DASH_LINE);
	}
}
