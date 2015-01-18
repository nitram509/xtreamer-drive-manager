/*
 * Copyright (c) 2012, Martin W. Kirst
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
