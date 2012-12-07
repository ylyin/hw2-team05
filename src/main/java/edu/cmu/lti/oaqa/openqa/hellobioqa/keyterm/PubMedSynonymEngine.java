package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PubMedSynonymEngine {

  public List<String> getSynonyms(String term) {
    List<String> synonyms = new ArrayList<String>();
    
    String html = getPage(term.replaceAll("\\s+", "%20"));
    int ratindex = html.indexOf("Rattus norvegicus");
    if (ratindex > 0) {
      html = html.substring(ratindex);
    }
    
    // Full name
    Pattern p = Pattern.compile("<p class=\"desc\">(.*?)</p>");
    Matcher m = p.matcher(html);
    if (m.find()) {
      String full = m.group(1).replaceAll("<b>|</b>", "");
      String[] tokens = full.split(",");
      System.out.println(full);
      for (String token : tokens) {
        synonyms.add(token);
      }
    }
    
    // Other alias
//    p = Pattern.compile("Other Aliases: </dt><dd class=\"desig\">(.*?)</dd>");
//    m = p.matcher(html);
//    if (m.find()) {
//      System.out.println(m.group(1));
//      String[] alias = m.group(1).replaceAll("<b>|</b>", "").split(",");
//      for (String token : alias) {
//        synonyms.add(token.trim());
//      }
//    }
    
    // Other designations
//    p = Pattern.compile("Other Designations: </dt><dd class=\"desig\">(.*?)</dd>");
//    m = p.matcher(html);
//    if (m.find()) {
//      System.out.println(m.group(1));
//      String[] desigs = m.group(1).replaceAll("<b>|</b>", "").split(";");
//      for (String token : desigs) {
//        synonyms.add(token.trim());
//      }
//    }

    return synonyms;
  }

  public String getPage(String term) {
    // Read webpage content of PubMed
    StringBuilder builder = new StringBuilder();
    try {
      URL url = new URL("http://www.ncbi.nlm.nih.gov/gene?term=" + term);
      URLConnection connection = url.openConnection();

      String line;
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
    
    return builder.toString();
  }
  
  public static void main(String[] argv) {
    // PubMedSynonymEngine engine = new PubMedSynonymEngine();
    // engine.getSynonyms("APC");
    Pattern p = Pattern.compile("<p>([\\s\\S]*?)</p>");
    Matcher m = p.matcher("<p><p><p>abc</a>\n\r     a</p><p>");
    if (m.find()) {
      String full = m.group(1).replaceAll("<b>|</b>", "");
      int index = full.lastIndexOf("<p>");
      System.out.println(full.substring(index + 3));
      System.out.println(full);
      // synonyms.add(full);
    }
  }

}
