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

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import de.bitkings.model.ImdbMovieInfo;
import de.bitkings.model.MovieFolder;

public class ImdbFetcherDialog extends Dialog {

	private static final String NL = System.getProperty("line.separator");

	private static final String PREFIX_IMDB_URL = "IMDb URL:";
	private static final String PREFIX_COVER_URL = "Cover URL:";
	private static final String PREFIX_FULL_COVER_URL = "Full size cover URL:";

	private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());

	protected Object result;
	private MovieFolder model;
	private String query;

	protected Shell shell;
	private Text txt_ResultSearchMovie;
	private Button btn_RunSearchScript;
	private Text txt_ParmMovie;
	private Button btn_RunGetMovieScript;
	private Text txt_ParmId;
	private Text txt_ResultGetMovie;
	private Composite composite;
	private Button btn_saveFullCover;
	private Button btn_saveCover;
	private Button btn_openImdbURL;
	private Button cbo_saveInfo;


	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ImdbFetcherDialog(Shell parent, int style) {
		super(parent, style);
		setText("Run IMDB Scripts");
	}

	public void setModel(MovieFolder model) {
		this.model = model;
		String q = model.getPathname();
		if (q != null) {
			// filter all non digits and letters
			StringBuilder sb = new StringBuilder();
			boolean onespace = false; // only one space
			for (int i=0; i<q.length(); i++) {
				char c = q.charAt(i);
				if (Character.isLetterOrDigit(c)) {
					sb.append(c);
					onespace = false;
				} else {
					if (!onespace) sb.append(' ');
					onespace = true;
				}
			}
			this.query = sb.toString().trim();
		}
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		loadPreferences();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		savePreferences();
		return result;
	}

	private void loadPreferences() {

		if (query != null) txt_ParmMovie.setText(query);

		txt_ParmMovie.setFocus();
		int len = txt_ParmMovie.getText().length();
		txt_ParmMovie.setSelection(len, len);

		validatePyGetMovieFileName();
		validatePySearchMovieFileName();
	}

	private void savePreferences() {
		try {
			preferences.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setMinimumSize(new Point(320, 240));
		shell.setSize(800, 600);
		shell.setText(getText());
		shell.setImage(SWTResourceManager.getImage(ManageDrives.class, "/imdb.png"));
		GridLayout gl_shell = new GridLayout(4, false);
		gl_shell.marginBottom = 5;
		gl_shell.marginTop = 5;
		gl_shell.marginHeight = 0;
		shell.setLayout(gl_shell);

		Label lblParmMovie = new Label(shell, SWT.NONE);
		lblParmMovie.setText("Movie name:");

		txt_ParmMovie = new Text(shell, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		txt_ParmMovie.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (validatePySearchMovieFileName()) {
					actionRunSearchMovieScript(txt_ParmMovie.getText().trim());
				}
			}
		});
		txt_ParmMovie.setTextLimit(255);
		txt_ParmMovie.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePySearchMovieFileName();
			}
		});
		txt_ParmMovie.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		btn_RunSearchScript = new Button(shell, SWT.NONE);
		btn_RunSearchScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionRunSearchMovieScript(txt_ParmMovie.getText().trim());
			}
		});
		btn_RunSearchScript.setText("Search");
		btn_RunSearchScript.setImage(SWTResourceManager.getImage(ManageDrives.class, "/application_go.png"));

		txt_ResultSearchMovie = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_ResultSearchMovie.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point sel = txt_ResultSearchMovie.getSelection();
				txt_ParmId.setText(txt_ResultSearchMovie.getText().substring(sel.x, sel.y).trim());
			}
		});
		GridData gd_txt_ResultSearchMovie = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd_txt_ResultSearchMovie.heightHint = 100;
		txt_ResultSearchMovie.setLayoutData(gd_txt_ResultSearchMovie);

		Label label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_label.heightHint = 3;
		label.setLayoutData(gd_label);

		Label lblParmId = new Label(shell, SWT.NONE);
		lblParmId.setText("IMDB ID:");

		txt_ParmId = new Text(shell, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		txt_ParmId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				actionRunGetMovieScript(txt_ParmId.getText().trim());
			}
		});
		txt_ParmId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePyGetMovieFileName();
			}
		});
		txt_ParmId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		btn_RunGetMovieScript = new Button(shell, SWT.NONE);
		btn_RunGetMovieScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionRunGetMovieScript(txt_ParmId.getText().trim());
			}
		});
		btn_RunGetMovieScript.setText("Retrieve");
		btn_RunGetMovieScript.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/application_go.png"));

		txt_ResultGetMovie = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_ResultGetMovie.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateResultMovieDetails();
			}
		});
		GridData gd_txt_ResultGetMovie = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_txt_ResultGetMovie.heightHint = 100;
		txt_ResultGetMovie.setLayoutData(gd_txt_ResultGetMovie);

		composite = new Composite(shell, SWT.NONE);
		RowLayout rl_composite = new RowLayout(SWT.VERTICAL);
		rl_composite.fill = true;
		composite.setLayout(rl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		btn_openImdbURL = new Button(composite, SWT.NONE);
		btn_openImdbURL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionOpenImdbLinkInBrowser((String)e.widget.getData());
			}
		});
		btn_openImdbURL.setImage(SWTResourceManager.getImage(this.getClass(), "/world.png"));
		btn_openImdbURL.setText("Browse");
		btn_openImdbURL.setEnabled(false);

		btn_saveCover = new Button(composite, SWT.NONE);
		btn_saveCover.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionSaveCover((String)e.widget.getData());
			}
		});
		btn_saveCover.setEnabled(false);
		btn_saveCover.setAlignment(SWT.LEFT);
		btn_saveCover.setText("Cover");
		btn_saveCover.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/disk.png"));

		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);

		cbo_saveInfo = new Button(composite, SWT.CHECK);
		cbo_saveInfo.setSelection(true);
		cbo_saveInfo.setText("Save .nfo?");

		btn_saveFullCover = new Button(composite, SWT.NONE);
		btn_saveFullCover.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionSaveCover((String)e.widget.getData());
				if (cbo_saveInfo.getSelection()) {
					actionSaveInfo();
				}
			}
		});
		btn_saveFullCover.setEnabled(false);
		btn_saveFullCover.setText("Big Cover");
		btn_saveFullCover.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/disk.png"));
	}

	/**
	 * @return TRUE if ok FALSE otherwise -> also enables/disables the buttons
	 */
	private boolean validatePySearchMovieFileName() {
		boolean isok = true;
		isok &= txt_ParmMovie.getText().trim().length() > 0;
		btn_RunSearchScript.setEnabled(isok);
		return isok;
	}

	/**
	 * @return TRUE if ok FALSE otherwise -> also enables/disables the buttons
	 */
	private boolean validatePyGetMovieFileName() {
		boolean isok = true;
		isok &= txt_ParmId.getText().trim().length() > 0;
		btn_RunGetMovieScript.setEnabled(isok);
		return isok;
	}

	private void validateResultMovieDetails() {
		btn_saveCover.setEnabled(false);
		btn_saveFullCover.setEnabled(false);
		String[] lines = txt_ResultGetMovie.getText().split("\\n");
		for (String l : lines) {
			l=l.trim();
			if (l.startsWith(PREFIX_COVER_URL)) {
				int idx = l.indexOf("http://");
				if (idx >= 0) {
					String coverurl = l.trim().substring(idx);
					btn_saveCover.setEnabled(true);
					btn_saveCover.setData(coverurl);
				}
			}
			if (l.startsWith(PREFIX_FULL_COVER_URL)) {
				int idx = l.indexOf("http://");
				if (idx >= 0) {
					String coverurl = l.trim().substring(idx);
					btn_saveFullCover.setEnabled(true);
					btn_saveFullCover.setData(coverurl);
				}
			}
			if (l.startsWith(PREFIX_IMDB_URL)) {
				int idx = l.indexOf("http://");
				if (idx >= 0) {
					String coverurl = l.trim().substring(idx);
					btn_openImdbURL.setEnabled(true);
					btn_openImdbURL.setData(coverurl);
				}
			}
		}
	}

	/**
	 * @param moviename
	 */
	private void actionRunSearchMovieScript(String moviename) {
		txt_ResultSearchMovie.setText("Running script ...\n");
		List<ImdbMovieInfo> movies = null;
		ServiceLoader<MovieDatabaseSearchProvider> sl = ServiceLoader.load(MovieDatabaseSearchProvider.class);
		for (Iterator<MovieDatabaseSearchProvider> it = sl.iterator(); it.hasNext();) {
			MovieDatabaseSearchProvider provider = it.next();
			movies = provider.searchMovie(moviename);
		}

//		List<ImdbMovieInfo> movies = imdb.searchMovie(moviename);
		txt_ResultSearchMovie.setText("");
		for (ImdbMovieInfo movie : movies) { // FIXME:  NPE !!!
			txt_ResultSearchMovie.append(movie.id + " : " + movie.title + " \t(" + movie.year + ")" + NL);
		}
		txt_ResultSearchMovie.setSelection(0,0);
	}

	/**
	 * @param movieId
	 */
	private void actionRunGetMovieScript(String movieId) {
		txt_ResultGetMovie.setText("Running script ...\n");
//		txt_ResultGetMovie.setText(imdb.getMovieById(movieId));
		txt_ResultGetMovie.setSelection(0,0);
	}

	/**
	 * @param file
	 * @see {@link Desktop#open(File)}
	 */
	private void openFileWithDefaultApplication(File file) {
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param urlstr
	 */
	private void actionSaveCover(String urlstr) {
		try {
			URL url = new URL(urlstr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			InputStream is = con.getInputStream();

			File file = new File(new File(model.getParent().getBasePath(), model.getPathname()), model.getPathname() + ".jpg");
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			int read;
			byte[] buf = new byte[4096];
			while ((read = is.read(buf)) > 0) {
				os.write(buf, 0, read);
			}
			is.close();
			os.close();

			openFileWithDefaultApplication(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param urlstr
	 */
	private void actionOpenImdbLinkInBrowser(String urlstr) {
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(new URI(urlstr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void actionSaveInfo() {
		try {
			File file = new File(new File(model.getParent().getBasePath(), model.getPathname()), model.getPathname() + ".nfo");
			if (file.exists()) {
				file = new File(new File(model.getParent().getBasePath(), model.getPathname()), model.getPathname() + "2.nfo");
			}
			FileWriter fw = new FileWriter(file);
			fw.write(txt_ResultGetMovie.getText());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
