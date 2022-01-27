package spring_boot._4_jms.using_spring_boot_jms_template._4_replay_to;

import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.stream.IntStream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


//this commented annotation is used to scan any class annotated with @ConfigurationProperties and 
//makes it a managed bean in spring we commented it because we have another alternative which is  @EnableConfigurationProperties

//@ConfigurationPropertiesScan 
@EnableConfigurationProperties(JMSProperties.class)
@SpringBootApplication
public class JmsSenderApplication {
	public static void main(String[] args) {
		SpringApplication.run(JmsSenderApplication.class, args);
	}

	@Bean
	CommandLineRunner process(JMSProperties props, RateSender sender) {
		return args -> {
			sender.sendCurrency(props.getRateQueue(), new Rate("EUR", 0.88857F, new Date()));
			sender.sendCurrency(props.getRateQueue(), new Rate("JPY", 102.17F, new Date()));
			sender.sendCurrency(props.getRateQueue(), new Rate("MXN", 19.232F, new Date()));
			sender.sendCurrency(props.getRateQueue(), new Rate("GBP", 0.75705F, new Date()));
		};
	}
}

//======================================  Domain ==========================================//

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

//======================================  Sender ==========================================//

@Component
class RateSender {

	private JmsTemplate jmsTemplate;

	@Autowired
	public RateSender(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendCurrency(String destination, Rate rate) {
		this.jmsTemplate.convertAndSend(destination, rate);
	}
	
	@JmsListener(destination = "${apress.jms.rate-reply-queue}")
	public void process(String body, @Header("CODE") String code) {
		
	}
}

//======================================  Listener =========================================//

@Component
class RateReplayReciever {

	@JmsListener(destination = "${apress.jms.rate-queue}")
	@SendTo("${apress.jms.rate-reply-queue}")
	public Message<String> processRate(Rate rate) {
		
		/*
		 * Process the Rate and return any significant value
		 */
	
		return MessageBuilder
				.withPayload("PROCCESSED")
				.setHeader("CODE", rate.getCode())
				.setHeader("RATE", rate.getRate())
				.setHeader("ID", UUID.randomUUID().toString())
				.setHeader("DATE", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
				.build();
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

//======================================  Configuration =========================================//

@Configuration
@EnableConfigurationProperties(JMSProperties.class)
class JMSConfig {

	// this is bean is used to convert the Rate object that needs to be
	// sent into the queue to a text message
	@Bean
	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_class_"); // TODO: what is it used for ??
		return converter;
	}

}

//======================================  Auditing =========================================//

@Aspect
@Component
class JMSAudit {
	private static final String DASH_LINE = "===================================";
	private static final String NEXT_LINE = "\n";
	private static final Logger log = LoggerFactory.getLogger("JMSAudit");

	@Pointcut("execution(* spring_boot._4_jms.using_spring_boot_jms_template._4_replay_to.*.*(..))")
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
