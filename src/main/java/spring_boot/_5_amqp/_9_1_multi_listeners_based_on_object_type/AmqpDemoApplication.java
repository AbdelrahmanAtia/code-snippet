package spring_boot._5_amqp._9_1_multi_listeners_based_on_object_type;

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
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
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
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AmqpDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}

	// exchange is an empty string since there is no property apress.amqp.exchange in the properties file
	// routing key is similar to the queue name since we are using the default exchange type. this might change according to our needs.
	@Bean
	CommandLineRunner simple(@Value("${apress.amqp.exchange:}") String exchange,
			@Value("${apress.amqp.queue}") String routingKey, Producer producer, RabbitTemplate template) {
				
		return args -> {
			Invoice invoice = new Invoice(1, 25.64);
			InvoiceWithTax invWithTax = new InvoiceWithTax(2, 30, 0.1F);

			producer.sendMessage(exchange, routingKey, invoice);
			producer.sendMessage(exchange, routingKey, invWithTax);

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

	public void sendMessage(String exchange, String routingKey, Object obj) {
		this.template.convertAndSend(exchange, routingKey, obj);
	}
}

//======================================  Consumer ==========================================//

@Component
@RabbitListener(id = "multi", queues = "${apress.amqp.queue}")
class MultiListenerService {

	// will consume only the messages with pay-load of type Invoice
	@RabbitHandler
	@SendTo("${apress.amqp.reply-queue}")
	public Order processInvoice(Invoice invoice) {
		Order order = new Order();

		// Process Invoice here...

		order.setInvoice(invoice);
		return order;
	}

	// will consume only the messages with pay-load of type InvoiceWithTax
	@RabbitHandler
	@SendTo("${apress.amqp.reply-queue}")
	public Order processInvoiceWithTax(InvoiceWithTax invoiceWithTax) {
		Order order = new Order();

		// Process Invoice with Tax here...

		order.setInvoiceWithTax(invoiceWithTax);
		return order;
	}
}

//====================================== Domain ==================================================//
class Order {

	private Invoice invoice;

	private InvoiceWithTax invoiceWithTax;

	public Order() {

	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public InvoiceWithTax getInvoiceWithTax() {
		return invoiceWithTax;
	}

	public void setInvoiceWithTax(InvoiceWithTax invoiceWithTax) {
		this.invoiceWithTax = invoiceWithTax;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Order [invoice=");
		builder.append(invoice);
		builder.append(", invoiceWithTax=");
		builder.append(invoiceWithTax);
		builder.append("]");
		return builder.toString();
	}

}

class Invoice {

	private int id;
	private double totalPrice;

	public Invoice() {

	}

	public Invoice(int id, double totalPrice) {

		this.id = id;
		this.totalPrice = totalPrice;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Invoice [id=");
		builder.append(id);
		builder.append(", totalPrice=");
		builder.append(totalPrice);
		builder.append("]");
		return builder.toString();
	}

}

class InvoiceWithTax {

	private int id;
	private double totalPrice;
	private float tax;

	public InvoiceWithTax() {

	}

	public InvoiceWithTax(int id, double totalPrice, float tax) {

		this.id = id;
		this.totalPrice = totalPrice;
		this.tax = tax;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public float getTax() {
		return tax;
	}

	public void setTax(float tax) {
		this.tax = tax;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InvoiceWithTax [id=");
		builder.append(id);
		builder.append(", totalPrice=");
		builder.append(totalPrice);
		builder.append(", tax=");
		builder.append(tax);
		builder.append("]");
		return builder.toString();
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
	
	//this bean is used to create the queue programmatically..
	@Bean
	public Queue queue(@Value("${apress.amqp.queue}") String queueName) {
		return new Queue(queueName, false);
	}
	
	@Bean
	public Queue replyQueue(@Value("${apress.amqp.reply-queue}") String queueName) {
		return new Queue(queueName, false);
	}	
	
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

	@Pointcut("execution(* spring_boot._5_amqp._9_1_multi_listeners_based_on_object_type.*.*(..))")
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
