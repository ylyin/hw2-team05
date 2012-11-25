package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KeytermComponent {
  
  private static List<String> stopwords;
  
  public KeytermComponent() {
    
    if (stopwords == null) {
      stopwords = new ArrayList<String>();
      
      File file = new File("src/main/resources/stopwords.txt");
      Scanner scanner = null;
      try {
        scanner = new Scanner(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return;
      }
      
      while (scanner.hasNextLine()) {
        stopwords.add(scanner.nextLine());
      }
    }
  }
  
  public List<Keyterm> getKeyterms(String question) {
    String[] q = question.replaceAll("\\?|\\(|\\)", "").split("\\s+");
    
    List<Keyterm> ret = new ArrayList<Keyterm>();
    for (String term : q) {
      if (!stopwords.contains(term.toLowerCase())) {
        // System.out.println("[KEYTERM] " + term);
        ret.add(new Keyterm(term));
        
        // Get synonyms
//        try {
//          String[] syns = getSynonyms(term);
//          for (String syn : syns) {
//            System.out.println("[SYNONYM] " + syn);
//            ret.add(new Keyterm(syn));
//          }
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
      }
    }
    
    return ret;
  }
  
  public String[] getSynonyms(String term) throws IOException {
    return WordNetSynonymEngine.getSynonms(term);
  }
  
  public static void main(String[] argv) {
    KeytermComponent com = new KeytermComponent();
    com.getKeyterms("How do  mutations in Sonic Hedgehog genes affect developmental disorders?");
  }

}
