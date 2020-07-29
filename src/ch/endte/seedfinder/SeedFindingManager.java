package ch.endte.seedfinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// handles loading and saving of the seedfinding data
// handles the generation of new tasks depending on client responses

public class SeedFindingManager {
	
	private TokenCommunication com;
	private String saveFile;
	private BufferedWriter fileWriter;
	
	public SeedFindingManager(TokenCommunication com, String saveFile) throws IOException {
		this.saveFile = saveFile;
		this.com = com;
		load();
		fileWriter = new BufferedWriter(new FileWriter(saveFile, true));
	}
	
	private void load() throws IOException {
		HashMap<String, Message> activeTasks = new HashMap<String, Message>();
		BufferedReader br = new BufferedReader(new FileReader(saveFile));
		while (br.ready()) {
			String mText[] = br.readLine().split(" ", 2);
			Message m = new Message(Token.getToken(mText[0]),mText[1]);
			switch (m.command) {
			case ADD_TASK:
				activeTasks.put(m.parameters, m);
				break;
			case FINISH_TASK:
				activeTasks.remove(m.parameters);
				break;
			default:
				continue;
			}
		}
		br.close();
		ArrayList<Message> taskList = new ArrayList<Message>();
		taskList.addAll(activeTasks.values());
		com.send(taskList);
		com.flush();
	}
	
	public void tick() throws IOException {
		for(Message m: com.receive()) {
			switch (m.command) {
			case RETURN_PASSED_FILTER_1:
				fileWriter.append(m.command.id + " " + m.parameters);
				String seed = m.parameters.split(" ", 2)[0];
				fileWriter.append(Token.ADD_TASK.id + " " + Filter2Task.id + " " + seed);
				com.send(new Message(Token.ADD_TASK, Filter2Task.id + " " + seed));
				break;
			case RETURN_PASSED_FILTER_2:
				fileWriter.append(m.command.id + " " + m.parameters);
				seed = m.parameters.split(" ", 2)[0];
				fileWriter.append(Token.ADD_TASK.id + " " + EvaluationTask.id + " " + seed);
				com.send(new Message(Token.ADD_TASK, EvaluationTask.id + " " + seed));
				break;
			case RETURN_EVALUATION_SCORE:
			case FINISH_TASK:
				fileWriter.append(m.command.id + " " + m.parameters);
				break;
			default:
				break;
			
			}
		}
	}
}
