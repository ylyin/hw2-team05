package edu.cmu.lti.oaqa.openqa.test.team05;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.QueryExpansion;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;

public class ParserKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * Lexical Parser
   */
  private static LexicalizedParser lp;

  /**
   * Stop words list
   */
  private static List<String> stoplist;

  @Override
  protected List<Keyterm> getKeyterms(String question) {

    if (lp == null)
      // lp = LexicalizedParser.getParserFromSerializedFile("model/englishPCFG.ser.gz");
      lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

    // Load stop list
    if (stoplist == null) {
      stoplist = new ArrayList<String>();
      File file = new File("stopwords.txt");
      // File file = new File("src/main/resources/stopwords.txt");
      Scanner scanner = null;
      try {
        scanner = new Scanner(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return null;
      }

      while (scanner.hasNextLine()) {
        stoplist.add(scanner.nextLine());
      }
    }

    // This option shows loading and using an explicit tokenizer
    TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
            new CoreLabelTokenFactory(), "");
    List<CoreLabel> rawWords2 = tokenizerFactory.getTokenizer(new StringReader(question))
            .tokenize();
    Tree parse = lp.apply(rawWords2);
    // parse.pennPrint();

    List<Keyterm> terms = new ArrayList<Keyterm>();
    String tmp = "";
    // Whether we should merge the next term
    boolean pick = false;
    // Keyterm iterator
    Iterator<Tree> iter = parse.iterator();
    while (iter.hasNext()) {
      String token = iter.next().value();
      if (pick) {
        if (token.startsWith("'")) { // Deal with A's situation.
          tmp = tmp.trim() + token + " ";
        } else {
          tmp += token + " ";
        }
        pick = false;
        continue;
      }

      if (token.equals("NN") || token.equals("NNP") || token.equals("JJ") || token.equals("NNS")
              || token.equals("CD") || token.equals("POS")) {
        pick = true;
      } else {
        if (!tmp.equals("")) {
          if (!stoplist.contains(tmp.trim())) {
            terms.add(new Keyterm(tmp.trim()));
          }
          tmp = "";
        }

        if (token.equals("VB")) {
          pick = true;
        }
      }
    }

    // After iteration
    if (!tmp.equals("") && !stoplist.contains(tmp.trim())) {
      terms.add(new Keyterm(tmp.trim()));
      tmp = "";
    }

    return terms;
  }

  public static void main(String[] argv) {
    ParserKeytermExtractor extractor = new ParserKeytermExtractor();
    List<Keyterm> terms = extractor
            .getKeyterms("How does nucleoside diphosphate kinase (NM23) contribute to tumor progression?");
    System.out.println(terms);
    QueryExpansion qe = new QueryExpansion();
    qe.expand(terms);
  }

}
