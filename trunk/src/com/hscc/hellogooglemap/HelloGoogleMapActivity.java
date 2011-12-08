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
	private ProgressDialog pd; //����
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
	
	//�Ĥ@��������ܪ�����
	private void FirstStart() {
		// TODO �|���إ߻���code
	}

	//Menu �����
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_QUICK2, 0, "���|����")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(0, MENU_QUICK4, 1, "��ܸ��")
			.setIcon(R.drawable.ic_menu_loopbin);
		menu.add(0, MENU_ABOUT , 2, "�]�wGPS�϶�")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(0, MENU_QUICK1, 3, "�{�b��m")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(0, MENU_QUICK3, 4, "�M�����|")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(0, MENU_QUIT  , 5, "����")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.add(1, MENU_QUICK2, 0, "���|����")
			.setIcon(R.drawable.ic_menu_to);
		menu.add(1, MENU_QUICK4, 1, "��ܸ��")
			.setIcon(R.drawable.ic_menu_loopbin);
		menu.add(1, MENU_ABOUT , 2, "�]�wGPS�϶�")
			.setIcon(R.drawable.ic_menu_pin);
		menu.add(1, MENU_QUICK1, 3, "�{�b��m")
			.setIcon(R.drawable.ic_menu_location);
		menu.add(1, MENU_QUICK3, 4, "�M�����|")
			.setIcon(R.drawable.ic_menu_garbage);
		menu.add(1, MENU_QUIT  , 5, "����")
			.setIcon(R.drawable.ic_menu_icon1);
		
		menu.setGroupVisible(0, false);
		return true;
	}
	
	//�ǳ� Menu
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
		
		case MENU_QUICK1: //�{�b��m
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
		case MENU_QUICK2: //���|����
			Toast.makeText(HelloGoogleMapActivity.this, "�бz�y��A���ݸ��|���ا���", Toast.LENGTH_LONG).show();
			processPathRecovery();
			break;
		case MENU_QUICK3: //�M�����|
		case MENU_STOP:
			ol.clear();	
			break;
		case MENU_QUICK4: //��ܸ��
			SelectData();
			break;
		case MENU_ABOUT:  //�]�wGPS�϶�
			SettingGPSinterval();
			break;
		case MENU_QUIT:
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//�]�wGPS�϶�
	private void SettingGPSinterval() {
		//Layout�t�m
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.menu, (ViewGroup) findViewById(R.layout.main));
		
		//��ܰt�m
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setView(layout);
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle("��ܵL GPS �϶�");
		alertDialog.setMessage("�ɶ��H�ʤ����ܨåB\n�����ɶ������j��}�l�ɶ�");
		alertDialog.setButton("����", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});

		alertDialog.setButton2("����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				 // Canceled.
			}
		});
		alertDialog.show();
		
		/* ���o���� */
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
					seekBarValueEnd.setText("  �п�ܤ�Start�ʤ����j���Ʀr!");
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
	                Log.d("�}��", "�w�}�ҡA���GPS���|�n���");
	            } else {
	            	isGpsDisplay = false;
	                Log.d("�}��", "�w�����A���GPS���|�����");
	            }
	        }});
	}

	//���� mapView ��ϥΪ̲{�b����m
	private void toMyLocation() {
		mapController.animateTo(my_location);		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	//��@"��ܸ��"
	private void SelectData()
	{
		LayoutInflater factory = LayoutInflater.from(this);            
				
		// �w�q menu.xml ���N�n��ܪ� textEntryView
		final View textEntryView = factory.inflate(R.layout.first, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("��ܸ��"); // "�w��ϥ�"
		alert.setMessage(R.string.OBD_dialog); // "�аݱz�n�ϥηP��������ƩάOOBD���W�E�_�t�Ϊ���Ƨ@���R�H"
	
		alert.setView(textEntryView);

		alert.setPositiveButton("�}���ɮ�", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) {
				
				Intent intent1 = new Intent(HelloGoogleMapActivity.this, FileListView.class);
				intent1.putExtra("FromAppMain", "appMain");
				startActivityForResult(intent1, REQUEST_CODE);
			}
		});

		alert.setNegativeButton("����", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//isOBDusing = false;
			}
		});
		alert.setIcon(R.drawable.ic_menu_info);
		alert.show();
		
	}
	
	//����  Activity �Ҧ^�Ǫ�code
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
				Log.d("���� Activity","�Ǧ^���: " + temp);
				userSelectFileName = temp;
				final TextView seekBarValueStart = (TextView)findViewById(R.id.Information);
				seekBarValueStart.setText("��ơG"+temp);
				setTitle(temp);
			}
		}
	}
	
	//�}�l�e�u�\��
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

	//�B�z�����������ƶǻ�
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setview();
			pd.dismiss();
			Toast.makeText(HelloGoogleMapActivity.this, 
					   "�`�@�� Google Query " + queryTime + " ��", Toast.LENGTH_SHORT).show();
		}
	};

	
	//���|����
	private void processPathRecovery(){
		

		pd = ProgressDialog.show(this, "���ظ��|��", 
				"\n�ɮצW��: " + userSelectFileName +
				"\n��ƨӷ�: " + returnOBD() +
				"\n���b�P Google Map ���q" +
				"\n�ݭn�ɶ��̺������p�өw" +
				"\n�Э@�ߵy��!"
				, true, true);

		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	//�Ǧ^�O�_�ϥ�OBD
	public String returnOBD(){
		if(isOBDusing){
			return "OBD";
		}else{
			return "�P����";
		}
	}
	
	//�b�Ҧ���GPS�y�иs����² index �q indexStart �� indexEnd ���I��
	public void eliminateGpsPoint(int indexStart, int indexEnd){
		ArrayList<Location> temp =  new ArrayList<Location>();
		ArrayList<Location> rslt =  new ArrayList<Location>();
		ArrayList<SenseRecord> curn = new ArrayList<SenseRecord>();
		int target = 0;
		if(indexStart == 1){ //��ܲ{�b�O�b tracking �e��
			target = 1;
		}else if(indexEnd == totalGPSdataSize){ //��ܲ{�b�O�b tracking ���
			target = 2;
		}
		
		//�N�Ҧ�GPS�ҩ�Jtemp�̭�
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

	//�p���� GeoPoint ��������
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
	
	//�ন����
	public double toRad(double number){
		return number*Math.PI/180.0;
	}
	
	//�ন����
	public double toDeg(double number){
		return number*180.0/Math.PI;
	}
	
	//�ܼƺ�ť
	public VariableChangeListener variableChangeListener;
	public interface VariableChangeListener {
        public void onVariableChanged(int variableThatHasChanged);
    }
	
	public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
	       this.variableChangeListener = variableChangeListener;
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