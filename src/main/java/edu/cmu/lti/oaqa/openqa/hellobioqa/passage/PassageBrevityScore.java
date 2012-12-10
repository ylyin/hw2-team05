package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;

public class PassageBrevityScore implements KeytermWindowScorer {
	@Override
	public double scoreWindow(int begin, int end, int matchesFound,
			int totalMatches, int keytermsFound, int totalKeyterms,
			int textSize) {
		int windowSize = end - begin;
		return 1 - ( (double)windowSize / (double)textSize );
	}
}
