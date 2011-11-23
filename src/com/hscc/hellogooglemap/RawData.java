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
		
		// �q SD �d�}���ɮ�
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
					
					// �v��Ū��
					while((InputLine = bReader.readLine())!=null){

						// �̪ťծ���� line ��  array
						String[] arr = InputLine.split("  ");						
						
						// �� array �ت���ƥ[�� list ��
						for (int i = 0; i < 5; i++){
							Speed = Double.parseDouble(arr[2]);
							Direction = Double.parseDouble(arr[3]);
							TimeStamp = Long.parseLong(arr[4].trim());  // �h���ť�
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