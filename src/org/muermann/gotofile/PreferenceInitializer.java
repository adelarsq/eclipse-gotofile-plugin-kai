/*
 * Created on 22/03/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.muermann.gotofile;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.muermann.gotofile.preferences.GotoFilePreferencePage;

/**
 * @author max
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = GotoFileE30Plugin.getDefault().getPreferenceStore();
		store.setDefault(GotoFilePreferencePage.P_PACKAGEEXPLORER, false);
		store.setDefault(GotoFilePreferencePage.P_FOCUSINNAVIGATOR, true);
		store.setDefault(GotoFilePreferencePage.P_DISPLAYRESULTS, "compact");
		store.setDefault(GotoFilePreferencePage.P_FOLDERS, "CVS,classes");
		store.setDefault(GotoFilePreferencePage.P_FILES, "*.class,*.jar,*.obj,*.exe");

    }

}
