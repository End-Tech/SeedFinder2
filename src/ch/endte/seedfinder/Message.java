package ch.endte.seedfinder;

public class Message {
	public Token command;
	public String parameters = null;
	public Message(Token t, String p) {
		command = t;
		parameters = p;
	}
	public Message(Token t) {
		command = t;
	}
}
