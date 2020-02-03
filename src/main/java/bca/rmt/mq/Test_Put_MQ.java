package bca.rmt.mq;


import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class Test_Put_MQ {

	public static void main(String[] args) {

		Connection connection = null;
		Session session = null;
		Destination destination = null;
		Destination tempDestination = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;

		try {

			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();

			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, "10.20.212.242"); 
			cf.setIntProperty(WMQConstants.WMQ_PORT, 1331);
			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "CHLDD.INT.OPR.CONN");
			//cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "QMDD9.INT.01");
			cf.setStringProperty(WMQConstants.WMQ_USER_PROPERTIES, "mqorir");
			cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);

			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue("queue:///Q1");
			producer = session.createProducer(destination);

			long uniqueNumber = System.currentTimeMillis() % 1000;
			TextMessage message = session.createTextMessage("SimpleRequestor: Your lucky number yesterday was " + uniqueNumber);
			connection.start();
			producer.send(message);

		}catch (JMSException jmsex) {
			jmsex.printStackTrace();
		}
	}
}

