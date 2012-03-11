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
