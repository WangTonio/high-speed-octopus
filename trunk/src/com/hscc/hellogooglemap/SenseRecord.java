package com.hscc.hellogooglemap;

//import android.util.Log;
import com.google.android.maps.GeoPoint;

public class SenseRecord {
	protected long TimeStamp;
	protected double Speed;
	protected double Direction;
	protected boolean Intersection;  // TRUE, If this record is intersection point. 
	protected GeoPoint Location;
	protected GeoPoint GPSLocation;
	
	
	public SenseRecord(long timestamp, double speed, double direction){
		TimeStamp = timestamp;
		Speed = speed;
		Direction = direction;
		Intersection = false;
	}
	
	public void setLocation(GeoPoint location){
		Location = new GeoPoint(location.getLatitudeE6(), location.getLongitudeE6());
	}
	
	public void setIntersection(boolean a){
		Intersection = a;
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
	
	public GeoPoint getGPSLocation(){
		return GPSLocation;
	}
	
	public boolean isIntersection(){
		return Intersection;
	}
}