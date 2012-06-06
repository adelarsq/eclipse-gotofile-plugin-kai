package org.muermann.gotofile.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.muermann.gotofile.GotoFileE30Plugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class GotoFilePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	//public static final String P_PATH = "pathPreference";
	public static final String P_PACKAGEEXPLORER = "packageExplorer";
	public static final String P_DISPLAYRESULTS = "displayResults";
	public static final String P_FOLDERS = "folders";
	public static final String P_FILES = "filenames";
	public static final String P_FOCUSINNAVIGATOR = "focusinnavigator";

	public GotoFilePreferencePage()
	{
		super(GRID);
		setPreferenceStore(GotoFileE30Plugin.getDefault().getPreferenceStore());
		setDescription("GotoFile Preferences");
		initializeDefaults();
	}
	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(P_PACKAGEEXPLORER, false);
		store.setDefault(P_FOCUSINNAVIGATOR, true);
		store.setDefault(P_DISPLAYRESULTS, "compact");
		store.setDefault(P_FOLDERS, "CVS,classes");
		store.setDefault(P_FILES, "*.class,*.jar,*.obj,*.exe");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors()
	{
		//addField(new DirectoryFieldEditor(P_PATH, "&Directory preference:", getFieldEditorParent()));

		addField(new BooleanFieldEditor(P_PACKAGEEXPLORER, "Show java files in Package Explorer", getFieldEditorParent()));
		addField(new BooleanFieldEditor(P_FOCUSINNAVIGATOR, "Don't focus files in navigator window", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
			P_DISPLAYRESULTS,
			"Display path information in search result list:",
			1,
			new String[][] { { "&Compact", "compact" }, {
				"&Tabular", "tabular" }
		}, getFieldEditorParent()));

		addField(new StringFieldEditor(P_FOLDERS, "Excluded folder patterns \n(comma separated, wildcards * \nand ? are allowed, or specify relative\n project paths: JavaSource/org/Test*):", getFieldEditorParent()));

		addField(new StringFieldEditor(P_FILES, "Excluded filename patterns \n(comma separated, wildcards * \nand ? are allowed):", getFieldEditorParent()));
	}
	

	public void init(IWorkbench workbench)	{
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    /*public boolean performOk()
    {        
        boolean b = super.performOk();
        GotoFileE30Plugin.getDefault().savePluginPreferences();
        return b;
    }*/
}