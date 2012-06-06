/*
 GotoFile Eclipse Plugin - Quicksearch for files in Eclipse IDE
 Copyright (C) 2004 Max Muermann

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.muermann.gotofile.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.muermann.gotofile.GotoFileE30Plugin;
import org.muermann.gotofile.SearchResult;
import org.muermann.gotofile.actions.GotoFileAction;
import org.muermann.gotofile.preferences.GotoFilePreferencePage;

/**
 * The search window
 * 
 * @author max
 * @version 0.1
 */
public class SearchWindow extends SelectionDialog
{
    /**
     * @param parentShell
     */
    protected SearchWindow(Shell parentShell)
    {
        super(parentShell);
    }

    Rectangle oldBounds = null;

    private java.util.List resultList;

    private String value = null;

    private boolean compact = true;

    private boolean focus = true;
    
    private String selection;

    Text pattern;

    Table resourceNames;

    Table folderNames;

    String patternString;

    boolean gatherResourcesDynamically;

    WorkbenchLabelProvider labelProvider;

    int typeMask;

    ResourceDescriptor descriptors[];

    int descriptorsSize;

    Composite dialogArea;

    //UpdateFilterThread updateFilterThread;
    //UpdateGatherThread updateGatherThread;

    private GotoFileAction action;

    int returnCode = 1;
    
    private Button searchInProject;

    private boolean isSearchInProject = false;

    /*
     * public SearchWindow(Shell parentShell, IResource resources[]) {
     * super(parentShell); gatherResourcesDynamically = true; labelProvider =
     * new WorkbenchLabelProvider(); setShellStyle(getShellStyle() | 0x10);
     * gatherResourcesDynamically = false; initDescriptors(resources); }
     */

    public SearchWindow(Shell parentShell, int typeMask, GotoFileAction action)
    {
        super(parentShell);
        gatherResourcesDynamically = true;
        labelProvider = new WorkbenchLabelProvider();
        this.typeMask = typeMask;
        setShellStyle(getShellStyle() | 0x10);
        this.action = action;
    }

