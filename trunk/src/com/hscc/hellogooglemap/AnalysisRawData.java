package com.hscc.hellogooglemap;

import com.google.android.maps.GeoPoint;

public class AnalysisRawData {
	public static RawData myData = new RawData();
	public int mySize = 0;                    //�P����Ƥj�p
	public int turnLook = 20;                 //�P�_�O�_���s�һݭn����ƶq
	public int R = 6371;                      //�a�y�b�|(km)
	public static final int GEO = 1000000;    //GeoPoint��g�n�ױ`��
	
	//�w�]�غc�l
	AnalysisRawData(){
		initialization();
	}
	
	
	//��l�ƷP�����
	private void initialization() {
		mySize = myData.DataList.size();
		
	}
	
	public turnType isTurn(int index){
		turnType turn = new turnType();
		
		double max_deg = 0;
		double current_deg = 0;
		
		//�H�Ufor�j��Ψӧ�X�o�䤤�̤j�����s��
		for(int i = -turnLook/2 ; i < turnLook/2; i++)
		{
			if( index + i < 1 ){
				break;
			}
			else{
				current_deg = (  myData.DataList.get(i).getDirection() 
						       - myData.DataList.get(i+1).getDirection() ) / 360.0; 
				if (max_deg < current_deg)
					max_deg = current_deg;
			}	
		}
		
		//��X�o�����s���ҥN���N�[
		turn.myTurn = turn.whichTurn(max_deg);
		return turn;
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
}
