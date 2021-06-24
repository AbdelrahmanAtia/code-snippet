package jms;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/*
 * ==============================================================
 * to start activeMQ, run the following commands
 * cd C:\_apache-activemq-5.16.2\bin
 * activemq start
 * ==============================================================
 * 
 * 
 * 
 */

class Producer implements Runnable {

	public void run() {
		try { // Create a connection factory.
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create connection.
			Connection connection = factory.createConnection();

			// Start the connection
			connection.start();

			// Create a session which is non transactional
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create Destination queue
			Destination queue = session.createQueue("Queue");

			// Create a producer
			MessageProducer producer = session.createProducer(queue);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			String msg = "Hello World";
			TextMessage message = session.createTextMessage(msg);
			System.out.println("Producer Sent: " + msg);
			producer.send(message);

			session.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

class Consumer implements Runnable {

	public void run() {
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create Connection
			Connection connection = factory.createConnection();

			// Start the connection
			connection.start();

			// Create Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create queue
			Destination queue = session.createQueue("Queue");

			MessageConsumer consumer = session.createConsumer(queue);

			Message message = consumer.receive(1000);

			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				String text = textMessage.getText();
				System.out.println("Consumer Received: " + text);
			}

			session.close();
			connection.close();
		} catch (Exception ex) {
			System.out.println("Exception Occured");
		}
	}
}

public class Main {

	public static void main(String[] args) {
		Producer producer = new Producer();
        Consumer consumer = new Consumer();
 
        Thread producerThread = new Thread(producer);
        producerThread.start();
 
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();
	}

}