    protected Control createDialogArea(Composite parent)
    {
        dialogArea = (Composite) super.createDialogArea(parent);
        getShell().setText("Goto File 1.3.5 + 1");
        GridData data = new GridData(768);
        Label l = new Label(dialogArea, 0);
        l
                .setText("Pattern (any sequence of characters, all CAPS forces CamelCase matching):");
        data = new GridData(768);
        l.setLayoutData(data);

        pattern = new Text(dialogArea, 2052);
        pattern.setLayoutData(new GridData(768));

        l = new Label(dialogArea, 0);
        l.setText("Matching Resources");
        data = new GridData(768);
        l.setLayoutData(data);
        resourceNames = new Table(dialogArea, 2564);
        String pref = GotoFileE30Plugin.getDefault().getPreferenceStore()
                .getString(GotoFilePreferencePage.P_DISPLAYRESULTS);
        compact = !"tabular".equals(pref);
        focus = !GotoFileE30Plugin.getDefault().getPreferenceStore().getBoolean(
                GotoFilePreferencePage.P_FOCUSINNAVIGATOR);
        if (!compact)
        {
            TableColumn col1 = new TableColumn(resourceNames, SWT.NONE);
            col1.setResizable(true);
            col1.setText("Resource");
            col1.setWidth(300);
            TableColumn col2 = new TableColumn(resourceNames, SWT.NONE);
            col2.setResizable(true);
            col2.setText("Project");
            col2.setWidth(300);
            TableColumn col3 = new TableColumn(resourceNames, SWT.NONE);
            col3.setResizable(true);
            col3.setText("Path");
            col3.setWidth(300);
            resourceNames.setHeaderVisible(true);
        }

        data = new GridData(1808);
        data.heightHint = 12 * resourceNames.getItemHeight();
        resourceNames.setLayoutData(data);
        /*
         * l = new Label(dialogArea, 0);
         * l.setText(WorkbenchMessages.getString("ResourceSelectionDialog.folders"));
         * data = new GridData(768); l.setLayoutData(data);
         */
        /*
         * folderNames = new Table(dialogArea, 2820); data = new GridData(1808);
         * data.widthHint = 300; data.heightHint = 4 *
         * folderNames.getItemHeight(); folderNames.setLayoutData(data);
         */

        resourceNames.addMouseListener(new MouseAdapter()
        {
            public void mouseDoubleClick(MouseEvent e)
            {
                returnCode = 0;
                value = pattern.getText();
                okPressed();
                //close();
            }

            public void mouseDown(MouseEvent e)
            {
            }

            public void mouseUp(MouseEvent e)
            {
            }
        });

        pattern.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == '\r')
                {
                    returnCode = 0;
                    value = pattern.getText();
                    okPressed();
                    //close();
                } else if (e.keyCode == SWT.ARROW_UP)
                {
                    resourceNames.select(resourceNames.getSelectionIndex() - 1);
                    resourceNames.showSelection();
                } else if (e.keyCode == SWT.ARROW_DOWN)
                {
                    resourceNames.select(resourceNames.getSelectionIndex() + 1);
                    resourceNames.showSelection();
                } else if (e.keyCode == 0x1000002)
                    resourceNames.setFocus();
            }

            public void keyReleased(KeyEvent e)
            {
                {
                    if (!pattern.getText().equals(value))
                    {
                        runSearch(pattern.getText());
                    }
                }
            }

        });

        resourceNames.addSelectionListener(new SelectionAdapter()
        {

            public void widgetSelected(SelectionEvent e)
            {
                //updateFolders((SearchResult)e.item.getData());
                //updateFolders((ResourceDescriptor)e.item.getData());
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                okPressed();
            }

        });

        /*
         * folderNames.addSelectionListener(new SelectionAdapter() {
         * 
         * public void widgetDefaultSelected(SelectionEvent e) { okPressed(); }
         * 
         * });
         */
        Dialog.applyDialogFont(dialogArea);
        
        if (null != selection && !selection.equals(value))
        {
        	value = selection;
        	if (null != pattern && !pattern.isDisposed())
        	{
        		pattern.setText(value);
        		pattern.selectAll();
        	}
            runSearch(value);
        } else if (null != value)
        {
            pattern.setText(value);
            pattern.selectAll();
        }

        if (null != resultList)
        {
            setResultList(resultList);
        }
        
        // TODO: add project scope control
        searchInProject = new Button( dialogArea, SWT.CHECK );
        searchInProject.setText("Search in current &Project");
        
        searchInProject.addSelectionListener( new SelectionListener () {

            public void widgetSelected(SelectionEvent e)
            {
                isSearchInProject = searchInProject.getSelection();
                pattern.setFocus();
                runSearch(value);
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub
            }});
        
        searchInProject.setSelection( isSearchInProject );
        searchInProject.setEnabled( null != getEditorProject() );

        return dialogArea;
    }

    public String getValue()
    {
        return value;
    }

    public int getReturnCode()
    {
        return returnCode;
    }

    /**
     * @param action
     */
    public void setAction(GotoFileAction action)
    {
        this.action = action;
    }

    /**
     * @param list
     */
    public void setResultList(java.util.List list)
    {
        resultList = list;
        /*
         * if (list.isEmpty()) { // no results - hide list window
         * resourceNames.setVisible(false); Rectangle r = shell.getBounds();
         * r.height = 2 * ((GridLayout)shell.getLayout()).marginHeight +
         * input.getBounds().height; shell.setBounds(r); return; }
         */

        // results - populate list and show
        resourceNames.removeAll();

        for (Iterator it = list.iterator(); it.hasNext();)
        {
            SearchResult res = (SearchResult) it.next();

            TableItem ti = new TableItem(resourceNames, SWT.NONE);
            if (compact)
            {
                ti.setText(res.getFile().getName() + " ("
                        + res.getFile().getProject().getName() + "/"
                        + res.getFile().getProjectRelativePath().toString()
                        + ")");
            } else
            {
                ti.setText(new String[] { res.getFile().getName(),
                        res.getFile().getProject().getName(),
                        res.getFile().getProjectRelativePath().toString() });
            }
            ti.setData(res);
        }

        resourceNames.setSelection(0);
    }

    private void openSelected()
    {
        IFile file = ((SearchResult) resultList.get(resourceNames
                .getSelectionIndex())).getFile();
        try
        {
            // open the file
            // @Eclipse2
            // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(file);
            //@Eclipse3
            //IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
            // file);

            if (file == null)
                return;

            // select the file
            org.eclipse.ui.IViewPart view = null;
            if (focus)
            {
                if (file.getName().toUpperCase().endsWith(".JAVA")
                        && GotoFileE30Plugin
                                .getDefault()
                                .getPreferenceStore()
                                .getBoolean(
                                        GotoFilePreferencePage.P_PACKAGEEXPLORER))
                {
                    view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView(
                                    "org.eclipse.jdt.ui.PackageExplorer");
                } else
                {
                    view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().showView(
                                    "org.eclipse.ui.views.ResourceNavigator");
                }

                if (null != view && view instanceof ISetSelectionTarget)
                {
                    org.eclipse.jface.viewers.ISelection selection = new StructuredSelection(
                            file);
                    ((ISetSelectionTarget) view).selectReveal(selection);
                }
            }

            try
            {
                // note that this mechanism of opening editors is now deprecated
                // in E3,
                // however E3 still maintains binary compatibility with the
                // openEditor methods,
                // so the E2 version of the plugin will still work in E3
                IWorkbenchPage page = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage();
                if (page != null)
                    org.eclipse.ui.ide.IDE.openEditor(page, file);
                //page.openEditor(new FileEditorInput(file), null, true);
                //page.openEditor(file);
            } catch (CoreException x)
            {
                String title = "Error opening Editor"; //$NON-NLS-1$
                String message = "Could not open Editor"; //$NON-NLS-1$
                //WorkbenchPlugin.log(title, x.getStatus());
                ErrorDialog.openError(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell(), title, message,
                        x.getStatus());
            }

        } catch (PartInitException e1)
        {
            e1.printStackTrace();
        }
    }

    private void runSearch(String search)
    {
    	value = search;
        
    	// kick off search by notifying listener
        if (null != value && value.length() != 0)
        {
            Runnable r = new Runnable()
            {
                public void run()
                {
                    java.util.List results = action.runSearch(value);
                    setResultList(results);
                }
            };
            r.run();
        }
    }

    /*
     * ---------------------------------------- Inner classes
     * ----------------------------------------
     */

    static class ResourceDescriptor implements Comparable
    {

        public int compareTo(Object o)
        {
            return 0;
            //SearchWindow.collator.compare(label,
            // ((ResourceDescriptor)o).label);
        }

        String label;

        ArrayList resources;

        boolean resourcesSorted;

        ResourceDescriptor()
        {
            resources = new ArrayList();
            resourcesSorted = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        returnCode = 0;
        value = pattern.getText();
        openSelected();
        super.okPressed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close()
    {
        oldBounds = getShell().getBounds();
        value = pattern.getText();
        selection = null;
        return super.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#initializeBounds()
     */
    protected void initializeBounds()
    {
        super.initializeBounds();
        if (null != oldBounds)
        {
            getShell().setBounds(oldBounds);
        }
    }
    
    public boolean isSearchInProject()
    {
        /*if (null == searchInProject || !searchInProject.isEnabled())
            return false;
        return searchInProject.getSelection();*/
    	return isSearchInProject;
    }

    /**
     * @return Returns the searchInProject.
     */
    public Button getSearchInProject()
    {
        return searchInProject;
    }
    
    
    public IProject getEditorProject() {

        IWorkbenchPart activePart= GotoFileE30Plugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (activePart instanceof IEditorPart) {
            IEditorPart editor= (IEditorPart) activePart;
            IEditorInput input= editor.getEditorInput();
            if (input instanceof IFileEditorInput) {
                return ((IFileEditorInput)input).getFile().getProject();
            }
        }
        return null;
    }

	public void setSelection(String text)
	{
		this.selection = text;
	}   
    
}