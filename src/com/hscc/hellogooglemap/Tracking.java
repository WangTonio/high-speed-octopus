package com.hscc.hellogooglemap;

import java.util.ArrayList;
import com.google.android.maps.GeoPoint;

public class Tracking {
	public final double RECORD_INTERVAL = 0.2;   // SENSOR 每 0.2 秒記錄一筆資料
	public AnalysisRawData AnalyzedData;
	public ArrayList<Intersection> ForwardIntersection;
	public ArrayList<Intersection> BackwardIntersection;
	GeoPoint StartPoint;
	GeoPoint EndPoint;
	public int MiddleIndex;
	
	public Tracking(){
		
		// 0. 初始化資料
		AnalyzedData = new AnalysisRawData();
		ForwardIntersection = new ArrayList<Intersection>();
		ForwardIntersection = new ArrayList<Intersection>();
		
		StartPoint = new GeoPoint(
				AnalyzedData.myData.StartPoint.getLatitudeE6(),
				AnalyzedData.myData.StartPoint.getLongitudeE6()
				);
		EndPoint = new GeoPoint(
				AnalyzedData.myData.EndPoint.getLatitudeE6(),
				AnalyzedData.myData.EndPoint.getLongitudeE6()
				);
		
		// 1. 從 RawData 中找出所有的 intersection
		int numIntersection = AnalyzedData.totalIntersection;
		int count = 0;
		int size = AnalyzedData.myData.DataList.size();
		GeoPoint location;
		for (int i = 0; i < size; i++){
			if (AnalyzedData.myData.DataList.get(i).Intersection == true){
				if (count++ < numIntersection/2){
					location  = AnalyzedData.myData.DataList.get(i).getLocation();
					ForwardIntersection.add(new Intersection(i, location));
				} else {
					location = AnalyzedData.myData.DataList.get(i).getLocation();
					BackwardIntersection.add(new Intersection(i, location));
				}
			}
		}
		
		// 2. 找出中間點
		int a, b;
		int fi = ForwardIntersection.size()-1;
		a = ForwardIntersection.get(fi).Index;
		b = BackwardIntersection.get(0).Index;
		MiddleIndex = (a + b) / 2;
		
		// 3. startTracking
		startTracking();
	}
	
	public void startTracking(){
		
		// 1. ForwardTracking
		GeoPoint ForwardEnd = ForwardTracking(StartPoint, MiddleIndex);
		
		// 2. BackwardTracking
		GeoPoint BackwardEnd = BackwardTracking(EndPoint, MiddleIndex + 1);
		
		// 3. 判斷 F 的終點跟 B 的終點的距離是否在合理範圍
		// 4. 若是, 所有 intersection 的點就確定了
		// 5. 若否....(Big problem here...)
	}

	
	// 1. 從 start point 出發, 直到遇到 intersection
	// 2. 從原始資料的 GeoPoint of intersection , 找出地圖上對應的  GeoPoint of intersection
	// 3. 把地圖上對應的 GeoPoint of intersection 存到 RawData 裏
	// 4. 令 intersection 成為新的 start point 重覆 Step1, 直到走到 StopIndex	
	public GeoPoint ForwardTracking(GeoPoint Start, int StopIndex){
		SenseRecord record;
		int fSize = ForwardIntersection.size();
		int countIntersection = 0;
		double direction;
		double distance;
		GeoPoint location;
		
		record = AnalyzedData.myData.DataList.get(0);
		direction = record.getDirection();
		distance = record.getSpeed()*RECORD_INTERVAL/1000;	
		location = AnalyzedData.findDest(StartPoint, direction, distance);
		AnalyzedData.myData.DataList.get(0).Location = location;
		
		for (int i = 1; i <= StopIndex; i++){
			record = AnalyzedData.myData.DataList.get(i);
			if (record.Intersection == false){
				direction = record.getDirection();
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(StartPoint, direction, distance);
				AnalyzedData.myData.DataList.get(i).Location = location;
			} else {
				// 計算出 RawLocation, 並存進 record.Location
				// 向 GOOGLE 找 PredictIntersection
				// 記錄 PredictionLocation
				// 計算 DeltaDistance
			}
		}
		
		

		return Start;
	}
	
	public GeoPoint BackwardTracking(GeoPoint End, int StopIndex){
		SenseRecord record;
		int bSize = BackwardIntersection.size();
		int lastRecordIndex;
		int countIntersection = 0;
		double direction;
		double distance;
		GeoPoint location;
		
		lastRecordIndex = AnalyzedData.myData.DataList.size() - 1;
		record = AnalyzedData.myData.DataList.get(lastRecordIndex);
		direction = (record.getDirection()+180) % 360;  // backward tracking, 方向相反
		distance = record.getSpeed()*RECORD_INTERVAL/1000;	
		location = AnalyzedData.findDest(EndPoint, direction, distance);
		AnalyzedData.myData.DataList.get(lastRecordIndex).Location = location;
		
		for (int i = lastRecordIndex - 1; i >= StopIndex; i--){
			record = AnalyzedData.myData.DataList.get(i);
			if (record.Intersection == false){
				direction = (record.getDirection()+180) % 360; // backward tracking, 方向相反
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(StartPoint, direction, distance);
				AnalyzedData.myData.DataList.get(i).Location = location;
			} else {
				// 計算出 RawLocation, 並存進 record.Location
				// 向 GOOGLE 找 PredictIntersection
				// 記錄 PredictionLocation
				// 計算 DeltaDistance
			}
		}
		
		return End;
	}
	
	class Intersection {
		int Index;
		double DeltaDistance;
		GeoPoint RawLocation;
		GeoPoint PredictLocation;
		
		public Intersection(int i, GeoPoint gp){
			Index = i;
			DeltaDistance = 0;
			RawLocation = gp;
		}
	}
}
