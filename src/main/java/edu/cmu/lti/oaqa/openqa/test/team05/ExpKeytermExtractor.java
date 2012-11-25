package edu.cmu.lti.oaqa.openqa.test.team05;

import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.KeytermComponent;

public class ExpKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String text) {
    
    System.out.println("text : " + text);
    
    KeytermComponent com = new KeytermComponent();
    return com.getKeyterms(text);
  }

}
