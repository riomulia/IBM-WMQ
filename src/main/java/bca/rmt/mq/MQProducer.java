package bca.rmt.mq;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import java.io.*;
import java.io.FileInputStream;
import java.util.Properties;
import javax.jms.*;

public class MQProducer {

	public MQProducer() {
	}

	private void putMessageIntoMQ() {
		String propertiesFile = "./ProducerProperties.properties";
		this.readPropertyFile(propertiesFile);
		Connection connection = null;
		Session session = null;
		javax.jms.Destination destination = null;
		MessageProducer producer = null;
		try {
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance("com.ibm.msg.client.wmq");
			JmsConnectionFactory cf = ff.createConnectionFactory();
			cf.setStringProperty("XMSC_WMQ_HOST_NAME", host);
			cf.setIntProperty("XMSC_WMQ_PORT", port);
			cf.setStringProperty("XMSC_WMQ_CHANNEL", channel);
			cf.setIntProperty("XMSC_WMQ_CONNECTION_MODE", 1);
			cf.setStringProperty("XMSC_WMQ_QUEUE_MANAGER", queueManagerName);
			cf.setStringProperty("XMSC_USERID","username"); //username to connect to MQ
			cf.setStringProperty("XMSC_PASSWORD","password"); // password to connect to MQ
			connection = cf.createConnection();
			session = connection.createSession(false, 1);
			if(isTopic)
				destination = session.createTopic(destinationName);
			else
				destination = session.createQueue(destinationName);
			producer = session.createProducer(destination);
			long uniqueNumber = System.currentTimeMillis() % 1000L;
			TextMessage message = session.createTextMessage();
			message.setText(sampleFile);
			connection.start();
			producer.send(message);
			System.out.println((new StringBuilder()).append("Sent message:\n").append(message).toString());
			recordSuccess();
		} catch(JMSException jmsex) {
			recordFailure(jmsex);
		} finally {
			if(producer != null)
				try {
					producer.close();
				} catch(JMSException jmsex) {
					System.out.println("Producer could not be closed.");
					recordFailure(jmsex);
				}
			if(session != null)
				try {
					session.close();
				} catch(JMSException jmsex) {
					System.out.println("Session could not be closed.");
					recordFailure(jmsex);
				}
			if(connection != null)
				try {
					connection.close();
				} catch(JMSException jmsex) {
					System.out.println("Connection could not be closed.");
					recordFailure(jmsex);
				}
		}
		System.exit(status);
	}

	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if(innerException != null)
			System.out.println("Inner exception(s):");
		for(; innerException != null; innerException = innerException.getCause())
			System.out.println(innerException);

	}

	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
	}

	private static void recordFailure(Exception ex) {
		if(ex != null)
			if(ex instanceof JMSException)
				processJMSException((JMSException)ex);
			else
				System.out.println(ex);
		System.out.println("FAILURE");
		status = -1;
	}

	private static String host;
	private static int port;
	private static String channel;
	private static String queueManagerName;
	private static String destinationName;
	private static boolean isTopic;
	private static int status = 1;
	private static String sampleFile;

	private void readPropertyFile(String fileName) { // reading from the property file
		try {
			Properties mqProperties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(fileName);
			mqProperties.load(fileInputStream);
			host = mqProperties.getProperty("host");
			port = Integer.parseInt(mqProperties.getProperty("port"));
			channel = mqProperties.getProperty("channel");
			queueManagerName = mqProperties.getProperty("queueManagerName");
			destinationName = mqProperties.getProperty("destinationName");
			isTopic = Boolean.parseBoolean(mqProperties.getProperty("isTopic"));
			sampleFile = mqProperties.getProperty("samplefilelocation");
			fileInputStream.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}


	public static void main(String args[]) {
		MQProducer mqProducer = new MQProducer();
		mqProducer.putMessageIntoMQ();
	}
}

