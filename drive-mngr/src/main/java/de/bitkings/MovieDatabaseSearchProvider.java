package de.bitkings;

import java.util.List;

import de.bitkings.model.ImdbMovieInfo;

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