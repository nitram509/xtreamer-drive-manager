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
import java.util.Comparator;

public class MovieFolder {

	private final BaseFolder parent;
	private final String pathname;
	private int imageCount = -1;

	public MovieFolder(BaseFolder parent, String pathname) {
		super();
		this.parent = parent;
		this.pathname = pathname;
	}

	public String getPathname() {
		return pathname;
	}

	public BaseFolder getParent() {
		return parent;
	}
	
	/**
	 * @return counts any .JPG .PNG or .GIF  (lazy and only once loaded)
	 */
	public int countImages() {
		if (imageCount < 0) {
			File f = new File(parent.getBasePath(), pathname);
			File[] images = f.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					boolean isok = true;
					isok &= pathname.isFile();
					String name = pathname.getName().toLowerCase();
					isok &= name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png"); 
					return isok;
				}
			});
			imageCount = images != null ? images.length : 0;
		}
		return imageCount;
	}
	
	/**
	 * @return
	 * @NotNull
	 */
	public File[] getFolders() {
		File f = new File(parent.getBasePath(), pathname);
		File[] folders = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				boolean isok = true;
				isok &= pathname.isDirectory();
				return isok;
			}
		});
		return folders == null ? new File[0] : folders;
	}
	
	/**
	 * @return
	 * @NotNull
	 */
	public File[] getFiles() {
		File f = new File(parent.getBasePath(), pathname);
		File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				boolean isok = true;
				isok &= pathname.isFile();
				return isok;
			}
		});
		return files == null ? new File[0] : files;
	}
	
	/**
	 * @return
	 */
	public static Comparator<MovieFolder> getComparatorByName() {
		return new Comparator<MovieFolder>() {
			@Override
			public int compare(MovieFolder o1, MovieFolder o2) {
				String s1 = o1.pathname.toLowerCase();
				String s2 = o2.pathname.toLowerCase();
				return s1.compareTo(s2);
			}
		};
	}
	
}
