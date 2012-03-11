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
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import de.bitkings.model.MovieFolder;
import de.bitkings.utils.FileUtils;

public class ImdbFetcherDialog extends Dialog {

	private static final String NL = System.getProperty("line.separator");
	
	private static final String PREF_SEARCHMOVIE_PATHNAME = "python.search.movie.name";
	private static final String PREF_GETMOVIE_PATHNAME = "python.get.movie.name";
	
	private static final String PREFIX_IMDB_URL = "IMDb URL:";
	private static final String PREFIX_COVER_URL = "Cover URL:";
	private static final String PREFIX_FULL_COVER_URL = "Full size cover URL:";
	
	private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());
	private final Charset charset = Charset.forName(System.getProperty("file.encoding"));
	
	protected Object result;
	private MovieFolder model;
	private String query;
	
	protected Shell shell;
	private Text txt_PySearchMovie;
	private Text txt_ResultSearchMovie;
	private Button btn_ChooseFile;
	private Button btn_RunSearchScript;
	private Text txt_ParmMovie;
	private Text txt_PyGetMovie;
	private Button btn_ChooseGetMovie;
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
		txt_PySearchMovie.setText(preferences.get(PREF_SEARCHMOVIE_PATHNAME, "search_movie.py"));
		txt_PyGetMovie.setText(preferences.get(PREF_GETMOVIE_PATHNAME, "get_movie.py"));
		
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
		
		Label lblPyimdbPath = new Label(shell, SWT.NONE);
		lblPyimdbPath.setText("PyIMDB Path:");
		lblPyimdbPath.setEnabled(false); // not needed anymore
		
		txt_PySearchMovie = new Text(shell, SWT.BORDER);
		txt_PySearchMovie.setTextLimit(255);
		txt_PySearchMovie.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txt_PySearchMovie.setEnabled(false); // not needed anymore
		
		btn_ChooseFile = new Button(shell, SWT.NONE);
		btn_ChooseFile.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/folder_explore.png"));
		btn_ChooseFile.setEnabled(false); // not needed anymore
		
		btn_RunSearchScript = new Button(shell, SWT.NONE);
		btn_RunSearchScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionRunSearchMovieScript(txt_ParmMovie.getText().trim());
			}
		});
		btn_RunSearchScript.setText("Run");
		btn_RunSearchScript.setImage(SWTResourceManager.getImage(ManageDrives.class, "/application_go.png"));
		
		Label lblParmMovie = new Label(shell, SWT.NONE);
		lblParmMovie.setText("Parm#1 Movie:");
		
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
			public void modifyText(ModifyEvent e) {
				validatePySearchMovieFileName();
			}
		});
		txt_ParmMovie.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
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
		
		Label lblPyimdbPath2 = new Label(shell, SWT.NONE);
		lblPyimdbPath2.setText("PyIMDB Path:");
		
		txt_PyGetMovie = new Text(shell, SWT.BORDER);
		txt_PyGetMovie.setText("get_movie.py");
		txt_PyGetMovie.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btn_ChooseGetMovie = new Button(shell, SWT.NONE);
		btn_ChooseGetMovie.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionChooseGetMovieScript();
				validatePyGetMovieFileName();
			}
		});
		btn_ChooseGetMovie.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/folder_explore.png"));
		
		btn_RunGetMovieScript = new Button(shell, SWT.NONE);
		btn_RunGetMovieScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionRunGetMovieScript(txt_ParmId.getText().trim());
			}
		});
		btn_RunGetMovieScript.setText("Run");
		btn_RunGetMovieScript.setImage(SWTResourceManager.getImage(ImdbFetcherDialog.class, "/application_go.png"));
		
		Label lblParmId = new Label(shell, SWT.NONE);
		lblParmId.setText("Parm#1 ID:");
		
		txt_ParmId = new Text(shell, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		txt_ParmId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				actionRunGetMovieScript(txt_ParmId.getText().trim());
			}
		});
		txt_ParmId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePyGetMovieFileName();
			}
		});
		txt_ParmId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(shell, SWT.NONE);
		
		txt_ResultGetMovie = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_ResultGetMovie.addModifyListener(new ModifyListener() {
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
		File f = new File(txt_PySearchMovie.getText());
		boolean isok = true;
		isok &= f.exists();
		isok &= f.isFile();
		isok &= txt_ParmMovie.getText().trim().length() > 0;
		btn_RunSearchScript.setEnabled(isok);
		return isok;
	}
	
	/**
	 * @return TRUE if ok FALSE otherwise -> also enables/disables the buttons
	 */
	private boolean validatePyGetMovieFileName() {
		File f = new File(txt_PyGetMovie.getText());
		boolean isok = true;
		isok &= f.exists();
		isok &= f.isFile();
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
	
	private void actionChooseGetMovieScript() {
		FileDialog dialog = new FileDialog(shell);
		dialog.setFileName("get_movie.py");
		String fname = dialog.open();
		if (fname != null) {
			preferences.put(PREF_GETMOVIE_PATHNAME, fname);
			txt_PyGetMovie.setText(fname);
		}
	}
	
	/**
	 * @param moviename
	 */
	private void actionRunSearchMovieScript(String moviename) {
		txt_ResultSearchMovie.setText("Running script ...\n");
		System.setProperty(PySystemState.PYTHON_CACHEDIR, new File(System.getProperty("java.io.tmpdir"), "jythoncachedir").toString());
		PythonInterpreter interp = new PythonInterpreter();
		interp.exec("import sys");
		interp.exec("sys.argv = [sys.argv[0], "+PyString.encode_UnicodeEscape(moviename, true)+"]");
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("search_movie.py")) {
			interp.execfile(is);

			// after running, extract information from 'results' variable
			PyObject pyobj = interp.get("results");
			int len = pyobj.__len__();
			if (len == 0) {
				txt_ResultSearchMovie.append("Nothing found or error happend :-/" + NL);
			} else {
				txt_ResultSearchMovie.setText("");
				for (int i=0; i<len; i++) {
					PyObject movieobj = pyobj.__getitem__(i);
					interp.set("movie", movieobj);
					interp.exec("imdbid = i.get_imdbID(movie)");
					PyObject imdbid = interp.get("imdbid");
					PyObject movie_data = movieobj.__getattr__("data");
					String movtitle = movie_data.__finditem__("title").toString();
					String movyear = movie_data.__finditem__("year").toString();
					txt_ResultSearchMovie.append(imdbid.toString() + " : " + movtitle + " \t(" + movyear + ")" + NL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		txt_ResultSearchMovie.setSelection(0,0);
	}
	
	/**
	 * @param movieId
	 */
	private void actionRunGetMovieScript(String movieId) {
		txt_ResultGetMovie.setText("Running script ...\n");
		StringBuilder sb = new StringBuilder();
		try {
			File pyf = new File(txt_PyGetMovie.getText());
			ArrayList<String> cmds = new ArrayList<String>();
			cmds.add(FileUtils.findPythonRuntime());
			cmds.add(pyf.getAbsolutePath());
			cmds.add(movieId);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(cmds);
			pb.directory(pyf.getParentFile());
			Process process = pb.start();
			
			InputStream is = process.getInputStream();
			byte[] buf = new byte[1024];
			int read;
			while ( (read = is.read(buf)) >=0){
				sb.append(new String(buf, 0, read, charset));
			};
			is.close();
			
			// error reporting
			is = process.getErrorStream();
			while ( (read = is.read(buf)) >=0){
				System.err.println(new String(buf, 0, read, charset));
			};
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		txt_ResultGetMovie.append(sb.toString());
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
