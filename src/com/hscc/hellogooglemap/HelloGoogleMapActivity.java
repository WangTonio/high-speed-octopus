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

//import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
//import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.hscc.hellogooglemap.Tracking.Intersection;

public class HelloGoogleMapActivity extends MapActivity implements Runnable {
    /** Called when the activity is first created. */	
	protected static final int MENU_QUICK1 = Menu.FIRST;
	protected static final int MENU_QUICK2 = Menu.FIRST+1;
	protected static final int MENU_QUICK3 = Menu.FIRST+2;
	protected static final int MENU_STOP   = Menu.FIRST+6;
	protected static final int MENU_QUICK4 = Menu.FIRST+3;
	protected static final int MENU_ABOUT  = Menu.FIRST+4;
	protected static final int MENU_QUIT   = Menu.FIRST+5;
	private   static final int GEO = 1000000;
	protected static int onRecord = 0;
	
	GeoPoint FirstPoint;
	GeoPoint point  		  = new GeoPoint(19240000, -99120000);
	GeoPoint point2 		  = new GeoPoint(35410000, 139460000);
	GeoPoint taipei_station   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	GeoPoint taichung_station = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO));
	// default location is set to be TaichungTrainStation
	GeoPoint my_location      = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO)); 
	GeoPoint my_destination   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	GeoPoint my_intersection  = null;
	GeoPoint my_pointTo		  = new GeoPoint( (int)(25.041111*GEO),(int)(121.516111*GEO));
	private List<GeoPoint> _points    = new ArrayList<GeoPoint>(); //hold the path signature
	private List<GeoPoint> pathPoints = new ArrayList<GeoPoint>();
	private ProgressDialog pd; //轉圈圈
	public  int queryTime = 0;

	public int progressMax = 50;
	public int progressNow = 0;
	protected LocationManager locationManager;
	protected MyLocationOverlay mylayer;
	protected MapView mapView;
	protected MapController mapController;
	protected boolean enableTool;
	protected String BestProvider;
	protected LandMarkOverlay markLayer;
	static final int REQUEST_CODE	= 1;
	
	public boolean isOBDusing = false;
	public boolean isGpsDisplay = false;
	int timeStart = 0;
    int timeEnd   = 100;
    public String userSelectFileName;
	protected Tracking TrackObj;
	private List<GeoPoint> FrontGPS;
	private List<GeoPoint> trackingResult;
	private List<GeoPoint> BackGPS;
	private int totalGPSdataSize = 0;
	List<com.google.android.maps.Overlay> ol;
	
	protected final LocationListener LocListener = new LocationListener(){
		public void onLocationChanged(Location location) {
			
			my_location = new GeoPoint((int)(location.getLatitude()*GEO),
					                   (int)(location.getLongitude()*GEO));

			if (onRecord == 1){
				pathPoints.add(my_location);
				List<com.google.android.maps.Overlay> ol = mapView.getOverlays();
		        ol.add(new RecordPathOverlay());
		        mapView.invalidate();
			} else {
				//
				
			}
			
		}

		public void onProviderDisabled(String provider) {
			
		}

		public void onProviderEnabled(String provider) {
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
	};
	
	//一開始啟動程式所執行的地方
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    //Initial Map View and Controller
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapController = mapView.getController();
	    
	    //Setup the location service
	    locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
	    Criteria myCriteria = new Criteria();
	    myCriteria.setAccuracy(Criteria.ACCURACY_FINE);  // 只使用 GPS 的 SENSOR 
	    myCriteria.setAltitudeRequired(false);
	    myCriteria.setBearingRequired(false);
	    myCriteria.setCostAllowed(true);
	    myCriteria.setPowerRequirement(Criteria.POWER_LOW);
	    BestProvider = locationManager.getBestProvider(myCriteria, true);  // 取得目前收訊最好的 SENSOR
	    
	    if( mapView != null )
        {
            mapView.setBuiltInZoomControls(true);
            mapView.setTraffic(false);
            mapController.setZoom(16);
            
        }
	    FirstStart(); 
	} // End of onCreate
	
	//第一次執行顯示的說明
	private void FirstStart() {
		// TODO 尚未建立說明code
	}

	//Menu 的顯示
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_QUICK2, 0, "路徑重建")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK4, 1, "選擇資料")
			.setIcon(R.drawable.ic_menu_loopbin);
		menu.add(0, MENU_ABOUT , 2, "設定GPS區間")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_QUICK1, 3, "現在位置")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK3, 4, "清除路徑")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(0, MENU_QUIT  , 5, "結束")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.add(1, MENU_QUICK2, 0, "路徑重建")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(1, MENU_QUICK4, 1, "選擇資料")
			.setIcon(R.drawable.ic_menu_loopbin);
		menu.add(1, MENU_ABOUT , 2, "設定GPS區間")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(1, MENU_QUICK1, 3, "現在位置")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(1, MENU_QUICK3, 4, "清除路徑")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(1, MENU_QUIT  , 5, "結束")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.setGroupVisible(0, false);
		return true;
	}
	
	//準備 Menu
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
	  super.onPrepareOptionsMenu(menu);
	  
	  switch(onRecord)
	  {
	  case 0:
	    menu.setGroupVisible(1, false);
	    menu.setGroupVisible(0, true);
	    return true;
	  case 1:
		menu.setGroupVisible(0, false);
		menu.setGroupVisible(1, true);
		return true;
	  default:
		menu.setGroupVisible(1, false);
		menu.setGroupVisible(0, true);
		return false;
	  }
	}
	
	//實作 Menu 的 code
	public boolean onOptionsItemSelected(MenuItem item)
	{
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.startLayoutAnimation();
		MapController mapController = mapView.getController();
		
		switch(item.getItemId()){
		
		case MENU_QUICK1: //現在位置
			if(my_location.equals(taichung_station)){
				toMyLocation();
				mapController.setZoom(18);
				Toast.makeText(HelloGoogleMapActivity.this, "因為您沒有GPS訊號，因此將您移至台中車站", Toast.LENGTH_SHORT).show();
			}else{
				toMyLocation();
				mapController.setZoom(18);
				Toast.makeText(HelloGoogleMapActivity.this, "已到達您現在的位置", Toast.LENGTH_SHORT).show();
			}    		
    		break;
		case MENU_QUICK2: //路徑重建
			Toast.makeText(HelloGoogleMapActivity.this, "請您稍後，等待路徑重建完成", Toast.LENGTH_LONG).show();
			processPathRecovery();
			break;
		case MENU_QUICK3: //清除路徑
		case MENU_STOP:
			ol.clear();	
			break;
		case MENU_QUICK4: //選擇資料
			SelectData();
			break;
		case MENU_ABOUT:  //設定GPS區間
			SettingGPSinterval();
			break;
		case MENU_QUIT:
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//設定GPS區間
	private void SettingGPSinterval() {
		//Layout配置
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.menu, (ViewGroup) findViewById(R.layout.main));
		
		//對話配置
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setView(layout);
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle("選擇無 GPS 區間");
		alertDialog.setMessage("時間以百分比表示並且\n結束時間必須大於開始時間");
		alertDialog.setButton("重建", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});

		alertDialog.setButton2("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				 // Canceled.
			}
		});
		alertDialog.show();
		
		/* 取得元件 */
		SeekBar seekBarStart = (SeekBar)layout.findViewById(R.id.seekBar1);
	    final TextView seekBarValueStart = (TextView)layout.findViewById(R.id.seekValueStart);  
	    SeekBar seekBarEnd =   (SeekBar)layout.findViewById(R.id.seekBar2);  
	    final TextView seekBarValueEnd = (TextView)layout.findViewById(R.id.seekValueEnd);
	    ToggleButton toggleGPS = (ToggleButton)layout.findViewById(R.id.toggleButton1);
	    
	    seekBarStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	    	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	    		seekBarValueStart.setText(String.valueOf(progress) + "%");
	    		timeStart = progress;
		        	}

	    	public void onStartTrackingTouch(SeekBar seekBar) {
	    		}
	    	public void onStopTrackingTouch(SeekBar seekBar) {}
		    	});
	    
	    seekBarEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				if( progress < timeStart){
					seekBarValueEnd.setText("  請選擇比Start百分比更大的數字!");
					timeEnd = 100;
				}else{
					seekBarValueEnd.setText(String.valueOf(progress) + "%");
					timeEnd = progress;
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}});
		
	    toggleGPS.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() { 
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	            if (isChecked) {
	            	isGpsDisplay = true;
	                Log.d("開關", "已開啟，表示GPS路徑要顯示");
	            } else {
	            	isGpsDisplay = false;
	                Log.d("開關", "已關閉，表示GPS路徑不顯示");
	            }
	        }});
	}

	//移動 mapView 到使用者現在的位置
	private void toMyLocation() {
		mapController.animateTo(my_location);		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	//實作"選擇資料"
	private void SelectData()
	{
		LayoutInflater factory = LayoutInflater.from(this);            
				
		// 定義 menu.xml 為將要顯示的 textEntryView
		final View textEntryView = factory.inflate(R.layout.first, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("選擇資料"); // "歡迎使用"
		alert.setMessage(R.string.OBD_dialog); // "請問您要使用感測器的資料或是OBD車上診斷系統的資料作分析？"
	
		alert.setView(textEntryView);

		alert.setPositiveButton("開啟檔案", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				
				Intent intent1 = new Intent(HelloGoogleMapActivity.this, FileListView.class);
				intent1.putExtra("FromAppMain", "appMain");
				startActivityForResult(intent1, REQUEST_CODE);
			}
		});

		alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//isOBDusing = false;
			}
		});
		alert.setIcon(R.drawable.ic_menu_info);
		alert.show();
		
	}
	
	//切換  Activity 所回傳的code
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				String temp = null;
				Bundle extras = data.getExtras();
				if (extras != null)
				{
					temp = extras.getString("FromActivity1");
				}
				Log.d("切換 Activity","傳回資料: " + temp);
				userSelectFileName = temp;
				final TextView seekBarValueStart = (TextView)findViewById(R.id.Information);
				seekBarValueStart.setText("資料："+temp);
				setTitle(temp);
			}
		}
	}
	
	//開始畫線功能
	public void setview(){
		ol = mapView.getOverlays();
        ol.clear();
        ol.add(new MyMapOverlay());
        mapView.invalidate();
        
        
        MapController mapController = mapView.getController();
        if( mapController != null )
        {
            mapController.animateTo( trackingResult.get(0));
            mapController.setZoom(16);
        }
	}
	
	//建立一個新的執行緒，用來一邊跑進度圈圈一邊跑重建路徑，但受限於android API，MapView必須在handler那邊執行
	public void run() {
		TrackObj = new Tracking(userSelectFileName,isOBDusing,timeStart,timeEnd);
		totalGPSdataSize = TrackObj.AnalyzedData.myData.DataList.size();
		queryTime = TrackObj.queryTimes;
		trackingResult = new ArrayList<GeoPoint>();
		eliminateGpsPoint(0,timeStart);
		eliminateGpsPoint(timeEnd,100);
		
		
		trackingResult.add(TrackObj.StartPoint);
		
		for(Intersection a : TrackObj.ForwardIntersection){
			trackingResult.add(a.PredictLocation);
		}
		for(Intersection b : TrackObj.BackwardIntersection){
			trackingResult.add(b.PredictLocation);
		}
		trackingResult.add(TrackObj.EndPoint);
		//trackingResult = TrackObj.getResult();
		handler.sendEmptyMessage(0);
	}

	//處理執行緒間的資料傳遞
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setview();
			pd.dismiss();
			Toast.makeText(HelloGoogleMapActivity.this, 
					   "總共對 Google Query " + queryTime + " 次", Toast.LENGTH_SHORT).show();
		}
	};

	
	//路徑重建
	private void processPathRecovery(){
		

		pd = ProgressDialog.show(this, "重建路徑中", 
				"\n檔案名稱: " + userSelectFileName +
				"\n資料來源: " + returnOBD() +
				"\n正在與 Google Map 溝通" +
				"\n需要時間依網路狀況而定" +
				"\n請耐心稍後!"
				, true, true);

		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	//傳回是否使用OBD
	public String returnOBD(){
		if(isOBDusing){
			return "OBD";
		}else{
			return "感測器";
		}
	}
	
	//在所有的GPS座標群內化簡 index 從 indexStart 到 indexEnd 的點集
	public void eliminateGpsPoint(int indexStart, int indexEnd){
		ArrayList<Location> temp =  new ArrayList<Location>();
		ArrayList<Location> rslt =  new ArrayList<Location>();
		ArrayList<SenseRecord> curn = new ArrayList<SenseRecord>();
		int target = 0;
		if(indexStart == 1){ //表示現在是在 tracking 前端
			target = 1;
		}else if(indexEnd == totalGPSdataSize){ //表示現在是在 tracking 後端
			target = 2;
		}
		
		//將所有GPS皆放入temp裡面
		for(SenseRecord involve: TrackObj.AnalyzedData.myData.DataList ){
			curn.add(involve);
		}
		
		for(int i = indexStart; i < indexEnd; i++){
			Location now = new Location("");
			float latitude =  curn.get(i).GPSLocation.getLatitudeE6() / 1000000F;
			float longitude = curn.get(i).GPSLocation.getLongitudeE6() / 1000000F;
			now.setLatitude(latitude);
			now.setLongitude(longitude);
			temp.add(now);
		}
		
		reduceGPS mGPS = new reduceGPS();
		if(target == 1){
			mGPS.decimate(150f,temp,rslt);
			for(Location now: rslt){
				int lat = (int)(now.getLatitude()/GEO);
				int lng = (int)(now.getLongitude()/GEO);
				GeoPoint b = new GeoPoint(lat,lng);
				FrontGPS.add(b);
			}
		}else{
			mGPS.decimate(150f,temp,rslt);
			for(Location now: rslt){
				int lat = (int)(now.getLatitude()/GEO);
				int lng = (int)(now.getLongitude()/GEO);
				GeoPoint b = new GeoPoint(lat,lng);
				BackGPS.add(b);
			}
		}	
	}

	//計算兩個 GeoPoint 間的夾角
	public double bearing(GeoPoint start, GeoPoint dest){
		double lat1 = toRad((double)start.getLatitudeE6()/GEO);
		double lat2 = toRad((double)dest.getLatitudeE6() /GEO);
		double lon1 = toRad((double)start.getLongitudeE6()/GEO);
		double lon2 = toRad((double)dest.getLongitudeE6() /GEO);
		double dLon = (lon2-lon1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		           Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double answer = toDeg(Math.atan2(y, x));
		
		if(answer < 0)
			answer += 360;
		
		return answer;
	}
	
	//轉成弧度
	public double toRad(double number){
		return number*Math.PI/180.0;
	}
	
	//轉成角度
	public double toDeg(double number){
		return number*180.0/Math.PI;
	}
	
	//變數監聽
	public VariableChangeListener variableChangeListener;
	public interface VariableChangeListener {
        public void onVariableChanged(int variableThatHasChanged);
    }
	
	public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
	       this.variableChangeListener = variableChangeListener;
	    }
	
	

	
/* *** 以下是 Google Path 要求 ****/
	//The following function is made for PATH CALCULATION	
	public List<GeoPoint> GetDirection(GeoPoint Destination)
	{
	    String mapAPI = "http://maps.google.com/maps/api/directions/json?origin={0}&destination={1}&language=zh-TW&sensor=true";
	    String url = MessageFormat.format(mapAPI, "25.04202,121.534761",ExtraLocation(Destination));

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
	        Log.e("map", "MapRoute:" + e.toString());
	    }

	    return _points;
	}
	
	private String ExtraLocation(GeoPoint a){
		String location = "";
		location = "" + Double.toString(((double)a.getLatitudeE6())/GEO) + "," + Double.toString(((double)a.getLongitudeE6())/GEO);
		return location;
	}
	
	//Decode google path XML
	private void decodePolylines(String poly)
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
	        _points.add(p);

	    }
	}
	
	public class LandMarkOverlay extends ItemizedOverlay<OverlayItem>{

		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		
		public LandMarkOverlay(Drawable defaultMarker) {
			super(defaultMarker);
			//for(GeoPoint point : _points){
			//	items.add(new OverlayItem(point, "HI", "這裡是路徑點"));
			//}
			items.add(new OverlayItem(taipei_station, "HI", "這裡是起點"));
			items.add(new OverlayItem(my_destination, "HI", "這裡是終點"));
			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i){
			return items.get(i);
		}
		
		@Override
		public int size(){
			return items.size();
		}
		
		@Override
		protected boolean onTap(int pIndex){
			Toast.makeText(HelloGoogleMapActivity.this, 
						   "這裡是" + items.get(pIndex).getSnippet(), Toast.LENGTH_SHORT).show();
			return true;
		}
		
	}
	
