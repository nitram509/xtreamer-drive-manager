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
