/* *** 以下使找路口的code實作 ****/
package com.hscc.hellogooglemap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import com.google.android.maps.GeoPoint;

public class FindIntersection {
	public static final int GEO = 1000000;
	public List<GeoPoint> passPoint = new ArrayList<GeoPoint>(); //記錄每一次向google取得的座標點
	public Line myLine = new Line();
	public int progressMax = 5;
	GeoPoint before1;
	GeoPoint before2;
	GeoPoint after;
	GeoPoint taipei_station   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	
	public FindIntersection(GeoPoint beforeTurn1, GeoPoint beforeTurn2,GeoPoint afterTurn){
		before1 = beforeTurn1;
		before2 = beforeTurn2;
		after   = afterTurn;
	}
	
	public GeoPoint findIntersec (boolean lookback){
    	GeoPoint intersec = taipei_station;
    	GeoPoint nextDest;
    	myLine = new Line(before1 ,before2, after);
    	boolean keepgoing = true;
    	for(double i = 0; (i < progressMax) & keepgoing; i = i + 1.0)
    	{
    		nextDest = myLine.Function(i);
    		GetDirection(before1, nextDest);
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		for(GeoPoint thistime : passPoint)
    		{
    			if ( !(thistime.equals(before1)) && !(thistime.equals(nextDest))){
    				intersec = thistime;
    				keepgoing = false;
    				Log.e("路口經度",""+intersec.getLatitudeE6() );
    				Log.e("路口緯度",""+intersec.getLongitudeE6());
    				break;
    			}
    		}
    		
    		passPoint.clear();
    		
    		if(lookback){
    			nextDest = myLine.Function(-i);
    			GetDirection(before1, nextDest);
    			for(GeoPoint thistime : passPoint)
    			{
    				if ( !(thistime.equals(before1)) && !(thistime.equals(nextDest))){
    					intersec = thistime;
    					keepgoing = false;
    					break;
    				}
    			}
    			passPoint.clear();
    		}
    	}    	
    	return intersec;
    }
	
	
/*  &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&防害分隔線&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& */	
	
	
	
	/* *** 以下是 Google Path 要求 ****/
	//The following function is made for PATH CALCULATION	
	public List<GeoPoint> GetDirection(GeoPoint StartPoint ,GeoPoint Destination)
	{
	    String mapAPI = "http://maps.google.com/maps/api/directions/json?origin={0}&destination={1}&language=zh-TW&sensor=true";
	    String url = MessageFormat.format(mapAPI, ExtraLocation(StartPoint),ExtraLocation(Destination));

	    HttpGet get = new HttpGet(url);
	    String strResult = "";
	    try
	    {
	        HttpParams httpParameters = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
	        HttpClient httpClient = new DefaultHttpClient(httpParameters);

	        HttpResponse httpResponse = null;
	        httpResponse = httpClient.execute(get);

	        if (httpResponse.getStatusLine().getStatusCode() == 200)
	        {
	            strResult = EntityUtils.toString(httpResponse.getEntity());

	            JSONObject jsonObject = new JSONObject(strResult);
	            JSONArray routeObject = jsonObject.getJSONArray("routes");
	            String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline").getString("points");

	            if (polyline.length() > 0)
	            {
	                decodePolylines(polyline);
	            }

	        }
	    }
	    catch (Exception e)
	    {
	        Log.e("地圖", "路線錯誤:" + "可能是 Google API Over Limit，或是沒有這個路徑");
	    }

	    return passPoint;
	}

	public String ExtraLocation(GeoPoint a){
		String location = "";
		location = "" + Double.toString(((double)a.getLatitudeE6())/GEO) + "," + Double.toString(((double)a.getLongitudeE6())/GEO);
		return location;
	}
	
	//Decode google path XML
	public void decodePolylines(String poly)
	{
	    int len = poly.length();
	    int index = 0;
	    int lat = 0;
	    int lng = 0;

	    while (index < len)
	    {
	        int b, shift = 0, result = 0;
	        do
	        {
	            b = poly.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do
	        {
	            b = poly.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
	        passPoint.add(p);

	    }
	}
}
