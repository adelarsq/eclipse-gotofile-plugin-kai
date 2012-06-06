/*
 * Created on 7/10/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.muermann.gotofile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.muermann.gotofile.preferences.GotoFilePreferencePage;

/**
 * Filters a list of filenames based on a list of exclusion patterns
 * 
 * @author Max Muermann
 * @version 0.1
 */
public class FilenameExclusionFilter
{
	private String[] patterns;
	private StringMatcher[] matchers;

	static final String COMMA_SEPARATOR = ","; //$NON-NLS-1$
	static final String FILTERS_TAG = "gotoFileFilenameFilters"; //$NON-NLS-1$
	public static final int MAX_SEARCH_RESULT_COUNT = 100;

	public List filter(List l)
	{
		ArrayList res = new ArrayList();
		for (Iterator it = l.iterator(); it.hasNext();)
		{
			SearchResult sr = (SearchResult)it.next();
			IFile resource = sr.getFile();
			if (resource != null)
			{
				String name = resource.getName();
				if (select(name))
				{
					res.add(sr);
				}
			}
			if (res.size() > MAX_SEARCH_RESULT_COUNT) {
				break;
			}
		}
		return res;
	}

	public boolean select(String name)
	{
		StringMatcher[] testMatchers = getMatchers();
		boolean matched = false;
		for (int i = 0; i < testMatchers.length && !matched; i++)
		{
			if (testMatchers[i].match(name))
			{
				matched = true;
			}
		}
		return !matched;
	}

	/**
	 * Return the currently configured StringMatchers. If there aren't any look
	 * them up.
	 */
	private StringMatcher[] getMatchers()
	{

		if (this.matchers == null)
			initializeFromPreferences();

		return this.matchers;
	}

	/**
	 * Initializes the filters from the preference store.
	 */
	private void initializeFromPreferences()
	{
		// get the filters that were saved by ResourceNavigator.setFiltersPreference
		String files = GotoFileE30Plugin.getDefault().getPreferenceStore().getString( GotoFilePreferencePage.P_FILES );
		StringTokenizer tok = new StringTokenizer( files, ",");
		ArrayList a = new ArrayList();
		while (tok.hasMoreTokens())
		{			
			String t = tok.nextToken();
			a.add( t );
		}		
		patterns = new String[a.size()];		
		a.toArray(patterns);
		setPatterns(patterns);
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 */
	public void setPatterns(String[] newPatterns)
	{

		this.patterns = newPatterns;
		this.matchers = new StringMatcher[newPatterns.length];
		for (int i = 0; i < newPatterns.length; i++)
		{
			//Reset the matchers to prevent constructor overhead
			matchers[i] = new StringMatcher(newPatterns[i], true, false);
		}
	}
}
