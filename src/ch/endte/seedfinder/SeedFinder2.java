package ch.endte.seedfinder;


import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.seedutils.util.math.Mth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SeedFinder2 {

	public static final SwampHut SWAMP_HUT = new SwampHut(MCVersion.v1_16);
	public static final Stronghold STRONGHOLD = new Stronghold(MCVersion.v1_7);
	public static final Monument MONUMENT = new Monument(MCVersion.v1_16);
	public static final DesertPyramid DESERT_PYRAMID = new DesertPyramid(MCVersion.v1_16);
	
	public static void main(String[] args) {
		try {
			BufferedReader objReader = new BufferedReader(new FileReader("D:\\Projects\\test3\\input.txt"));
			BufferedWriter objWriter = new BufferedWriter(new FileWriter("D:\\Projects\\test3\\output.txt"));
			applyFilter(objReader, objWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// big thanks to KaptainWutax for providing the following code
	public static void generateQuads(BufferedReader reader, BufferedWriter writer) throws IOException {
		int swampHutSalt = SWAMP_HUT.getSalt(); //you can target any structure salt
		long a = 341873128712L, b = 132897987541L;

		Set<CPos> hutRegions = new HashSet<>();

		for(CPos ringStart: StrongholdGen.getAllStarts(0, StrongholdGen.DEFAULT_CONFIG)) {
			for(int xo = 0; xo < 53; xo++) {
				for(int zo = 0; zo < 53; zo++) {
					hutRegions.add(toRegion(ringStart.getX() - 26 + xo, ringStart.getZ() - 26 + zo, SWAMP_HUT));
				}
			}
		}

		ChunkRand rand = new ChunkRand();
		long totalCount = 0;

		while(reader.ready()) {
			long regionSeed = Long.parseLong(reader.readLine().trim());

			for(CPos hutRegion: hutRegions) {
				long structureSeed = (regionSeed - swampHutSalt - hutRegion.getX() * a - hutRegion.getZ() * b) & Mth.MASK_48;
				CPos strongholdStart = StrongholdGen.getFirstStart(structureSeed, rand);
				CPos hutStart = new CPos(hutRegion.getX() * SWAMP_HUT.getSpacing(), hutRegion.getZ() * SWAMP_HUT.getSpacing());
				double distance = strongholdStart.distanceTo(hutStart, DistanceMetric.CHEBYSHEV);
				if(distance > 14.0D)continue;
				writer.write(structureSeed + " " + hutRegion.getX() + " "  + hutRegion.getZ() + "\n");
			}

			writer.flush();
			totalCount++;

			if(totalCount % 2048 == 0) {
				System.out.println(totalCount);
			}
		}
	}

	private static CPos toRegion(int chunkX, int chunkZ, RegionStructure<?, ?> structure) {
		RegionStructure.Data<?> data = structure.at(chunkX, chunkZ);
		return new CPos(data.regionX, data.regionZ);
	}
	
	// for now assumes that the AFK spot will be in the center
	private static CPos getAfkSpot(CPos... positions) {
		int afkPosX = 0, afkPosZ = 0;
		for(int i = 1; i < positions.length; i++) {
			CPos position = positions[i];
			afkPosX += position.getX();
			afkPosZ += position.getZ();
		}
		return new CPos(afkPosX / (positions.length-1), afkPosZ / (positions.length-1));
	}

	public static void filterQuads(BufferedReader reader, BufferedWriter writer) throws IOException {
		ChunkRand rand = new ChunkRand();

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			int regionX = Integer.parseInt(line[1]);
			int regionZ = Integer.parseInt(line[2]);

			CPos hut = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ, rand);
			CPos hut1 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ - 1, rand);
			CPos hut2 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ, rand);
			CPos hut3 = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ - 1, rand);

			for(long upperBits = 0; upperBits < 1L << 8; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
				if(!SWAMP_HUT.canSpawn(hut.getX(), hut.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut1.getX(), hut1.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut2.getX(), hut2.getZ(), source))continue;
				if(!SWAMP_HUT.canSpawn(hut3.getX(), hut3.getZ(), source))continue;

				CPos[] firstStarts = STRONGHOLD.getStarts(source, 3, rand);
				
				int id = testMonument(source, firstStarts[1], firstStarts[2], rand);
				if(id < 1)continue;
				if(!testTemple(source, id == 1 ? firstStarts[2] : firstStarts[1], rand))continue;
				System.out.println(worldSeed + " with structure seed " + structureSeed);
			}
		}
	}
	
	

	private static boolean testTemple(OverworldBiomeSource source, CPos start, ChunkRand rand) {
		CPos region = toRegion(start.getX(), start.getZ(), DESERT_PYRAMID);
		if(testDouble(source, region.getX(), region.getZ(), DESERT_PYRAMID, rand))return true;
		return false;
	}

	private static int testMonument(OverworldBiomeSource source, CPos start1, CPos start2, ChunkRand rand) {
		CPos region = toRegion(start1.getX(), start1.getZ(), MONUMENT);
		if(testDouble(source, region.getX(), region.getZ(), MONUMENT, rand))return 1;
		region = toRegion(start2.getX(), start2.getZ(), MONUMENT);
		if(testDouble(source, region.getX(), region.getZ(), MONUMENT, rand))return 2;
		return 0;
	}
	
	
	// finds double structures that are at least in 26 chunks range to each other
	// an afk spot can only be made if the structures are contained within a 128 block
	// radius sphere. 26 chunks is 416 blocks diameter
	// thats a little too much
	public static boolean testDouble(OverworldBiomeSource source, int regionX, int regionZ, RegionStructure<?, ?> structure, ChunkRand rand) {
		CPos monument00 = structure.getInRegion(source.getWorldSeed(), regionX, regionZ, rand);
		if(!structure.canSpawn(monument00.getX(), monument00.getZ(), source))return false;

		CPos newMonument = structure.getInRegion(source.getWorldSeed(), regionX + 1, regionZ, rand);
		if(monument00.distanceTo(newMonument, DistanceMetric.EUCLIDEAN_SQ) <= 26 * 26
				&& structure.canSpawn(newMonument.getX(), newMonument.getZ(), source))return true;

		newMonument = structure.getInRegion(source.getWorldSeed(), regionX, regionZ + 1, rand);
		if(monument00.distanceTo(newMonument, DistanceMetric.EUCLIDEAN_SQ) <= 26 * 26
				&& structure.canSpawn(newMonument.getX(), newMonument.getZ(), source))return true;

		newMonument = structure.getInRegion(source.getWorldSeed(), regionX - 1, regionZ, rand);
		if(monument00.distanceTo(newMonument, DistanceMetric.EUCLIDEAN_SQ) <= 26 * 26
				&& structure.canSpawn(newMonument.getX(), newMonument.getZ(), source))return true;

		newMonument = structure.getInRegion(source.getWorldSeed(), regionX, regionZ - 1, rand);
		if(monument00.distanceTo(newMonument, DistanceMetric.EUCLIDEAN_SQ) <= 26 * 26
				&& structure.canSpawn(newMonument.getX(), newMonument.getZ(), source))return true;

		return false;
	}
	
	// But I dont want to touch any of the original code so Im going to copy the code above and copy the pattern from above
	// and adjust it to our needs.
	
	public static void applyFilter(BufferedReader reader, BufferedWriter writer) throws IOException {
		ChunkRand rand = new ChunkRand();

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			int regionX = Integer.parseInt(line[1]);
			int regionZ = Integer.parseInt(line[2]);

			CPos hut = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ, rand);
			CPos hut1 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ - 1, rand);
			CPos hut2 = SWAMP_HUT.getInRegion(structureSeed, regionX - 1, regionZ, rand);
			CPos hut3 = SWAMP_HUT.getInRegion(structureSeed, regionX, regionZ - 1, rand);
			CPos hutAfk = getAfkSpot(hut, hut1, hut2, hut3);
			

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
				System.out.println(worldSeed + " with structure seed " + structureSeed);
			}
		}
	}
	
	public static boolean testDoublePyramid(OverworldBiomeSource source, CPos strongholdPos, ChunkRand rand) {
		CPos region = toRegion(strongholdPos.getX(), strongholdPos.getZ(), DESERT_PYRAMID);
		int regionX = region.getX(), regionZ = region.getZ();
		
		CPos pyramid00 = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ, rand);
		if(pyramid00.distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) > 23) return false;
		if(!DESERT_PYRAMID.canSpawn(pyramid00.getX(), pyramid00.getZ(), source))return false;

		CPos newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX + 1, regionZ, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;

		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ + 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;

		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX - 1, regionZ, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;

		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX, regionZ - 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX + 1, regionZ + 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;

		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX + 1, regionZ - 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX - 1, regionZ + 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;

		newPyramid = DESERT_PYRAMID.getInRegion(source.getWorldSeed(), regionX - 1, regionZ - 1, rand);
		if(pyramid00.distanceTo(newPyramid, DistanceMetric.EUCLIDEAN_SQ) <= 249
				&& getAfkSpot(pyramid00, newPyramid).distanceTo(strongholdPos, DistanceMetric.CHEBYSHEV) <= 15
				&& DESERT_PYRAMID.canSpawn(newPyramid.getX(), newPyramid.getZ(), source))return true;
		return false;
	}
	
}
