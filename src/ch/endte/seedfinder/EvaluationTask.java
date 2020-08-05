package ch.endte.seedfinder;

import java.util.ArrayList;

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
	
	// contains the result of the evaluation and will be returned by it
	// ad string
	public class Result {
		
		long worldSeed;
		
		// used to store at which position the largest slime chunk cluster overlaps with a stronghold
		CPos slimeChunkStronghold;
		CPos slimeChunkCenter;
		int slimeChunkMaxCount;
		
		// Evaluate which biomes are around spawn
		BiomeComposition spawnCenterBiomes = new BiomeComposition();
		BiomeComposition spawnRimBiomes = new BiomeComposition();
		
		// evaluate the position (and in the scoring step the distance) of mandatory structures
		CPos quadWitchHutPos;
		CPos doublePyramidPos;
		CPos monumentPos;
		CPos fortressPos;
		CPos outpostPos;
		
		// store any extra structure we look for (WitchHuts and Pyramids)
		ArrayList<ExtraStructure> structureList = new ArrayList<ExtraStructure>();
		
		// store the found long mesa (if any)
		CPos longMesaStart;
		CPos longMesaEnd;
		
		// used to turn a result to a message 
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			sb.append("{\"seed\":"+worldSeed+",");
			
			sb.append("\"slimechunk\":{");
			sb.append("\"stronghold\":"+posToString(slimeChunkStronghold));
			sb.append(",\"center\":"+posToString(slimeChunkCenter));
			sb.append(",\"count\":"+slimeChunkMaxCount+"},");
			
			sb.append("\"spawnBiomes\":{\"center\":"+spawnCenterBiomes.toString());
			sb.append(",\"rim\":"+spawnRimBiomes.toString()+"},");
			
			sb.append("\"mandatorystructures\":{");
			sb.append("\"quadHut\":"+posToString(quadWitchHutPos));
			sb.append(",\"doublePyramid\":"+posToString(doublePyramidPos));
			sb.append(",\"monument\":"+posToString(monumentPos));
			sb.append(",\"fortress\":"+posToString(fortressPos));
			sb.append(",\"outpost\":"+posToString(outpostPos)+"},");
			
			sb.append("\"extrastructures\":[");
			boolean comma = false;
			for (ExtraStructure es: structureList) {
				if (comma) {sb.append(",");}
				else {comma = true;}
				sb.append("{\"name\":\""+es.structureName+"\",\"pos\":"+posToString(es.pos)+",\"count\":"+es.count+"}");
			}
			sb.append("],");
			
			sb.append("\"longmesa\":{\"start\":"+posToString(longMesaStart)+",\"end\":"+posToString(longMesaEnd)+"}}");
			return worldSeed+" "+sb.toString();
		}
		
		// used to construct a result from a message for later use
		public void fromString(String data) {
			
		}
		
		private String posToString(CPos pos) {
			if (pos == null) {return "[]";}
			return "["+pos.getX()+","+pos.getZ()+"]";
		}
		
		// used to contain one extra structure found around strongholds
		class ExtraStructure {
			CPos pos;
			String structureName;
			int count;
		}
		
	}

}
