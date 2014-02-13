package edu.berkeley.eduride.editoroverlay.handlers;

import org.eclipse.ui.IStartup;

import edu.berkeley.eduride.base_plugin.IStartupSync;
import edu.berkeley.eduride.editoroverlay.EditorOverlayActivator;

public class EdurideStartup implements IStartupSync, IStartup  {

	@Override
	public void install() {
		
		System.out.println("Staring up editor overlay, woot");
		
	}

	@Override
	public void earlyStartup() {
		
		EditorOverlayActivator.getDefault();
		//System.out.println("Staring up editor overlay, woot");
		
	}

	
	
}
