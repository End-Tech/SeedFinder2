package ch.endte.seedfinder;

import java.util.ArrayList;

public class SeedFinder2 {
	public static final int THREAD_COUNT = java.lang.Runtime.getRuntime().availableProcessors() - 1;
	// public static final int THREAD_COUNT = 2;
	public static final ArrayList<Thread> thread = new ArrayList<Thread>();

//	public static void main(String[] args) throws IOException {
//		DirectTokenCommunication[] ds = DirectTokenCommunication.createCommunicationLine();
//		SeedFindingManager sfm = new SeedFindingManager(ds[1],"./data.log");
//		for (int i=0; i<THREAD_COUNT; i++) {
//			Thread t = new Thread(new TaskWorkerThread(ds[0]));
//			thread.add(t);
//			t.start();
//		}
//		int t = 0;
//		while (sfm.hasOpenTasks()) {
//			sfm.tick();
//			try {
//				Thread.sleep(250);
//			} catch (InterruptedException e) {
//			}
//			t++;
//			if (Math.floorMod(t, 10)==0) {
//				System.out.println((sfm.getTotalCount()-sfm.getActiveCount()) + "/" + sfm.getTotalCount());
//			}
//		}
//		sfm.shutdown();
//		System.out.println((sfm.getTotalCount()-sfm.getActiveCount()) + "/" + sfm.getTotalCount());
//	}

	// Debug main function for MesaBiomeEvaluator#evaluate()
	public static void main(String[] args) {

		/*
		Scicraft seed: 2791111690993685248
		should expect a mesa at around x47k z-45k, but doesn't
		seem to find it...
		 */

		for (long seed = 0; seed < 2791111690993685249l; seed++) {
			MesaBiomeEvaluator mesaBiomeEvaluator = new MesaBiomeEvaluator();
			EvaluationTask.Context g = new EvaluationTask.Context(seed);
			EvaluationTask.Result r = new EvaluationTask.Result();
			r.worldSeed = seed;

			System.out.println(seed);
			mesaBiomeEvaluator.evaluate(g, r);
		}
	}
}
