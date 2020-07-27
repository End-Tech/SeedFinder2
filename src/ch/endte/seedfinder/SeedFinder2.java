package ch.endte.seedfinder;


import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;



public class SeedFinder2 implements Runnable {
	
	private HashMap<CPos, Boolean> pyramidCache;
	private BufferedWriter objWriter;
	private List<String> structureSeedList;
	private static final ArrayList<Thread> threads = new ArrayList<Thread>(); 
	
	public static final SwampHut SWAMP_HUT = new SwampHut(MCVersion.v1_16);
	public static final Stronghold STRONGHOLD = new Stronghold(MCVersion.v1_7);
	public static final Monument MONUMENT = new Monument(MCVersion.v1_16);
	public static final DesertPyramid DESERT_PYRAMID = new DesertPyramid(MCVersion.v1_16);
	
	public static final int THREAD_COUNT = 23;
	
	public static void main(String[] args) {
		try {
			BufferedReader objReader = new BufferedReader(new FileReader("."+File.separator+"input.txt"));
			ArrayList<String> seedList = new ArrayList<String>();
			while(objReader.ready()) {
				seedList.add(objReader.readLine());
			}
			objReader.close();
			int lineCountPerThread = seedList.size()/THREAD_COUNT;
			for(int i=0;i<THREAD_COUNT;i++) {
				BufferedWriter objWriter = new BufferedWriter(new FileWriter("."+File.separator+"partialOutput" + i + ".txt"));
				List<String> threadList = seedList.subList(lineCountPerThread*i, (i==THREAD_COUNT-1) ? seedList.size()-1 : lineCountPerThread*(i+1)-1);
				SeedFinder2 finderThread = new SeedFinder2(threadList, objWriter);
				Thread newThread = new Thread(finderThread);
				threads.add(newThread);
				newThread.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected SeedFinder2(List<String> seedList, BufferedWriter oWriter) {
		structureSeedList = seedList;
		objWriter = oWriter;
	}
	
	protected static CPos toRegion(int chunkX, int chunkZ, RegionStructure<?, ?> structure) {
		RegionStructure.Data<?> data = structure.at(chunkX, chunkZ);
		return new CPos(data.regionX, data.regionZ);
	}
	
	// for now assumes that the AFK spot will be in the center
	protected static CPos getAfkSpot(CPos... positions) {
		int afkPosX = 0, afkPosZ = 0;
		for(int i = 1; i < positions.length; i++) {
			CPos position = positions[i];
			afkPosX += position.getX();
			afkPosZ += position.getZ();
		}
		return new CPos(afkPosX / (positions.length-1), afkPosZ / (positions.length-1));
	}
	
	public void applyFilter() throws IOException {
		ChunkRand rand = new ChunkRand();

		for(String sLine: structureSeedList) {
			String[] line = sLine.split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			int regionX = Integer.parseInt(line[1]);
			int regionZ = Integer.parseInt(line[2]);

			CPos hut = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ, rand);
			CPos hut1 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ - 1, rand);
			CPos hut2 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ, rand);
			CPos hut3 = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ - 1, rand);
			CPos hutAfk = getAfkSpot(hut, hut1, hut2, hut3);
			pyramidCache = new HashMap<CPos, Boolean>();
			for(long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
				if(!SWAMP_HUT.canSpawn(hut.getX(), hut.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut1.getX(), hut1.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut2.getX(), hut2.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut3.getX(), hut3.getZ(), source))continue;
				CPos[] firstStarts = STRONGHOLD.getStarts(source, 3, rand);
				if(hutAfk.distanceTo(firstStarts[0], DistanceMetric.CHEBYSHEV) > 15)continue;
				if(!testDoublePyramid(source, firstStarts[1], rand)
						&& !testDoublePyramid(source, firstStarts[2], rand))continue;
				objWriter.append(worldSeed + "\n");
			}
			objWriter.flush();
		}
	}
	
	public boolean testDoublePyramid(OverworldBiomeSource source, CPos strongholdPos, ChunkRand rand) {
		CPos region = toRegion(strongholdPos.getX(), strongholdPos.getZ(), DESERT_PYRAMID);
		int regionX = region.getX(), regionZ = region.getZ();
		
		if (!pyramidCache.getOrDefault(region, true))return false;
		CPos pyramid00 = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ, rand);
		if(pyramid00.distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) > 23)return false;
		if(!DESERT_PYRAMID.canSpawn(pyramid00.getX(), pyramid00.getZ(), source))return false;
		// use code similar to village generation in outposts
		int regionDiffX = ((pyramid00.getX()+16) >> 5)+((pyramid00.getX()-10) >> 5) - 2 * (pyramid00.getX()>>5);
		int regionDiffZ = ((pyramid00.getZ()+16) >> 5)+((pyramid00.getZ()-10) >> 5) - 2 * (pyramid00.getZ()>>5);
		boolean hasPossibleNeighbour = false;
		CPos newPyramid;
		if (regionDiffX != 0) {
			newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX + regionDiffX, regionZ, rand);
			if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249) {
				hasPossibleNeighbour = true;
				if(getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
						&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
			}
			if (regionDiffZ != 0) {
				newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ + regionDiffZ, rand);
				if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249) {
					hasPossibleNeighbour = true;
					if(getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
							&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
				}
				newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX + regionDiffX, regionZ + regionDiffZ, rand);
				if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249) {
					hasPossibleNeighbour = true;
					if(getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
							&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
				}
			}
		} else if (regionDiffZ != 0) {
			newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ + regionDiffZ, rand);
			if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249) {
				hasPossibleNeighbour = true;
				if(getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
						&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
			}
		}
		pyramidCache.put(region, hasPossibleNeighbour);
		return false;
	}

	@Override
	public void run() {
		try {
			applyFilter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
