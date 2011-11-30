package com.hscc.hellogooglemap;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class AnalysisRawData {
	public RawData myData;
	public int totalIntersection;
	public int mySize = 0;                    //感測資料大小
	public int turnLook = 6;                  //判斷是否轉彎所需要的資料量
	public double R = 6371;                   //地球半徑(km)
	public static final int GEO = 1000000;    //GeoPoint轉經緯度常數
	
	//預設建構子
	AnalysisRawData(){
		initialization();
	}
		
	//初始化感測資料
	private void initialization() {
		myData = new RawData();
		mySize = myData.DataList.size();
		fillIntersec();
	}
	
	public void fillIntersec(){
		counterType counter     = new counterType();
		turnType    currentTurn = new turnType();
		boolean	isRush = false;
		int mid;
		
		for(int index = 1; index < mySize; index ++){
			isTurn(index, currentTurn);
			
			if(currentTurn.myTurn != currentTurn.NoTurn){         //有轉彎(承接上個轉彎或是新的轉彎)
				isRush = identify(counter, currentTurn);
				if(isRush){//發生rush
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					totalIntersection++;
					myData.totalIntersection++;
					Log.d("路口","該點 TimeStamp :" + myData.DataList.get(mid).getTimeStamp());
					//Log.d("路口","速度 :" + myData.DataList.get(mid).Speed);
					counter.count = 1;
					counter.state.myTurn = currentTurn.myTurn;
				}
			}else if(currentTurn.myTurn == currentTurn.NoTurn){   //沒有轉彎
				if(counter.count != 0){ //上一個是彎道的一部份
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					myData.totalIntersection++;
					Log.d("路口","該點 TimeStamp :" + myData.DataList.get(mid).getTimeStamp());
					//Log.d("路口","速度 :" + myData.DataList.get(mid).Speed);
					counter.count = 0;
				}
			}
			
		}
	}
	
	public boolean identify(counterType counts, turnType turn){
		boolean what = false;
		if(counts.count != 0 && turn.myTurn == counts.state.myTurn){ //承接上個轉彎
			counts.count++;
		}else if(counts.count == 0){ //新的轉彎
			counts.count++;
			counts.state.myTurn = turn.myTurn;
		}else{ //新的急轉彎
			what = true;
		}		
		return what;
	}
	
	public void isTurn(int index, turnType turn){
		
		double max_deg = 0;
		double cur_deg = 0;
		int max_dis = 0;
		
		//以下for迴圈用來找出這其中最大的轉彎角 (宗憲演算法)
		
		for(int i = -turnLook/2 + 1 ; i <= turnLook/2; i++){
			if(index + i < 0 || index + i >= mySize){
				break;
			}
			for(int j = i+1 ; j <= turnLook/2; j++){
				if(index + j < 0 || index + j >= mySize){
					break;
				}
				cur_deg = (  myData.DataList.get(index + j).getDirection() 
						   - myData.DataList.get(index + i).getDirection() );
				if(cur_deg < 0)
					cur_deg += 360;
				if(cur_deg > 190)
					cur_deg -= 180;
				if(max_deg < cur_deg){
					max_deg = cur_deg;
					max_dis = j;
				}
			}
		}
	
		//找出這個轉彎角所代表的意涵
		turn.myTurn = turn.whichTurn(max_deg);
		turn.final_index = max_dis;
	}
	
	//計算兩個 GeoPoint 間的距離
	public double distance(GeoPoint start, GeoPoint dest){
		double lat1 = toRad((double)start.getLatitudeE6()/GEO);
		double lat2 = toRad((double)dest.getLatitudeE6() /GEO);
		double lon1 = toRad((double)start.getLongitudeE6()/GEO);
		double lon2 = toRad((double)dest.getLongitudeE6() /GEO);

		double dLat = (lat2-lat1);
		double dLon = (lon2-lon1);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		           Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d;
	}
	
	//計算兩個 GeoPoint 間的夾角
	public double bearing(GeoPoint start, GeoPoint dest){
		double lat1 = toRad((double)start.getLatitudeE6()/GEO);
		double lat2 = toRad((double)dest.getLatitudeE6() /GEO);
		double lon1 = toRad((double)start.getLongitudeE6()/GEO);
		double lon2 = toRad((double)dest.getLongitudeE6() /GEO);
		double dLon = (lon2-lon1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		           Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double answer = toDeg(Math.atan2(y, x));
		
		if(answer < 0)
			answer += 360;
		
		return answer;
	}
	
	//計算 一個 GeoPoint 作為起點  bearing(弧度) 作為方向 走了長度d(km)  所產生的目的座標
	public GeoPoint findDest(GeoPoint start, double brng, double d){
		double lat1 = toRad((double)start.getLatitudeE6()/GEO);
		double lon1 = toRad((double)start.getLongitudeE6()/GEO);
		
		brng = toRad(brng);
		double d_div_r = d/R;
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d_div_r) + 
                                 Math.cos(lat1)*Math.sin(d_div_r)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d_div_r)*Math.cos(lat1), 
                                        Math.cos(d_div_r)-Math.sin(lat1)*Math.sin(lat2));
		lat2 = toDeg(lat2);
		lon2 = toDeg(lon2);
		return (new GeoPoint((int)(lat2*GEO),(int)(lon2*GEO)));
	}
	
	//轉成弧度
	public double toRad(double number){
		return number*Math.PI/180.0;
	}
	
	//轉成角度
	public double toDeg(double number){
		return number*180.0/Math.PI;
	}
	
	//轉彎型態類別，呼叫 myTurn 來看是哪一種轉彎型態
	class turnType{
		public int TurnLeft  = 0;
		public int TurnRight = 1;
		public int UTurn     = 2;
		public int NoTurn    = 3;
		public int myTurn;
		
		public double turnThreshold = 35;
		public double TurnLeftDeg   = 270;
		public double TurnRightDeg  = 90;
		public double UTurnDeg      = 180;
		
		public int final_index = 0;
		
		public turnType(){
			myTurn = NoTurn;
		}
		
		public int whichTurn(double deg){
			if     ((deg > (TurnLeftDeg  - turnThreshold)) && (deg < (TurnLeftDeg  + turnThreshold)))
				return TurnLeft;
			else if((deg > (TurnRightDeg  - turnThreshold)) && (deg < (TurnRightDeg  + turnThreshold)))
				return TurnRight;
			else if((deg > (UTurnDeg  - turnThreshold)) && (deg < (UTurnDeg  + turnThreshold)))
				return UTurn;
			else
				return NoTurn;
		}
	}
	
	class counterType{
		public int count;
		public turnType state = new turnType();
		
		public counterType(){
			count = 0;
			state.myTurn = state.NoTurn;
		}
	}
	
	public void testFunction(){
		GeoPoint start = new GeoPoint(24799377,120992149);
		GeoPoint dest  = new GeoPoint(24796942,120993557);
		GeoPoint test  = findDest(dest,18.389444,3);
		Log.d("距離","" + distance(start,dest) + "公里");
		Log.d("角度","" + bearing(start,dest) + "度");
		Log.d("末點","緯度:" + (double)test.getLatitudeE6()/GEO + "  經度" + (double)test.getLongitudeE6()/GEO);
	}
	
}
