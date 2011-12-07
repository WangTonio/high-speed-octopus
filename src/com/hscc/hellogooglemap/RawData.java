package com.hscc.hellogooglemap;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import com.google.android.maps.GeoPoint;



public class RawData{
	GeoPoint StartPoint;
	GeoPoint EndPoint;
	public int totalIntersection = 0;	// total number of intersection in this Record set.
	public ArrayList<SenseRecord> DataList = new ArrayList<SenseRecord>();
	
	public RawData(boolean useOBD){
		
		
		// For debug
		double lat1 = 24.796699;
		double lon1 = 120.997193;
		double lat2 = 24.809574;
		double lon2 = 120.983557;
		StartPoint = new GeoPoint((int)(lat1 * 1000000), (int)(lon1 * 1000000));
		EndPoint   = new GeoPoint((int)(lat2 * 1000000), (int)(lon2 * 1000000));
		
		
		// 從 SD 卡開啟檔案
		String filename = "RawData.txt";
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				FileInputStream DataFile = new FileInputStream(
						Environment.getExternalStorageDirectory() + "/" + filename);
				
				if (DataFile != null){
					
					long TimeStamp;
					double Speed;
					double Direction;
					String InputLine = "";
					
					InputStreamReader iStream = new InputStreamReader(DataFile);
					BufferedReader bReader = new BufferedReader(iStream);
					
					// 逐行讀取
					while((InputLine = bReader.readLine())!=null){

						// 依空白格分割 line 成  array
						String[] arr = InputLine.split(" ");
						
						//// Debug
						/*
						for (String e:arr){
							Log.d("e",e+".");
						}
						*/

						// 把 array 裏的資料加到 list 裏
						if ( arr[0].equals("START") ){
							
							StartPoint = new GeoPoint(
									(int)(Double.parseDouble(arr[1].trim())*1000000), 
									(int)(Double.parseDouble(arr[2].trim())*1000000));
							
						} else if( arr[0].equals("END") ){
							
							EndPoint = new GeoPoint(
									(int)(Double.parseDouble(arr[1].trim())*1000000),
									(int)(Double.parseDouble(arr[2].trim())*1000000));
							
						} else {
							
							if (useOBD){
								Speed = Double.parseDouble(arr[4].trim());
							} else {
								Speed = Double.parseDouble(arr[1].trim());
							}
							// Log.d("Speed",Speed+".");
							Direction = Double.parseDouble(arr[2].trim());
							// Log.d("Direction", Direction+".");
							TimeStamp = Long.parseLong(arr[3].trim());  // 去除空白
							// Log.d("TimeStamp", TimeStamp+".");
							SenseRecord sRecord = new SenseRecord(TimeStamp, Speed, Direction);
							DataList.add(sRecord);
						}
					}
					bReader.close();
					iStream.close();
					DataFile.close();
					if (StartPoint.equals(null)){
						Log.e("Wrong data format", "No Start Point!");
					}
					if (StartPoint.equals(null)){
						Log.e("Wrong data format", "No End Point!");
					}
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
}