/* *** 以下是Overlay path產生 ****/
	class MyMapOverlay extends com.google.android.maps.Overlay
	{	
		// 畫路徑
	    @Override
	    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
	    {
	        super.draw(canvas, mapView, shadow);
	        
	        //turnCir++;
	        if( true )
	        {
	            Projection p = mapView.getProjection();
	            Point out = new Point();
	            Path myPath = new Path();
	                          
	            Paint myPaint = new Paint();
	            myPaint.setStyle(Paint.Style.STROKE);
	            myPaint.setStrokeWidth(7);
	            myPaint.setAlpha(255);
                boolean turn = true;
                
                myPaint.setColor(Color.BLUE);
	            for(GeoPoint point : trackingResult){
	            	if(turn){
	            		out = p.toPixels(point, out);
	            		myPath.moveTo(out.x, out.y);
	                    turn = false;
	            	}else{
	            		out = p.toPixels(point, out);
	                    myPath.lineTo(out.x, out.y);
	                	canvas.drawPath(myPath, myPaint);
	                	myPath.moveTo(out.x, out.y);	
	            	}
				}  
	            
	            turn = true;
	            myPaint.setColor(Color.RED);
	            for(GeoPoint point : FrontGPS){
	            	if(turn){
	            		out = p.toPixels(point, out);
	            		myPath.moveTo(out.x, out.y);
	                    turn = false;
	            	}else{
	            		out = p.toPixels(point, out);
	                    myPath.lineTo(out.x, out.y);
	                	canvas.drawPath(myPath, myPaint);
	                	myPath.moveTo(out.x, out.y);	
	            	}
				}  
	            
	            turn = true;
	            myPaint.setColor(Color.RED);
	            for(GeoPoint point : BackGPS){
	            	if(turn){
	            		out = p.toPixels(point, out);
	            		myPath.moveTo(out.x, out.y);
	                    turn = false;
	            	}else{
	            		out = p.toPixels(point, out);
	                    myPath.lineTo(out.x, out.y);
	                	canvas.drawPath(myPath, myPaint);
	                	myPath.moveTo(out.x, out.y);	
	            	}
				}  
	            
	            mapView.invalidate();
	        }
	        return true;
	    }
	}
		
	class RecordPathOverlay extends com.google.android.maps.Overlay
	{
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
	    {
	        super.draw(canvas, mapView, shadow);
	        
	        Projection p = mapView.getProjection();
            Point out = new Point();
            Path myPath = new Path();
                          
            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(10);
            myPaint.setAlpha(50);
     
            boolean turn = true;
            for(GeoPoint point : pathPoints){
            	if(turn){
            		out = p.toPixels(point, out);
            		myPath.moveTo(out.x, out.y);
                    turn = false;
            	}else{
            		out = p.toPixels(point, out);
                    myPath.lineTo(out.x, out.y);
                	canvas.drawPath(myPath, myPaint);
                	myPath.moveTo(out.x, out.y);     	
            	}
			}
	        return true;
	    }
	}
	
