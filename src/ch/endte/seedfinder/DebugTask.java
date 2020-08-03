package ch.endte.seedfinder;

public class DebugTask extends Task {

	protected final static String id = "DBT";
	
	@Override
	public void run(String parameter, TokenCommunication c) {
		System.out.println(parameter);
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

}
