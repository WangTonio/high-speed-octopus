/* 用於建立直線方程式並保存其參數以備日後回傳	 *
 * latitude   是緯度 (X軸)				  	 *
 * longitude  是經度 (Y軸)					 */

package com.hscc.hellogooglemap;

import com.google.android.maps.GeoPoint;

public class Line {
	private static final int DIS = 1530;
	private double paraX = 0;
	private double paraY = 0;
	private double middleX = 0;
	private double middleY = 0;
	private double fixCoord = 0;
	public GeoPoint myReturn = null;
	
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
		paraX = before1.getLatitudeE6()  - before2.getLatitudeE6();
		paraY = before1.getLongitudeE6() - before2.getLongitudeE6();
		middleX = (before1.getLatitudeE6() - after.getLatitudeE6() )/2;
		middleY = (before1.getLongitudeE6()- after.getLongitudeE6())/2;
		fixCoord = DIS / (Math.sqrt( paraX*paraX+paraY*paraY ));
	}
	
	public GeoPoint Function(double var){
		int X,Y;
		X = (int)(middleX + paraX * fixCoord * var);
		Y = (int)(middleY + paraY * fixCoord * var);
		myReturn = new GeoPoint(X,Y);
		return myReturn;
	}
}
