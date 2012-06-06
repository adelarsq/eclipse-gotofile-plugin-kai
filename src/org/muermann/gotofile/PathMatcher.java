/*
 * Created on 8/10/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.muermann.gotofile;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * matches resource paths
 * 
 * @author Max Muermann
 * @version 0.1
 */
public class PathMatcher implements ResourceMatcher
{
	ArrayList matchers;
	
	/**
	 * 
	 */
	public PathMatcher( String pattern, boolean ignoreCase, boolean ignoreWildCards )
	{
		matchers = new ArrayList();
		StringTokenizer tok = new StringTokenizer( pattern, "/" );
		while (tok.hasMoreTokens())
		{
			String t = tok.nextToken();
			matchers.add( new StringMatcher( t, ignoreCase, ignoreWildCards ) );
		}
	}
	
	public boolean match( IResource res )
	{
		IPath path = res.getProjectRelativePath();
		if (0==path.segmentCount()) return false;
		boolean matched = true;
		for (int i = 0; i < path.segments().length && matched && i < matchers.size(); i++ )
		{
			String segment = path.segment(i);
			matched &= ((StringMatcher)matchers.get(i)).match( segment );
		}
		return matched;
	}

}
