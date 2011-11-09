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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

public class HelloGoogleMapActivity extends MapActivity {
    /** Called when the activity is first created. */	
	protected static final int MENU_QUICK1 = Menu.FIRST;
	protected static final int MENU_QUICK2 = Menu.FIRST+1;
	protected static final int MENU_QUICK3 = Menu.FIRST+2;
	protected static final int MENU_QUICK4 = Menu.FIRST+3;
	protected static final int MENU_ABOUT  = Menu.FIRST+4;
	protected static final int MENU_QUIT   = Menu.FIRST+5;
	private   static final int GEO = 1000000;
	
	protected static int onRecord = 0;
	
	GeoPoint FirstPoint;
	
	GeoPoint point  = new GeoPoint(19240000, -99120000);
	GeoPoint point2 = new GeoPoint(35410000, 139460000);
	GeoPoint taipei_station   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));
	GeoPoint taichung_station = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO));
	// default location is set to be TaichungTrainStation
	GeoPoint my_location      = new GeoPoint( (int)(24.136895*GEO),(int)(120.684975*GEO)); 
	GeoPoint my_destination   = new GeoPoint( (int)(25.047192*GEO),(int)(121.516981*GEO));;
	private List<GeoPoint> _points = new ArrayList<GeoPoint>(); //hold the path signature
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	
	private List<GeoPoint> pathPoints = new ArrayList<GeoPoint>();
	//private GeoPoint oldLocation = new GeoPoint(0, 0);
	//private GeoPoint newLocation = new GeoPoint(0, 0);
	
	protected LocationManager locationManager;
	protected MyLocationOverlay mylayer;
	protected MapView mapView;
	protected MapController mapController;
	protected boolean enableTool;
	protected String BestProvider;
	protected LandMarkOverlay markLayer;
	
	StringBuilder debugOut, Test;
	double dialog_user_input_lat, dialog_user_input_long;
	
	protected final LocationListener LocListener = new LocationListener(){
		public void onLocationChanged(Location location) {
			
			my_location = new GeoPoint((int)(location.getLatitude()*GEO),
					                   (int)(location.getLongitude()*GEO));
			/*StringBuffer msg = new StringBuffer();
			msg.append("目前真正的位置: \n");
			msg.append("緯度: ");
			msg.append(Double.toString(location.getLatitude()));
			msg.append("\n經度: ");
			msg.append(Double.toString(location.getLongitude()));
			Toast.makeText(HelloGoogleMapActivity.this, msg, Toast.LENGTH_LONG).show();
			*/
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
	    
	    // setup showing strings for Buttons
	    debugOut = new StringBuilder();
	    debugOut.append("按一下OK以更新GPS資訊");
	    Test = new StringBuilder();
	    Test.append("要不要去台北車站\n");
	    
	    if( mapView != null )
        {
            mapView.setBuiltInZoomControls(true);
            mapView.setTraffic(false);
            mapController.setZoom(16);
        }
	    
	    // Button of Debug
	    // 移到目前位置的按鈕
	    Button b;
        if( ( b = (Button) findViewById(R.id.button2) ) != null )
        {
            b.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View v) {	// 顯示對話框	
            	AlertDialog alertDialog = new AlertDialog.Builder(HelloGoogleMapActivity.this).create();
            	alertDialog.setTitle("Debug");
            	alertDialog.setMessage(debugOut);
            	alertDialog.setButton("好啊！", new DialogInterface.OnClickListener(){
            		// 當對話框按"好啊!"之後, 顯示 GPS 讀到的位置
                	public void onClick(DialogInterface dialog, int which)
                     {
                		locationManager.requestLocationUpdates("gps", 5000, 5, LocListener);
                		Location location = locationManager.getLastKnownLocation("gps");
            			if (location != null){
            				//pop up
            				StringBuffer msg = new StringBuffer();
            				msg.append("Latitude: ");
            				msg.append(Double.toString(location.getLatitude()));
            				msg.append("\nLongitude: ");
            				msg.append(Double.toString(location.getLongitude()));
            				Toast.makeText(HelloGoogleMapActivity.this, msg, Toast.LENGTH_LONG).show();
            			}else{
            				Toast.makeText(HelloGoogleMapActivity.this, "您目前收不到GPS訊號。", Toast.LENGTH_LONG).show();			
            			}
                     	dialog.cancel();
                     }
                });
                alertDialog.show();
             	}
            });
        }
        
        // Button of WRAP
        // 移動到台北車站的按鈕
        Button c;
        if( ( c = (Button) findViewById(R.id.button1) ) != null )
        {
            c.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View v) {	            	        	
            	AlertDialog alertDialog = new AlertDialog.Builder(HelloGoogleMapActivity.this).create();
            	alertDialog.setTitle(R.string.dialog_wrap);
            	alertDialog.setMessage(Test);
            	alertDialog.setButton("好啊！", new DialogInterface.OnClickListener(){
                	public void onClick(DialogInterface dialog, int which)
                    {
                		MapView mapView = (MapView) findViewById(R.id.mapview);
                		mapView.startLayoutAnimation();
                		MapController mapController = mapView.getController();               		
                		mapController.setZoom(19);
                		mapController.animateTo(taipei_station);
                		Toast.makeText(HelloGoogleMapActivity.this, "您已到達台北車站", Toast.LENGTH_SHORT).show();
                		
                     	dialog.cancel();
                    }
                });
                alertDialog.show();
             	}
            });
        }
                
	    //Satellite View CHECKBOX design
        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox1);
        checkbox.setOnClickListener(new CheckBox.OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now checked
            	MapView mapView = (MapView) findViewById(R.id.mapview);
                if (((CheckBox) v).isChecked()) {
                	mapView.setSatellite(true);
                    Toast.makeText(HelloGoogleMapActivity.this, "衛星模式", Toast.LENGTH_SHORT).show();
                } else {
                	mapView.setSatellite(false);
                    Toast.makeText(HelloGoogleMapActivity.this, "一般模式", Toast.LENGTH_SHORT).show();
                }
            }
        });
	} // End of onCreate
	
	//Option of Menu->Information
	private void openOptionsDialog(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.buttom_debug)
		.setMessage("GPS Location-based Navigation")
		.setPositiveButton("好！", 
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						}
					})
		.setNegativeButton("首頁", 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("http://140.114.71.246");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
			}
		})
		.show();
	}
	
	//Menu appearance
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		menu.add(0, MENU_QUICK1, 0, "現在位置")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK2, 0, "選擇座標")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK3, 0, "記錄路徑")
			.setIcon(R.drawable.ic_menu_star);
		menu.add(0, MENU_QUICK4, 0, "建立路徑")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_ABOUT , 0, "關於")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(0, MENU_QUIT  , 0, "結束")
			.setIcon(R.drawable.ic_menu_icon1);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Menu implementation
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
			Toast.makeText(HelloGoogleMapActivity.this, "這個功能尚未完全實作", Toast.LENGTH_SHORT).show();
			break;
		case MENU_QUICK3:
			if ( onRecord == 0){
				onRecord = 1;
				Toast.makeText(HelloGoogleMapActivity.this, "開始記錄您的路徑...", Toast.LENGTH_SHORT).show();
			} else {
				onRecord = 0;
				pathPoints.clear();
				Toast.makeText(HelloGoogleMapActivity.this, "已停止記錄您的路徑。", Toast.LENGTH_SHORT).show();
			}
			
			break;
		case MENU_QUICK4:
			GetDirection(my_destination);
			formMyPath();
			UpdateMyPath();
			Toast.makeText(HelloGoogleMapActivity.this, "您的路線已經建立！", Toast.LENGTH_LONG).show();
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
	

	private void UpdateMyPath() {
		List<com.google.android.maps.Overlay> ol = mapView.getOverlays();
        //ol.clear();
        ol.add(new MyMapOverlay());
        mapView.invalidate();
	}

	//Move mapView to user's location now
	private void toMyLocation() {
		mapController.animateTo(my_location);		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	
	// Deal with User input for warping
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

	//Process user's location
	protected void process_dialog_input(double _lat, double _long) {
		MapView mapView = (MapView) findViewById(R.id.mapview);
	    MapController mapController = mapView.getController();
	    
	    GeoPoint user_spec = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    my_destination = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    mapController.animateTo(user_spec);
		mapController.setZoom(19);
		Toast.makeText(HelloGoogleMapActivity.this, "已完成設定目標，可以開始生成路徑", Toast.LENGTH_SHORT).show();
		
	}

/**** 以下是 Google Path 要求 ****/
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

	private void formMyPath() {
		
		Drawable pin = getResources().getDrawable(R.drawable.cluster);
		pin.setBounds(0,0,pin.getMinimumWidth(),pin.getMinimumHeight());
		
		markLayer = new LandMarkOverlay(pin);
		mapView.getOverlays().add(markLayer);
	}
	
	
/**** 以下是Overlay path產生 ****/
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
	            for(GeoPoint point : _points){
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
	    	/*
			nextPaint.setColor(Color.GREEN);
			nextPaint.setStyle(Paint.Style.STROKE);
			nextPaint.setStrokeWidth(10);
			nextPaint.setAlpha(50);
			
			pastPoint = a.toPixels(oldLocation, pastPoint);
			nextPath.moveTo(pastPoint.x, pastPoint.y);
			
			nextPoint = a.toPixels(newLocation, nextPoint);
			nextPath.lineTo(nextPoint.x, nextPoint.y);
			
			canvas.drawPath(nextPath, nextPaint);
			*/
	        return true;
	    }
	}
	
/**** 以下是 系統狀態處理 ****/
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
				msg.append("Latitude: ");
				msg.append(Double.toString(location.getLatitude()));
				msg.append("\nLongitude: ");
				msg.append(Double.toString(location.getLongitude()));
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "您目前收不到GPS訊號", Toast.LENGTH_LONG).show();			
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