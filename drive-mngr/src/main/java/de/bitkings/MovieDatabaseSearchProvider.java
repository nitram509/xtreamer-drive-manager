package de.bitkings;

import de.bitkings.model.ImdbMovieInfo;

import java.util.List;

public interface MovieDatabaseSearchProvider {

	/**
	 * @return The providers name
	 */
	public String getName();

	/**
	 * @param id
	 * @return
	 */
	public abstract String getMovieById(String id);

	/**
	 * @param moviename
	 * @return
	 */
	public abstract List<ImdbMovieInfo> searchMovie(String moviename);

}