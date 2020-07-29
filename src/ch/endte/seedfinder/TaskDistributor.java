package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskDistributor {
	
	private TokenCommunication com; //could probably add another abstraction layer for
	// better intra thread communication but whatever
	private final ArrayList<Connection> clients = new ArrayList<Connection>();
	private final ArrayList<SpecificTask> queue = new ArrayList<SpecificTask>();
	
	public TaskDistributor(TokenCommunication c) {com = c;}
	
	public void addConnection(TokenCommunication c) {clients.add(new Connection(c));}
	
	public void tick() {
		handleServerCommunication();
		handleClientDistribution();
		handleClientDisconnection();
	}
	
	private void handleServerCommunication() {
		if (com.isClosed()) return;
		for (Message m: com.receive()) {
			switch (m.command) {
			case ADD_TASK:
				String[] parameters  = m.parameters.split(" ", 2);
				Task t = Task.getTask(parameters[0]);
				if (t == null) {
					throw new IllegalArgumentException();
				}
				queue.add(new SpecificTask(t, parameters[1]));
				break;
			case SHUTDOWN:
				com.close();
				return;
			default:
				break;
			}
		}
		if (queue.size() < clients.size()) {
			com.send(new Message(Token.REQUEST_TASK, Integer.toString(clients.size()*3)));
		}
		com.flush();
	}
	
	private void handleClientDistribution() {
		for(Connection c: clients) {
			if (c.com.isClosed()) continue;
			for (Message m: c.com.receive()) {
				switch (m.command) {
				case REQUEST_TASK:
					c.requestCount = Integer.parseInt(m.parameters);
					break;
				case FINISH_TASK:
					if (!c.tasks.containsKey(m.parameters)) 
						break;
					c.tasks.remove(m.parameters);
				case RETURN_PASSED_FILTER_1:
				case RETURN_PASSED_FILTER_2:
				case RETURN_EVALUATION_SCORE:
					com.send(m);
				default:
					break;
				}
			}
			sendClientTasks(c);
			c.com.flush();
		}
	}
	
	private void handleClientDisconnection() {
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		int i = 0;
		for(Connection c: clients) {
			if (c.com.isClosed()) {
				queue.addAll(c.tasks.values());
				toRemove.add(i);
			}
			i++;
		}
		for(i = toRemove.size()-1;i>0;i--) {
			clients.remove(i);
		}
	}
	
	private class Connection {
		final TokenCommunication com;
		final HashMap<String, SpecificTask> tasks = new HashMap<String, SpecificTask>();
		int requestCount = 0;
		Connection(TokenCommunication c) {com = c;}
	}
	
	private void sendClientTasks(Connection c) {
		if (c.requestCount > 0) {
			if (queue.size() == 0) return;
			int toSend = c.requestCount;
			for (int i = 0;i<toSend;i++) {
				if (queue.size() == 0)return;
				sendTask(c, queue.remove(0));
			}
		}
	}
	
	protected static void sendTask(Connection c, SpecificTask st) {
		c.com.send(new Message(Token.ADD_TASK, st.task.getId() + " " + st.taskParameter));
		c.tasks.put(st.task.getId() + " " + st.taskParameter, st);
		c.requestCount = Math.max(c.requestCount-1, 0); //decrease to 0
	}
	
}
