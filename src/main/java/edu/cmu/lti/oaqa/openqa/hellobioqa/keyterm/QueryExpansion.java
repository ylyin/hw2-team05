package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class QueryExpansion {
  
  public List<Keyterm> expand(List<Keyterm> terms) {
    
    // Using google engine
//    GoogleSynonymEngine engine = new GoogleSynonymEngine();
    
    // Using PubMed engine
    PubMedSynonymEngine engine = new PubMedSynonymEngine();
    
    
    Set<String> expanded = new HashSet<String>();
    for (Keyterm term : terms) {
      expanded.add(term.getText());
      if (term.getText().charAt(0) <= 'Z' && term.getText().charAt(0) >= 'A' && term.getText().indexOf(" ") < 0) {
        System.out.println(term.getText());
        expanded.addAll(engine.getSynonyms(term.getText()));
      }
    }
    
    System.out.println(expanded);
    List<Keyterm> keys = new ArrayList<Keyterm>();
    for (String token : expanded) {
      keys.add(new Keyterm(token));
    }
    return keys;
  }

}
