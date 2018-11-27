package com.tmm.android.rssreader.util;

import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.Resources;
import android.os.StrictMode;
import android.util.Log;


public class RSSHandler extends DefaultHandler {

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private List<Article> articleList = new ArrayList<Article>();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 50;
	
	//Current characters being accumulated
	StringBuffer chars = new StringBuffer();
	
	int start = 0;
	
	/* 
	 * This method is called everytime a start element is found (an opening XML marker)
	 * here we always reset the characters StringBuffer as we are only currently interested
	 * in the the text values stored at leaf nodes
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		chars = new StringBuffer();
		start = 0;
	}



	/* 
	 * This method is called everytime an end element is found (a closing XML marker)
	 * here we check what element is being closed, if it is a relevant leaf node that we are
	 * checking, such as Title, then we get the characters we have accumulated in the StringBuffer
	 * and set the current Article's title to the value
	 * 
	 * If this is closing the "Item", it means it is the end of the article, so we add that to the list
	 * and then reset our Article object for the next one on the stream
	 * 
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {

		
		if (localName.equalsIgnoreCase("title"))
		{
			Log.i("TAG", "Setting article title: " + chars.toString());
			currentArticle.setTitle(chars.toString());
			if (chars.toString().contains("一週天氣預報"))
			{
				start = 1;
			}
		}
		else if (localName.equalsIgnoreCase("description"))
		{
			//Log.i("LOGGING RSS XML", "Setting article description: " + chars.toString());
			if (chars.toString().contains("溫度") && start == 0)
			{
				currentArticle.setDescription(chars.toString());
			}
		}
		else if (localName.equalsIgnoreCase("pubDate"))
		{
			//Log.i("LOGGING RSS XML", "Setting article published date: " + chars.toString());
			currentArticle.setPubDate(chars.toString());
		}
		else if (localName.equalsIgnoreCase("encoded"))
		{
			//Log.d("LOGGING RSS XML", "Setting article content: " + chars.toString());
			currentArticle.setEncodedContent(chars.toString());
		}
		else if (localName.equalsIgnoreCase("item"))
		{

		}
		else if (localName.equalsIgnoreCase("link"))
		{
			try {
				//Log.d("LOGGING RSS XML", "Setting article link url: " + chars.toString());
				currentArticle.setUrl(new URL(chars.toString()));
				currentArticle.setUrls(chars.toString());
			} catch (MalformedURLException e) {
				Log.e("RSA Error", e.getMessage());
			}

		}

		// Check if looking for article, and if article is complete
		if (localName.equalsIgnoreCase("item")) 
		{
			try
			{
			
				Log.i("TAG", currentArticle.title);
				Log.i("TAG", currentArticle.description);
				
				articleList.add(currentArticle);
				
				currentArticle = new Article();

				// Lets check if we've hit our limit on number of articles
				articlesAdded++;
				if (articlesAdded >= ARTICLES_LIMIT)
				{
					throw new SAXException();
				}
			
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/* 
	 * This method is called when characters are found in between XML markers, however, there is no
	 * guarante that this will be called at the end of the node, or that it will be called only once
	 * , so we just accumulate these and then deal with them in endElement() to be sure we have all the
	 * text
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char ch[], int start, int length) {
		chars.append(new String(ch, start, length));
	}

	/**
	 * This is the entry point to the parser and creates the feed to be parsed
	 * 
	 * @param feedUrl
	 * @return
	 */
	public List<Article> getLatestArticles(String feedUrl) {
		
		
		URL url = null;
		try {

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			url = new URL(feedUrl);

			Log.i("TAG", "fuck " + feedUrl);

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			//url.setInstanceFollowRedirects(true);
			xr.setContentHandler(this);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
			HttpURLConnection.setFollowRedirects(true);

			InputStream xml1 = conn.getInputStream();

			xr.parse(new InputSource(xml1));

		} catch (IOException e) {
			Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
		} catch (SAXException e) {
			Log.e("RSS Handler SAX", e.toString());
		} catch (ParserConfigurationException e) {
		}
		
		return articleList;
	}

}
