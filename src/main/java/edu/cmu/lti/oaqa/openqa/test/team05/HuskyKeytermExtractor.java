package edu.cmu.lti.oaqa.openqa.test.team05;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class HuskyKeytermExtractor extends AbstractKeytermExtractor {

  public static final String PARAM_REGEX_MODEL_PATH = "src/main/resources/model/regex_1.model";

  protected RegexModel regexModel = null;

  /**
   * Loads the model from file given by configuration parameter "regexModelPath"
   * @throws IOException can't find or can't open the model file. 
   */
  protected void loadModels() throws IOException {
    regexModel = new RegexModel(PARAM_REGEX_MODEL_PATH);
  }
  
  @Override
  protected List<Keyterm> getKeyterms(String text) {
    System.err.println("HuskieGeneAnnotator ... ");
    // load the model
    if (regexModel == null) {
      try {
        loadModels();
      } catch (IOException e) {
        System.err.printf("Unable to load model ...\n");
        return null;
      }
    }
    
    String[] rawKeyterms = regexModel.annotateLine(text);
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    for (String kt : rawKeyterms) {
      keyterms.add(new Keyterm(kt));
    }
    
    return keyterms;
  }

}
