/* *** 以下使找路口的code實作 ****/
package com.hscc.hellogooglemap;

import com.google.android.maps.GeoPoint;

public class FindIntersection {
	private static final int GEO = 1000000;
	private static final int DIS = 1530;
	GeoPoint taipei_station   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	
	public GeoPoint findIntersec (GeoPoint beforeTurn1, GeoPoint beforeTurn2,GeoPoint afterTurn, int accurary){
    	GeoPoint intersec = null;
    	
    	return intersec;
    }
}
