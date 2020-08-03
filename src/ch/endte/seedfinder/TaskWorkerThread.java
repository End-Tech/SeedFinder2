package ch.endte.seedfinder;


public class TaskWorkerThread implements Runnable {
	
	private TokenCommunication comms;
	private SpecificTask activeTask;
	private boolean shutdown = false;
	
	public TaskWorkerThread(TokenCommunication c) {
		comms = c;
	}
	
	
	@Override
	public void run() {
		while (!shutdown) {
			receiveMessages();
			if (activeTask == null) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
				}
			}
			doTask();
			comms.flush();
		}
		comms.close();
	}
	
	public void doTask() {
		if (activeTask == null)return;
		activeTask.task.run(activeTask.taskParameter, comms);
		Message m = new Message(Token.FINISH_TASK, activeTask.task.getId() + " " + activeTask.taskParameter);
		activeTask = null;
		comms.send(m);
	}

	public void receiveMessages() {
		Message m = comms.receiveOne();
		if (m == null) return;
		switch (m.command) {
		case ADD_TASK:
			String[] parameters  = m.parameters.split(" ", 2);
			Task t = Task.getTask(parameters[0]);
			if (t == null) {
				throw new IllegalArgumentException();
			}
			activeTask = new SpecificTask(t, parameters[1]);
			break;
		case SHUTDOWN:
			shutdown = true;
			break;
		default:
			break;
		}
	}

}
