package ch.endte.seedfinder;

import kaptainwutax.featureutils.structure.RegionStructure;

public class FeatureSearchData {

	RegionStructure<?,?> feature;
	boolean isNether;
	int searchExtension;
	int bitId;
	int coordinateMultiplier;

	public FeatureSearchData(RegionStructure<?,?> f, boolean in, int se, int bid, int cm) {
		feature = f;
		isNether = in;
		searchExtension = se;
		bitId = bid;
		coordinateMultiplier = cm;
	}

}