package de.bitkings.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FileUtilsTest {

	@Test
	public void testFindPythonRuntime() {
		String pyfn = FileUtils.findPythonRuntime();
		assertNotNull(pyfn);
		assertTrue(new File(pyfn).exists());
		assertTrue(new File(pyfn).isFile());
	}

}
