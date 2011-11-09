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
			msg.append("�ثe�u������m: \n");
			msg.append("�n��: ");
			msg.append(Double.toString(location.getLatitude()));
			msg.append("\n�g��: ");
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
	    myCriteria.setAccuracy(Criteria.ACCURACY_FINE);  // �u�ϥ� GPS �� SENSOR 
	    myCriteria.setAltitudeRequired(false);
	    myCriteria.setBearingRequired(false);
	    myCriteria.setCostAllowed(true);
	    myCriteria.setPowerRequirement(Criteria.POWER_LOW);
	    BestProvider = locationManager.getBestProvider(myCriteria, true);  // ���o�ثe���T�̦n�� SENSOR
	    
	    // setup showing strings for Buttons
	    debugOut = new StringBuilder();
	    debugOut.append("���@�UOK�H��sGPS��T");
	    Test = new StringBuilder();
	    Test.append("�n���n�h�x�_����\n");
	    
	    if( mapView != null )
        {
            mapView.setBuiltInZoomControls(true);
            mapView.setTraffic(false);
            mapController.setZoom(16);
        }
	    
	    // Button of Debug
	    // ����ثe��m�����s
	    Button b;
        if( ( b = (Button) findViewById(R.id.button2) ) != null )
        {
            b.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View v) {	// ��ܹ�ܮ�	
            	AlertDialog alertDialog = new AlertDialog.Builder(HelloGoogleMapActivity.this).create();
            	alertDialog.setTitle("Debug");
            	alertDialog.setMessage(debugOut);
            	alertDialog.setButton("�n�ڡI", new DialogInterface.OnClickListener(){
            		// ���ܮث�"�n��!"����, ��� GPS Ū�쪺��m
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
            				Toast.makeText(HelloGoogleMapActivity.this, "�z�ثe������GPS�T���C", Toast.LENGTH_LONG).show();			
            			}
                     	dialog.cancel();
                     }
                });
                alertDialog.show();
             	}
            });
        }
        
        // Button of WRAP
        // ���ʨ�x�_���������s
        Button c;
        if( ( c = (Button) findViewById(R.id.button1) ) != null )
        {
            c.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View v) {	            	        	
            	AlertDialog alertDialog = new AlertDialog.Builder(HelloGoogleMapActivity.this).create();
            	alertDialog.setTitle(R.string.dialog_wrap);
            	alertDialog.setMessage(Test);
            	alertDialog.setButton("�n�ڡI", new DialogInterface.OnClickListener(){
                	public void onClick(DialogInterface dialog, int which)
                    {
                		MapView mapView = (MapView) findViewById(R.id.mapview);
                		mapView.startLayoutAnimation();
                		MapController mapController = mapView.getController();               		
                		mapController.setZoom(19);
                		mapController.animateTo(taipei_station);
                		Toast.makeText(HelloGoogleMapActivity.this, "�z�w��F�x�_����", Toast.LENGTH_SHORT).show();
                		
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
                    Toast.makeText(HelloGoogleMapActivity.this, "�ìP�Ҧ�", Toast.LENGTH_SHORT).show();
                } else {
                	mapView.setSatellite(false);
                    Toast.makeText(HelloGoogleMapActivity.this, "�@��Ҧ�", Toast.LENGTH_SHORT).show();
                }
            }
        });
	} // End of onCreate
	
	//Option of Menu->Information
	private void openOptionsDialog(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.buttom_debug)
		.setMessage("GPS Location-based Navigation")
		.setPositiveButton("�n�I", 
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						}
					})
		.setNegativeButton("����", 
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
		
		menu.add(0, MENU_QUICK1, 0, "�{�b��m")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK2, 0, "��ܮy��")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK3, 0, "�O�����|")
			.setIcon(R.drawable.ic_menu_star);
		menu.add(0, MENU_QUICK4, 0, "�إ߸��|")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_ABOUT , 0, "����")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(0, MENU_QUIT  , 0, "����")
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
				Toast.makeText(HelloGoogleMapActivity.this, "�]���z�S��GPS�T���A�]���N�z���ܥx������", Toast.LENGTH_SHORT).show();
			}else{
				toMyLocation();
				mapController.setZoom(18);
				Toast.makeText(HelloGoogleMapActivity.this, "�w��F�z�{�b����m", Toast.LENGTH_SHORT).show();
			}    		
    		break;
		case MENU_QUICK2:
			InputLocationDialog();
			Toast.makeText(HelloGoogleMapActivity.this, "�o�ӥ\��|��������@", Toast.LENGTH_SHORT).show();
			break;
		case MENU_QUICK3:
			if ( onRecord == 0){
				onRecord = 1;
				Toast.makeText(HelloGoogleMapActivity.this, "�}�l�O���z�����|...", Toast.LENGTH_SHORT).show();
			} else {
				onRecord = 0;
				pathPoints.clear();
				Toast.makeText(HelloGoogleMapActivity.this, "�w����O���z�����|�C", Toast.LENGTH_SHORT).show();
			}
			
			break;
		case MENU_QUICK4:
			GetDirection(my_destination);
			formMyPath();
			UpdateMyPath();
			Toast.makeText(HelloGoogleMapActivity.this, "�z�����u�w�g�إߡI", Toast.LENGTH_LONG).show();
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
		// �I�s�s�� layout ���ϥΪ̿�J�g�n��
		LayoutInflater factory = LayoutInflater.from(this);            
		
		// �w�q menu.xml ���N�n��ܪ� textEntryView
        final View textEntryView = factory.inflate(R.layout.menu, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.dialog_select_position); // "��ܮy��"
		alert.setMessage(R.string.dialog_select_inside); // "�п�J���e�����y��"

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
		Toast.makeText(HelloGoogleMapActivity.this, "�w�����]�w�ؼСA�i�H�}�l�ͦ����|", Toast.LENGTH_SHORT).show();
		
	}

/**** �H�U�O Google Path �n�D ****/
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
			//	items.add(new OverlayItem(point, "HI", "�o�̬O���|�I"));
			//}
			items.add(new OverlayItem(taipei_station, "HI", "�o�̬O�_�I"));
			items.add(new OverlayItem(my_destination, "HI", "�o�̬O���I"));
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
						   "�o�̬O" + items.get(pIndex).getSnippet(), Toast.LENGTH_SHORT).show();
			return true;
		}
		
	}

	private void formMyPath() {
		
		Drawable pin = getResources().getDrawable(R.drawable.cluster);
		pin.setBounds(0,0,pin.getMinimumWidth(),pin.getMinimumHeight());
		
		markLayer = new LandMarkOverlay(pin);
		mapView.getOverlays().add(markLayer);
	}
	
	
/**** �H�U�OOverlay path���� ****/
	class MyMapOverlay extends com.google.android.maps.Overlay
	{		
		// �e���|
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
	
/**** �H�U�O �t�Ϊ��A�B�z ****/
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
				Toast.makeText(this, "�z�ثe������GPS�T��", Toast.LENGTH_LONG).show();			
			}
		}else{
			new AlertDialog.Builder(HelloGoogleMapActivity.this)
					.setTitle("�z�S���}��GPS")
					.setMessage("�Ц� �]�w ���}��GPS�A���UOK�e���]�w�C")
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