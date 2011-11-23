package com.hscc.hellogooglemap;

import java.util.ArrayList;
import com.google.android.maps.GeoPoint;

public class Tracking {
	AnalysisRawData OriData = new AnalysisRawData();         //建立 AnalysisRawData
	ArrayList<SenseRecord> forwarding  = new ArrayList();    //forwarding 所產生的資料   
	ArrayList<SenseRecord> backwarding = new ArrayList();    //backwarding所產生的資料
	
	public Tracking(){
		// 1. 從 RawData 中找出所有的 intersection
		// 2. 找出中間點
		// 3. startTracking
	}
	
	public void startTracking(){
		// 1. ForwardTracking
		// 2. BackwardTracking
		// 3. 判斷 F 的終點跟 B 的終點的距離是否在合理範圍
		// 4. 若是, 所有 intersection 的點就確定了
		// 5. 若否....
	}
	
	public void ForwardTracking(GeoPoint Start, int StopIndex){
		// 1. 從 start point 出發, 直到遇到 intersection
		// 2. 從原始資料的 GeoPoint of intersection , 找出地圖上對應的  GeoPoint of intersection
		// 3. 把地圖上對應的 GeoPoint of intersection 存到 RawData 裏
		// 4. 令 intersection 成為新的 start point 重覆 Step1, 直到走到 StopIndex
	}
	
	public void BackwardTracking(GeoPoint End, int StopIndex){
		// 1. 從 End point 出發, 直到遇到 intersection
		// 2. 從原始資料的 GeoPoint of intersection , 找出地圖上對應的  GeoPoint of intersection
		// 3. 把地圖上對應的 GeoPoint of intersection 存到 RawData 裏
		// 4. 令 intersection 成為新的 start point 重覆 Step1, 直到走到 StopIndex

	}
}
