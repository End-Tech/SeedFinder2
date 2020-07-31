package ch.endte.seedfinder;

import java.io.IOException;
import java.util.ArrayList;

public class SeedFinder2 {
	public static final int THREAD_COUNT = java.lang.Runtime.getRuntime().availableProcessors() - 1;
	// public static final int THREAD_COUNT = 2;
	public static final ArrayList<Thread> thread = new ArrayList<Thread>();
	
	public static void main(String[] args) throws IOException {
		DirectTokenCommunication[] ds = DirectTokenCommunication.createCommunicationLine();
		TaskDistributor td = new TaskDistributor(ds[0]);
		SeedFindingManager sfm = new SeedFindingManager(ds[1],"./data.log");
		for (int i=0; i<THREAD_COUNT; i++) {
			ds = DirectTokenCommunication.createCommunicationLine();
			Thread t = new Thread(new TaskWorkerThread(ds[0]));
			thread.add(t);
			td.addConnection(ds[1]);
			t.start();
		}
		td.tick();
		int t = 0;
		while (td.hasOpenTasks()) {
			td.tick();
			sfm.tick();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			t++;
			if (Math.floorMod(t, 10)==0) {
				System.out.println((sfm.getTotalCount()-sfm.getActiveCount()) + "/" + sfm.getTotalCount());
			}
		}
		sfm.shutdown();
		td.tick();
	}
}
