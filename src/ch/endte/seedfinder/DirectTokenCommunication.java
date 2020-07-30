package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DirectTokenCommunication implements TokenCommunication {
	
	private ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
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
	public List<Message> receive() {
		ArrayList<Message> result = new ArrayList<Message>();
		for (Message m: messageQueue) {
			result.add(m);
			messageQueue.remove(m);
		}
		return result;
	}

	@Override
	public void send(List<Message> m) {
		if (this.target != null)target.addMessageFrom(m, this);
	}

	@Override
	public void send(Message m) {
		if (this.target != null)target.addMessageFrom(m, this);
	}
	
	private void addMessageFrom(List<Message> m, DirectTokenCommunication c) {
		if (!isClosedFrom(c))messageQueue.addAll(m);
	}
	
	private void addMessageFrom(Message m, DirectTokenCommunication c) {
		if (!isClosedFrom(c))messageQueue.add(m);
	}

	// irrelevant for this specific implementation since unbuffered
	@Override
	public void flush() {}

	// remove circular reference
	@Override
	public synchronized void close() {
		if (target != null) {
			target.closeFrom(this);
			target = null;
		}
	}
	
	private void closeFrom(DirectTokenCommunication t) {if (target == t) {target = null;}}

	@Override
	public boolean isClosed() {return target == null || target.isClosedFrom(this);}
	public boolean isClosedFrom(DirectTokenCommunication t) {return target != t;}
	
}
