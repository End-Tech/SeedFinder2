package ch.endte.seedfinder;

import java.io.IOException;
import java.util.ArrayList;

public class SeedFinder2 {
	public static final int THREAD_COUNT = 2;
	public static final ArrayList<Thread> thread = new ArrayList<Thread>();
	
	public static void main(String[] args) throws IOException {
		DirectTokenCommunication[] ds = DirectTokenCommunication.createCommunicationLine();
		TaskDistributor tb = new TaskDistributor(ds[0]);
		SeedFindingManager sfm = new SeedFindingManager(ds[1],"./data.log");
		for (int i=0; i<THREAD_COUNT; i++) {
			ds = DirectTokenCommunication.createCommunicationLine();
			Thread t = new Thread(new TaskWorkerThread(ds[0]));
			thread.add(t);
			tb.addConnection(ds[1]);
			t.start();
		}
		while (true) {
			tb.tick();
			sfm.tick();
		}
	}
}
