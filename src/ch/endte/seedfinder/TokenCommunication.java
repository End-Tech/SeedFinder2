package ch.endte.seedfinder;

import java.util.List;

public interface TokenCommunication {
	public List<Message> receive();
	public void send(List<Message> m);
	public void send(Message m);
	// assuming communication is buffered
	// this is used to flush all messages in the outgoing queue
	public void flush();
	// assuming that a communication has to end at some point
	public void close();
	// assuming there is some way to identify this:
	public boolean isClosed();
}
