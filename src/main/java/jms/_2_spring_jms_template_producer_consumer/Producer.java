package jms._2_spring_jms_template_producer_consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class Producer {

	@Autowired
	JmsTemplate jmsTemplate;

	public void sendMessage(final String queueName, final String message) {

		System.out.println("Sending message " + message + "to queue - " + queueName);

		jmsTemplate.send(queueName, new MessageCreator() {

			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				return message;
			}
		});
	}

}