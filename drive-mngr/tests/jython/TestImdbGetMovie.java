package jython;

import de.bitkings.ImdbContext;

public class TestImdbGetMovie {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImdbContext imdb = new ImdbContext();
		imdb.getMovieById("1480660");
	}

}
