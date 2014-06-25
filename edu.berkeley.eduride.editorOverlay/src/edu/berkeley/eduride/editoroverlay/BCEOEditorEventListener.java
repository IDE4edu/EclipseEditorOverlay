package edu.berkeley.eduride.editoroverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.ide.ResourceUtil;

import edu.berkeley.eduride.base_plugin.isafile.ISABceoBoxSpec;
import edu.berkeley.eduride.base_plugin.isafile.ISABceoBoxSpec.BceoBoxType;
import edu.berkeley.eduride.base_plugin.model.EduRideFile;
import edu.berkeley.eduride.base_plugin.util.Console;
import edu.berkeley.eduride.base_plugin.util.IPartListenerInstaller;
import edu.berkeley.eduride.editoroverlay.marker.Util;

public class BCEOEditorEventListener implements IPartListener2 {

	
	public BCEOEditorEventListener(boolean install) {

		String errStr;
		// install listener for editor events
		errStr = IPartListenerInstaller.installOnWorkbench(this, "BCEO");
		if (errStr != null) {
			Console.err("BCEO install fail: " + errStr);
		}

		// install on currently open editors
		// run this in the UI thread?
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				ArrayList<IEditorPart> eds = IPartListenerInstaller
						.getCurrentEditors();
				for (IEditorPart ed : eds) {
					installDance(ed);
				}
			}
		}

		);

		//Console.msg("BCEOEditorEventListener - yo");

	}
	
	
	private static void installDance(IWorkbenchPartReference partRef) {
		IEditorPart editor = getEditor(partRef);
		if (editor != null) {
			installDance(editor);
		}
	}
	
	private static void installDance(IEditorPart ed) {
		createMarkers(ed);
		
		BoxConstrainedEditorOverlay.ensureInstalled(ed);
	}
	
	private static void createMarkers(IEditorPart editor) {
		
		IFile file = ResourceUtil.getFile(editor.getEditorInput());
		ArrayList<ISABceoBoxSpec> specs = EduRideFile.getBceoBoxSpecs(file);
		
		/*
		//for testing only
		specs = new ArrayList<ISABceoBoxSpec>();
		ISABceoBoxSpec s = new ISABceoBoxSpec(ISABceoBoxSpec.BceoBoxType.INLINE, "Foo", 20, 24);
		specs.add(s);
		*/
		
		if (specs == null) { return; }  //nothing to do

		IResource res = ResourceUtil.getResource(editor.getEditorInput());  //ugh
		
		
		
		if (res == null) { return; } 
		
		System.out.println("trying to add markers");
		
		//TODO: MAKE SURE LINE NUMBERS/OFFSETS ARE CONSISTENT ACROSS WINDOWS/MAC/LINUX!
		
		//get old markers
		List<IMarker[]> existingMultiline = Util.getMultilineMarkers(res);
		List<IMarker> existingInline = Util.getInlineMarkers(res);
		
		//for each proposed box, check if it already exists, and if it doesn't create new markers
		for (ISABceoBoxSpec boxSpec : specs) {
			try {
			if (boxSpec.type == BceoBoxType.INLINE) {
				boolean exists = false;
				for (IMarker oldMark : existingInline) {
					if (oldMark.getAttribute("id").equals(boxSpec.name)) {  //TODO: might need IMarker.MESSAGE, not id
						exists = true;
						System.out.println("box already exists: " + boxSpec.name);
						break;
					}
				}
				if (!exists) {
					System.out.println("new box: " + boxSpec.name);
					Util.createInlineMarker(res, boxSpec.start, boxSpec.stop, boxSpec.name);
				}
			}
			
			if (boxSpec.type == BceoBoxType.MULTILINE) {
				boolean exists = false;
				for (IMarker[] oldMark : existingMultiline) {
					if (oldMark[0].getAttribute("id").equals(boxSpec.name)) {  //TODO: might need IMarker.MESSAGE, not id
						exists = true;
						System.out.println("box already exists: " + boxSpec.name);
						break;
					}
				}
				if (!exists) {
					System.out.println("new box: " + boxSpec.name);
					Util.createMultiLine(res, boxSpec.start, boxSpec.stop, boxSpec.name);
				}
			}
			
			} catch (Exception e) {  //marker getAttr can throw errors
				System.err.println("Problem parsing bceoboxspec: " + boxSpec.name);
			}
		}

	}
	
	
	
	private static IEditorPart getEditor(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		IEditorPart editor = null;
		if (part != null && part instanceof IEditorPart) {
			editor = (IEditorPart) part.getAdapter(IEditorPart.class);
		}
		return editor;
	}
	
	
	private static void drawBoxesOnEditor(IEditorPart editor) {
		BoxConstrainedEditorOverlay bceo = BoxConstrainedEditorOverlay.getBCEO(editor);
		if (bceo != null) {
			bceo.drawBoxes();
		}
	}
	
	////////////////////////
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		//drawBoxesOnEditor(getEditor(partRef));
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
