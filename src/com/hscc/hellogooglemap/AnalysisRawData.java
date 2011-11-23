package com.hscc.hellogooglemap;

import com.google.android.maps.GeoPoint;

public class AnalysisRawData {
	public static RawData myData;
	public int mySize = 0;                    //感測資料大小
	public int turnLook = 10;                 //判斷是否轉彎所需要的資料量
	public int R = 6371;                      //地球半徑(km)
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
		idenType	currentIden = new idenType();
		int mid;
		
		for(int index = 1; index < mySize; index ++){
			currentTurn = isTurn(index);
			
			if(currentTurn.myTurn != currentTurn.NoTurn){         //有轉彎(承接上個轉彎或是新的轉彎)
				currentIden = identify(counter, currentTurn);
				if(currentIden.rush){//發生rush
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					counter.count = 1;
					counter.state.myTurn = currentTurn.myTurn;
				}
			}else if(currentTurn.myTurn == currentTurn.NoTurn){   //沒有轉彎
				if(counter.count != 0){ //上一個是彎道的一部份
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					counter.count = 0;
				}
			}
		}
		
	}
	
	public idenType identify(counterType counts, turnType turn){
		idenType returnIden = new idenType();
		
		if(counts.count != 0 && turn.myTurn == counts.state.myTurn){ //承接上個轉彎
			counts.count++;
			returnIden.keep = true;
		}else if(counts.count == 0){ //新的轉彎
			counts.count++;
			counts.state.myTurn = turn.myTurn;
			returnIden.keep = true;
		}else{ //新的急轉彎
			returnIden.rush = true;
		}
		
		return returnIden;
	}
	
	public turnType isTurn(int index){
		turnType turn = new turnType();
		
		int max_index = index-turnLook/2;
		int min_index = index-turnLook/2;
		double max_deg = myData.DataList.get(index-turnLook/2).getDirection();
		double min_deg = myData.DataList.get(index-turnLook/2).getDirection();
		double cur_deg = 0;
		
		//以下for迴圈用來找出這其中最大的轉彎角 (宗憲演算法)
		for(int i = -turnLook/2 + 1 ; i < turnLook/2; i++)	
		{
			cur_deg = myData.DataList.get(index+i).getDirection();
			
			if(cur_deg > max_deg){
				max_index = index + i;
				max_deg = cur_deg;
			}else if( cur_deg < min_deg){
				min_index = index + i;
				min_deg = cur_deg;
			}
		}
		
		if(max_index > min_index){ //確認是 後index值 - 前index值
			max_deg = (max_deg - min_deg) / 360;
		}else{
			max_deg = (min_deg - max_deg) / 360;
		}
		
		while(max_deg < 0) //將負角轉換成正向角
			max_deg += 360;
		
		//找出這個轉彎角所代表的意涵
		turn.myTurn = turn.whichTurn(max_deg);
		return turn;
	}
	
	//計算兩個 GeoPoint 間的距離
	public double distance(GeoPoint start, GeoPoint dest){
		double lat1 = start.getLatitudeE6()/GEO;
		double lat2 = dest.getLatitudeE6()/GEO;
		double lon1 = start.getLongitudeE6()/GEO;
		double lon2 = start.getLongitudeE6()/GEO;
		
		double dLat = toRad((lat2-lat1));
		double dLon = toRad((lon2-lon1));
		lat1 = toRad(lat1);
		lat2 = toRad(lat2);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		           Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d;
	}
	
	//計算兩個 GeoPoint 間的夾角
	public double bearing(GeoPoint start, GeoPoint dest){
		double lat1 = start.getLatitudeE6()/GEO;
		double lat2 = dest.getLatitudeE6()/GEO;
		double lon1 = start.getLongitudeE6()/GEO;
		double lon2 = start.getLongitudeE6()/GEO;
		double dLon = toRad((lon2-lon1));

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		        Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		return toDeg(Math.atan2(y, x));
	}
	
	//計算 一個 GeoPoint 作為起點  bearing 作為方向 走了長度d(km)  所產生的目的座標
	public GeoPoint findDest(GeoPoint start, double brng, double d){
		double lat1 = start.getLatitudeE6()/GEO;
		double lon1 = start.getLongitudeE6()/GEO;
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) + 
                Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), 
                       Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));
		
		return (new GeoPoint((int)(lat2*GEO),(int)(lon2*GEO)));
	}
	
	//轉成弧度
	public double toRad(double number){
		return number*Math.PI/180;
	}
	
	//轉成角度
	public double toDeg(double number){
		return number*180/Math.PI;
	}
	
	//轉彎型態類別，呼叫 myTurn 來看是哪一種轉彎型態
	class turnType{
		public int TurnLeft  = 0;
		public int TurnRight = 1;
		public int UTurn     = 2;
		public int NoTurn    = 3;
		public int myTurn;
		
		public double turnThreshold = 10;
		public double TurnLeftDeg = 270;
		public double TurnRightDeg = 90;
		public double UTurnDeg = 180;
		
		public turnType(){
			myTurn = NoTurn;
		}
		
		public int whichTurn(double deg){
			if(deg > TurnLeftDeg - turnThreshold || deg < TurnLeftDeg - turnThreshold)
				return TurnLeft;
			else if(deg > TurnRightDeg - turnThreshold || deg < TurnRightDeg - turnThreshold )
				return TurnRight;
			else if(deg > UTurnDeg - turnThreshold || deg < UTurnDeg - turnThreshold )
				return UTurn;
			else
				return NoTurn;
		}
	}
	
	class counterType{
		public int count;
		public turnType state;
		
		public counterType(){
			count = 0;
			state.myTurn = state.NoTurn;
		}
	}
	
	class idenType{
		public boolean keep;
		public boolean rush;
		
		public idenType(){
			keep = false;
			rush = false;
		}
	}
}
