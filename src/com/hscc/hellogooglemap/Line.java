/* 用於建立直線方程式並保存其參數以備日後回傳	 *
 * latitude   是緯度 (X軸)				  	 *
 * longitude  是經度 (Y軸)					 */

package com.hscc.hellogooglemap;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Line {
	public static final double DIS = 44.0;
	public static final int GEO = 1000000;
	public double paraX = 0;
	public double paraY = 0;
	public double middleX = 0;
	public double middleY = 0;
	public double fixCoord = 0;
	public GeoPoint myReturn = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	
	public Line(){
		paraX = 30;
		paraY = 30;
	}
	
	/*      
	 * (before2)方向 ->
	 *     ●──────────→● (before1)
	 *      ╲          	   │  
	 *        ╲                  │  
	 * (middle) ◎             │
	 *            ╲       │
	 *              ╲  │
	 *                 ● (after)
	 * 
	 */
	public Line(GeoPoint before1, GeoPoint before2, GeoPoint after){
		paraX = (double) (before1.getLatitudeE6()  - before2.getLatitudeE6()  );
		paraY = (double) (before1.getLongitudeE6() - before2.getLongitudeE6() );
		middleX = ((double)(before2.getLatitudeE6()  + after.getLatitudeE6() ) )/2;
		middleY = ((double)(before2.getLongitudeE6() + after.getLongitudeE6()) )/2;
		fixCoord = DIS / (Math.sqrt( (double)(paraX*paraX + paraY*paraY) ));
		/*
		Log.e("paraX","" + paraX);
		Log.e("paraY","" + paraY);
		Log.e("middleX","" + middleX);
		Log.e("middleY","" + middleY);
		Log.e("fixCoord","" + fixCoord);*/
	}
	
	public GeoPoint Function(double var){
		int X,Y;
		X = (int)(middleX + paraX * fixCoord * var);
		Y = (int)(middleY + paraY * fixCoord * var);
		Log.e("算出的X座標","" + X);
		Log.e("算出的Y座標","" + Y);
		myReturn = new GeoPoint(X,Y);
		return myReturn;
	}
}
