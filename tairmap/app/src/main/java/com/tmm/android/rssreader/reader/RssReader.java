/**
 * 
 */
package com.tmm.android.rssreader.reader;


import java.util.ArrayList;


import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.util.Log;

import com.tmm.android.rssreader.util.Article;
import com.tmm.android.rssreader.util.RSSHandler;

/**
 * @author rob
 *
 */
public class RssReader {
	
	private final static String BOLD_OPEN = "<B>";
	private final static String BOLD_CLOSE = "</B>";
	private final static String BREAK = "<BR>";
	private final static String ITALIC_OPEN = "<I>";
	private final static String ITALIC_CLOSE = "</I>";
	private final static String SMALL_OPEN = "<SMALL>";
	private final static String SMALL_CLOSE = "</SMALL>";
	
	
	static ArrayList<String> url;
	
	public static void setRss(String[] myurl)
	{
		
		url = new ArrayList<String>();
		
		for  (int i=0; i<myurl.length; i++)
		{
			url.add(myurl[i]);
		}
		
	}

	
	/**
	 * This method defines a feed URL and then calles our SAX Handler to read the article list
	 * from the stream
	 * 
	 * @return List<JSONObject> - suitable for the List View activity
	 */
	public static List<Article> getLatestRssFeed(int area)
	{
		
		RSSHandler rh = new RSSHandler();
		
		
		List<Article> allarticle = new ArrayList<Article>(); 
		List<Article> articles = new ArrayList<Article>(); 

		Log.i("TAG", url.get(area)+"");
		
		List<Article> articles1 =  rh.getLatestArticles(url.get(area));
		articles.addAll(articles1);

		HashSet<Article> set = new HashSet<Article>();
		for (int j=0; j<articles.size(); j++)
		{ 
			set.add(articles.get(j));
		}
		
		allarticle.addAll(set);

		
		Log.i("RSS ERROR", "Number of articles " + allarticle.size());
		
		//return fillData(articles);
		return allarticle;
	}
	
	
	public static List<Article> getLatestRssFeed(int classify, ArrayList<String> urls)
	{
		//String feed = "http://www.moc.gov.tw/findArtActivityRss.do?method=findRss&category=" + classify;
		
		RSSHandler rh = new RSSHandler();
		
		List<Article> allarticle = new ArrayList<Article>(); 
		List<Article> articles = new ArrayList<Article>(); 
		
		for (int i=0; i<urls.size(); i++)
		{
			List<Article> articles1 =  rh.getLatestArticles(urls.get(i));
			articles.addAll(articles1);
		}

		HashSet<Article> set = new HashSet<Article>();
		for (int j=0; j<articles.size(); j++)
		{ 
			set.add(articles.get(j));
		}
		
		allarticle.addAll(set);

		Log.i("RSS ERROR", "Number of articles " + allarticle.size());
		
		//return fillData(articles);
		return allarticle;
	}
	
	
	
    public static double geo_distance(double lat1, double lng1, double lat2,  
            double lng2) {  
        // earth's mean radius in KM  
        double r = 6378.137;  
        lat1 = Math.toRadians(lat1);  
        lng1 = Math.toRadians(lng1);  
        lat2 = Math.toRadians(lat2);  
        lng2 = Math.toRadians(lng2);  
        double d1 = Math.abs(lat1 - lat2);  
        double d2 = Math.abs(lng1 - lng2);  
        double p = Math.pow(Math.sin(d1 / 2), 2) + Math.cos(lat1)  
                * Math.cos(lat2) * Math.pow(Math.sin(d2 / 2), 2);  
        double dis = r * 2 * Math.asin(Math.sqrt(p));  
        return dis;  
    }  
    
	
	
	/**
	 * This method takes a list of Article objects and converts them in to the 
	 * correct JSON format so the info can be processed by our list view
	 * 
	 * @param articles - list<Article>
	 * @return List<JSONObject> - suitable for the List View activity
	 */
	private static List<JSONObject> fillData(List<Article> articles) {

        List<JSONObject> items = new ArrayList<JSONObject>();
        for (Article article : articles) {
            JSONObject current = new JSONObject();
            try {
            	buildJsonObject(article, current);
			} catch (JSONException e) {
				Log.e("RSS ERROR", "Error creating JSON Object from RSS feed");
			}
			items.add(current);
        }
        
        return items;
	}


	/**
	 * This method takes a single Article Object and converts it in to a single JSON object
	 * including some additional HTML formating so they can be displayed nicely
	 * 
	 * @param article
	 * @param current
	 * @throws JSONException
	 */
	private static void buildJsonObject(Article article, JSONObject current) throws JSONException {
		String title = article.getTitle();
		String description = article.getDescription();
		String date = article.getPubDate();
		String imgLink = article.getImgLink();
		
		StringBuffer sb = new StringBuffer();
		sb.append(BOLD_OPEN).append(title).append(BOLD_CLOSE);
		sb.append(BREAK);
		sb.append(description);
		sb.append(BREAK);
		sb.append(SMALL_OPEN).append(ITALIC_OPEN).append(date).append(ITALIC_CLOSE).append(SMALL_CLOSE);
		
		current.put("text", Html.fromHtml(sb.toString()));
		current.put("imageLink", imgLink);
	}
}
