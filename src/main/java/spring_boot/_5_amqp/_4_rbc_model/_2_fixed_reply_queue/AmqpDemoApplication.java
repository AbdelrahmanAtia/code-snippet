package spring_boot._5_amqp._4_rbc_model._2_fixed_reply_queue;

import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
public class AmqpDemoApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner simple(@Value("${apress.amqp.exchange:}") String exchange,
			@Value("${apress.amqp.queue}") String routingKey, RpcClient client) {
		return args -> {
			Object result = client.sendMessage(exchange, routingKey, "HELLO AMQP/RPC!");
			assert result != null;
		};
	}
}


//======================================  Producer ==========================================//

@Component
class RpcClient {
	
	private RabbitTemplate template;

	@Autowired
	public RpcClient(RabbitTemplate template) {		
		this.template = template;
		
	}

	public Object sendMessage(String exchange, String routingKey, String message) {
		
		// note the difference between this function convertSendAndReceive() that does [convert-send-receive] and the 
		// function that we used to use, which was convertAndSend () doing [convert-send] and it was not
		// receiving any response
		
		// if a response is not received within a certain time period, then the response object will 
		// be null and when the response is returned, an exception will be thrown saying that 
		// the Reply is received after timeout..
		
		//note that u can set the replay timeout using following function
		//this.template.setReplyTimeout(6000L); this is already done below inside the creation of rabbit 
		//template bean
		
		Object response = this.template.convertSendAndReceive(exchange, routingKey, message);
		return response;
	}
}

//======================================  Consumer ==========================================//

@Component
class RpcServer {
	
	@RabbitListener(queues = "${apress.amqp.queue}")
	public Message<String> process(String message) {
		
		// More Processing here...
		
		return MessageBuilder.withPayload("PROCESSED:OK")
				.setHeader("PROCESSED", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
				.setHeader("CODE", UUID.randomUUID().toString()).build();
	}
	
}

//======================================  Configuration ====================================//

@Configuration
@EnableConfigurationProperties(AMQPProperties.class)
class AMQPConfig {

	@Autowired
	ConnectionFactory connectionFactory;

	@Value("${apress.amqp.reply-queue}")
	String replyQueueName;

	// this bean is used by the RPC client to send messages to the spring-boot-queue not
	// the replay queue
	@Bean
	public RabbitTemplate fixedReplyQueueRabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setReplyAddress(replyQueueName);
		template.setReplyTimeout(60000L);
		
		return template;
	}

	//i'm not sure what this bean really do??
	@Bean
	public SimpleMessageListenerContainer replyListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(replyQueue());
		container.setMessageListener(fixedReplyQueueRabbitTemplate());
		return container;
	}

	// TODO: is this bean really needed ?? if we removed it, we still can send and
	// listen to the message..
	@Bean
	public Queue queue(@Value("${apress.amqp.queue}") String queueName) {
		return new Queue(queueName, false);
	}

	@Bean
	public Queue replyQueue() {
		return new Queue(replyQueueName, false);
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

@Aspect
@Component
class AMQPAudit {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger("AMQPAudit");

	@Pointcut("execution(* spring_boot._5_amqp._4_rbc_model._2_fixed_reply_queue.*.*(..))")
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
