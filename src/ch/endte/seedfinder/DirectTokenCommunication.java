package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DirectTokenCommunication implements TokenCommunication {
	
	private ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
	private ConcurrentLinkedQueue<Message> target;
	
	// returns an array with 2 communication entries that will send and receive
	// from one another
	public static DirectTokenCommunication[] createCommunicationLine() {
		DirectTokenCommunication[] result = new DirectTokenCommunication[] {
				new DirectTokenCommunication(),
				new DirectTokenCommunication()
		};
		result[0].target = result[1].messageQueue;
		result[1].target = result[0].messageQueue;
		return result;
	}
	
	private DirectTokenCommunication() {}
	
	@Override
	public List<Message> receive() {
		ArrayList<Message> result = new ArrayList<Message>();
		if (isClosed()) {
			result.add(new Message(Token.SHUTDOWN));
		} else {
			while (!messageQueue.isEmpty()) {
				result.add(messageQueue.poll());
			}
		}
		return result;
	}
	
	@Override
	public Message receiveOne() {
		if (isClosed()) {return new Message(Token.SHUTDOWN);}
		return messageQueue.poll();
	}

	@Override
	public void send(List<Message> m) {
		if (this.target != null)target.addAll(m);
	}

	@Override
	public void send(Message m) {
		if (this.target != null)target.add(m);
	}

	// irrelevant for this specific implementation since unbuffered
	@Override
	public void flush() {}

	@Override
	public synchronized void close() {
		if (target != null) {
			send(new Message(Token.SHUTDOWN));
			target = null;
		}
	}
	

	@Override
	public boolean isClosed() {return target == null;}
	
}
