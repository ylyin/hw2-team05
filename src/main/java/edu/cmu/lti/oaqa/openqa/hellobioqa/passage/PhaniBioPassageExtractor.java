package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mysql.jdbc.MysqlParameterMetadata;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.QueryExpansion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhaniBioPassageExtractor extends AbstractPassageExtractor {

  private CompositeKeytermWindowScorer scorer;

  protected SolrWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {

    super.initialize(aContext);
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");

    List<Double> scorerLambdas = new ArrayList<Double>();
    for (String lambdaString : (String[]) aContext.getConfigParameterValue("keytermScorerLambdas"))
      scorerLambdas.add(new Double(Double.parseDouble(lambdaString)));

    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
      this.scorer = new CompositeKeytermWindowScorer();
      String[] scorers = (String[]) aContext.getConfigParameterValue("keytermWindowScorers");
      if (!(scorers.length == scorerLambdas.size()))
        throw new RuntimeException(
                "Configuration Parameter values must be same length: keytermWindowScorers, keytermScorerLambdas");
      for (int i = 0; i < scorers.length; i++)
        scorer.add(
                (edu.cmu.lti.oaqa.openqa.hellobioqa.passage.KeytermWindowScorer) Class.forName(
                        scorers[i]).newInstance(), scorerLambdas.get(i));
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

  }

  private class PhaniPassageSpan {

    public int begin, end;

    public String text;

    public PhaniPassageSpan(int begin, int end, String text) {
      this.begin = begin;
      this.end = end;
      this.text = text;
    }

  }

  private List<PassageCandidate> htmlBasedExtraction(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, SolrWrapper wrapper) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    HashMap<PassageCandidate, Float> M = new HashMap<PassageCandidate, Float>();
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
        ArrayList<PhaniPassageSpan> basicTextSpans = getBasicHTMLSpans(htmlText);
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        String[] keytermStringsArray = keytermStrings.toArray(new String[0]);
        List<PassageCandidate> passageSpans = extractWithNybergScoring(keytermStringsArray,
                basicTextSpans, document);

        // printQuesAnalysis(document, keytermStringsArray, passageSpans);

        for (PassageCandidate passageSpan : passageSpans) {

          M.put(passageSpan, passageSpan.getProbability());
        }

      } catch (SolrServerException e) {
        e.printStackTrace();
      } catch (AnalysisEngineProcessException e) {
        e.printStackTrace();
      }
    }

    List<Map.Entry<PassageCandidate, Float>> list = new ArrayList<Map.Entry<PassageCandidate, Float>>(
            M.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<PassageCandidate, Float>>() {
      @Override
      public int compare(java.util.Map.Entry<PassageCandidate, Float> o1,
              java.util.Map.Entry<PassageCandidate, Float> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });
    for (Map.Entry<PassageCandidate, Float> E : list) {
      result.add(E.getKey());
    }
    System.err.println("No. of passages returned for Question:" + question + " " + result.size());
    return result;
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {

    QueryExpansion qe = new QueryExpansion();
    List<Keyterm> expanded = qe.expand(keyterms);
    return htmlBasedExtraction(question, expanded, documents, this.wrapper);
  }

  @SuppressWarnings("unused")
  private void printQuesAnalysis(RetrievalResult document, String[] keyTermStrings,
          List<PassageCandidate> L) {
    try {
      String docId = document.getDocID();
      // System.out.println("Here " + document.getDocID());
      System.out.println("Inside");
      BufferedWriter writer = new BufferedWriter(new FileWriter(
              "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId
                      + "passages.txt"));

      BufferedWriter keytermWriter = new BufferedWriter(new FileWriter(
              "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId
                      + "keyterms.txt"));

      BufferedWriter documentWriter = new BufferedWriter(new FileWriter(
              "/usr0/home/pgadde/EclipseWorkspaces/SE/hw2Debug/AllPassages/" + docId + ".txt"));

      String htmlText = wrapper.getDocText(document.getDocID());
      int goldStart = 6440;
      int goldEnd = 6901;
      documentWriter.write(htmlText + "\n\n");
      documentWriter.close();

      for (String keyTerm : keyTermStrings) {
        keytermWriter.write(keyTerm + "\n");
      }
      keytermWriter.close();

      writer.write("Gold:\n");
      writer.write("-----------------------------------\n");
      writer.write(htmlText.substring(goldStart, goldEnd) + "\n");
      writer.write("-----------------------------------\n");

      writer.write("Mine:\n");
      for (PassageCandidate P : L) {
        writer.write("-----------------------------------\n");
        writer.write(P.toString() + "\n");
        writer.write(htmlText.substring(P.getStart(), P.getEnd()) + "\n");
        writer.write("-----------------------------------\n");
      }
      writer.close();
    } catch (Exception E) {
      E.printStackTrace();
      System.out.println("Not printed!!");
    }
  }

  private ArrayList<PhaniPassageSpan> getBasicHTMLSpans(String htmlText) {
    // System.out.println(htmlText);
    Pattern P = Pattern.compile("<P>([\\S\\s]*?)<P>");

    // Taking only first 5000
    // htmlText.substring(0, Math.min(1000, htmlText.length()));

    Matcher M = P.matcher(htmlText);
    ArrayList<PhaniPassageSpan> passageSpans = new ArrayList<PhaniPassageSpan>();
    // System.out.println("Outside");
    while (M.find()) {
      // System.out.println("Inside basic match");
      int begin = M.start(1);
      int end = M.end(1);
      String text = M.group(1);
      // int lastP = Math.max(
      // begin,
      // begin
      // + (text.lastIndexOf("<P>") > 0 ? text.lastIndexOf("<P>") + 3 : text
      // .lastIndexOf("<P>")));
      // System.out.println("last index:" + lastP);
      // System.out.println("\nText: "+text);
      // PhaniPassageSpan p = new PhaniPassageSpan(lastP, end, htmlText.substring(lastP, end));
      PhaniPassageSpan p = new PhaniPassageSpan(begin, end, text);
      passageSpans.add(p);
    }
    /*
     * List<String> L = Arrays.asList(htmlText.split("<P>")); //ArrayList<PhaniPassageSpan>
     * passageSpans = new ArrayList<PhaniPassageSpan>(); for (String str : L) { int begin =
     * htmlText.indexOf(str); int end = begin + str.length(); PhaniPassageSpan p = new
     * PhaniPassageSpan(begin, end, str); passageSpans.add(p); }
     */
    return passageSpans;

  }

  private ArrayList<Integer> getOverallMacthes(RetrievalResult document, String[] keyTermStrings)
          throws SolrServerException {
    HashSet<String> keyterms = new HashSet<String>();
    String text = wrapper.getDocText(document.getDocID());

    text = text.toLowerCase();
    // text = text.substring(0, Math.min(1000, text.length()));
    // text = Jsoup.parse(text).text().replaceAll("([\177-\377\0-\32]*)", "");

    ArrayList<Integer> Matches = new ArrayList<Integer>();
    int matches = 0;
    for (String keyTerm : keyTermStrings) {
      Pattern p = Pattern.compile(keyTerm);
      Matcher m = p.matcher(text);
      while (m.find()) {
        matches++;
        keyterms.add(keyTerm);
      }
    }
    Matches.add(matches);
    Matches.add(keyterms.size());
    Matches.add(text.length());
    return Matches;
  }

  private List<PassageCandidate> extractWithNybergScoring(String[] keyTermStrings,
          ArrayList<PhaniPassageSpan> basicTextSpans, RetrievalResult document)
          throws AnalysisEngineProcessException, SolrServerException {
    String docID = document.getDocID();
    List<PassageCandidate> L = new ArrayList<PassageCandidate>();
    ArrayList<Integer> overallMatches = getOverallMacthes(document, keyTermStrings);
    int totalMatches = overallMatches.get(0);
    int totalKeyterms = overallMatches.get(1);
    int totalTextLength = overallMatches.get(2);
    // KeytermWindowScorer scorer = new KeytermWindowScorerSum();
    for (PhaniPassageSpan textSpan : basicTextSpans) {
      int matches = 0;
      int keyterms = 0;

      int minStart = textSpan.end;
      int maxEnd = textSpan.begin;

      boolean flag = false;
      for (String keyTerm : keyTermStrings) {
        flag = false;
        Pattern p = Pattern.compile(keyTerm);

        String text = textSpan.text;
        text = text.toLowerCase();
        // text = Jsoup.parse(text).text().replaceAll("([\177-\377\0-\32]*)", "");

        Matcher m = p.matcher(text);
        int prevStart = 0;
        while (m.find()) {
          if (minStart > textSpan.begin + m.start())
            minStart = textSpan.begin + m.start();

          if (maxEnd < textSpan.begin + m.end())
            maxEnd = textSpan.begin + m.end();

          if (m.start() - prevStart > 50)
            minStart = textSpan.begin + m.start();

          prevStart = m.start();
          matches++;
          flag = true;
        }
        if (flag)
          keyterms++;
      }
      if (matches > 0) {

        int begin = Math.max(textSpan.begin, minStart);
        int end = (int) Math.min(textSpan.end, maxEnd);

        double score = scorer.scoreWindow(begin, end, matches, totalMatches, keyterms,
                totalKeyterms, totalTextLength);

        // double score = scorer.scoreWindow(textSpan.begin, textSpan.end, matches, totalMatches,
        // keyterms, totalKeyterms, totalTextLength);

        // PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end,
        // (float) (score * document.getProbability()), document.getQueryString());

        PassageCandidate pc = new PassageCandidate(docID, begin, end,
                (float) (score * document.getProbability()), document.getQueryString());

        // PassageCandidate pc = new PassageCandidate(docID, begin, end,
        // (float) (score), document.getQueryString());

        // PassageCandidate pc = new PassageCandidate(docID, textSpan.begin, textSpan.end,
        // (float) (score), document.getQueryString());

        L.add(pc);
      }
    }
    return L;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    // System.out.println( keytermMatchLimit + "," + passageSizeLimit + "," + maxPassages + "," +
    // score.format( valueBytes ) + "," + totalBytes + "," + score.format(
    // valueBytes/(float)totalBytes ) );
    wrapper.close();
  }
}
