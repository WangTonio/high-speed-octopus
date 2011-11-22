package com.hscc.hellogooglemap;

//import android.util.Log;
import com.google.android.maps.GeoPoint;

public class SenseRecord {
	protected long TimeStamp;
	protected double Speed;
	protected double Direction;
	protected GeoPoint Location;
	
	public SenseRecord(long timestamp, double speed, double direction){
		TimeStamp = timestamp;
		Speed = speed;
		Direction = direction;
	}
	
	public void setLocation(GeoPoint location){
		Location = location;
	}
	
	public long getTimeStamp(){
		return TimeStamp;
	}
	
	public double getSpeed(){
		return Speed;
	}
	
	public double getDirection(){
		return Direction;
	}
	
	public GeoPoint getLocation(){
		return Location;
	}
}