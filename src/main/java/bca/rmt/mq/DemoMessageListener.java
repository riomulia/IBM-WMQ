package bca.rmt.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class DemoMessageListener implements MessageListener {

	public void onMessage(Message message) {
		System.out.println("## entry onMessage");
		
		if (message instanceof TextMessage){ // The message is sent as a Message object, so we must determine its type
			TextMessage textMessage = (TextMessage) message; // Casts to TextMessage object
			try {
				System.out.println("-- MyMessageListener received message with payload: " + textMessage.getText());
			} catch (JMSException jmse) {
				System.out.println("JMS Exception in MyMessageListener class!");
				System.out.println(jmse.getLinkedException());
			}
		} else {
			System.out.println("-- Message received was not of type TextMessage.\n");
		}
		
		System.out.println("##exit onMessage"); // So we know when onMessage has finished execution
	}
}