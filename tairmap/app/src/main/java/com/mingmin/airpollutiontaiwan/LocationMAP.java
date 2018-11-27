package com.mingmin.airpollutiontaiwan;

import java.io.BufferedReader;
import java.io.File;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationMAP extends FragmentActivity implements OnMapClickListener, OnMarkerClickListener, OnMarkerDragListener, OnMapReadyCallback {

	private static final int MSG_UPDATE = 1;

	//MapView mapView;
	GoogleMap mapView = null;

	TextView textView1;
	SupportMapFragment fragment;
	LatLng Pune = null;
	Marker pune;

	EditText fileedit;

	int start=0;
	int press=0;

	private Timer timer;

	Marker lastOpenned = null;

	String IPAddress;

	public LocationManager locationManager1;
	public Location location1 = null;
	public String locationProvider1;

	String groupname[];

	LatLng newpos = null;

	public static final int TAKE_PHOTO_IMAGE_CODE = 1;

	public static final String FileName = "mypic";

	String pic;
	String picfilename;

	Bitmap mBitmap = null;

	int gindex = 0;

	String lat;
	String lng;

	float[] latLong = new float[2];

	ArrayList<AQI> data = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview1);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapview);
		mapFragment.getMapAsync(this);

		Button s4 = (Button) findViewById(R.id.button1);
		s4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view)
			{
				MapRefresh();
			}
		});

	}

	private void MapRefresh()
	{
		PolylineOptions rectOptions = new PolylineOptions();
		rectOptions.width(10);
		rectOptions.color(0xff659d32);

		//if (mapView == null) return;

		//
		data = AQIActivity.my.aqiList;

		Log.i("TAG", data.size()+"");

		for (int i=0; i<data.size(); i++)
		{
			Log.i("TAG", data.get(i).getSiteName());
			LatLng latlng = new LatLng(data.get(i).getLatitude(), data.get(i).getLongitude());
			if (latlng != null) {
				//mapView.addMarker(new MarkerOptions().position(latlng).title(data.get(i).getAQI()+"").snippet(i+"").visible(true)).showInfoWindow();
				TextView text = new TextView(this);
				text.setText(data.get(i).getAQI()+"");

				if (data.get(i).getAQILevel() >= 4)
				{
					text.setTextColor(Color.RED);
				}

				IconGenerator generator = new IconGenerator(this);
				generator.setContentView(text);
				Bitmap icon = generator.makeIcon();

				MarkerOptions tp = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromBitmap(icon));
				mapView.addMarker(tp);
			}
		}

		mapView.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			public void onInfoWindowClick(Marker marker) {
				//your code

			}
		});


	}

	public Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
				case MSG_UPDATE:
					MapRefresh();
					break;
			}
		}
	};


	public LocationListener locationListener1 =
			new LocationListener()
			{

				//@Override
				public void onStatusChanged(String provider, int status, Bundle extras)
				{

				}

				//@Override
				public void onProviderEnabled(String provider)
				{

				}

				//@Override
				public void onProviderDisabled(String provider)
				{
					location1 = null;
				}

				//@Override
				public void onLocationChanged(Location location)
				{
					location1 = location;

					Pune = new LatLng(location1.getLatitude(), location1.getLongitude());

				}
			};

	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub

		newpos = arg0.getPosition();

		return false;
	}

	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub

	}

	public void onMapReady(GoogleMap googleMap) {
		mapView = googleMap;

		mapView.setOnMapClickListener(this);

		locationManager1 = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.NETWORK_PROVIDER;
		locationProvider1 = LocationManager.NETWORK_PROVIDER;
		//location1 = getMyLocationProvider(locationManager1);

		Pune = new LatLng(24.9364636,121.0570716);
		mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(Pune, 14));

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mapView.setMyLocationEnabled(true);
		} else {
			Toast.makeText(this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				mapView.setMyLocationEnabled(true);
			}
		}

		//locationManager1.requestLocationUpdates(locationProvider1, 0, 0, locationListener1);

		MapRefresh();
	}

	public static LatLng getGeoPointFromAddress(String locationAddress) {
		LatLng locationPoint = null;
		String locationAddres = locationAddress.replaceAll(" ", "%20");
		String str = "https://maps.googleapis.com/maps/api/geocode/json?address="
				+ locationAddres + "&key=AIzaSyDhFES0_xuChAFZYcRuMAJpomxJrnSTPx0";

		//Log.i("TAG", str);

		String fullString = "";
		try
		{
			URL url = new URL(str);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				fullString += line;
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		JSONObject json;
		try {

			String lat, lon;
			json = new JSONObject(fullString);
			JSONObject geoMetryObject = new JSONObject();
			JSONObject locations = new JSONObject();
			JSONArray jarr = json.getJSONArray("results");
			int i;
			for (i = 0; i < jarr.length(); i++) {
				json = jarr.getJSONObject(i);
				geoMetryObject = json.getJSONObject("geometry");
				locations = geoMetryObject.getJSONObject("location");
				lat = locations.getString("lat");
				lon = locations.getString("lng");

				Log.i("TAG", lat + "," + lon);

				locationPoint = new LatLng(Double.parseDouble(lat),
						Double.parseDouble(lon));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return locationPoint;
	}

}
