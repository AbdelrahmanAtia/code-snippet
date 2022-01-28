package spring_boot._4_jms.no_framework.point_to_point;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SenderClient {

	public static void main(String[] args) throws NamingException, JMSException {
		
		/**
		 * the properties are loaded from jndi.properties file 
		 * in the resources folder
		 */

		// Step 1. Create the Connection
		InitialContext ctx = new InitialContext();
		QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("connectionFactory");
		QueueConnection connection = factory.createQueueConnection();
		connection.start();

		// Step 2. Create a Queue Session
		QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		// Step 3. Get the Queue object
		Queue queue = (Queue) ctx.lookup("myQueue");

		// Step 4. Create the Sender
		QueueSender sender = session.createSender(queue);

		// Step 5. Create the Message
		TextMessage msg = session.createTextMessage();
		msg.setText("Hello World");

		// Step 6. Send the Message
		sender.send(msg);
		
		System.out.println("message sent successfully..");
	}

}
