package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ch.endte.seedfinder.EvaluationTask.Context;
import ch.endte.seedfinder.Result.ExtraStructure;
import kaptainwutax.featureutils.structure.DesertPyramid;
import kaptainwutax.featureutils.structure.Fortress;
import kaptainwutax.featureutils.structure.Monument;
import kaptainwutax.featureutils.structure.PillagerOutpost;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.SwampHut;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;

public class StructureEvaluator {

	private static final SwampHut        WITCH_HUT = new SwampHut(MCVersion.v1_16_1);
	private static final DesertPyramid   PYRAMID = new DesertPyramid(MCVersion.v1_16_1);
	private static final Fortress        FORTRESS = new Fortress(MCVersion.v1_16_1);
	private static final Monument        MONUMENT = new Monument(MCVersion.v1_16_1);
	private static final PillagerOutpost OUTPOST = new PillagerOutpost(MCVersion.v1_16_1);
	
	private static final MagnitudeSorter SORTER = new MagnitudeSorter();
	
	private static final ArrayList<FeatureSearchData> searchList = new ArrayList<>();
	
	public void evaluate(Context g, Result r) {
		
		
		r.quadWitchHutPos = evalWitchHut(g);
		searchStrongholds(g, r);
		identifyMandatoryStructures(g, r);
		
	}

	private static CPos evalWitchHut(Context g) {
		// unique evaluation since we know quad witch hut exists
		// is placed on a corner
		// we just need to evaluate which of the corners of the region
		// its placed on
		CPos[] huts = new CPos[4];
		CPos startRegion = toRegion(g.stronghold[0].getX(),g.stronghold[0].getX(), WITCH_HUT);
		huts[0] = WITCH_HUT.getInRegion(g.worldSeed, startRegion.getX(), startRegion.getZ(), g.rand);
		huts[1] = WITCH_HUT.getInRegion(g.worldSeed, startRegion.getX() - 1, startRegion.getZ(), g.rand);
		huts[2] = WITCH_HUT.getInRegion(g.worldSeed, startRegion.getX() - 1, startRegion.getZ()-1, g.rand);
		huts[3] = WITCH_HUT.getInRegion(g.worldSeed, startRegion.getX(), startRegion.getZ()-1, g.rand);
		return averagePosition(huts);
	}
	
	private static void searchStrongholds(Context g, Result r) {
		for (FeatureSearchData fsd: searchList) {
			for(int i=1;i<g.stronghold.length;i++) {
				searchForFeature(g, r, g.stronghold[i], fsd);
			}
			for (ExtraStructure es: r.structureList) {
				if (es.structureName == fsd.feature.getName()) {
					es.pos = new CPos(es.pos.getX()*fsd.coordinateMultiplier, es.pos.getZ()*fsd.coordinateMultiplier);
				}
			}
		}
		Collections.sort(r.structureList, SORTER);
	}
	
	private static void identifyMandatoryStructures(Context g, Result r) {
		ArrayList<Integer> toRemove = new ArrayList<>();
		for (int i =0; i<r.structureList.size();i++) {
			ExtraStructure es = r.structureList.get(i);
			switch (es.structureName) {
			case ("monument"):
				if (r.monumentPos == null) {
					toRemove.add(i);
					r.monumentPos = es.pos;
				}
				break;
			case ("desert_pyramid"):
				if (r.doublePyramidPos == null && es.count <= 2) {
					toRemove.add(i);
					r.doublePyramidPos = es.pos;
				}
				break;
			case ("pillager_outpost"):
				if (r.outpostPos == null) {
					toRemove.add(i);
					r.outpostPos = es.pos;
				}
				break;
			case ("fortress"):
				if (r.fortressPos == null) {
					toRemove.add(i);
					r.fortressPos = es.pos;
				}
				break;
			default:
				break;
			}
		}
		
		for (int i = toRemove.size()-1; i>=0;i--) {
			int tr = toRemove.get(i);
			r.structureList.remove(tr);
		}
	}
	
	private static CPos toRegion(int chunkX, int chunkZ, RegionStructure<?, ?> structure) {
		RegionStructure.Data<?> data = structure.at(chunkX, chunkZ);
		return new CPos(data.regionX, data.regionZ);
	}
	
	private static CPos averagePosition(CPos... cp) {
		int x = 0;
		int z = 0;
		for (int i =0;i<cp.length;i++) {
			x += cp[i].getX();
			z += cp[i].getZ();
		}
		return new CPos(x/(cp.length-1),z/(cp.length-1));
	}
	
