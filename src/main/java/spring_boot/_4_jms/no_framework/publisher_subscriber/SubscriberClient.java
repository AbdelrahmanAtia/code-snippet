package spring_boot._4_jms.no_framework.publisher_subscriber;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SubscriberClient {

	public static void main(String[] args) throws NamingException, JMSException {

		// Step 1. Create Connection
		InitialContext ctx = new InitialContext();
		TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup("connectionFactory");
		TopicConnection connection = factory.createTopicConnection();
		connection.start();

		// Step 2. Create Session
		TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

		// Step 3. Get the Topic
		Topic topic = (Topic) ctx.lookup("myTopic");

		// Step 4. Create the Receiver
		TopicSubscriber subscriber = session.createSubscriber(topic);

		// Step 5. Create the Listener
		MessageListener listener = new MessageListener() {
			@Override
			public void onMessage(Message message) {
				// Process the message here
				
				System.out.println("Recieved message: " + message);
			}
		};

		// Step 6. Register the Listener
		subscriber.setMessageListener(listener);
	}

}
