package spring_boot._5_amqp._9_0_send_object_as_json_using_converter;

import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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


@SpringBootApplication
public class AmqpDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}

	// >> exchange is an empty string since there is no property apress.amqp.exchange
	//    in the properties file.
	// >> routing key is similar to the queue name since we are using the default
	//    exchange type where the routing key is similar to the queue name. 
	//this might change according to our needs.
	
	@Bean
	CommandLineRunner simple(@Value("${apress.amqp.exchange:}") String exchange,
			@Value("${apress.amqp.queue}") String routingKey, Producer producer, RabbitTemplate template) {

		return args -> {

			producer.sendCurrency(exchange, routingKey, new Rate("EUR", 0.88857F, new Date()));
			producer.sendCurrency(exchange, routingKey, new Rate("JPY", 102.17F, new Date()));

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

	public void sendCurrency(String exchange, String routingKey, Rate rate) {
		this.template.convertAndSend(exchange, routingKey, rate);
	}
}

//======================================  Consumer ==========================================//

@Component
class AnnotatedConsumer {

	//if u disabled this listener, u will see that the message in the queue is in JSON format
	@RabbitListener(queues = "${apress.amqp.queue}")
	public void process(Rate rate) {

	}
}

//====================================== Domain ==================================================//

class Rate {
	private String code;
	private Float rate;
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

	public void setCode(String code) {
		this.code = code;
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

//======================================  Configuration =========================================//

@Configuration
@EnableConfigurationProperties(AMQPProperties.class)
class AMQPConfig {

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jackson2MessageConverter());
		return rabbitTemplate;
	}

	// this bean is used to create the queue programmatically..
	@Bean
	public Queue queue(@Value("${apress.amqp.queue}") String queueName) {
		return new Queue(queueName, false);
	}

	//this bean converts the object that will be sent to the queue into JSON
	//and also converts the JSON back to an object when being consumed with the listener 
	@Bean
	public Jackson2JsonMessageConverter jackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
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

	@Pointcut("execution(* spring_boot._5_amqp._9_0_send_object_as_json_using_converter.*.*(..))")
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
