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

package jython;

import java.io.File;
import java.io.InputStream;

import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class TestRunSearchMovie {

	public static void main(String[] args) {
		TestRunSearchMovie pgm = new TestRunSearchMovie();
		try {
			pgm.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {
		System.setProperty(PySystemState.PYTHON_CACHEDIR, new File(System.getProperty("java.io.tmpdir"), "jythoncachedir").toString());
		PythonInterpreter interp = new PythonInterpreter();
		interp.exec("import sys");
		interp.exec("sys.argv = [sys.argv[0], 'gritters']");
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("search_movie.py");
		interp.execfile(is);
		PyObject pyobj = interp.get("results");
		System.out.println(pyobj);
		is.close();
	}

}
