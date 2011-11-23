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
	public int totalIntersection;	// total number of intersection in this Record set.
	public ArrayList<SenseRecord> DataList = new ArrayList<SenseRecord>();
	
	public RawData(){
		
		// 從 SD 卡開啟檔案
		String filename = "RawData3.txt";
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
						String[] arr = InputLine.split("  ");						
						
						// 把 array 裏的資料加到 list 裏
						for (int i = 0; i < 5; i++){
							Speed = Double.parseDouble(arr[2]);
							Direction = Double.parseDouble(arr[3]);
							TimeStamp = Long.parseLong(arr[4].trim());  // 去除空白
							SenseRecord sRecord = new SenseRecord(TimeStamp, Speed, Direction);
							DataList.add(sRecord);
						}
					}
					bReader.close();
					iStream.close();
					DataFile.close();
					
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
}