/* *** 以下是 系統狀態處理 ****/
	@Override
	protected void onResume(){
		super.onResume();
		LocationManager status = (LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
		
		if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)||
		   status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationManager = (LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
			locationManager.requestLocationUpdates("gps", 1000, 0, LocListener);
			Location location = locationManager.getLastKnownLocation("gps");
			if (location != null){
				//pop up
				StringBuffer msg = new StringBuffer();
				msg.append("目前緯度: ");
				msg.append(Double.toString(location.getLatitude()));
				msg.append("\n目前經度: ");
				msg.append(Double.toString(location.getLongitude()));
				//Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}else{
				//Toast.makeText(this, "您目前收不到GPS訊號", Toast.LENGTH_LONG).show();			
			}
		}else{
			new AlertDialog.Builder(HelloGoogleMapActivity.this)
					.setTitle("您沒有開啟GPS")
					.setMessage("請至 設定 內開啟GPS，按下OK前往設定。")
					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {						
						public void onClick(DialogInterface arg0, int arg1) {
							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));							
						}})
					.show();
		}		
	}
		
	@Override
	protected void onPause(){
		super.onPause();
		
		if (enableTool)
		{
			locationManager.removeUpdates(LocListener);
			mylayer.disableCompass();
			mylayer.disableMyLocation();

		}
		
	}
 
}