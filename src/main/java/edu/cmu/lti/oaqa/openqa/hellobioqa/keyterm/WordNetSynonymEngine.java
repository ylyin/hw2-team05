package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.wordnet.Syns2Index;

public class WordNetSynonymEngine {
  
  private static RAMDirectory directory;
  
  private static IndexSearcher searcher;
  
  private static String path = "src/main/resources/model/wordnetIndex";
  
  public static String[] getSynonms(String word) throws IOException {
    if (directory == null || searcher == null) {
      directory = new RAMDirectory(FSDirectory.open(new File(path)));
      searcher = new IndexSearcher(IndexReader.open(directory));
    }
    
    List<String> syns = new ArrayList<String>();
    CountingCollector countingCollector = new CountingCollector();
    Query query = new TermQuery(new Term(Syns2Index.F_WORD, word));
    searcher.search(query, countingCollector);
    
    if (countingCollector.numHits > 0) {
      ScoreDoc[] hits = searcher.search(query, countingCollector.numHits).scoreDocs;
      
      for (ScoreDoc hit : hits) {
        System.out.println(hit.doc);
        Document doc = searcher.doc(hit.doc);
        String[] values = doc.getValues(Syns2Index.F_SYN);
        
        for (String val : values) {
          syns.add(val);
          System.out.println(val);
        }
      }
    }
    
    searcher.close();
    directory.close();
    
    return syns.subList(0, Math.min(2, syns.size())).toArray(new String[0]);
  }
  
  final static class CountingCollector extends Collector {
    public int numHits = 0;
    
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) throws IOException {
      numHits++;
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) {}
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }    
  }
  
  public static void main(String[] argv) {
    try {
      System.out.println(WordNetSynonymEngine.getSynonms("woods").length);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
