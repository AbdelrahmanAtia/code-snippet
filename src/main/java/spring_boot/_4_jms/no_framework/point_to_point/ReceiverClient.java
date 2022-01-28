package spring_boot._4_jms.no_framework.point_to_point;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ReceiverClient {

	public static void main(String[] args) throws JMSException, NamingException {
		
		/**
		 * the properties are loaded from jndi.properties file 
		 * in the resources folder
		 */
		
		// Step 1. Create Connection
		InitialContext ctx = new InitialContext();
		QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("connectionFactory");
		QueueConnection connection = factory.createQueueConnection();
		connection.start();

		// Step 2. Create Session
		QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		// Step 3. Get the Queue
		Queue queue = (Queue) ctx.lookup("myQueue");

		// Step 4. Create the Receiver
		QueueReceiver receiver = session.createReceiver(queue);

		// Step 5. Create the Listener
		MessageListener listener = new MessageListener() {
			@Override
			public void onMessage(Message message) {
				// Process the message here
				
				System.out.println("recieved message: " + message);
			}
		};

		// Step 6. Register the Listener
		receiver.setMessageListener(listener);

	}

}
