package bca.rmt.mq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class DemoMessageListenerApplication {

	// Create variables for the connection to MQ
		private static final String HOST = "10.20.212.242"; // Host name or IP address
		private static final int PORT = 1331; // Listener port for your queue manager
		private static final String CHANNEL = "CHLDD.INT.ORI.CONN"; // Channel name
		private static final String QMGR = "QMDD9.INT.01"; // Queue manager name
		private static final String APP_USER = "mqorir"; // User name that application uses to connect to MQ
		//private static final String APP_PASSWORD = "password"; // Password that the application uses to connect to MQ
		private static final String QUEUE_NAME = "ORIR.LLD.INTF_ITS.NN"; // Queue that the application uses to put and get messages to and from

		public static void main(String[] args) throws JMSException, MQException, IOException {

			MQEnvironment.hostname ="10.20.212.242";
			MQEnvironment.channel = "CHLDD.INT.ORI.CONN";
			MQEnvironment.port = 1331;
			MQEnvironment.CCSID = 819;
			MQEnvironment.userID = "mqorir";
			
			String qManager = "QMDD9.INT.01";
			String queueName = "ORIR.LLD.INTF_ITS.NN";
			
			// Declare JMS 2.0 objects
			JMSContext context;
			Destination destination; // The destination will be a queue, but could also be a topic 
			JMSConsumer consumer;
			
			// Create a connection factory
			
			JmsConnectionFactory  connectionFactory =  createJMSConnectionFactory();
	
			setJMSProperties(connectionFactory);
			
//			MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
//		    connectionFactory.setChannel("CHLDD.INT.ORI.CONN");
//		    connectionFactory.setHostName("10.20.212.242");
//		    connectionFactory.setPort(1331);
//		    connectionFactory.setQueueManager("QMDD9.INT.01");
//		    connectionFactory.setCCSID(819);
//		    connectionFactory.setTransportType(0);
//		    connectionFactory.setStringProperty(MQConstants.USER_ID_PROPERTY, "mqorir");
			//MQQueueConnection connection = (MQQueueConnection) connectionFactory.createQueueConnection("MUSR_MQADMIN", "password");
			
			//TransferSwitchingListener tsl = new TransferSwitchingListener();
		    //MQSender mqs = new MQSender();
		    
		    java.net.URL chanTab1  = new URL("file:///C:/Users/U067735/Documents/OJT/IBM MQ/File/AMQCLCHL/AMQCLCHL.TAB") ;
		    chanTab1.openConnection();
		    
		    String CCDTURL = "file:///C:/Users/U067735/Documents/OJT/IBM MQ/File/AMQCLCHL/AMQCLCHL.TAB";
		    
//		    connectionFactory.setCCDTURL(chanTab1);
		    connectionFactory.setStringProperty(WMQConstants.WMQ_CCDTURL, CCDTURL);
		    
		    //Setting up Queue Manager
			MQQueueManager qMgr2 = null;
			
			qMgr2 = new MQQueueManager(qManager);
			
			int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF |
					MQConstants.MQOO_OUTPUT | MQConstants.MQOO_INQUIRE;
			
			MQQueue queue2 = qMgr2.accessQueue(queueName, openOptions);
			
			System.out.println(qMgr2.isConnected());
		    
//			System.out.println("MQ Test: Connecting to " + HOST + ", Port " + PORT + ", Channel " + CHANNEL
//			+ ", Connecting to " + QUEUE_NAME);
		    
			try {
//				context = connectionFactory.createContext(); // This is connection + session. The connection is started by default
//				destination = context.createQueue("queue:///" + QUEUE_NAME); // Set the producer and consumer destination to be the same... not true in general 
//				consumer = context.createConsumer(destination); // associate consumer with the queue we put messages onto
				
				destination = connectionFactory.createContext().createQueue("queue:///" + QUEUE_NAME);
				consumer = connectionFactory.createContext().createConsumer(destination);
				
				/************IMPORTANT PART******************************/
				MessageListener ml = new DemoMessageListener(); // Creates a listener object
//				consumer.setMessageListener(ml); // Associates listener object with the consumer
				
				// The message listener will now listen for messages in a separate thread (see MyMessageListener.java file)
				System.out.println("The message listener is running."); // (Because the connection is started by default)
				// The messaging system is now set up
				/********************************************************/

				// The other logic for your program can now be implemented. This app can run whilst the listener listens in the background
				// For our application, the userInterface method runs a loop for the user to interact with the listener by sending and receiving messages
//				userInterface(context, connectionFactory, destination);
				
			} catch (Exception e) {
				// if there is an associated linked exception, print it. Otherwise print the stack trace
				if (e instanceof JMSException) { 
					e.printStackTrace();
					JMSException jmse = (JMSException) e;
					if (jmse.getLinkedException() != null) { 
						System.out.println("!! JMS exception thrown in application main method !!");
						System.out.println(jmse.getLinkedException());
					}
					else {
						jmse.printStackTrace();
					}
				} else {
					System.out.println("!! Failure in application main method !!");
					e.printStackTrace();
				}
			}
		}

		private static JmsConnectionFactory createJMSConnectionFactory() {
			JmsFactoryFactory ff;
			JmsConnectionFactory cf;
			try {
				ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
				cf = ff.createConnectionFactory();
			} catch (JMSException jmse) {
				System.out.println("JMS Exception when trying to create connection factory!");
				if (jmse.getLinkedException() != null){ // if there is an associated linked exception, print it. Otherwise print the stack trace
					System.out.println(((JMSException) jmse).getLinkedException());
				} else {jmse.printStackTrace();}
				cf = null;
			}
			return cf;
		}

		private static void setJMSProperties(JmsConnectionFactory cf) {
			try {
				cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
				cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
				cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
				//cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
				cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, 0);
				cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
				cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
				cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);
				cf.setStringProperty(WMQConstants.USERID, APP_USER);
				//cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
				
			} catch (JMSException jmse) {
				System.out.println("JMS Exception when trying to set JMS properties!");
				if (jmse.getLinkedException() != null){ // if there is an associated linked exception, print it. Otherwise print the stack trace
					System.out.println(((JMSException) jmse).getLinkedException());
				} else {jmse.printStackTrace();}
			}
			return;
		}

		public static void userInterface(JMSContext context, JmsConnectionFactory connectionFactory, Destination destination) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Boolean exit = false;
			while (!exit) {
				String command;
				try {
					System.out.print("Ready : ");
					command = br.readLine(); // Takes command line input
					command = command.toLowerCase();
					
					if(command.equalsIgnoreCase("start") || command.equalsIgnoreCase("restart")) {
						context.start(); // Starting the context also starts the message listener
						System.out.println("--Message Listener started.");
						break;
					} else if (command.equalsIgnoreCase("stop")) {
						context.stop(); // Stopping the context also stops the message listener
						System.out.println("--Message Listener stopped.");
						break;
					} else if (command.equalsIgnoreCase("send")) {
						sendATextMessage(connectionFactory, destination);
						System.out.println("--Sent text message.");
						break;
					} else if (command.equalsIgnoreCase("exit")) {
						context.close(); // Also stops the context
						System.out.println("bye...");
						exit = true;
						break;
					} else {
						System.out.println("Help: valid commands are start/restart, stop, send and exit");
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.exit(0);
		}

		public static void sendATextMessage(JmsConnectionFactory connectionFactory, Destination destination) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("payload : "); // Asks for the message payload as input
				String payload = br.readLine();

				// Need a separate context to create and send the messages because they are received asynchronously
				JMSContext producerContext = connectionFactory.createContext();
				JMSProducer producer = producerContext.createProducer();
				Message m = producerContext.createTextMessage(payload);
				producer.send(destination, m);
				producerContext.close();
			} catch (Exception e) {
				System.out.println("Exception when trying to send a text message!");
				// if there is an associated linked exception, print it. Otherwise print the stack trace
				if (e instanceof JMSException) { 
					JMSException jmse = (JMSException) e;
					if (jmse.getLinkedException() != null) { 
						System.out.println(jmse.getLinkedException());
					}
					else {
						jmse.printStackTrace();
					}
				} else {
					e.printStackTrace();
				}
			}
		}
		
}
