package edu.cmu.lti.oaqa.openqa.test.team05;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class mengKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String arg0) {
    
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    
    String str = null;
    String[] strarray = str.split("\n");
    Object genetag;
    for (int i = 0; i < strarray.length; i++) {
      

      String s = strarray[i].substring(15);
      String id = strarray[i].substring(0, 14);

      Map map = new HashMap();
      try {
        PosTagNamedEntityRecognizer postag = new PosTagNamedEntityRecognizer();
        map = postag.getGeneSpans(s);
      } catch (ResourceInitializationException e) {

        e.printStackTrace();
      }
      Set keys = map.keySet();
      if (keys != null) {
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
          int key = (Integer) iterator.next();
          int value = (Integer) map.get(key);
          String words = s.substring(key, value);
          
          
          keyterms.add(new Keyterm(words));
          
  
        }
      }
    }
    return keyterms;
  }
}

