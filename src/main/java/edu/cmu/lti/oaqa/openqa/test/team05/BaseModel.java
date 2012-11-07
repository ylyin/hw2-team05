package edu.cmu.lti.oaqa.openqa.test.team05;
import java.io.IOException;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * BaseModel - base class for all the models
 * @author yuchenz
 *
 */
public abstract class BaseModel {
  
  /**
   * Loads the model from given path. 
   * @param modelPath path to the file contains the model. 
   * @throws IOException
   */
  public abstract void loadModelFromFile(String modelPath) throws IOException;

  /**
   * Annotate a line (sentence).
   * @param line line to annotate
   * @param aJCas the CAS that should get the resulting annotation. 
   * @return an array of annotations. 
   */
  public abstract String[] annotateLine(String line);
}
