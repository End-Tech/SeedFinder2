package ch.endte.seedfinder;

import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;

public class EvaluationTask extends Task {

	public static final String id = "EVAL";
	
	public static final int STRONGHOLD_SEARCH_COUNT = 34;
	public static final int STRONGHOLD_SEARCH_RANGE = 16000/16;
	
	public static final Stronghold STRONGHOLD = new Stronghold(MCVersion.v1_16_1);
	
	public static final SlimeChunkEvaluator SLIME_CHUNK = new SlimeChunkEvaluator();
	public static final SpawnBiomeEvaluator SPAWN_BIOME = new SpawnBiomeEvaluator();
	public static final StructureEvaluator STRUCTURE = new StructureEvaluator();
	public static final MesaBiomeEvaluator MESA_STRIP = new MesaBiomeEvaluator();
	
	@Override
	public void run(String parameter, TokenCommunication c) {
		// get all the parameters
		long seed = Long.parseLong(parameter);
		// set all the important meta parameters
		Context g = new Context(seed);
		g.stronghold = STRONGHOLD.getStarts(g.oSource, STRONGHOLD_SEARCH_COUNT, g.rand);
		Result r = new Result();
		r.worldSeed = seed;
		// run all the evaluation tasks
		SLIME_CHUNK.evaluate(g, r);
		SPAWN_BIOME.evaluate(g, r);
		STRUCTURE.evaluate(g, r);
		MESA_STRIP.evaluate(g, r);
		// finally print the meta data as is so that scoring can be applied
		c.send(new Message(Token.RETURN_EVALUATION_DATA, r.toString()));
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}
	
	// contains the working information for one seed
	public class Context {
		final ChunkRand rand = new ChunkRand();
		final long worldSeed;
		final OverworldBiomeSource oSource;
		final NetherBiomeSource nSource;
		
		CPos[] stronghold;
		
		Context(long seed) {
			worldSeed = seed;
			oSource = new OverworldBiomeSource(MCVersion.v1_16_1, worldSeed);
			nSource = new NetherBiomeSource(MCVersion.v1_16_1, worldSeed);
		}
	}
}
