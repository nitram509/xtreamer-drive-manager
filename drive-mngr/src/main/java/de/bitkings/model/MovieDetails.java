package de.bitkings.model;

public class MovieDetails {

  static final String NEW_LINE = "\n";

  public static final String PREFIX_IMDB_URL = "IMDb URL:";
  public static final String PREFIX_COVER_URL = "Cover URL:";
  public static final String PREFIX_POSTER_URL = "Full size cover URL:";

  private String title;
  private String genre;
  private String cast;
  private String year;
  private String country;
  private String imdbUrl;
  private String overview;
  private String posterUrl;

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public String toFormattedString() {
    StringBuilder sb = new StringBuilder();
    if (title != null) sb.append("Title: ").append(title).append(NEW_LINE);
    if (genre != null) sb.append("Genre: ").append(genre).append(NEW_LINE);
    if (cast != null) sb.append("Cast: ").append(cast).append(NEW_LINE);
    if (year != null) sb.append("Year: ").append(year).append(NEW_LINE);
    if (country != null) sb.append("Country: ").append(country).append(NEW_LINE);
    if (imdbUrl != null) sb.append(PREFIX_IMDB_URL).append(imdbUrl).append(NEW_LINE);
    if (posterUrl != null) sb.append(PREFIX_POSTER_URL).append(posterUrl).append(NEW_LINE);
    if (overview != null) sb.append("Description: ").append(overview).append(NEW_LINE);
    return sb.toString();
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getGenre() {
    return genre;
  }

  public void setCast(String cast) {
    this.cast = cast;
  }

  public String getCast() {
    return cast;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getYear() {
    return year;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCountry() {
    return country;
  }

  public void setImdbUrl(String imdbUrl) {
    this.imdbUrl = imdbUrl;
  }

  public String getImdbUrl() {
    return imdbUrl;
  }

  public void setOverview(String description) {
    this.overview = description;
  }

  public String getOverview() {
    return overview;
  }

  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
  }

  public String getPosterUrl() {
    return posterUrl;
  }
}
