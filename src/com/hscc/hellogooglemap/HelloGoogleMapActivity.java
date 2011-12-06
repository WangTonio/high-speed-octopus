package com.hscc.hellogooglemap;

import java.lang.reflect.Field;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
//import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

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
	private   FindIntersection findIntersection;
	protected static int onRecord = 0;
	
	GeoPoint FirstPoint;
	GeoPoint debug_before1 = new GeoPoint((int)(24.144600*GEO),(int)(120.694460*GEO));
	GeoPoint debug_before2 = new GeoPoint((int)(24.144257*GEO),(int)(120.694476*GEO));
	GeoPoint debug_after   = new GeoPoint((int)(24.144590*GEO),(int)(120.694816*GEO));
	GeoPoint point  = new GeoPoint(19240000, -99120000);
	GeoPoint point2 = new GeoPoint(35410000, 139460000);
	GeoPoint taipei_station   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	GeoPoint taichung_station = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO));
	// default location is set to be TaichungTrainStation
	GeoPoint my_location      = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO)); 
	GeoPoint my_destination   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	GeoPoint my_intersection  = null;
	GeoPoint my_pointTo		  = new GeoPoint( (int)(25.041111*GEO),(int)(121.516111*GEO));
	private List<GeoPoint> _points = new ArrayList<GeoPoint>(); //hold the path signature
	//private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private ProgressDialog m_pDialog; //進度條實作
	private List<GeoPoint> pathPoints = new ArrayList<GeoPoint>();
	//private GeoPoint oldLocation = new GeoPoint(0, 0);
	//private GeoPoint newLocation = new GeoPoint(0, 0);
	private ProgressDialog pd; //轉圈圈
	public int progressMax = 50;
	public int progressNow = 0;
	protected LocationManager locationManager;
	protected MyLocationOverlay mylayer;
	protected MapView mapView;
	protected MapController mapController;
	protected boolean enableTool;
	protected String BestProvider;
	protected LandMarkOverlay markLayer;
	public boolean isOBDusing = false;
	protected Tracking TrackObj;
	private List<GeoPoint> trackingResult;
	List<com.google.android.maps.Overlay> ol;
	
	StringBuilder debugOut, Test;
	double dialog_user_input_lat, dialog_user_input_long;
	
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
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
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
	
	//按下  Menu-> 尋找路口  所做的事
	private void openOptionsDialog(){		
		new AlertDialog.Builder(this)
		.setTitle(R.string.buttom_debug)
		.setMessage("找路口測試實作與此\n按下OK來測試")
		.setPositiveButton("OK", 
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {		
						m_pDialog = new ProgressDialog(HelloGoogleMapActivity.this);
						m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						m_pDialog.setTitle("Confirm Request");
						m_pDialog.setMessage("按下開始後，可能需要一點時間\n按下後，請您耐心等他跑完");
						m_pDialog.setProgress(100);
						m_pDialog.setIndeterminate(false);
						m_pDialog.setCancelable(true);
						m_pDialog.setButton("開始", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int i){
								findIntersection();
								dialog.cancel();
							}});
						m_pDialog.show();	
						}	
					})
		.setNegativeButton("Help", 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("http://140.114.71.246");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
			}
		})
		.show();
	}
	
	//Menu 的顯示
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_QUICK1, 0, "現在位置")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK2, 1, "選擇座標")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK3, 2, "[debug]除線")
			.setIcon(R.drawable.ic_menu_star);
		menu.add(0, MENU_QUICK4, 3, "[debug]呼叫")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_ABOUT , 4, "尋找路口")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(0, MENU_QUIT  , 5, "結束")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.add(1, MENU_QUICK1, 0, "現在位置")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(1, MENU_QUICK2, 1, "選擇座標")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(1, MENU_STOP  , 3, "[debug]除線")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(1, MENU_QUICK4, 4, "[debug]呼叫")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(1, MENU_ABOUT , 5, "尋找路口")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(1, MENU_QUIT  , 6, "結束")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.setGroupVisible(0, false);
		return true;
	}
	
	//準備Menu
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
		
		case MENU_QUICK1:
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
		case MENU_QUICK2:
			InputLocationDialog();
			Toast.makeText(HelloGoogleMapActivity.this, "按下建立路徑便可開始移動", Toast.LENGTH_SHORT).show();
			break;
		case MENU_QUICK3:
		case MENU_STOP:
			ol.clear();	
			break;
		case MENU_QUICK4:
			FirstStart();
			break;
		case MENU_ABOUT:
			openOptionsDialog();
			break;
		case MENU_QUIT:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//移動 mapView 到使用者現在的位置
	private void toMyLocation() {
		mapController.animateTo(my_location);		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	//尋找路口
	public void findIntersection(){
		findIntersection = new FindIntersection();
		my_intersection = findIntersection.findIntersec(debug_before1, debug_before2,debug_after,false,1); //尋找路口，不要lookback
		
		if(my_intersection != null){
			if(my_intersection.equals(taipei_station)){
				Toast.makeText(HelloGoogleMapActivity.this, "找不到路口！", Toast.LENGTH_SHORT).show();
			}else{
				StringBuffer msg = new StringBuffer();
				msg.append("路口緯度: ");
				msg.append(Double.toString( ((double)my_intersection.getLatitudeE6()   ) / GEO));
				msg.append("\n路口經度: ");
				msg.append(Double.toString( ((double)my_intersection.getLongitudeE6()  ) / GEO));
				Toast.makeText(HelloGoogleMapActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//實作第一次打開程式會呼叫的OBD選擇介面
	private void FirstStart()
	{
		// 呼叫新的 layout 讓使用者輸入經緯度
		LayoutInflater factory = LayoutInflater.from(this);            
				
		// 定義 menu.xml 為將要顯示的 textEntryView
		final View textEntryView = factory.inflate(R.layout.first, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("歡迎使用"); // "歡迎使用"
		alert.setMessage(R.string.OBD_dialog); // "請問您要使用感測器的資料或是OBD車上診斷系統的資料作分析？"
	
		alert.setView(textEntryView);

		alert.setPositiveButton("使用OBD", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				isOBDusing = true;
				Toast.makeText(HelloGoogleMapActivity.this, "請您稍後，等待路徑重建完成", Toast.LENGTH_LONG).show();
				processPathRecovery();
			}
		});

		alert.setNegativeButton("使用感測器", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				isOBDusing = false;
				Toast.makeText(HelloGoogleMapActivity.this, "請您稍後，等待路徑重建完成", Toast.LENGTH_LONG).show();
				processPathRecovery();
			}
		});
		alert.setIcon(R.drawable.ic_menu_info);
		alert.show();
		
	}
	
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
		TrackObj = new Tracking(isOBDusing);
		trackingResult = new ArrayList<GeoPoint>();
		trackingResult = TrackObj.getResult();
		handler.sendEmptyMessage(0);
	}

	//蛋疼的 android....
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setview();
			pd.dismiss();
		}
	};
	
	//處理使用者資料，在按下選擇資料來源之後
	private void processPathRecovery(){
		
		pd = ProgressDialog.show(this, "請稍後", "重建路徑中", true,
				false);

		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	//處理使用者要移動的定點(指定座標)
	private void InputLocationDialog()
	{
		// 呼叫新的 layout 讓使用者輸入經緯度
		LayoutInflater factory = LayoutInflater.from(this);            
		
		// 定義 menu.xml 為將要顯示的 textEntryView
        final View textEntryView = factory.inflate(R.layout.menu, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.dialog_select_position); // "選擇座標"
		alert.setMessage(R.string.dialog_select_inside); // "請輸入欲前往的座標"

		// Set an EditText view to get user input (without  textEntryView. with result in an error)
		final EditText input_lat  = (EditText) textEntryView.findViewById(R.id.dialog_input_lat);		
		final EditText input_long = (EditText) textEntryView.findViewById(R.id.dialog_input_long);
		
		alert.setView(textEntryView);

		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog_user_input_lat  = Double.parseDouble(input_lat.getText().toString());
				dialog_user_input_long = Double.parseDouble(input_long.getText().toString());
				process_dialog_input(dialog_user_input_lat, dialog_user_input_long);
			}
		});

		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}

	//處理使用者輸入的定點座標
	protected void process_dialog_input(double _lat, double _long) {
		MapView mapView = (MapView) findViewById(R.id.mapview);
	    MapController mapController = mapView.getController();
	    
	    GeoPoint user_spec = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    my_destination = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    mapController.animateTo(user_spec);
		mapController.setZoom(19);
		Toast.makeText(HelloGoogleMapActivity.this, "已完成設定目標，可以開始生成路徑", Toast.LENGTH_SHORT).show();
		
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
	        
	        if( true )
	        {
	            Projection p = mapView.getProjection();
	            Point out = new Point();
	            Path myPath = new Path();
	                          
	            Paint myPaint = new Paint();
	            myPaint.setColor(Color.BLUE);
	            myPaint.setStyle(Paint.Style.STROKE);
	            myPaint.setStrokeWidth(7);
	            myPaint.setAlpha(70);
	            
                boolean turn = true;
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