package ch.endte.seedfinder;

import java.util.HashMap;

public enum Token {
	// general WorkerThread communication
	ADD_TASK("ADDTASK"),
	FINISH_TASK("FINISHTASK"),
	REQUEST_TASK("REQUESTTASK"),
	SHUTDOWN("SHUTDOWN"),
	
	// task specific communication of results
	RETURN_PASSED_FILTER_1("RETURNFILTER1"),
	RETURN_PASSED_FILTER_2("RETURNFILTER2"),
	RETURN_EVALUATION_SCORE("RETURNEVALUATION");
	
	public final String id;
	
	public static HashMap<String, Token> idList;
	
	private Token(String id) {
		this.id = id;
		registerToken(this);
	}
	
	public static Token getToken(String id) {
		return idList.getOrDefault(id, null);
	}
	
	private static void registerToken(Token t) {
		if (idList == null) {
			idList = new HashMap<String, Token>();
		}
		idList.put(t.id, t);
	}
	
}
