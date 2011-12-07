package com.hscc.hellogooglemap;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class FileListView extends ListActivity{
	public ArrayList<String> FileList = new ArrayList<String>();
	OnClickListener	ocl_Activity1	= null;
	Button			btn_Back_To_appMain;
	TextView txv_Activity1;
	
	Bundle bul_Extra;
	String s_ActivityData = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	  
		File sdCardRoot = Environment.getExternalStorageDirectory();
		File yourDir = new File(sdCardRoot, "iTaxi");
		//將所有 iTaxi 底下的檔案名稱都列在 FileList 裡面
		for (File f : yourDir.listFiles()) {
		    if (f.isFile()){
		    	String name = f.getName();
		        FileList.add(name);
		    }
		}
		setListAdapter(new ArrayAdapter<String>(this, R.layout.progress, FileList));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 點了物品要做的事情
				String itemName = ((TextView) view).getText().toString();
				Toast.makeText(getApplicationContext(), "已選擇 " + itemName,
						Toast.LENGTH_SHORT).show();
				
				Bundle bundle = new Bundle();
				bundle.putString("FromActivity1", ""+itemName);
				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		});
	  
		setTitle("選擇檔案 :   /SDCard/iTaxi ");
		
		
		bul_Extra = getIntent().getExtras();
		
		if (bul_Extra != null)
		{
			s_ActivityData = bul_Extra.getString("FromAppMain");
		}
		

	}
}