	private static void searchForFeature(Context g, Result r, CPos stronghold, FeatureSearchData fsd) {
		// adapted from KaptainWutax Pillager Outpost verification code:
		CPos nw = toRegion((stronghold.getX()-fsd.searchExtension)/fsd.coordinateMultiplier, (stronghold.getZ()-fsd.searchExtension)/fsd.coordinateMultiplier, fsd.feature);
		CPos se = toRegion((stronghold.getX()+fsd.searchExtension)/fsd.coordinateMultiplier, (stronghold.getZ()+fsd.searchExtension)/fsd.coordinateMultiplier, fsd.feature);
		HashMap<CPos,ExtraStructure> found = new HashMap<>();
		HashMap<CPos,ExtraStructure> foundStructures = new HashMap<>();
		ExtraStructure es;
		es = getFeatureNeighboursInRegion(g, nw.getX(), nw.getZ(), stronghold, fsd, foundStructures);
		if (es != null) {found.put(es.pos, es);}
		if (se.getX() != nw.getX()) {
			es = getFeatureNeighboursInRegion(g, se.getX(), nw.getZ(), stronghold, fsd, foundStructures);
			if (es != null) {found.put(es.pos, es);}
			if (se.getZ() != nw.getZ()) {
				es = getFeatureNeighboursInRegion(g, nw.getX(), se.getZ(), stronghold, fsd, foundStructures);
				if (es != null) {found.put(es.pos, es);}
				es = getFeatureNeighboursInRegion(g, se.getX(), se.getZ(), stronghold, fsd, foundStructures);
				if (es != null) {found.put(es.pos, es);}
			}
		} else if (se.getZ() != nw.getZ()) {
			es = getFeatureNeighboursInRegion(g, se.getX(), se.getZ(), stronghold, fsd, foundStructures);
			if (es != null) {found.put(es.pos, es);}
		}
		r.structureList.addAll(found.values());
	}
	
	private static ExtraStructure getFeatureNeighboursInRegion(Context g, int regionX, int regionZ, CPos stronghold, FeatureSearchData fsd, Map<CPos,ExtraStructure> foundStructures) {
		CPos featurePos = fsd.feature.getInRegion(g.worldSeed, regionX, regionZ, g.rand);
		if (featurePos != null) {
			CPos dPos = new CPos(featurePos.getX()*fsd.coordinateMultiplier, featurePos.getZ()*fsd.coordinateMultiplier);
			if (stronghold.distanceTo(dPos, DistanceMetric.CHEBYSHEV) <= fsd.searchExtension
					&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) {
				return findNeighbors(g, featurePos, fsd, foundStructures); // position within the dimension of the feature
			}
		}
		return null;
	}
	
	private static ExtraStructure findNeighbors(Context g, CPos start, FeatureSearchData fsd, Map<CPos,ExtraStructure> foundStructures) {
		
		boolean isNew = !foundStructures.containsKey(start);
		ExtraStructure es = foundStructures.compute(start, (k,v)-> {
			if (v == null) { 
				v = new ExtraStructure();
				v.count = 1;
				v.structureName = fsd.feature.getName();
				v.pos = start;
			}
			return v;
		});
		CPos center = toRegion(start.getX(), start.getZ(), fsd.feature);
		CPos nw = toRegion(start.getX()+15, start.getZ()+15, fsd.feature);
		CPos se = toRegion(start.getX()-15, start.getZ()-15, fsd.feature);
		int xMod = nw.getX()+se.getX()-2*center.getX(); // assuming that fsd.feature.getSpacing() > 15 will only turn into -1, 0, 1
		int zMod = nw.getZ()+se.getZ()-2*center.getZ();
		
		
		CPos neighbour = testNeighbour(g, start, center.getX()+xMod, center.getZ(), fsd, foundStructures);
		if (neighbour != null) {
			foundStructures.put(neighbour,es);
			addPointToResult(neighbour,es);
		}
		neighbour = testNeighbour(g, start, center.getX(), center.getZ()+zMod, fsd, foundStructures);
		if (neighbour != null) {
			foundStructures.put(neighbour,es);
			addPointToResult(neighbour,es);
		}
		neighbour = testNeighbour(g, start, center.getX()+xMod, center.getZ()+zMod, fsd, foundStructures);
		if (neighbour != null) {
			foundStructures.put(neighbour,es);
			addPointToResult(neighbour,es);
		}
		if (isNew) {return es;}
		return null;
	}
	
	public static CPos testNeighbour(Context g, CPos start, int regionX, int regionZ, FeatureSearchData fsd, Map<CPos,ExtraStructure> foundStructures) {
		CPos featurePos = fsd.feature.getInRegion(g.worldSeed, regionX, regionZ, g.rand);
		if (featurePos != null // is valid structure position
				&& !foundStructures.containsKey(featurePos) // has not been found before
				&& start.distanceTo(featurePos, DistanceMetric.EUCLIDEAN_SQ) <= 249 // is within range
				&& fsd.feature.canSpawn(featurePos.getX(), featurePos.getZ(), fsd.isNether?g.nSource:g.oSource)) { // and can actually generate
			return featurePos;
		}
		return null;
	}
	
	private static void addPointToResult(CPos result, ExtraStructure es) {
		es.count++;
		int x = result.getX()/es.count+es.pos.getX()*(es.count-1)/es.count;
		int z = result.getZ()/es.count+es.pos.getZ()*(es.count-1)/es.count;
		es.pos = new CPos(x,z);
	}
	
	static {
		searchList.add(new FeatureSearchData(MONUMENT,false,15,1,1));
		searchList.add(new FeatureSearchData(FORTRESS,true,18,2,8));
		searchList.add(new FeatureSearchData(OUTPOST,false,21,4,1));
		searchList.add(new FeatureSearchData(PYRAMID,false,15,8,1));
		searchList.add(new FeatureSearchData(WITCH_HUT,false,15,16,1));
	}
	
	private static class MagnitudeSorter implements Comparator<ExtraStructure> {
		@Override
		public int compare(ExtraStructure arg0, ExtraStructure arg1) {
			return Double.compare(arg0.pos.getMagnitudeSq(), arg1.pos.getMagnitudeSq());
		}
	}
	
}
