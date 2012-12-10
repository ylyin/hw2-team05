package edu.cmu.lti.oaqa.openqa.hellobioqa.passage;

public interface KeytermWindowScorer {
	public double scoreWindow ( int begin , int end , int matchesFound , int totalMatches , int keytermsFound , int totalKeyterms , int textSize );
		
}
