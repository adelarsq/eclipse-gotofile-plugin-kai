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
package org.muermann.gotofile.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.muermann.gotofile.FilenameExclusionFilter;
import org.muermann.gotofile.GotoFileE30Plugin;
import org.muermann.gotofile.MatchComparatorFuzzy;
import org.muermann.gotofile.PathExclusionFilter;
import org.muermann.gotofile.SearchResult;
import org.muermann.gotofile.ui.SearchWindow;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class GotoFileAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener
{
	IStructuredSelection selection;

	private SearchWindow dlg;

	private PathExclusionFilter pathFilter = new PathExclusionFilter();

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window)
	{
		GotoFileE30Plugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action)
	{
		if (null == dlg)
		{
			initDlg();
		}
		
		// get current selectino
    	ISelection sel = GotoFileE30Plugin.getActiveWorkbenchWindow().getSelectionService().getSelection();
    	
    	if (null != sel && sel instanceof TextSelection)
    	{
    		TextSelection ts = (TextSelection) sel;
    		if (0!=ts.getLength())
    			dlg.setSelection(ts.getText());
    		else dlg.setSelection(null);
    	}
		
		dlg.open();
	}

	protected List processResourcesFuzzy(IResource[] resources, List results, String searchTerm) throws CoreException
	{

		for (int i = 0; i < resources.length; i++)
		{
		    boolean valid = true;
			if (resources[i] instanceof IContainer)
			{

				if (resources[i] instanceof IProject)
				{
					if (!((IProject)resources[i]).isOpen())
					{
						continue;
					}
				}
				
			    // check for project
			    if ( null != dlg.getEditorProject() && resources[i] instanceof IProject && dlg.isSearchInProject())
			    {
			        if (!resources[i].equals(dlg.getEditorProject()))
			        {
			            valid = false;
			        }
			    }
			    
				if ( valid && pathFilter.select(resources[i]))
				{
					results = processResourcesFuzzy(((IContainer)resources[i]).members(), results, searchTerm);
				}
				
			} else if (resources[i] instanceof File)
			{
				if (resources[i].exists())
				{
					String nameNoUpper = ((File)resources[i]).getProjectRelativePath().toString();
					String name = nameNoUpper.toUpperCase();
					String term = searchTerm;

					String capsString = "";

					// construct caps-only string
					for (int k = 0; k < nameNoUpper.length(); k++)
					{
						if (nameNoUpper.charAt(k) >= 'A' && nameNoUpper.charAt(k) <= 'Z')
						{
							capsString += nameNoUpper.substring(k, k + 1);
						}
					}

					int matchPos = 0;
					int matchConsecutive = 0;
					int index = 0;
					int charIndex = -1;

					while (term.length() != 0 && (charIndex = name.indexOf(term.charAt(0))) != -1)
					{
						index += charIndex;
						matchPos += index;
						if (charIndex == 0)
						{
							matchConsecutive++;
						}

						term = term.substring(1);
						name = name.substring(charIndex + 1);
						nameNoUpper = nameNoUpper.substring(charIndex + 1);
					}

					if (term.length() == 0)
					{
						results.add(new SearchResult((IFile)resources[i], matchPos, matchConsecutive, capsString.equals(searchTerm.toUpperCase())));
					}
				}
			}
		}
		return results;
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
	    if ( selection instanceof IStructuredSelection)
	        this.selection = (IStructuredSelection) selection;
	    else
	        this.selection = null;
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose()
	{
	}

	public List runSearch(String search)
	{
		IWorkspace spc = GotoFileE30Plugin.getWorkspace();

		IWorkspaceRoot root = spc.getRoot();

		List results = new ArrayList();

		if (results.isEmpty())
		{
			try
			{
				results = processResourcesFuzzy(root.members(), results, search.toUpperCase());
			} catch (CoreException e1)
			{
				e1.printStackTrace();
			}

			FilenameExclusionFilter filter = new FilenameExclusionFilter();
			results = filter.filter(results);

			Collections.sort(results, new MatchComparatorFuzzy());
		}

		/*
		for (Iterator it = results.iterator(); it.hasNext();)
		{
			SearchResult res = (SearchResult)it.next();
		}
		*/

		return results;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		// rebuild the dialog
		initDlg();
	}
	
	public void initDlg()
	{
		this.dlg = new SearchWindow(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 7, this);
		dlg.setAction(this);
	} 

}