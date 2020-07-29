package ch.endte.seedfinder;

import java.util.ArrayList;

public class TaskDistributor {
	
	private TokenCommunication com; //could probably add another abstraction layer for
	// better intra thread communication but whatever
	private ArrayList<Connection> clients;
	
	public TaskDistributor(TokenCommunication c) {
		com = c;
	}
	
	public void addConnection(TokenCommunication c) {
		clients.add(new Connection(c));
	}
	
	public void handleClientDistribution() {
		for(Connection c: clients) {
			
		}
	}
	
	private class Connection {
		
		final TokenCommunication com;
		final ArrayList<SpecificTask> tasks = new ArrayList<SpecificTask>();
		Connection(TokenCommunication c) {com = c;}
	}
	
	
}
