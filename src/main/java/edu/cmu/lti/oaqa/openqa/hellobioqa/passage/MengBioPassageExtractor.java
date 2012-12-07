package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.answerselection.AnswerSelection;
import edu.cmu.lti.oaqa.openqa.filters.CutKeywordsFilter;
import edu.cmu.lti.oaqa.openqa.filters.DirectSpeechFilter;

import edu.cmu.lti.oaqa.openqa.filters.Result;

import edu.cmu.lti.oaqa.openqa.filters.UnnecessaryCharactersFilter;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class MengBioPassageExtractor extends SimplePassageExtractor {
  
  /*PhaniBioPassageExtractor P;
  
  public MengBioPassageExtractor() {
    P = new PhaniBioPassageExtractor();
  }*/

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    
    
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (RetrievalResult document : documents) {
      System.out.println("RetrievalResult: " + document.toString());
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);

        // cleaning HTML text
        String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", ""); // .trim() ;
        // for now, making sure the text isn't too long
        text = text.substring(0, Math.min(5000, text.length()));
        System.out.println(text);

        PassageCandidateFinder finder = new PassageCandidateFinder(id, text,
                new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings
                .toArray(new String[0]));
        for (PassageCandidate passageSpan : passageSpans)
          result.add(passageSpan);
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    
    // return F.apply(result);
    // return resultsList.toArray(new Result[resultsList.size()]);
    // return result;

   
    /*List<PassageCandidate> result = P.extractPassages(question, keyterms, documents);*/
    System.out.print("Got the basic passages:"+result.size()+"\n");

    Result[] R = makeResults(result);
    AnswerSelection.clearFilters();

   
    AnswerSelection.addFilter(new CutKeywordsFilter());
    AnswerSelection.addFilter(new DirectSpeechFilter());
    AnswerSelection.addFilter(new UnnecessaryCharactersFilter());
    
    Result[] filteredResult = AnswerSelection.getResults(R, R.length, 0);
    
    System.out.println("filtered the passages");
    
    try {
      return makePassageCandidates(result,filteredResult);
    } catch (AnalysisEngineProcessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    System.out.println("Back to passage candidates");
    
    return new ArrayList<PassageCandidate>();
  }

  List<PassageCandidate> makePassageCandidates(List<PassageCandidate> prevResult, Result[] filteredResults) throws AnalysisEngineProcessException {
    List<PassageCandidate> L = new ArrayList<PassageCandidate>();
    //String docID, int start, int end, float score, String queryString
    for (Result r:filteredResults) {
      PassageCandidate prevP = prevResult.get(r.getPassageCandidateIndex());
      PassageCandidate p = new PassageCandidate(prevP.getDocID(), prevP.getStart(), prevP.getEnd(), r.getScore(), prevP.getQueryString());
      L.add(p);
    }
    return L;
  }
  
  Result[] makeResults(List<PassageCandidate> thirdPhaseResult) {
    Result[] L = new Result[thirdPhaseResult.size()];
    int index=0;
    for (PassageCandidate p : thirdPhaseResult) {
      L[index] = new Result(p, index);
      index += 1;
    }
    return L;
  }

}
