package de.bitkings.utils;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

import de.bitkings.ManageDrives;

public class ImageUtils {

	/**
	 * @param fname
	 * @return
	 */
	public final static Image getByFileName(String fname) {
		if (fname == null) fname = "";
		fname = fname.toLowerCase();
		if (fname.endsWith(".jpg")) return SWTResourceManager.getImage(ManageDrives.class, "/picture.png");
		if (fname.endsWith(".png")) return SWTResourceManager.getImage(ManageDrives.class, "/picture.png");
		if (fname.endsWith(".gif")) return SWTResourceManager.getImage(ManageDrives.class, "/picture.png");
		
		if (fname.endsWith(".avi")) return SWTResourceManager.getImage(ManageDrives.class, "/film.png");
		if (fname.endsWith(".mkv")) return SWTResourceManager.getImage(ManageDrives.class, "/film.png");
		if (fname.endsWith(".ogm")) return SWTResourceManager.getImage(ManageDrives.class, "/film.png");
		if (fname.endsWith(".mpg")) return SWTResourceManager.getImage(ManageDrives.class, "/film.png");
		
		if (fname.endsWith(".txt")) return SWTResourceManager.getImage(ManageDrives.class, "/page_white_text.png");
		if (fname.endsWith(".nfo")) return SWTResourceManager.getImage(ManageDrives.class, "/page_white_text.png");
		
		return SWTResourceManager.getImage(ManageDrives.class, "/page_white.png");
	}
	
}
