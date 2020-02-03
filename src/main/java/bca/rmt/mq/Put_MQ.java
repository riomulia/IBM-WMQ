package bca.rmt.mq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Hashtable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQAsyncStatus;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;

public class Put_MQ {
	
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

        String qManager = "QMDD9.INT.01";
        String queueName = "ORIR.LLD.INTF_ITS.NN";
        MQQueueManager qMgr = null;
        try {
            qMgr = new MQQueueManager(qManager, props);

            // MQOO_OUTPUT = Open the queue to put messages. The queue is opened for use with subsequent MQPUT calls.
            // MQOO_INPUT_AS_Q_DEF = Open the queue to get messages using the queue-defined default.
            // The queue is opened for use with subsequent MQGET calls. The type of access is either
            // shared or exclusive, depending on the value of the DefInputOpenOption queue attribute.
            //int openOptions = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_INPUT_AS_Q_DEF;
            int openOptions = CMQC.MQOO_INQUIRE + CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED;
            
            // creating destination
            MQQueue queue = qMgr.accessQueue(queueName, openOptions);

            
            // specify the message options...
            MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
            // MQPMO_ASYNC_RESPONSE = The MQPMO_ASYNC_RESPONSE option requests that an MQPUT or MQPUT1 operation
            // is completed without the application waiting for the queue manager to complete the call.
            // Using this option can improve messaging performance, particularly for applications using client bindings.
            pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;

            MQMessage mqMsg = new MQMessage();
            queue.get(mqMsg);
            System.err.println("received: " + mqMsg.readLine() );

            queue.close();
            qMgr.disconnect(); 
            
            // create message
            MQMessage message = new MQMessage();
            // MQFMT_STRING = The application message data can be either an SBCS string (single-byte character set),
            // or a DBCS string (double-byte character set). Messages of this format can be converted
            // if the MQGMO_CONVERT option is specified on the MQGET call.
            message.format = MQConstants.MQFMT_STRING;
            message.writeString("<message>Hello World</message>");
            queue.put(message, pmo);
            queue.close();
            
            MQAsyncStatus asyncStatus = qMgr.getAsyncStatus();
            assertEquals(1, asyncStatus.putSuccessCount);
            
            System.out.println("Put Success");
        } catch (MQException e) {
            LOGGER.error("The connection to the Message Broker with the "
                    + "properties {} and the QueueManager {} could not be established.", props, qManager, e);
            System.out.println("Catch MQException");
        } catch (IOException e) {
        	System.out.println("Catch IOException");
            LOGGER.error("Error writing the message.", e);
        } finally {
            try {
                qMgr.disconnect();
            } catch (MQException e) {
                LOGGER.error("The connection could not be closed.", e);
            }
        }
        
    }
    
    public static void main(String[] args) {
    	testSendMessageQueue();
	}
	
}
