package edu.berkeley.eduride.editoroverlay;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.berkeley.eduride.base_plugin.EduRideBase;

public class EditorOverlayActivator implements BundleActivator {

	private static BundleContext context;

	
	public static final String PLUGIN_ID = "edu.berkeley.eduride.editorOverlay";
	// The shared instance
	private static EditorOverlayActivator plugin = null;
	
	
	
	
	static BundleContext getContext() {
		return context;
	}

	
	/**
	 * The constructor
	 */
	public EditorOverlayActivator() {
		super();
		
		plugin = this;
	}
	
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EditorOverlayActivator getDefault() {
		return plugin;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		EditorOverlayActivator.context = bundleContext;
		
		install();
	}



	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		EditorOverlayActivator.context = null;
	}
	
	
	
	
	
	//////////
	
	private static BCEOEditorEventListener editorListener = null;
	
	private static void install() {
		
		editorListener = new BCEOEditorEventListener(true);
		
		
	}

	
	

}
