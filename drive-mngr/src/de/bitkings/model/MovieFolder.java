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
