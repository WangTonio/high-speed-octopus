package com.hscc.hellogooglemap;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class AnalysisRawData {
	public RawData myData;
	public int mySize = 0;                    //�P����Ƥj�p
	public int turnLook = 6;                 //�P�_�O�_���s�һݭn����ƶq
	public int R = 6371;                      //�a�y�b�|(km)
	public static final int GEO = 1000000;    //GeoPoint��g�n�ױ`��
	
	//�w�]�غc�l
	AnalysisRawData(){
		initialization();
	}
		
	//��l�ƷP�����
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
			
			if(currentTurn.myTurn != currentTurn.NoTurn){         //�����s(�ӱ��W�����s�άO�s�����s)
				isRush = identify(counter, currentTurn);
				if(isRush){//�o��rush
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					myData.totalIntersection++;
					Log.e("���f","�n�� :" + myData.DataList.get(mid).Direction);
					Log.e("���f","�g�� :" + myData.DataList.get(mid).Speed);
					counter.count = 1;
					counter.state.myTurn = currentTurn.myTurn;
				}
			}else if(currentTurn.myTurn == currentTurn.NoTurn){   //�S�����s
				if(counter.count != 0){ //�W�@�ӬO�s�D���@����
					mid = (index - counter.count/2);
					myData.DataList.get(mid).Intersection = true;
					myData.totalIntersection++;
					Log.e("���f","�n�� :" + myData.DataList.get(mid).Direction);
					Log.e("���f","�g�� :" + myData.DataList.get(mid).Speed);
					counter.count = 0;
				}
			}
			
			
		}
	}
	
	public boolean identify(counterType counts, turnType turn){
		boolean what = false;
		if(counts.count != 0 && turn.myTurn == counts.state.myTurn){ //�ӱ��W�����s
			counts.count++;
		}else if(counts.count == 0){ //�s�����s
			counts.count++;
			counts.state.myTurn = turn.myTurn;
		}else{ //�s�������s
			what = true;
		}		
		return what;
	}
	
	public void isTurn(int index, turnType turn){
		
		double max_deg = 0;
		double cur_deg = 0;
		
		//�H�Ufor�j��Ψӧ�X�o�䤤�̤j�����s�� (�v�˺t��k)
		
		for(int i = -turnLook/2 + 1 ; i < turnLook/2; i++){
			if(index + i < 0 || index + i >= mySize){
				break;
			}
			for(int j = i ; j < turnLook/2; i++){
				if(index + i >= mySize){
					break;
				}
				cur_deg = (  myData.DataList.get(index + j).getDirection() 
						       - myData.DataList.get(index + i).getDirection() ) % 360;
				if(max_deg < cur_deg)
					max_deg = cur_deg;
			}
		}
		
		Log.e("���s��","max_deg:" + max_deg);
			
		//��X�o�����s���ҥN���N�[
		turn.myTurn = turn.whichTurn(max_deg);
		Log.e("���s��","�p��:" + turn.myTurn);
	}
	
	//�p���� GeoPoint �����Z��
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
	
	//�p���� GeoPoint ��������
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
	
	//�p�� �@�� GeoPoint �@���_�I  bearing �@����V ���F����d(km)  �Ҳ��ͪ��ت��y��
	public GeoPoint findDest(GeoPoint start, double brng, double d){
		double lat1 = start.getLatitudeE6()/GEO;
		double lon1 = start.getLongitudeE6()/GEO;
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) + 
                Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), 
                       Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));
		
		return (new GeoPoint((int)(lat2*GEO),(int)(lon2*GEO)));
	}
	
	//�ন����
	public double toRad(double number){
		return number*Math.PI/180;
	}
	
	//�ন����
	public double toDeg(double number){
		return number*180/Math.PI;
	}
	
	//���s���A���O�A�I�s myTurn �ӬݬO���@�����s���A
	class turnType{
		public int TurnLeft  = 0;
		public int TurnRight = 1;
		public int UTurn     = 2;
		public int NoTurn    = 3;
		public int myTurn;
		
		public double turnThreshold = 20;
		public double TurnLeftDeg   = 270;
		public double TurnRightDeg  = 90;
		public double UTurnDeg      = 180;
		
		public turnType(){
			myTurn = NoTurn;
		}
		
		public int whichTurn(double deg){
			if     (deg > TurnLeftDeg  - turnThreshold && deg < TurnLeftDeg  - turnThreshold)
				return TurnLeft;
			else if(deg > TurnRightDeg - turnThreshold && deg < TurnRightDeg - turnThreshold )
				return TurnRight;
			else if(deg > UTurnDeg     - turnThreshold && deg < UTurnDeg     - turnThreshold )
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
	
}
