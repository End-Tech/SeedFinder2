package ch.endte.seedfinder;

import java.util.ArrayList;

import kaptainwutax.seedutils.mc.pos.CPos;

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
	public static class ExtraStructure {
		CPos pos;
		String structureName;
		int count;
		
		public ExtraStructure() {
			count = 1;
		}
	}
	
}