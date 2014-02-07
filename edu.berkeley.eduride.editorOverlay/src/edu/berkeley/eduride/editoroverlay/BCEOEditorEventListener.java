package edu.berkeley.eduride.editoroverlay;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.berkeley.eduride.base_plugin.util.IPartListenerInstaller;

public class BCEOEditorEventListener implements IPartListener2 {

	
	public BCEOEditorEventListener(boolean install) {
		
		String errStr;
		errStr = IPartListenerInstaller.installOnWorkbench(this);
		if (errStr != null) {
			System.err.println(errStr);
		}
		
		ArrayList<IEditorPart> eds = IPartListenerInstaller.getCurrentEditors();
		for (IEditorPart ed : eds) {
			installDance(ed);
		}

	}
	
	
	private static void installDance(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		IEditorPart editor = null;
		if (part != null && part instanceof IEditorPart) {
			editor = (IEditorPart) part.getAdapter(IEditorPart.class);
		}
		if (editor != null) {
			installDance(editor);
		}
	}
	
	private static void installDance(IEditorPart ed) {
		// TODO install dance
	}
	
	
	////////////////////////
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		installDance(partRef);
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {


	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO we probably need to worry about this, yo

	}

}
