package spring_boot._4_jms.no_framework.publisher_subscriber;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class PublisherClient {

	public static void main(String[] args) throws NamingException, JMSException {

		// Step 1. Create the Connection
		InitialContext ctx = new InitialContext();
		TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup("connectionFactory");
		TopicConnection connection = factory.createTopicConnection();
		connection.start();

		// Step 2. Create a Topic Session
		TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

		// Step 3. Get the Topic object
		Topic topic = (Topic) ctx.lookup("myTopic");

		// Step 4. Create the Sender
		TopicPublisher publisher = session.createPublisher(topic);

		// Step 5. Create the Message
		TextMessage msg = session.createTextMessage();
		msg.setText("Hello World");

		// Step 6. Send the Message
		publisher.publish(msg);
		
		System.out.println("message sent to topic successfully..");
	}

}
