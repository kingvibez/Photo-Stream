package com.mohan.android.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.util.Log;

public class PhotoFetcher {
	
	public static final String TAG = "PhotoFetcher";
	
	public static final String PREF_SEARCH_QUERY = "searchQuery";
	
	private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
	private static final String API_KEY = "4acc7d0e103d0f26f4cb2a0f59fc8efa";
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String METHOD_SEARCH = "flickr.photos.search";
	private static final String PARAM_EXTRAS = "extras";
	private static final String EXTRA_SMALL_URL = "url_s";
	private static final String PAGE_NUMBER = "page";
	
	private static final String XML_PHOTO = "photo";

	byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec);
		//url.openConnection() returns a URLConnection, thus cast it to HttpURLConnection
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
			
			int bytesRead = 0;
            byte[] buffer = new byte[1024];
            
            while ((bytesRead = in.read(buffer)) > 0) {
            	out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}
	
	public String getUrl(String urlSpec) throws IOException {
		return new String(getUrlBytes(urlSpec));
	}
	
	public ArrayList<GalleryItem> fetchFlickrItems(String page) {
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_GET_RECENT)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.appendQueryParameter(PAGE_NUMBER, page)
				.build().toString();
		return downloadGalleryItems(url);
	}
	
	public ArrayList<GalleryItem> searchFlickr(String query) {
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_SEARCH)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.appendQueryParameter("text", query)
				.build().toString();
		return downloadGalleryItems(url);
	}
	
	private ArrayList<GalleryItem> downloadGalleryItems(String url) {
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
		
		try {
			//Get the XML into a string from the URL
			String xmlString = getUrl(url);
			Log.i(TAG, "Received XML: "+xmlString);
			//Create an XMLPullParser and set its input as the xmlString
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			//Call parseItems method
			parseItems(items, parser);
		} catch (IOException ioe) {
			Log.e(TAG, "Failed to get XML, exception: "+ioe);
		} catch (XmlPullParserException xppe) {
			Log.e(TAG, "Failed to get XML, exception: "+xppe);
		}
		return items;
	}
	
	void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int eventType = parser.next();
		
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG &&
					parser.getName().equals(XML_PHOTO)) {
				String id = parser.getAttributeValue(null, "id");
				String caption = parser.getAttributeValue(null, "title");
				String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
				
				GalleryItem item = new GalleryItem();
				item.setCaption(caption);
				item.setId(id);
				item.setUrl(smallUrl);
				items.add(item);
			}
			eventType = parser.next();
		}
	}
	
}
