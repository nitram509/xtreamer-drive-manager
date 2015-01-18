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

package de.bitkings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import de.bitkings.model.ImdbMovieInfo;

public class ImdbContext implements MovieDatabaseSearchProvider {

	private static final String NL = System.getProperty("line.separator");

	private PythonInterpreter interpreter;

	@Override
	public String getName() {
		return "IMDBpy (slow)";
	}

	/* (non-Javadoc)
	 * @see de.bitkings.MovieDatabaseSearchProvider#getMovieById(java.lang.String)
	 */
	@Override
	public String getMovieById(String id) {
		interpreter = getPythonInterpreter();
		interpreter.exec("import sys");
		interpreter.exec("sys.argv = [sys.argv[0], "+PyString.encode_UnicodeEscape(id, true)+"]");
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("get_movie.py")) {
			interpreter.execfile(is);
			PyObject movieobj = interpreter.get("movie");
			interpreter.exec("imdbid = i.get_imdbURL(movie)");
			PyObject imdbURL = interpreter.get("imdbid");
			PyObject coverurl = movieobj.__findattr__("get").__call__(new PyString("cover url"));
			PyObject fullsizecoverurl = movieobj.__findattr__("get").__call__(new PyString("full-size cover url"));
			String s = "";
			s += "IMDb URL: " +imdbURL.toString() + NL;
			s += "Cover URL: " + coverurl.toString() + NL;
			s += "Full size cover URL: " + fullsizecoverurl.toString() + NL ;
			String summary = movieobj.__getattr__("summary").__call__().toString();
			return s +summary;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see de.bitkings.MovieDatabaseSearchProvider#searchMovie(java.lang.String)
	 */
	@Override
	public List<ImdbMovieInfo> searchMovie(String moviename) {
		interpreter = getPythonInterpreter();

		List<ImdbMovieInfo> movlist = new ArrayList<ImdbMovieInfo>();

		interpreter.exec("import sys");
		interpreter.exec("sys.argv = [sys.argv[0], "+PyString.encode_UnicodeEscape(moviename, true)+"]");
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("search_movie.py")) {
			interpreter.execfile(is);

			// after running, extract information from 'results' variable
			PyObject pyobj = interpreter.get("results");
			int len = pyobj.__len__();
			if (len == 0) {
//				txt_ResultSearchMovie.append("Nothing found or error happend :-/" + NL + NL);
//				txt_ResultSearchMovie.append("Output log file " + outf + NL);
//				txt_ResultSearchMovie.append("Error log file " + errf + NL);
			} else {
//				txt_ResultSearchMovie.setText("");
				for (int i=0; i<len; i++) {
					PyObject movieobj = pyobj.__getitem__(i);
					interpreter.set("movie", movieobj);
					interpreter.exec("imdbid = i.get_imdbID(movie)");
					PyObject imdbid = interpreter.get("imdbid");
					PyObject movie_data = movieobj.__getattr__("data");
					String movtitle = movie_data.__finditem__("title").toString();
					String movyear = movie_data.__finditem__("year").toString();
					movlist.add(new ImdbMovieInfo(imdbid.toString(), movtitle, movyear));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return movlist;
	}

	private PythonInterpreter getPythonInterpreter() {
		if (interpreter == null) {
			System.setProperty(PySystemState.PYTHON_CACHEDIR, new File(System.getProperty("java.io.tmpdir"), "jythoncachedir").toString());
			this.interpreter = new PythonInterpreter();
			File outf, errf;
			try {
				outf = File.createTempFile("search_imdb_py_LOG_OUT", ".txt");
				errf = File.createTempFile("search_imdb_py_LOG_ERR", ".txt");
				interpreter.setOut(new BufferedOutputStream(new FileOutputStream(outf)));
				interpreter.setErr(new BufferedOutputStream(new FileOutputStream(errf)));
			} catch (Exception exc) {
				throw new RuntimeException("Error configuring PythonInterpreter.", exc);
			}
		}
		return interpreter;
	}

}
