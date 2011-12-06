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
	private ProgressDialog m_pDialog; //�i�ױ���@
	private List<GeoPoint> pathPoints = new ArrayList<GeoPoint>();
	//private GeoPoint oldLocation = new GeoPoint(0, 0);
	//private GeoPoint newLocation = new GeoPoint(0, 0);
	private ProgressDialog pd; //����
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
	
	//�@�}�l�Ұʵ{���Ұ��檺�a��
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
	    
	    if( mapView != null )
        {
            mapView.setBuiltInZoomControls(true);
            mapView.setTraffic(false);
            mapController.setZoom(16);
        }
	    FirstStart();
	    
	} // End of onCreate
	
	//���U  Menu-> �M����f  �Ұ�����
	private void openOptionsDialog(){		
		new AlertDialog.Builder(this)
		.setTitle(R.string.buttom_debug)
		.setMessage("����f���չ�@�P��\n���UOK�Ӵ���")
		.setPositiveButton("OK", 
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {		
						m_pDialog = new ProgressDialog(HelloGoogleMapActivity.this);
						m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						m_pDialog.setTitle("Confirm Request");
						m_pDialog.setMessage("���U�}�l��A�i��ݭn�@�I�ɶ�\n���U��A�бz�@�ߵ��L�]��");
						m_pDialog.setProgress(100);
						m_pDialog.setIndeterminate(false);
						m_pDialog.setCancelable(true);
						m_pDialog.setButton("�}�l", new DialogInterface.OnClickListener() {
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
	
	//Menu �����
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_QUICK1, 0, "�{�b��m")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK2, 1, "��ܮy��")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK3, 2, "[debug]���u")
			.setIcon(R.drawable.ic_menu_star);
		menu.add(0, MENU_QUICK4, 3, "[debug]�I�s")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_ABOUT , 4, "�M����f")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(0, MENU_QUIT  , 5, "����")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.add(1, MENU_QUICK1, 0, "�{�b��m")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(1, MENU_QUICK2, 1, "��ܮy��")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(1, MENU_STOP  , 3, "[debug]���u")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(1, MENU_QUICK4, 4, "[debug]�I�s")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(1, MENU_ABOUT , 5, "�M����f")
			.setIcon(R.drawable.ic_menu_info);
		menu.add(1, MENU_QUIT  , 6, "����")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.setGroupVisible(0, false);
		return true;
	}
	
	//�ǳ�Menu
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
	
	//��@ Menu �� code
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
			Toast.makeText(HelloGoogleMapActivity.this, "���U�إ߸��|�K�i�}�l����", Toast.LENGTH_SHORT).show();
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
	
	//���� mapView ��ϥΪ̲{�b����m
	private void toMyLocation() {
		mapController.animateTo(my_location);		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	//�M����f
	public void findIntersection(){
		findIntersection = new FindIntersection();
		my_intersection = findIntersection.findIntersec(debug_before1, debug_before2,debug_after,false,1); //�M����f�A���nlookback
		
		if(my_intersection != null){
			if(my_intersection.equals(taipei_station)){
				Toast.makeText(HelloGoogleMapActivity.this, "�䤣����f�I", Toast.LENGTH_SHORT).show();
			}else{
				StringBuffer msg = new StringBuffer();
				msg.append("���f�n��: ");
				msg.append(Double.toString( ((double)my_intersection.getLatitudeE6()   ) / GEO));
				msg.append("\n���f�g��: ");
				msg.append(Double.toString( ((double)my_intersection.getLongitudeE6()  ) / GEO));
				Toast.makeText(HelloGoogleMapActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//��@�Ĥ@�����}�{���|�I�s��OBD��ܤ���
	private void FirstStart()
	{
		// �I�s�s�� layout ���ϥΪ̿�J�g�n��
		LayoutInflater factory = LayoutInflater.from(this);            
				
		// �w�q menu.xml ���N�n��ܪ� textEntryView
		final View textEntryView = factory.inflate(R.layout.first, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("�w��ϥ�"); // "�w��ϥ�"
		alert.setMessage(R.string.OBD_dialog); // "�аݱz�n�ϥηP��������ƩάOOBD���W�E�_�t�Ϊ���Ƨ@���R�H"
	
		alert.setView(textEntryView);

		alert.setPositiveButton("�ϥ�OBD", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				isOBDusing = true;
				Toast.makeText(HelloGoogleMapActivity.this, "�бz�y��A���ݸ��|���ا���", Toast.LENGTH_LONG).show();
				processPathRecovery();
			}
		});

		alert.setNegativeButton("�ϥηP����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				isOBDusing = false;
				Toast.makeText(HelloGoogleMapActivity.this, "�бz�y��A���ݸ��|���ا���", Toast.LENGTH_LONG).show();
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
	
	//�إߤ@�ӷs��������A�ΨӤ@��]�i�װ��@��]���ظ��|�A��������android API�AMapView�����bhandler�������
	public void run() {
		TrackObj = new Tracking(isOBDusing);
		trackingResult = new ArrayList<GeoPoint>();
		trackingResult = TrackObj.getResult();
		handler.sendEmptyMessage(0);
	}

	//�J�k�� android....
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setview();
			pd.dismiss();
		}
	};
	
	//�B�z�ϥΪ̸�ơA�b���U��ܸ�ƨӷ�����
	private void processPathRecovery(){
		
		pd = ProgressDialog.show(this, "�еy��", "���ظ��|��", true,
				false);

		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	//�B�z�ϥΪ̭n���ʪ��w�I(���w�y��)
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

	//�B�z�ϥΪ̿�J���w�I�y��
	protected void process_dialog_input(double _lat, double _long) {
		MapView mapView = (MapView) findViewById(R.id.mapview);
	    MapController mapController = mapView.getController();
	    
	    GeoPoint user_spec = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    my_destination = new GeoPoint((int)(_lat*GEO), (int)(_long*GEO));
	    mapController.animateTo(user_spec);
		mapController.setZoom(19);
		Toast.makeText(HelloGoogleMapActivity.this, "�w�����]�w�ؼСA�i�H�}�l�ͦ����|", Toast.LENGTH_SHORT).show();
		
	}
	

/* *** �H�U�O Google Path �n�D ****/
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
	
/* *** �H�U�OOverlay path���� ****/
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
	
/* *** �H�U�O �t�Ϊ��A�B�z ****/
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
				msg.append("�ثe�n��: ");
				msg.append(Double.toString(location.getLatitude()));
				msg.append("\n�ثe�g��: ");
				msg.append(Double.toString(location.getLongitude()));
				//Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}else{
				//Toast.makeText(this, "�z�ثe������GPS�T��", Toast.LENGTH_LONG).show();			
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