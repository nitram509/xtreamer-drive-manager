package de.bitkings.themoviedb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TheMovieDbSearchProviderTest {

  private TheMovieDbSearchProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new TheMovieDbSearchProvider();
  }

  @Test
  public void testGetMovieInfo() throws Exception {
    int movieId = 10036;
    String movieById = provider.getMovieById(Integer.toString(movieId)).toFormattedString();
    System.out.println(movieById);
  }
}