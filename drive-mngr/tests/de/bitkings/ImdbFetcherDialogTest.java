package de.bitkings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ImdbFetcherDialogTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testOpen() {
//		fail("Not yet implemented");
		ImdbFetcherDialog dlg = new ImdbFetcherDialog(new Shell(), SWT.NONE);
		dlg.open();
	}

}
