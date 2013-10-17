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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.HashMap;
import java.util.HashSet;

public class EditorVerifyKeyListener implements VerifyKeyListener {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashMap<IEditorPart, EditorVerifyKeyListener> installedOn = new HashMap<IEditorPart, EditorVerifyKeyListener>();
	
	private boolean turnedOn = true;
	
	public EditorVerifyKeyListener(IEditorPart editor) {
		this.installMe(editor);
	}
	
	public static void ensureInstalled(IEditorPart editor) {
		if (shouldInstall(editor)) {	//is it an ISA File?
			if(installedOn.containsKey(editor)) {	  //Don't install if already installed on
				EditorVerifyKeyListener ekpl = installedOn.get(editor);
				ekpl.toggle();		//TODO: Move this to a logical place...  it toggles behavior on/off
				System.out.println("Already Installed!");
				return;
			}
			EditorVerifyKeyListener ekpl = new EditorVerifyKeyListener(editor);	
			installedOn.put(editor, ekpl);
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
			
			this.boxText = text;  //TODO TESTING THIS, REMOVE LATER
			
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
	
	
	private void toggle() {
		turnedOn = !turnedOn;
	}
	
	
//	@Override
//	public void keyPressed(KeyEvent e) {
//		System.out.println("Key pressed: " + e.character);
//		
//	}
//
//	@Override
//	public void keyReleased(KeyEvent e) {
//		
//	}

	@Override
	public void verifyKey(VerifyEvent event) {
		System.out.println("verifyKey called: " + event.character);
		if (turnedOn) {
			//TODO: Check if we're actually outside the typing bounds...
			event.doit = false;
		}
		//TODO: Call this somewhere better
		drawBox(3,5);
	}
	
	
	//ATTEMPT #1 AT BOX DRAWING
	//Most of this is stolen from editbox
	//don't understand what parts of it does
	private StyledText boxText;
	
	public void setStyledText(StyledText newSt) {
		System.out.println("setStyledText called");
        this.boxText = newSt;
	}
	
	//start and stop are line numbers to draw the box around, should probably just store them as instance variables...
	public void drawBox(int start, int stop) {
		Color c = new Color(null, 200, 120, 255);  //Purple?
		
		//big picture: create an image (newImage), edit with the gc, then set as styledtext background later
		Rectangle r0 = boxText.getClientArea();
		Image newImage = new Image(null, r0.width, r0.height);
        GC gc = new GC(newImage);
        
        
        if (turnedOn) {
        	int startY = boxText.getLocationAtOffset(boxText.getOffsetAtLine(start-1)).y;
        	int stopY = boxText.getLocationAtOffset(boxText.getOffsetAtLine(stop)).y;

        	gc.setForeground(c);
        	gc.setLineWidth(2);
        	gc.drawRectangle(1, startY, r0.width - 4, stopY - startY);
        }
        
        
        Image oldImage = boxText.getBackgroundImage();  //(so we can null check)
        boxText.setBackgroundImage(newImage);	//draw our box!  :D
        if (oldImage != null)
                oldImage.dispose();   //if we had a box before, clean up after ourselves
        gc.dispose();
	}

}
