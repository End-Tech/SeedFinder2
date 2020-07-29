package ch.endte.seedfinder;

public enum Token {
	// general WorkerThread communication
	ADD_TASK,
	FINISH_TASK,
	REQUEST_TASK,
	SHUTDOWN,
	
	// task specific communication of results
	RETURN_PASSED_FILTER_1,
	RETURN_PASSED_FILTER_2,
	RETURN_EVALUATION_SCORE;
	
	private Token() {}
	
}
