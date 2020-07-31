package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.regex.Pattern;

import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.Fortress;
import kaptainwutax.featureutils.structure.Monument;
import kaptainwutax.featureutils.structure.PillagerOutpost;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;

public class Filter2Task extends Task {

	public static final String id = "FILTER2";
	
	public static final Stronghold STRONGHOLD = new Stronghold(MCVersion.v1_7);
	public static final Monument MONUMENT = new Monument(MCVersion.v1_16);
	public static final PillagerOutpost PILLAGER_OUTPOST = new PillagerOutpost(MCVersion.v1_16);
	public static final Fortress FORTRESS = new Fortress(MCVersion.v1_16);
	
	@Override
	public void run(String parameter, TokenCommunication c) {
		String[] line = parameter.split(Pattern.quote(" "));
		Context g = new Context(Long.parseLong(line[0]),
				new FeatureSearchData(MONUMENT,false,15,1),
				new FeatureSearchData(FORTRESS,true,18,2),
				new FeatureSearchData(PILLAGER_OUTPOST,false,21,4)
				);
		CPos[] firstStarts = STRONGHOLD.getStarts(g.oSource, 34, g.rand);
		// start at 3 since quad witch hut at 0 and finding out
		// whether the double is at 2 and/or 3 is unnecessary expensive
		
		for (int i=3;i<34;i++) {
			ArrayList<FeatureSearchData> results = performSearch(g, firstStarts[i]);
			if (results.size() == 1) foundEntry(g, results.get(0));
			else if (results.size() > 0) foundMultiEntry(g, results);
			if (g.resultContainer.size() == 0) {
				c.send(new Message(Token.RETURN_PASSED_FILTER_2,line[0]));
				return;
			}
		}
	}
	
	private void foundEntry(Context g,FeatureSearchData fsd) {
		g.searchList.remove(fsd);
		ResultContainer cont = null;
		for (ResultContainer rc: g.resultContainer) {
			if (rc.feature == fsd) {cont = rc;}
			else if ((rc.relatedIds & fsd.bitId) != 0) {rc.relatedCount--;}
		}
		if (cont != null)g.resultContainer.remove(cont); //idiotic check but I get a warn about it so whatever
		updateResultContainers(g);
	}
	
	private void foundMultiEntry(Context g,ArrayList<FeatureSearchData> results) {
		for (ResultContainer rc: g.resultContainer) {
			if (results.contains(rc.feature)) {
				rc.foundCount++;
				for (FeatureSearchData fsd: results) {
					if ((rc.relatedIds & fsd.bitId) == 0) {
						rc.relatedIds = rc.relatedIds | fsd.bitId;
						rc.relatedCount++;
					}
				}
			}
		}
		updateResultContainers(g);
	}

	private void updateResultContainers(Context g) {
		FeatureSearchData found = null;
		for (ResultContainer rc: g.resultContainer) {
			if (rc.foundCount >= rc.relatedCount) {
				found = rc.feature;
				break;
			}
		}
		if (found != null)foundEntry(g, found);
	}

	private ArrayList<FeatureSearchData> performSearch(Context g, CPos stronghold) {
		ArrayList<FeatureSearchData> found = new ArrayList<FeatureSearchData>();
		for (FeatureSearchData fsd: g.searchList) {
			// adapted from KaptainWutax Pillager Outpost verification code:
			CPos nw = toRegion(stronghold.getX()-fsd.searchExtension, stronghold.getZ()-fsd.searchExtension, fsd.feature);
			CPos se = toRegion(stronghold.getX()+fsd.searchExtension, stronghold.getZ()+fsd.searchExtension, fsd.feature);
			CPos featurePos = fsd.feature.getInRegion(g.worldSeed, nw.getX(), nw.getZ(), g.rand);
			if (featurePos != null
					&& stronghold.distanceTo(featurePos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
					&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
				found.add(fsd);
				continue;
			}
			if (se.getX() != nw.getX()) {
				featurePos = fsd.feature.getInRegion(g.worldSeed, se.getX(), nw.getZ(), g.rand);
				if (featurePos != null
						&& stronghold.distanceTo(featurePos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
						&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
					found.add(fsd);
					continue;
				}
				if (se.getZ() != nw.getZ()) {
					featurePos = fsd.feature.getInRegion(g.worldSeed, nw.getX(), se.getZ(), g.rand);
					if (featurePos != null
							&& stronghold.distanceTo(featurePos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
							&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
						found.add(fsd);
						continue;
					}
					featurePos = fsd.feature.getInRegion(g.worldSeed, se.getX(), se.getZ(), g.rand);
					if (featurePos != null
							&& stronghold.distanceTo(featurePos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
							&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
						found.add(fsd);
						continue;
					}
				}
			} else if (se.getZ() != nw.getZ()) {
				featurePos = fsd.feature.getInRegion(g.worldSeed, nw.getX(), se.getZ(), g.rand);
				if (featurePos != null
						&& stronghold.distanceTo(featurePos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
						&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
					found.add(fsd);
					continue;
				}
			}
		}
		return found;
	}
	
	protected static CPos toRegion(int chunkX, int chunkZ, RegionStructure<?, ?> structure) {
		RegionStructure.Data<?> data = structure.at(chunkX, chunkZ);
		return new CPos(data.regionX, data.regionZ);
	}

	@Override
	public String getId() {
		return id;
	}
	
	private static class FeatureSearchData {
		RegionStructure<?,?> feature;
		boolean isNether;
		int searchExtension;
		int bitId;
		
		public FeatureSearchData(RegionStructure<?,?> f, boolean in, int se, int bid) {
			feature = f;
			isNether = in;
			searchExtension = se;
			bitId = bid;
		}
		
	}
	
	// used to figure out if there is at least one stronghold per 
	// structure type even if multiple structures are next to the outposts
	private static class ResultContainer {
		FeatureSearchData feature; //for which feature are these results for
		int relatedIds;
		int relatedCount = 1;
		int foundCount = 0;
		
		public ResultContainer(FeatureSearchData feature) {
			this.feature = feature;
			relatedIds = feature.bitId;
		}
		
	}
	
	private static class Context {
		final ChunkRand rand = new ChunkRand();
		OverworldBiomeSource oSource;
		NetherBiomeSource nSource;
		long worldSeed;
		final ArrayList<ResultContainer> resultContainer = new ArrayList<>();
		final ArrayList<FeatureSearchData> searchList = new ArrayList<>();
		
		Context(long seed, FeatureSearchData... features) {
			worldSeed = seed;
			oSource = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
			nSource = new NetherBiomeSource(MCVersion.v1_16, worldSeed);
			for(int i=0;i<features.length;i++) {
				searchList.add(features[i]);
				resultContainer.add(new ResultContainer(features[i]));
			}
		}
	}

}
