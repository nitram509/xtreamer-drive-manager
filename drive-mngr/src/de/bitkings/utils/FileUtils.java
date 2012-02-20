package de.bitkings.utils;

import java.io.File;
import java.util.prefs.BackingStoreException;

import net.infotrek.util.WindowsRegistry;

public class FileUtils {

	private static final String PYTHON_RUNTIME = "python.exe";
	
	public static String findPythonRuntime() {
		// 1st Try - PATH environment variable
		String path = System.getenv("PATH");
		String[] paths = path.split("[" + File.pathSeparatorChar + "]");
		String result = null;
		for (String p : paths) {
			File f = new File(p, PYTHON_RUNTIME);
			if (f.exists() && f.isFile()) {
				result = f.getAbsolutePath();
				break;
			}
		}
		// 2nd Try - Windows Registry
		if (result == null) {
			try {
				String s = WindowsRegistry.getKeySz(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\Python.exe", "");
				File f = new File(s);
				if (f.exists() && f.isFile()) {
					result = s;
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * @param file
	 * @return TRUE if .JPG or .PNG or .GIF file
	 */
	public static boolean isImageFile(File file) {
		if (file == null) return false;
		String fname = file.getName().toLowerCase();
		if (fname.endsWith(".jpg")) return true;
		if (fname.endsWith(".png")) return true;
		if (fname.endsWith(".gif")) return true;
		return false;
	}
	
}
