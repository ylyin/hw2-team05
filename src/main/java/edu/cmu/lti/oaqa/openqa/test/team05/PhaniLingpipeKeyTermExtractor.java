/**
 * 
 */
package edu.cmu.lti.oaqa.openqa.test.team05;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * @author pgadde
 * 
 */
public class PhaniLingpipeKeyTermExtractor extends AbstractKeytermExtractor {

  private String mFile;

  private Chunker chunker;

  /**
   * 
   */
  public PhaniLingpipeKeyTermExtractor() {
    mFile = "src/main/resources/model/ne-en-bio-genetag.HmmChunker";
    File modelFile = new File(mFile);
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * HMM based chunker from Lingpipe
   */
  @Override
  protected List<Keyterm> getKeyterms(String text) {
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    Chunking chunking = chunker.chunk(text);
    Set<Chunk> chunkSet = chunking.chunkSet();
    for (Chunk C : chunkSet) {
      int chunkStart = C.start();
      int chunkEnd = C.end();
      String chunkText = text.substring(chunkStart, chunkEnd);
      Keyterm k = new Keyterm(chunkText);
      keyterms.add(k);
    }
    return keyterms;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    PhaniLingpipeKeyTermExtractor P = new PhaniLingpipeKeyTermExtractor();
    System.out.println(P.getKeyterms("Comparison with alkaline phosphatases and 5-nucleotidase"));
  }

}
