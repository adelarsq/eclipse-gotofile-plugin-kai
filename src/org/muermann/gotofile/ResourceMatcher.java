/*
 * Created on 8/10/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.muermann.gotofile;

import org.eclipse.core.resources.IResource;

/**
 * Matcher interface to filter resources
 * 
 * @author Max Muermann
 * @version 0.1
 */
public interface ResourceMatcher
{
	public boolean match( IResource res );
}
