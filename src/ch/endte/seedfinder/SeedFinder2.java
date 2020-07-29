package ch.endte.seedfinder;





public class SeedFinder2 {
	public static final int THREAD_COUNT = 23;
	
	public static void main(String[] args) {
		DirectTokenCommunication[] taskComm = DirectTokenCommunication.createCommunicationLine();
		TaskWorkerThread t = new TaskWorkerThread(taskComm[1]);
		Thread th = new Thread(t);
		th.start();
		boolean receivedRequest = false;
		while (!receivedRequest) {
			for (Message m:taskComm[0].receive()) {
				if (m.command == Token.REQUEST_TASK) {
					receivedRequest = true;
					break;
				}
			}
		}
		taskComm[0].send(new Message(Token.ADD_TASK, "DBT Hello World!"));
		boolean receivedResponse = false;
		while (!receivedResponse) {
			for (Message m:taskComm[0].receive()) {
				if (m.command == Token.FINISH_TASK) {
					receivedResponse = true;
					break;
				}
			}
		}
		taskComm[0].send(new Message(Token.SHUTDOWN));
	}

	
}
