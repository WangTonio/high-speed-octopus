package com.hscc.hellogooglemap;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class HelloItemizedOverlay extends ItemizedOverlay {
	
	//Declare
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	public Context mContext;
	
	//Constructor 1
	public HelloItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		}
	//Constructor 2
	public HelloItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(defaultMarker);
		  mContext = context;
		}
	//Constructor 3
	public HelloItemizedOverlay(Context context, Drawable defaultMarker) {
	    super(boundCenterBottom(defaultMarker));
	    mContext = context;
	}
	//Method
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		Toast.makeText(mContext, mOverlays.get(index).getSnippet(),
		        Toast.LENGTH_LONG).show();
		return true;
	}
	
}

