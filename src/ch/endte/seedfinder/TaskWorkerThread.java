package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.List;

public class TaskWorkerThread implements Runnable {
	
	private TokenCommunication comms;
	private final ArrayList<SpecificTask> queue = new ArrayList<SpecificTask>();
	private boolean shutdown = false;
	private boolean didRequest = false;
	
	public TaskWorkerThread(TokenCommunication c) {
		comms = c;
	}
	
	
	@Override
	public void run() {
		while (!shutdown) {
			receiveMessages();
			doTask();
			requestTasks();
			comms.flush();
		}
		comms.close();
	}
	
	public void doTask() {
		if (queue.size() == 0)return;
		SpecificTask ts = queue.remove(0);
		ts.task.run(ts.taskParameter, comms);
		Message m = new Message(Token.FINISH_TASK, ts.task.getId() + " " + ts.taskParameter);
		comms.send(m);
	}

	public void receiveMessages() {
		List<Message> messages = comms.receive();
		for(Message m: messages) {
			switch (m.command) {
			case ADD_TASK:
				String[] parameters  = m.parameters.split(" ", 2);
				Task t = Task.getTask(parameters[0]);
				if (t == null) {
					throw new IllegalArgumentException();
				}
				queue.add(new SpecificTask(t, parameters[1]));
				didRequest = false;
				break;
			case SHUTDOWN:
				shutdown = true;
				return;
			default:
				break;
			}
		}
	}
	
	private void requestTasks() {
		if (queue.size() < 2 && !shutdown && !didRequest) {
			comms.send(new Message(Token.REQUEST_TASK, "3"));
			didRequest = true;
		}
	}

}
