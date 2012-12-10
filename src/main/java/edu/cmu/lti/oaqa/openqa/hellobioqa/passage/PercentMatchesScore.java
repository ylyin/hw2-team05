package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;

public class PercentMatchesScore implements KeytermWindowScorer {
	@Override
	public double scoreWindow(int begin, int end, int matchesFound,
			int totalMatches, int keytermsFound, int totalKeyterms,
			int textSize) {
		return (double)matchesFound / (double)totalMatches;
	}
	
}
