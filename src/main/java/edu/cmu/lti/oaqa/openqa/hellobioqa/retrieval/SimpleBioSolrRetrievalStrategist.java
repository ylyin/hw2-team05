package edu.cmu.lti.oaqa.openqa.hellobioqa.retrieval;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.retrieval.SimpleSolrRetrievalStrategist;
import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.QueryExpansion;

public class SimpleBioSolrRetrievalStrategist extends SimpleSolrRetrievalStrategist {

  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      int localHitListSize = 10;
      //SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      SolrDocumentList docs = wrapper.runQuery(query, localHitListSize);

      for (SolrDocument doc : docs) {

        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }
  
  /*
  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {
    // Query Expansion
    QueryExpansion qe = new QueryExpansion();
    List<Keyterm> expanded = qe.expand(keyterms);
    
    StringBuffer result = new StringBuffer();
    for (Keyterm keyterm : expanded) {
      result.append(keyterm.getText() + " ");
    }
    String query = result.toString();
    System.out.println("[UPDATED] QUERY: " + query);
    return query;
  }*/

}
