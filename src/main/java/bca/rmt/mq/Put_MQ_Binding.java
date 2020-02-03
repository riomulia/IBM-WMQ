package bca.rmt.mq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Hashtable;

import javax.jms.Message;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQAsyncStatus;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

public class Put_MQ_Binding {

	private static final Logger LOGGER = LoggerFactory.getLogger(Put_MQ.class);

	@Test
	public static void testSendMessageQueue() {
		// Create a connection to the queue manager
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(MQConstants.CHANNEL_PROPERTY, "CHLDD.INT.OPR.CONN");
		props.put(MQConstants.PORT_PROPERTY, 1331);
		props.put(MQConstants.HOST_NAME_PROPERTY, "10.20.212.242");
		//props.put(MQConstants.USER_ID_PROPERTY, "mqopr");
		//props.put(MQConstants.PASSWORD_PROPERTY, "10781");

		//Karena gak ada password, jadi authentication pakai Binding Environment
		MQEnvironment.hostname ="10.20.212.242";
		MQEnvironment.channel = "CHLDD.INT.OPR.CONN";
		MQEnvironment.port = 1331;
		MQEnvironment.CCSID = 819;
		MQEnvironment.userID = "mqorir";

		String qManager = "QMDD9.INT.01";
		String queueName = "ORIR.LLD.INTF_ITS.NN";
		MQQueueManager qMgr2 = null;
		//MQ_Listener listener = new MQ_Listener();

		try {
			// MQOO_OUTPUT = Open the queue to put messages. The queue is opened for use with subsequent MQPUT calls.
			// MQOO_INPUT_AS_Q_DEF = Open the queue to get messages using the queue-defined default.
			// The queue is opened for use with subsequent MQGET calls. The type of access is either
			// shared or exclusive, depending on the value of the DefInputOpenOption queue attribute.

			qMgr2 = new MQQueueManager(qManager);
			int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF |
					MQConstants.MQOO_OUTPUT | MQConstants.MQOO_INQUIRE;
			
			MQQueue queue2 = qMgr2.accessQueue(queueName, openOptions);
			
			// specify the message options...
			MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
			// MQPMO_ASYNC_RESPONSE = The MQPMO_ASYNC_RESPONSE option requests that an MQPUT or MQPUT1 operation
			// is completed without the application waiting for the queue manager to complete the call.
			// Using this option can improve messaging performance, particularly for applications using client bindings.
			pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;

			//            MQMessage mqMsg = new MQMessage();
			//            queue2.get(mqMsg);
			//            System.err.println("received: " + mqMsg.readLine() );

			// Build a new message containing my age followed by my name
			MQMessage myMessage = new MQMessage();
			
			myMessage.format = MQConstants.MQFMT_STRING;

			String msgString = "Test Input Queue 31 Jan";
			myMessage.writeString(msgString);
			
			queue2.put(myMessage, pmo);
			queue2.close();
			
			qMgr2.commit();
			qMgr2.disconnect();

			// create message
			MQMessage message = new MQMessage();
			// MQFMT_STRING = The application message data can be either an SBCS string (single-byte character set),
			// or a DBCS string (double-byte character set). Messages of this format can be converted
			// if the MQGMO_CONVERT option is specified on the MQGET call.
			message.format = MQConstants.MQFMT_STRING;

			//MQAsyncStatus asyncStatus = qMgr2.getAsyncStatus();
			//assertEquals(1, asyncStatus.putSuccessCount);

			System.out.println("Put Success");
		} catch (MQException e) {
			e.printStackTrace();
			LOGGER.error("The connection to the Message Broker with the "
					+ "properties {} and the QueueManager {} could not be established.", props, qManager, e);
			System.out.println("Catch MQException");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Catch IOException");
			LOGGER.error("Error writing the message.", e);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Catch Exception");
			LOGGER.error("Error writing the message.", e);
		}finally {
			try {
				qMgr2.disconnect();
			} catch (MQException e) {
				LOGGER.error("The connection could not be closed.", e);
			}
		}

	}

	public static void testReceiveMessageQueue() {
		//Karena gak ada password, jadi authentication pakai Binding
		MQEnvironment.hostname ="10.20.212.242";
		MQEnvironment.channel = "CHLDD.INT.OPR.CONN";
		MQEnvironment.port = 1331;
		MQEnvironment.CCSID = 819;
		MQEnvironment.userID = "mqorir";

		String qManager = "QMDD9.INT.01";
		String queueName = "ORIR.LLD.INTF_ITS.NN";
		MQQueueManager qMgr2 = null;

		MQMessage receiveMsg = null;
		int msgCount = 0;
		boolean getMore = true;

		try {

			qMgr2 = new MQQueueManager(qManager);

			//Open Options untuk Get
			int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF + MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING;

			MQQueue queue2 = qMgr2.accessQueue(queueName, openOptions);
			MQGetMessageOptions gmo = new MQGetMessageOptions();
			gmo.options = MQConstants.MQGMO_NO_WAIT + MQConstants.MQGMO_FAIL_IF_QUIESCING;

//			MQ_Listener listener = new MQ_Listener();
			
			while (getMore) {
				receiveMsg = new MQMessage();

				Message message = null;
				try {

					// get the message on the queue
					queue2.get(receiveMsg, gmo);
					msgCount++;
					
					//Nyobain Listener
//					listener.onMessage(message);
					
		               if (MQConstants.MQFMT_STRING.equals(receiveMsg.format)) {
		            	  //Simpen ke variabel msgStr
		                  String msgStr = receiveMsg.readStringOfByteLength(receiveMsg.getMessageLength());
		                  System.out.println(msgStr);
		               } else {
		                   byte[] b = new byte[receiveMsg.getMessageLength()];
		                   receiveMsg.readFully(b);
		               }
					
				} catch (MQException e) {
					if ( (e.completionCode == MQConstants.MQCC_FAILED) && 
							(e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) )
					{
						// All messages read.
						getMore = false;
						break;
					}
					else
					{
						LOGGER.error("MQException: " + e.getLocalizedMessage());
						LOGGER.error("CC=" + e.completionCode + " : RC=" + e.reasonCode);
						getMore = false;
						break;
					}
				} catch (IOException e) {
					LOGGER.error("IOException:" +e.getLocalizedMessage());
				}

			}
			System.out.println("Jumlah message dalam Queue adalah " + msgCount);
		} catch (MQException e) {
			e.printStackTrace();
			System.out.println("Catch MQException");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Catch Exception");
			LOGGER.error("Error writing the message.", e);
		}finally {
			try {
				qMgr2.disconnect();
			} catch (MQException e) {
				LOGGER.error("The connection could not be closed.", e);
			}
		}
	}

	public static void main(String[] args) {
		//testSendMessageQueue();
		testReceiveMessageQueue();
	}

}
