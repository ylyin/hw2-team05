package edu.cmu.lti.oaqa.openqa.test.team05;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class LingpipeKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * The maximum number of chunks that the chunker will return in order of confidence.
   */
  private static final int MAX_N_BEST_CHUNKS = 8;
  
  /**
   * The confidence threshold to help us decide whether we should consider a chunk as
   * a true gene tag.
   */
  private static final double CONFIDENCE_THRESHOLD = 0.2;

  /**
   * A confidence chunker that provided by LingPipe
   */
  private static ConfidenceChunker chunker;
  
  public LingpipeKeytermExtractor() {
    super();
    if (chunker == null) {
      try {
        chunker = (ConfidenceChunker) AbstractExternalizable.readObject(new File("src/main/resources/model/ne-en-bio-genetag.HmmChunker"));
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
  
  @Override
  protected List<Keyterm> getKeyterms(String text) {
    
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    
    char[] cs = text.toCharArray();
    
    // Extract gene mentions
    Iterator<Chunk> it = chunker.nBestChunks(cs, 0, cs.length, MAX_N_BEST_CHUNKS);

    while (it.hasNext()) {
      Chunk chunk = it.next();

      // Get begin and end
      int begin = chunk.start();
      int end = chunk.end();
      // Get confidence
      double conf = Math.pow(2.0, chunk.score());

      // Ignore confidence less than 0.5
      if (conf < CONFIDENCE_THRESHOLD)
        continue;
      System.out.println("[KEYTERM] " + text.substring(begin, end));
      keyterms.add(new Keyterm(text.substring(begin, end)));
    }
    
    return keyterms;
  }

}
