package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import java.util.HashSet;

public class EditorVerifyKeyListener implements VerifyKeyListener {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashSet<IEditorPart> installedOn = new HashSet<IEditorPart>();
	
	
	public EditorVerifyKeyListener(IEditorPart editor) {
		this.installMe(editor);
	}
	
	public static void ensureInstalled(IEditorPart editor) {
		if (shouldInstall(editor)) {
			//TODO Don't install if already exists
			if(installedOn.contains(editor)) {
				System.out.println("Already Installed!");
				return;
			}
			installedOn.add(editor);
			EditorVerifyKeyListener ekpl = new EditorVerifyKeyListener(editor);	
		}
	}
	
	public static boolean shouldInstall(IEditorPart editor) {
		// first, needs to be a text editor -- we only care about java, though
		if (editor instanceof AbstractDecoratedTextEditor) {
			IEditorInput input = editor.getEditorInput();
			// Check that the editor is reading from a file
			IFile file = (input instanceof FileEditorInput)
					? ((FileEditorInput)input).getFile()
					: null;
			return edu.berkeley.eduride.base_plugin.UIHelper.containedInISA(file);
		}
		return false;
	}
	
	public void installMe(IEditorPart editor) {
		StyledText text = null;
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditors()
		if (editor != null) {
//			if only I could import the internal package, sigh
//			if (editor instanceof JavaEditor) {
//				text = editor.getViewer().getTextWidget();
//			}
//			ITextOperationTarget target = (ITextOperationTarget)editorPart.getAdapter(ITextOperationTarget.class);
//			text = target.getTextWidget();
			
			text = (StyledText) editor.getAdapter(Control.class);
	    } 
		if (text != null) {
			text.addVerifyKeyListener(this);
//			log("loggerInstall", "KeyPressInEditor installed in " + editor.getTitle());
			this.editor = (ITextEditor) editor;
			IDocumentProvider dp = this.editor.getDocumentProvider();
			this.doc = dp.getDocument(editor.getEditorInput());
			ISelectionProvider sp = this.editor.getSelectionProvider();
			this.sel = (ITextSelection) sp.getSelection();
			sp.addSelectionChangedListener(new selChanged(this));
		} else {
			System.out.println("loggerInstall " + "EditorKeyPressListener failed to installed in " + editor.getTitle());
		}

	}
	
	private class selChanged implements ISelectionChangedListener {

		EditorVerifyKeyListener kpie;
		
		public selChanged(EditorVerifyKeyListener kpie) {
			this.kpie = kpie;
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			kpie.sel = (ITextSelection) event.getSelection();
		}
		
	}
	
//	@Override
//	public void keyPressed(KeyEvent e) {
//		// TODO Auto-generated method stub
//		System.out.println("Key pressed: " + e.character);
//		
//	}
//
//	@Override
//	public void keyReleased(KeyEvent e) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void verifyKey(VerifyEvent event) {
		// TODO Auto-generated method stub
		System.out.println("Thing called: " + event.character);
		event.doit = false;
	}

}
