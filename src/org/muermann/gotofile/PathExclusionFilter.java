/*
 * Created on 7/10/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.muermann.gotofile;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.muermann.gotofile.preferences.GotoFilePreferencePage;

/**
 * Filters a list of resource paths based on exclusion patterns
 * 
 * @author Max Muermann
 * @version 0.1
 */
public class PathExclusionFilter
{
	private String[] patterns;
	private ResourceMatcher[] matchers;

	static final String COMMA_SEPARATOR = ","; //$NON-NLS-1$
	static final String FILTERS_TAG = "gotoFilePathFilters"; //$NON-NLS-1$


	public boolean select(IResource res)
	{		
		ResourceMatcher[] testMatchers = getMatchers();
		boolean matched = false;
		for (int i = 0; i < testMatchers.length && !matched; i++)
		{
			if (testMatchers[i].match(res))
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
	private ResourceMatcher[] getMatchers()
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

		String folders = GotoFileE30Plugin.getDefault().getPreferenceStore().getString( GotoFilePreferencePage.P_FOLDERS );
		
		StringTokenizer tok = new StringTokenizer( folders, ",");
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
		this.matchers = new ResourceMatcher[newPatterns.length];
		for (int i = 0; i < newPatterns.length; i++)
		{
			if (-1 != newPatterns[i].indexOf("/"))
			{
				matchers[i] = new PathMatcher( newPatterns[i], true, false );
			} else
			{
				matchers[i] = new StringMatcher( newPatterns[i], true, false);
			}
		}
	}
}
