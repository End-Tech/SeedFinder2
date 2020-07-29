package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.List;

public class DirectTokenCommunication implements TokenCommunication {
	
	private ArrayList<Message> messageQueue = new ArrayList<Message>();
	
	// epic circular reference :)
	private DirectTokenCommunication target;
	
	// returns an array with 2 communication entries that will send and receive
	// from one another
	public static DirectTokenCommunication[] createCommunicationLine() {
		DirectTokenCommunication[] result = new DirectTokenCommunication[] {
				new DirectTokenCommunication(),
				new DirectTokenCommunication()
		};
		result[0].target = result[1];
		result[1].target = result[0];
		return result;
	}
	
	private DirectTokenCommunication() {}
	
	@Override
	public synchronized List<Message> receive() {
		List<Message> result = (List<Message>) messageQueue;
		messageQueue = new ArrayList<Message>();
		return result;
	}

	@Override
	public synchronized void send(List<Message> m) {
		target.addMessage(m);
	}

	@Override
	public synchronized void send(Message m) {
		target.addMessage(m);
	}
	
	private synchronized void addMessage(List<Message> m) {
		messageQueue.addAll(m);
	}
	
	private synchronized void addMessage(Message m) {
		messageQueue.add(m);
	}

	// irrelevant for this specific implementation since unbuffered
	@Override
	public void flush() {}

	// remove circular reference
	@Override
	public void close() {
		if (target != null) {
			if (target.target == this) {
				target.target = null;
			}
			target = null;
		}
	}
}
