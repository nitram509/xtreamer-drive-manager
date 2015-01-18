package de.bitkings.themoviedb;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.*;
import com.omertron.themoviedbapi.results.TmdbResultsList;
import de.bitkings.MovieDatabaseSearchProvider;
import de.bitkings.model.MovieDetails;
import de.bitkings.model.MovieInfo;

import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class TheMovieDbSearchProvider implements MovieDatabaseSearchProvider {

  static final String API_KEY;

  static {
    String apikey = System.getProperty("API_KEY");
    if (apikey == null) {
      apikey = System.getenv("API_KEY");
    }
    assert apikey != null;
    API_KEY = apikey;
  }

  public static final String LANGUAGE = "de";

  private final TheMovieDbApi tmdb;

  public TheMovieDbSearchProvider() {
    try {
      tmdb = new TheMovieDbApi(API_KEY);
    } catch (MovieDbException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getName() {
    return "themoviedb.org";
  }

  @Override
  public MovieDetails getMovieById(String id) {
    try {
      MovieDb movie = tmdb.getMovieInfo(parseInt(id), LANGUAGE);
      MovieDetails movieDetails = new MovieDetails();
      movieDetails.setTitle(movie.getTitle());
      movieDetails.setGenre(asStringList(movie.getGenres().stream().map(AbstractIdName::getName).collect(toList())));
      TmdbResultsList<Person> movieCasts = tmdb.getMovieCasts(parseInt(id), LANGUAGE);
      movieDetails.setCast(asStringList(movieCasts.getResults().stream().filter(person -> person.getPersonType() == PersonType.CAST).limit(15).map(Person::getName).collect(toList())));
      movieDetails.setYear(movie.getReleaseDate());
      movieDetails.setCountry(asStringList(movie.getProductionCountries().stream().map(ProductionCountry::getName).collect(toList())));
      MovieDb infoImdb = tmdb.getMovieInfoImdb(id, LANGUAGE);
      movieDetails.setImdb("http://www.imdb.com/title/" + infoImdb.getImdbID() + "/");
      movieDetails.setOverview(movie.getOverview());
      movieDetails.setPosterUrl(tmdb.getConfiguration().getBaseUrl() + "/original" + movie.getPosterPath());
      return movieDetails;
    } catch (MovieDbException e) {
      throw new RuntimeException(e);
    }
  }

  private String asStringList(List<String> strings) {
    StringBuilder sb = new StringBuilder();
    for (String s : strings) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(s);
    }
    return sb.toString();
  }

  @Override
  public List<MovieInfo> searchMovie(String moviename) {
    try {
      TmdbResultsList<MovieDb> movieDbTmdbResultsList = tmdb.searchMovie(moviename, 0, null, false, 0);
      return movieDbTmdbResultsList.getResults().stream()
          .map(movieDb -> {
                String id = "" + movieDb.getId();
                String title = movieDb.getTitle();
                String year = movieDb.getReleaseDate();
                return new MovieInfo(id, title, year);
              }
          ).collect(toList());
    } catch (MovieDbException e) {
      throw new RuntimeException(e);
    }
  }
}
