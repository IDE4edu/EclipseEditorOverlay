package edu.berkeley.eduride.editoroverlay.handlers;

import org.eclipse.ui.IStartup;

import edu.berkeley.eduride.base_plugin.IStartupSync;
import edu.berkeley.eduride.base_plugin.util.Console;
import edu.berkeley.eduride.editoroverlay.EditorOverlayActivator;

public class EdurideStartup implements IStartupSync, IStartup  {

	@Override
	public void install() {
		
		Console.msg("Staring up editor overlay, woot");
		
	}

	@Override
	public void earlyStartup() {
		
		EditorOverlayActivator.getDefault();
		//Console.msg("Staring up editor overlay, woot");
		
	}

	
	
}
