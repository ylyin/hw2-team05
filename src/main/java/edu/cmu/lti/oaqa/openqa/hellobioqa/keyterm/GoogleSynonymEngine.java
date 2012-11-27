package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSynonymEngine {
  
  public static void getFromGoogle() throws IOException, JSONException {
    URL url = new URL("https://ajax.googleapis.com/ajax/services/search/web?v=1.0&"
            + "q=colon%20cancer");
    URLConnection connection = url.openConnection();
    
    String line;
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    
    JSONObject json = new JSONObject(builder.toString());
    
    System.out.println(builder.toString());
    JSONArray jarray = json.getJSONObject("responseData").getJSONArray("results");
    for (int i = 0;i < jarray.length();i++) {
      System.out.println("Title : " + ((JSONObject) jarray.get(i)).getString("titleNoFormatting"));
      System.out.println("Content : " + ((JSONObject) jarray.get(i)).getString("content").replaceAll("<b>|</b>", "").replaceAll("\\s+", " "));
    }
    
    
  }
  
  public static void main(String[] argv) {
    
    try {
      GoogleSynonymEngine.getFromGoogle();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

}
