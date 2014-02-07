package edu.berkeley.eduride.editoroverlay.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.berkeley.eduride.editoroverlay.BoxConstrainedEditorOverlay;

public class ToggleBoxes implements IHandler2 {

	private static IEditorPart getActiveEditor(ExecutionEvent event) throws ExecutionException {
		// Get the active window
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (window == null)
			return null;
		// Get the active page
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;
		IEditorPart editor = page.getActiveEditor();
		
		return editor;
	}
	
	
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	
	// Finds the active editor, determines if BCEO can be used on it; if it
	// can, ensures that it is installed, and then toggles.
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = getActiveEditor(event);
		
		
		if (BoxConstrainedEditorOverlay.shouldInstall(activeEditor)) {
			BoxConstrainedEditorOverlay evkl = BoxConstrainedEditorOverlay
					.ensureInstalled(activeEditor);
			// evkl won't be null because we already check shouldInstall
			evkl.toggle();

		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(Object evaluationContext) {
		// TODO Auto-generated method stub
		
	}

}
