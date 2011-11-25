package com.hscc.hellogooglemap;

import java.util.ArrayList;
import com.google.android.maps.GeoPoint;

public class Tracking {
	public static final double RECORD_INTERVAL = 0.2;   // SENSOR 每 0.2 秒記錄一筆資料
	public static final int BEFORE_TURN_PEROID = 5;
	public static final int AFTER_TURN_PEROID = 5;
	
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
		// GeoPoint location;
		
		for (int i = 0; i < size; i++){
			if (AnalyzedData.myData.DataList.get(i).Intersection == true){
				if (count++ < numIntersection/2){
					// location  = AnalyzedData.myData.DataList.get(i).getLocation();
					ForwardIntersection.add(new Intersection(i));
				} else {
					// location = AnalyzedData.myData.DataList.get(i).getLocation();
					BackwardIntersection.add(new Intersection(i));
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
		int Fsize = ForwardIntersection.size();
		int Fi;
		double direction;
		double distance;
		double deltaDistance;
		GeoPoint location, predictLocation;
		GeoPoint beforeTurn1, beforeTurn2;
		GeoPoint afterTurn;
		FindIntersection iCalculator = new FindIntersection();
		
		Fi = 0;
		record = AnalyzedData.myData.DataList.get(0);
		direction = record.getDirection();
		distance = record.getSpeed()*RECORD_INTERVAL/1000;	
		location = AnalyzedData.findDest(StartPoint, direction, distance);
		record.setLocation(location);
	
		for (int i = 1; i <= StopIndex; i++){
			record = AnalyzedData.myData.DataList.get(i);
			
			if (record.Intersection == false){
				
				direction = record.getDirection();
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
				record.setLocation(location);
				
			} else {
				
				if (Fi < Fsize) {
					if(ForwardIntersection.get(Fi).Index == i){
						
						// 計算出 RawLocation
						direction = record.getDirection();
						distance = record.getSpeed()*RECORD_INTERVAL/1000;	
						location = AnalyzedData.findDest(location, direction, distance);
						ForwardIntersection.get(Fi).setRawLocation(location);
						
						// 找出轉彎前的兩個點, 以及轉彎後的一個點
						beforeTurn1 = AnalyzedData.myData.DataList.get(i - BEFORE_TURN_PEROID).Location;
						beforeTurn2 = AnalyzedData.myData.DataList.get(i - BEFORE_TURN_PEROID - 1).Location;
						afterTurn = AnalyzedData.myData.DataList.get(i + AFTER_TURN_PEROID).Location;
						
						// 使用 FindIntersec 找出 PredictIntersection
						predictLocation = iCalculator.findIntersec(beforeTurn1, beforeTurn2, afterTurn, true);

						// 記錄 PredictionLocation
						ForwardIntersection.get(Fi).setPredictLocation(predictLocation);
						record.setLocation(predictLocation);
						
						// 計算 DeltaDistance
						deltaDistance = AnalyzedData.distance(
											ForwardIntersection.get(Fi).PredictLocation, 
											ForwardIntersection.get(Fi).RawLocation);
						ForwardIntersection.get(Fi).DeltaDistance = deltaDistance;
						
						Fi++;
						
					} else { // Something wrong here
					}
				} else { // Error: Intersection 比預期多
				}

			}
		} // end for
		
		return AnalyzedData.myData.DataList.get(StopIndex).Location;
	}
	
	public GeoPoint BackwardTracking(GeoPoint End, int StopIndex){
		SenseRecord record;
		int Bsize = BackwardIntersection.size();
		int Bi;
		int lastRecordIndex;
		double direction;
		double distance;
		double deltaDistance;
		GeoPoint location, predictLocation;;
		GeoPoint beforeTurn1, beforeTurn2;
		GeoPoint afterTurn;
		FindIntersection iCalculator = new FindIntersection();
		
		Bi = Bsize - 1;
		// 從最後一點開始 BackTracking
		lastRecordIndex = AnalyzedData.myData.DataList.size() - 1;
		record = AnalyzedData.myData.DataList.get(lastRecordIndex);
		
		direction = (record.getDirection()+180) % 360;  // Backward tracking, 方向相反
		distance = record.getSpeed()*RECORD_INTERVAL/1000;	
		location = AnalyzedData.findDest(EndPoint, direction, distance);
		record.setLocation(location);
		
		for (int i = lastRecordIndex - 1; i >= StopIndex; i--){
			record = AnalyzedData.myData.DataList.get(i);
			if (record.Intersection == false){
				direction = (record.getDirection()+180) % 360; // Backward tracking, 方向相反
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
				record.setLocation(location);
			} else {
				
				if (Bi > 0) {
					if(BackwardIntersection.get(Bi).Index == i){
						
						// 計算出 RawLocation
						direction = (record.getDirection() + 180) % 360;	// Backward tracking, 方向相反
						distance = record.getSpeed()*RECORD_INTERVAL/1000;	
						location = AnalyzedData.findDest(location, direction, distance);
						BackwardIntersection.get(Bi).setRawLocation(location);
						
						// 找出轉彎前的兩個點, 以及轉彎後的一個點
						beforeTurn1 = AnalyzedData.myData.DataList.get(i + BEFORE_TURN_PEROID - 1).Location;
						beforeTurn2 = AnalyzedData.myData.DataList.get(i + BEFORE_TURN_PEROID).Location;
						afterTurn = AnalyzedData.myData.DataList.get(i - AFTER_TURN_PEROID).Location;
						
						// 使用 FindIntersec 找出 PredictIntersection
						predictLocation = iCalculator.findIntersec(beforeTurn1, beforeTurn2, afterTurn, true);

						// 記錄 PredictionLocation
						BackwardIntersection.get(Bi).setPredictLocation(predictLocation);
						record.setLocation(predictLocation);
						
						// 計算 DeltaDistance
						deltaDistance = AnalyzedData.distance(
											BackwardIntersection.get(Bi).PredictLocation, 
											BackwardIntersection.get(Bi).RawLocation);
						BackwardIntersection.get(Bi).DeltaDistance = deltaDistance;
						
						Bi--;
					} else {} // 若有 Error: index 不配對
				} else {} // 若有 Error: Intersection 數量比預期多

			} // End If : record is intersection or not
		}
		
		return AnalyzedData.myData.DataList.get(StopIndex).Location;
	}
	
	class Intersection {
		int Index;
		double DeltaDistance;
		GeoPoint RawLocation;
		GeoPoint PredictLocation;
		
		public Intersection(int i){
			Index = i;
			DeltaDistance = 0;
		}
		
		public void setRawLocation(GeoPoint gp){
			RawLocation = new GeoPoint(gp.getLatitudeE6(), gp.getLongitudeE6());
		}
		
		public void setPredictLocation (GeoPoint gp){
			PredictLocation = new GeoPoint(gp.getLatitudeE6(), gp.getLongitudeE6());
		}
	}
}
