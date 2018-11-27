package com.mingmin.airpollutiontaiwan;

import java.io.ByteArrayOutputStream;

import java.net.URL;
import java.util.ArrayList;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.tmm.android.rssreader.reader.RssReader;
import com.tmm.android.rssreader.util.Article;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class RssActivity extends Activity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
	private static final int MSG_ADD_OK = 1;  
	private static final int MSG_ADD_FAIL = 2;  
	
	ProgressDialog  myDialog;

	ArrayList<String> rep = new ArrayList<String>();

	public static RssActivity my;
	
	/** Called when the activity is first created. */
	TextView show_view;
	
	List<Article> jobs = new ArrayList<Article>();
	List<Article> searchjobs = new ArrayList<Article>();
	
	int mchildid;

	int area=1;
	
	String allarea[];
	
	private SharedPreferences settings;
	private static final String data = "DATA";
	private static final String areaField = "AREA";
	
	Spinner spinner;
	
	int flag = 0;
	

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchlist);
        spinner = (Spinner) findViewById(R.id.spinner1);
		
		Resources res = getResources();
		allarea = res.getStringArray(R.array.area);	
		
		my = this;
		
        show_view = (TextView)findViewById(R.id.tv);

        int idx = readData();
        
        spinner.setSelection(idx);

        refresh();
        
		ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, allarea);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id)
            {
            	if (flag == 1)
            	{
	            	saveData(position);
	            	area = position;
	            	refresh();
            	}
            	
            	flag = 1;
            }
            public void onNothingSelected(AdapterView arg0) {
            }
        });
        
		
	}	
	
	void refresh()
	{
		//Progress
        myDialog = ProgressDialog.show
        (
        		RssActivity.this,
        		"Loading...",
        		"",
            true
        );
        
        new Thread()
        {
          public void run()
          {
        	try 
      		{
                Resources res =getResources();
                String url[]=res.getStringArray(R.array.gpsurl);

        		RssReader.setRss(url);
      			jobs = RssReader.getLatestRssFeed(area);
      			
      			Log.i("TAG", jobs.size()+"");
      		} 
        	catch (Exception e) 
      		{
      			myDialog.dismiss();
                Message msg = new Message();
                msg.what = MSG_ADD_FAIL;
                myHandler.sendMessage(msg);
      			e.printStackTrace();
      			return;
      		}
      		finally
      		{
      			myDialog.dismiss();
                Message msg = new Message();
                msg.what = MSG_ADD_OK;
                myHandler.sendMessage(msg);
      		}
      		
          }
         }.start();          		
	}
	
	    
    public Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
              case MSG_ADD_OK:
          		
				int j;
				  
				int flag=0;
				for (j=0; j<jobs.size(); j++)
				{ 
					Log.i("TAG", jobs.get(j).getDescription());
					if (jobs.get(j).getDescription().contains("明日白天"))
					{
						flag = 1;
						break;
					}
				}
				
				
				if (flag == 0)
				{
					Toast.makeText(RssActivity.this, "API ERROR", Toast.LENGTH_LONG).show();
					return;
				}
        		
				if (j == jobs.size())
					show_view.setText(Html.fromHtml(jobs.get(j-1).getDescription()));
				else
	            	show_view.setText(Html.fromHtml(jobs.get(j).getDescription()));
        		
            	break;
              case MSG_ADD_FAIL:
  	        	Context context = getApplicationContext();
	            int duration = Toast.LENGTH_LONG;
	            Toast toast = Toast.makeText(context, "", duration);
	            toast.show();
            	break;
            }
        }
    };
	    
	    
	public int readData(){
	    settings = getSharedPreferences(data,0);
	    return settings.getInt(areaField, 0);
	}
	public void saveData(int area){
	    settings = getSharedPreferences(data,0);
	    settings.edit()
	        .putInt(areaField, area)
	        .commit();
	}
    

}