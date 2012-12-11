package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSynonymEngine {

  private static List<String> stoplist;

  public GoogleSynonymEngine() {
    try {
      loadStoplist();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  public List<String> getSynonyms(String term) {
    List<String> synonyms = new ArrayList<String>();
    
    //File file = new File("model/google1/" + term + ".txt");
    File file = new File("src/main/resources/model/google1/" + term + ".txt");
    if (!file.exists()) {
      System.err.println("[MISSING] " + term);
      return synonyms;
    }
    
    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
      
      // Term frequency map
      Map<String, Integer> tmap = new HashMap<String, Integer>();
      // Document frequency map
      Map<String, Integer> dmap = new HashMap<String, Integer>();
      Map<String, Integer> tdmap = new HashMap<String, Integer>();
      
      while (scanner.hasNextLine()) {
        String[] tokens = scanner.nextLine().split(" ");
        
        Set<String> unique = new HashSet<String>();
        for (String token : tokens) {
          token = token.toLowerCase();
          if (!stoplist.contains(token)) {
            if (tmap.containsKey(token)) {
              tmap.put(token, tmap.get(token) + 1);
            } else {
              tmap.put(token, 1);
            }
            unique.add(token);
          }
        }
        
        for (String token : unique) {
          if (dmap.containsKey(token)) {
            dmap.put(token, dmap.get(token) + 1);
          } else {
            dmap.put(token, 1);
          }
        }
      }
      
      // Calculate score of TF * DF
      for (String token : tmap.keySet()) {
        tdmap.put(token, tmap.get(token) * dmap.get(token));
      }

      // Sort terms according to their score of TF * DF
      List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
              tdmap.entrySet());
      Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
        @Override
        public int compare(Entry<String, Integer> e0, Entry<String, Integer> e1) {
          return e1.getValue().compareTo(e0.getValue());
        }
      });

      // Pick the top 6 terms
      for (int i = 0; i < 6; i++) {
        if (!list.get(i).getKey().equals(term.toLowerCase())
                && list.get(i).getValue() >= 4) {
          System.out.println("[GOOGLE] " + list.get(i).getKey() + " : " + list.get(i).getValue());
          synonyms.add(list.get(i).getKey());
        }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
      return synonyms;
    }
    
    return synonyms;
  }

  public List<String> getSynonyms1(String term) {

    List<String> synonyms = new ArrayList<String>();

    PrintWriter writer = null;
    try {
      //File output = new File("model/google1/" + term + ".txt");
      File output = new File("src/main/resources/model/google1/" + term + ".txt");
      output.createNewFile();
      writer = new PrintWriter(output);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    String key = "AIzaSyAAJnRujqrgbZMTEuQp3F45AcLXmdylxNY";
    String cx = "003602488204300441587:n2yrkbyyshi";

    StringBuilder builder = new StringBuilder();
    try {
      // URL url = new URL("https://ajax.googleapis.com/ajax/services/search/web?v=1.0&" + "q=" + term);
      URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + cx
             + "&q=" + term + "&alt=json");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "application/json");

      String line;
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
    } catch (IOException e) {
      System.err.println("[GOOGLE] URL connection error!");
      e.printStackTrace();
      return synonyms;
    }

    Map<String, Integer> tmap = new HashMap<String, Integer>();
    Map<String, Integer> dmap = new HashMap<String, Integer>();
    Map<String, Integer> tdmap = new HashMap<String, Integer>();

    try {
      System.out.println(builder.toString());
      JSONObject json = new JSONObject(builder.toString());

      System.out.println(builder.toString());
      // JSONArray jarray = json.getJSONObject("responseData").getJSONArray("results");
      JSONArray jarray = json.getJSONArray("items");
      for (int i = 0; i < jarray.length(); i++) {
        String title = ((JSONObject) jarray.get(i)).getString("title")
//        String title = ((JSONObject) jarray.get(i)).getString("titleNoFormatting")
                .replaceAll("\\(|\\)|\\,|\\||\\-|\\@|\\.|\\[|\\]|\\:", "").replaceAll("\\s+", " ").trim();
        String content = ((JSONObject) jarray.get(i)).getString("snippet")
//        String content = ((JSONObject) jarray.get(i)).getString("content")
                .replaceAll("<b>|</b>", "").replaceAll("\\(|\\)|\\,|\\||\\-|\\@|\\.|\\:|\\;|\\[|\\]|\\:", "")
                .replaceAll("\\s+", " ").trim();
        System.out.println("Title : " + title);
        System.out.println("Content : " + content);
        writer.println(title + " " + content);

        Set<String> unique = new HashSet<String>();

        String[] tokens = title.split(" ");
        for (String token : tokens) {
          token = token.toLowerCase();
          if (!stoplist.contains(token)) {
            if (tmap.containsKey(token)) {
              tmap.put(token, tmap.get(token) + 1);
            } else {
              tmap.put(token, 1);
            }
            unique.add(token);
          }
        }
        tokens = content.split(" ");
        for (String token : tokens) {
          token = token.toLowerCase();
          if (!stoplist.contains(token)) {
            if (tmap.containsKey(token)) {
              tmap.put(token, tmap.get(token) + 1);
            } else {
              tmap.put(token, 1);
            }
            unique.add(token);
          }
        }

        for (String token : unique) {
          if (dmap.containsKey(token)) {
            dmap.put(token, dmap.get(token) + 1);
          } else {
            dmap.put(token, 1);
          }
        }
      }
      
      writer.flush();
      writer.close();

    } catch (JSONException e) {
      System.err.println("[GOOGLE] Google JSON error!");
      e.printStackTrace();
      return synonyms;
    }

    for (String token : tmap.keySet()) {
      tdmap.put(token, tmap.get(token) * dmap.get(token));
    }

    List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
            tdmap.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      @Override
      public int compare(Entry<String, Integer> e0, Entry<String, Integer> e1) {
        return e1.getValue().compareTo(e0.getValue());
      }
    });

    for (int i = 0; i < 5; i++) {
      System.out.println("[GOOGLE] " + list.get(i).getKey() + " : " + list.get(i).getValue());
      if (!list.get(i).getKey().equals(term.toLowerCase())
      // && term.toLowerCase().indexOf(list.get(i).getKey()) < 0
              && list.get(i).getValue() >= 4) {
        synonyms.add(list.get(i).getKey());
      }
    }

    return synonyms;
  }

  /**
   * Load the stop list from 'stoplist.txt'.
   * @throws FileNotFoundException
   */
  public void loadStoplist() throws FileNotFoundException {
    stoplist = new ArrayList<String>();
    //File file = new File("stoplist.txt");
    File file = new File("src/main/resources/stoplist.txt");
    Scanner scanner = new Scanner(file);

    while (scanner.hasNextLine()) {
      stoplist.add(scanner.nextLine());
    }
    scanner.close();
  }

  public static void main(String[] argv) {

    GoogleSynonymEngine engine = new GoogleSynonymEngine();
    // engine.getSynonyms("BARD1%20gene");
    engine.getSynonyms1("NM23");

  }

}
