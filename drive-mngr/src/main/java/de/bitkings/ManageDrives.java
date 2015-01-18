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
import java.io.File;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import de.bitkings.model.BaseFolder;
import de.bitkings.model.MovieFolder;
import de.bitkings.utils.FileUtils;
import de.bitkings.utils.ImageUtils;

public class ManageDrives {

	private static final String PREF_BASE_FOLDER = "base.folder";

	protected Shell shell;

	private final Preferences preferences = Preferences.userNodeForPackage(this.getClass());

	private final BaseFolder model = new BaseFolder();

	private Text txt_chooseFolder;
	private Button btn_refresh;
	private Table tab_folders;
	private SashForm sash_folders_files;
	private Table tab_details;
	private Button btn_Imdb;


	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ManageDrives window = new ManageDrives();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		loadPreferences();
		createContents();
		fillData();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		savePreferences();
		SWTResourceManager.dispose();
	}

	private void savePreferences() {
		try {
			preferences.put(PREF_BASE_FOLDER, model.getBasePath());
			preferences.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadPreferences() {
		model.setBasePath(preferences.get(PREF_BASE_FOLDER, ""));
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setMinimumSize(new Point(800, 500));
		shell.setSize(450, 300);
		shell.setText("Xtreamer Drive Manager");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setImage(SWTResourceManager.getImage(ManageDrives.class, "/xtreamer.png"));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		final Composite composite_basefolder = new Composite(composite, SWT.NONE);
		composite_basefolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		composite_basefolder.setLayout(new GridLayout(4, false));

		Label lbl_folder = new Label(composite_basefolder, SWT.NONE);
		lbl_folder.setText("Folder:");

		txt_chooseFolder = new Text(composite_basefolder, SWT.BORDER);
		txt_chooseFolder.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				File f = new File(txt_chooseFolder.getText());
				boolean isok = f.exists() && f.isDirectory();
				btn_refresh.setEnabled(isok);
			}
		});
		txt_chooseFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btn_choosFolder = new Button(composite_basefolder, SWT.NONE);
		btn_choosFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionChooseFolder();
			}
		});
		btn_choosFolder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btn_choosFolder.setImage(SWTResourceManager.getImage(ManageDrives.class, "/folder.png"));

		btn_refresh = new Button(composite_basefolder, SWT.NONE);
		btn_refresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionRefreshModel();
			}
		});
		btn_refresh.setImage(SWTResourceManager.getImage(ManageDrives.class, "/arrow_refresh.png"));

		sash_folders_files = new SashForm(composite, SWT.NONE);
		sash_folders_files.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tab_folders = new Table(sash_folders_files, SWT.BORDER | SWT.FULL_SELECTION);
		tab_folders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem ti = (TableItem) e.item;
				MovieFolder mf = (MovieFolder) ti.getData();
				refreshTableDetails(mf);
				btn_Imdb.setEnabled(true);
				btn_Imdb.setData(mf);
			}
		});
		tab_folders.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Table ta = (Table) e.widget;
				if (ta.getSelection().length > 0) {
					MovieFolder mf = (MovieFolder) ta.getSelection()[0].getData();
					txt_chooseFolder.setText(new File(mf.getParent().getBasePath(), mf.getPathname()).toString());
				}
			}
		});
		tab_folders.setHeaderVisible(true);
		tab_folders.setLinesVisible(true);

		final FoldersSortListener foldersSortListener = new FoldersSortListener();
		{
			TableColumn column = new TableColumn(tab_folders, SWT.LEFT);
			column.setWidth(23);
			column.setResizable(false);
			column.addListener(SWT.Selection, foldersSortListener);
		}
		{
			TableColumn column = new TableColumn(tab_folders, SWT.LEFT);
			column.setText("Name");
			column.setResizable(true);
			column.setWidth(250);
			column.addListener(SWT.Selection, foldersSortListener);
		}
		{
			TableColumn column = new TableColumn(tab_folders, SWT.RIGHT);
			column.setText("img");
			column.setResizable(true);
			column.setWidth(25);
			column.addListener(SWT.Selection, foldersSortListener);
		}

		Composite composite_action_details = new Composite(sash_folders_files, SWT.NONE);
		GridLayout gl_composite_action_details = new GridLayout(1, false);
		gl_composite_action_details.marginWidth = 0;
		gl_composite_action_details.marginHeight = 0;
		composite_action_details.setLayout(gl_composite_action_details);
		sash_folders_files.setWeights(new int[] {1, 1});

		Composite composite_actionbuttons = new Composite(composite_action_details, SWT.NONE);
		composite_actionbuttons.setBounds(0, 0, 64, 64);
		composite_actionbuttons.setLayout(new FillLayout(SWT.HORIZONTAL));

		btn_Imdb = new Button(composite_actionbuttons, SWT.NONE);
		btn_Imdb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MovieFolder mf = (MovieFolder) e.widget.getData();
				loadDetailsFromImdb(mf);
			}
		});
		btn_Imdb.setText("IMDB");
		btn_Imdb.setImage(SWTResourceManager.getImage(ManageDrives.class, "/imdb.png"));
		btn_Imdb.setEnabled(false);

		tab_details = new Table(composite_action_details, SWT.BORDER | SWT.FULL_SELECTION);
		tab_details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tab_details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File f = (File) e.item.getData();
				openFileWithDefaultApplication(f);
			}
		});
		tab_details.setHeaderVisible(true);
		// -----------
		{
			TableColumn column = new TableColumn(tab_details, SWT.LEFT);
			column.setWidth(64);
			column.setResizable(false);
		}
		{
			TableColumn column = new TableColumn(tab_details, SWT.LEFT);
			column.setText("Name");
			column.setResizable(true);
			column.setWidth(200);
		}
		{
			TableColumn column = new TableColumn(tab_details, SWT.RIGHT);
			column.setText("Size");
			column.setResizable(true);
			column.setWidth(70);
		}
		{
			TableColumn column = new TableColumn(tab_details, SWT.LEFT);
			column.setText("Details");
			column.setResizable(true);
			column.setWidth(200);
		}
	}

	/**
	 *
	 */
	private void fillData() {
		txt_chooseFolder.setText(model.getBasePath());
	}

	private void actionChooseFolder() {
		DirectoryDialog dialog = new DirectoryDialog (shell);
		File f = new File(txt_chooseFolder.getText());
		boolean isok = f.exists() && f.isDirectory();
		if (isok) dialog.setFilterPath(txt_chooseFolder.getText());
		String fldr = dialog.open();
		if (fldr != null) {
			txt_chooseFolder.setText(fldr);
			model.setBasePath(fldr);
			model.refreshBasePath();
		}
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
	 *
	 */
	private void actionRefreshModel() {
		model.setBasePath(txt_chooseFolder.getText());
		model.refreshBasePath();
		refreshTableFolders();
	}

	private void refreshTableFolders() {
		if (tab_folders.getItemCount() > 0) {
			tab_folders.removeAll();
		}
		for (MovieFolder mf : model.getMovies()) {
			final TableItem ti = new TableItem(tab_folders, SWT.NONE);
			ti.setData(mf);
			ti.setText(1, mf.getPathname());
			ti.setText(2, Integer.toString(mf.countImages()));
			if (mf.countImages() > 0) {
				ti.setImage(SWTResourceManager.getImage(ManageDrives.class, "/accept.png"));
			} else {
				ti.setImage(SWTResourceManager.getImage(ManageDrives.class, "/folder.png"));
			}
		}
	}

	/**
	 * @param movieFolder
	 */
	private void loadDetailsFromImdb(MovieFolder movieFolder) {
		if (movieFolder == null) return;
		ImdbFetcherDialog dlg = new ImdbFetcherDialog(this.shell, SWT.NONE);
		dlg.setModel(movieFolder);
		dlg.open();
	}

	/**
	 * @param movieFolder
	 */
	private void refreshTableDetails(MovieFolder movieFolder) {
		if (movieFolder == null) {
			tab_details.removeAll();
		} else {
			if (tab_details.getItemCount() > 0) tab_details.removeAll();
			for (File f : movieFolder.getFolders()) {
				TableItem ti = new TableItem(tab_details, SWT.NONE);
				ti.setImage(0, SWTResourceManager.getImage(ManageDrives.class, "/folder.png"));
				ti.setText(1, f.getName());
			}
			for (File f : movieFolder.getFiles()) {
				TableItem ti = new TableItem(tab_details, SWT.NONE);
				ti.setData(f);
				if (FileUtils.isImageFile(f)) {
					Image img = SWTResourceManager.getImage(f.getAbsolutePath());
					ti.setImage(img);
					ImageData imgdata = img.getImageData();
					ti.setText(3, "Size: " + imgdata.width + " x " + imgdata.height + " x " + imgdata.depth + "bpp");
				} else {
					ti.setImage(0, ImageUtils.getByFileName(f.getName()));
				}
				ti.setText(1, f.getName());
				ti.setText(2, "" + String.format("%,d", f.length() / 1024L) + "k");
			}
		}
	}

	private final class FoldersSortListener implements Listener {
		@Override
		public void handleEvent(Event e) {
			//FIXME: not yet supported

//				// determine new sort column and direction
//				TableColumn sortColumn = tab_folders.getSortColumn();
//				TableColumn currentColumn = (TableColumn) e.widget;
//				int dir = tab_folders.getSortDirection();
//				if (sortColumn == currentColumn) {
//					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
//				} else {
//					tab_folders.setSortColumn(currentColumn);
//					dir = SWT.UP;
//				}
//				// sort the data based on column and direction
//				final int index = tab_folders.getC
//				final int direction = dir;
//
//				Collections.sort(model.getMovies(), MovieFolder.getComparatorByName());
//				// update data displayed in table
//				tab_folders.setSortDirection(dir);
////				tab_folders.clearAll();
//				refreshTableFolders();
		}
	}
}
