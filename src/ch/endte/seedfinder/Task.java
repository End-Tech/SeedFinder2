package ch.endte.seedfinder;

import java.util.HashMap;

public abstract class Task {
	
	private static final HashMap<String,Task> taskList = new HashMap<String,Task>();
	
	public abstract void run(String parameter,TokenCommunication c);
	public abstract String getId();
	
	public static Task getTask(String taskId) {
		return taskList.getOrDefault(taskId, null);
	}
	
	public static void registerTask(Task task) {
		if (getTask(task.getId()) == null) {
			taskList.put(task.getId(), task);
		}
	}
	
	static {
		registerTask(new Filter1Task());
		registerTask(new Filter2Task());
		registerTask(new DebugTask());
		registerTask(new EvaluationTask());
	}
}
