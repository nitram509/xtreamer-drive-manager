package de.bitkings.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class BaseFolder {
	
	private String basePath = "";
	
	private File[] files;
	private final List<MovieFolder> movies = new ArrayList<MovieFolder>();

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void refreshBasePath() {
		final File base = new File(basePath);
		files = base.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				boolean isok = true;
				isok &= f.isFile();
				return isok;
			}
		});
		File[] folders = base.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				boolean isok = true;
				isok &= f.isDirectory();
				return isok;
			}
		});
		movies.clear();
		for (File f : folders) {
			MovieFolder mf = new MovieFolder(this, f.getName());
			movies.add(mf);
		}
	}
	
	/**
	 * @return
	 * @NotNull
	 */
	public File[] getFiles() {
		if (files == null) {
			return new File[0];
		}
		return files;
	}

	/**
	 * @return
	 * @NotNull
	 */
	public List<MovieFolder> getMovies() {
		return movies;
	}
}
