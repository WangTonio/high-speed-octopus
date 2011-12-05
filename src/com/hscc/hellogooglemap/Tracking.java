package com.hscc.hellogooglemap;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Tracking {
	
	// 常數定義
	public static final double RECORD_INTERVAL = 0.2;   // SENSOR 每 0.2 秒記錄一筆資料
	public static final double DISTANCE_RANGE = 0.1326;
	public static final int BEFORE_TURN_PEROID = 10;
	public static final int AFTER_TURN_PEROID = 15;
	
	
	public AnalysisRawData AnalyzedData;
	public ArrayList<Intersection> ForwardIntersection;
	public ArrayList<Intersection> BackwardIntersection;
	List<GeoPoint> ReturnList = new ArrayList<GeoPoint>();
	GeoPoint StartPoint;
	GeoPoint EndPoint;
	public int MiddleIndex;
	
	public Tracking(boolean useOBD){
		
		// 0. 初始化資料
		AnalyzedData = new AnalysisRawData(useOBD);  
		ForwardIntersection = new ArrayList<Intersection>();
		BackwardIntersection = new ArrayList<Intersection>();
		
		StartPoint = new GeoPoint(
				AnalyzedData.myData.StartPoint.getLatitudeE6(),
				AnalyzedData.myData.StartPoint.getLongitudeE6()
				);
		EndPoint = new GeoPoint(
				AnalyzedData.myData.EndPoint.getLatitudeE6(),
				AnalyzedData.myData.EndPoint.getLongitudeE6()
				);
		
		 
	
		// 1. 從 RawData 中找出所有的 intersection
		int numIntersection = AnalyzedData.myData.totalIntersection;
		
		// Debug
		Log.d("numIntersection", ""+numIntersection);
		
		int count = 0;
		int size = AnalyzedData.myData.DataList.size();
		
		for (int i = 0; i < size; i++){
			if (AnalyzedData.myData.DataList.get(i).Intersection == true){
				if (count < (int)(numIntersection/2)){
					ForwardIntersection.add(new Intersection(i));
					Log.e("ForwardIntersection", "count: " + count + ". Index: " + i);
				} else {
					BackwardIntersection.add(new Intersection(i));
					Log.e("BackwardIntersection", "count: " + (count-numIntersection/2) + ". Index: " + i);
				}
				count++;
			}
		}
		
		// 2. 找出中間點
		int a, b;
		int fi = ForwardIntersection.size() - 1;
		a = ForwardIntersection.get(fi).Index;
		b = BackwardIntersection.get(0).Index;
		MiddleIndex = (a + b) / 2;
		
		Log.e("MiddleIndex", ""+MiddleIndex);

		
		// 3. startTracking
		boolean isSuccess = startTracking();
		
		
		// 4. calculate path
		CalculatePath(isSuccess);
		
	}
	
	public List<GeoPoint> getResult(){
		return ReturnList;
		
	}
	
	private boolean startTracking(){
		double FBDistance;
		
		// 1. ForwardTracking
		GeoPoint ForwardEnd = ForwardTracking(StartPoint, MiddleIndex);
		Log.e("Forward End", "lat: " + ForwardEnd.getLatitudeE6()+ ", lon: " +ForwardEnd.getLongitudeE6());
		
		
		// 2. BackwardTracking
		GeoPoint BackwardEnd = BackwardTracking(EndPoint, MiddleIndex + 1);
		

		
		// 3. 判斷 F 的終點跟 B 的終點的距離是否在合理範圍
		// 4. 若是, 所有 intersection 的點就確定了
		// 5. 若否....(Big problem here...)
		GeoPoint a = new GeoPoint(24802041, 120995385);
		GeoPoint b = new GeoPoint(24799002, 120993518);
		Log.d("Distance Range", ""+AnalyzedData.distance(a, b));

		FBDistance = AnalyzedData.distance(ForwardEnd, BackwardEnd);
		if (FBDistance < DISTANCE_RANGE){
			Log.d("Tracking Success!", ""+FBDistance);
			return true;
		} else {
			Log.d("Tracking fault!", ""+ FBDistance);
			return false;
		}

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
		
		// for debug
		Log.e("StartPoint", "lat: "+StartPoint.getLatitudeE6()+". lon: "+StartPoint.getLongitudeE6());
		Log.e("Record", "Direction: "+ direction + ", Distance: " + distance + ".");
		Log.e("ForwardTracking 0", "lat: "+location.getLatitudeE6()+". lon: "+location.getLongitudeE6());
		//
		
		for (int i = 1; i <= StopIndex; i++){
			record = AnalyzedData.myData.DataList.get(i);
			
			if (record.Intersection == false){
				
				direction = record.getDirection();
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
				record.setLocation(location);

				// for debug
				Log.e("ForwardTracking "+ i, "lat: "+location.getLatitudeE6()+". lon: "+location.getLongitudeE6());

				
			} else {
				// for debug
				//return location;
				
				if (Fi < Fsize) {
					if(ForwardIntersection.get(Fi).Index == i){
						
						// 1. 計算出 RawLocation
						direction = record.getDirection();
						distance = record.getSpeed()*RECORD_INTERVAL/1000;	
						location = AnalyzedData.findDest(location, direction, distance);
						// record.setLocation(location);
						ForwardIntersection.get(Fi).setRawLocation(location);
						
						// Debug
						Log.d("Intersection", "RawLoacation, lat: "+location.getLatitudeE6()+", lon: "+location.getLongitudeE6());
						
						// 2. 找出轉彎前的兩個點, 以及轉彎後的一個點
						beforeTurn1 = AnalyzedData.myData.DataList.get(i - BEFORE_TURN_PEROID).Location;
						beforeTurn2 = AnalyzedData.myData.DataList.get(i - BEFORE_TURN_PEROID - 1).Location;
						
						afterTurn = calAfterTurn(location, i, true);
						
						// Debug
						Log.d("beforTurn1", "Index: "+(i- BEFORE_TURN_PEROID )+", lat: "+beforeTurn1.getLatitudeE6()+", lon: "+beforeTurn1.getLongitudeE6());
						Log.d("beforTurn2", "Index: "+(i- BEFORE_TURN_PEROID -1 )+", lat: "+beforeTurn2.getLatitudeE6()+", lon: "+beforeTurn2.getLongitudeE6());
						Log.d("afterTurn", "lat: "+beforeTurn2.getLatitudeE6()+", lon: "+beforeTurn2.getLongitudeE6());
						
						
						
						//afterTurn = AnalyzedData.myData.DataList.get(i + AFTER_TURN_PEROID).Location;
						
						// 3. 使用 FindIntersec 找出 PredictIntersection
						predictLocation = iCalculator.findIntersec(beforeTurn1, beforeTurn2, afterTurn, true, 0);
						
						// Debug
						Log.d("Predict Intersection", "Lat: "+ predictLocation.getLatitudeE6()+", Lon: "+predictLocation.getLongitudeE6());

						
						// 4. 記錄 PredictionLocation
						ForwardIntersection.get(Fi).setPredictLocation(predictLocation);
						record.setLocation(predictLocation);
						
						// 5. 計算 DeltaDistance
						deltaDistance = AnalyzedData.distance(
											ForwardIntersection.get(Fi).PredictLocation, 
											ForwardIntersection.get(Fi).RawLocation);
						ForwardIntersection.get(Fi).DeltaDistance = deltaDistance;
						
						// Debug
						Log.d("Delta Distance", deltaDistance+" km");
						
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
		
		// Debug
		Log.d("BackwardTracking "+lastRecordIndex, "Lat: "+location.getLatitudeE6()+", Lon: "+location.getLongitudeE6());
		
		
		for (int i = lastRecordIndex - 1; i >= StopIndex; i--){
			record = AnalyzedData.myData.DataList.get(i);
			if (record.Intersection == false){
				
				direction = (record.getDirection()+180) % 360; // Backward tracking, 方向相反
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
				record.setLocation(location);
				Log.d("BackwardTracking " + i, "Lat: "+location.getLatitudeE6()+", Lon: "+location.getLongitudeE6());
				
			} else {
				
				if (Bi >= 0) {
					if(BackwardIntersection.get(Bi).Index == i){
						
						// 計算出 RawLocation
						direction = (record.getDirection() + 180) % 360;	// Backward tracking, 方向相反
						distance = record.getSpeed()*RECORD_INTERVAL/1000;	
						location = AnalyzedData.findDest(location, direction, distance);
						BackwardIntersection.get(Bi).setRawLocation(location);
						
						// for debug
						record.setLocation(location);
						
						// 找出轉彎前的兩個點, 以及轉彎後的一個點
						beforeTurn1 = AnalyzedData.myData.DataList.get(i + BEFORE_TURN_PEROID - 1).Location;
						beforeTurn2 = AnalyzedData.myData.DataList.get(i + BEFORE_TURN_PEROID).Location;						
						afterTurn = calAfterTurn(location, i, false);

						// Debug
						Log.d("beforTurn1", "Index: "+(i+BEFORE_TURN_PEROID-1)+", lat: "+beforeTurn1.getLatitudeE6()+", lon: "+beforeTurn1.getLongitudeE6());
						Log.d("beforTurn2", "Index: "+(i+BEFORE_TURN_PEROID)+", lat: "+beforeTurn2.getLatitudeE6()+", lon: "+beforeTurn2.getLongitudeE6());
						Log.d("afterTurn", "lat: "+afterTurn.getLatitudeE6()+", lon: "+afterTurn.getLongitudeE6());
						
						
						
						
						// 使用 FindIntersec 找出 PredictIntersection
						predictLocation = iCalculator.findIntersec(beforeTurn1, beforeTurn2, afterTurn, true, 0);

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
	
	public GeoPoint calAfterTurn(GeoPoint start, int index, boolean isForward){
		SenseRecord record;
		double direction;
		double distance;
		GeoPoint location;
		
		if (isForward){
			
			record = AnalyzedData.myData.DataList.get(index);
			direction = record.getDirection();
			distance = record.getSpeed()*RECORD_INTERVAL/1000;	
			location = AnalyzedData.findDest(start, direction, distance);
			
			for (int i = 1; i < AFTER_TURN_PEROID; i++){
				record = AnalyzedData.myData.DataList.get(index + i);
				direction = record.getDirection();
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
			}
			
		} else { // Backward
			
			record = AnalyzedData.myData.DataList.get(index);
			direction = (record.getDirection()+180)%360;
			distance = record.getSpeed()*RECORD_INTERVAL/1000;	
			location = AnalyzedData.findDest(start, direction, distance);
			
			for (int i = 1; i < AFTER_TURN_PEROID; i++){
				record = AnalyzedData.myData.DataList.get(index - i);
				direction = (record.getDirection()+180)%360;
				distance = record.getSpeed()*RECORD_INTERVAL/1000;	
				location = AnalyzedData.findDest(location, direction, distance);
			}
		}

		return location;
	}
	
	private void CalculatePath(boolean isSuccess){
		FindIntersection f = new FindIntersection();
		List<GeoPoint> tempList;
		GeoPoint prePoint;
		
		if (isSuccess){
			ReturnList.add(StartPoint);
			prePoint = StartPoint; 
			for (Intersection intersection : ForwardIntersection){
				tempList = f.GetDirection(prePoint, intersection.PredictLocation);
				tempList.remove(0);
				ReturnList.addAll(tempList);
				prePoint = intersection.PredictLocation;
			}
			for (Intersection intersection : BackwardIntersection){
				tempList = f.GetDirection(prePoint, intersection.PredictLocation);
				tempList.remove(0);
				ReturnList.addAll(tempList);
				prePoint = intersection.PredictLocation;
			}
			tempList = f.GetDirection(prePoint, EndPoint);
			tempList.remove(0);
			ReturnList.addAll(tempList);
			
		} else {
			
			int Fsize = ForwardIntersection.size();
			int Bsize = BackwardIntersection.size();
			
			GeoPoint x, y;
			
			ReturnList.add(StartPoint);
			prePoint = StartPoint;
			for (int i = 0; i < Fsize - 1; i++){
				x = prePoint;
				y = ForwardIntersection.get(i).PredictLocation;
				tempList = f.GetDirection(x, y);
				tempList.remove(0);
				ReturnList.addAll(tempList);
				prePoint = ForwardIntersection.get(i).PredictLocation;
			}
			for (int i = 1; i < Bsize; i++){
				x = prePoint;
				y = BackwardIntersection.get(i).PredictLocation;
				tempList = f.GetDirection(x, y);
				tempList.remove(0);
				ReturnList.addAll(tempList);
				prePoint = BackwardIntersection.get(i).PredictLocation;
			}
			tempList = f.GetDirection(prePoint, EndPoint);
			tempList.remove(0);
			ReturnList.addAll(tempList);
		}
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
