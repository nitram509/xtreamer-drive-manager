package de.bitkings;

import de.bitkings.model.MovieDetails;
import de.bitkings.model.MovieInfo;

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
	public abstract MovieDetails getMovieById(String id);

	/**
	 * @param moviename
	 * @return
	 */
	public abstract List<MovieInfo> searchMovie(String moviename);

}