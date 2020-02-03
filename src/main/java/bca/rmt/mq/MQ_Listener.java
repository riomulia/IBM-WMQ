package bca.rmt.mq;

import javax.jms.Queue;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSession;

public class MQ_Listener implements MessageListener {
	// The method that is called asynchronously when a suitable message is available
	public void onMessage(Message message)
	{
		try {
			//	    System.out.println("Message is "+ message);
			//
			//	    // The code to process the message
			//	    
			//	    
			//	    // Main program (possibly in another class)
			//	    MessageConsumer consumer = session.createConsumer(topic, null, true);
			//	    // Creating the message listener
			//	    MQ_Listener listener = new MQ_Listener();
			//
			//	    // Registering the message listener with a message consumer
			//	    consumer.setMessageListener(listener);

			// The main program now continues with other processing
			// Karena gak ada password, jadi authentication pakai Binding
			MQEnvironment.hostname ="10.20.212.242";
			MQEnvironment.channel = "CHLDD.INT.OPR.CONN";
			MQEnvironment.port = 1331;
			MQEnvironment.CCSID = 819;
			MQEnvironment.userID = "mqorir";

			MQQueueConnectionFactory cf = new MQQueueConnectionFactory();
			cf.setChannel("CHLDD.INT.OPR.CONN");
			cf.setHostName("10.20.212.242");
			cf.setPort(1331);
			cf.setQueueManager("QMDD9.INT.01");
			cf.setCCSID(819);
			cf.setTransportType(0);
			MQQueueConnection conn = (MQQueueConnection)cf.createQueueConnection();
			MQQueueSession session = (MQQueueSession)conn.createSession(false, 1);

			Queue queue = session.createQueue("QUEUE");

			MQQueueReceiver receiver = (MQQueueReceiver)session.createReceiver(queue);

			receiver.setMessageListener(new MQ_Listener());
			conn.start();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
