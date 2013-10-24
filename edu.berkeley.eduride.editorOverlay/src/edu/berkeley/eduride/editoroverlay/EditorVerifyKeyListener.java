package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.berkeley.eduride.editoroverlay.marker.AllowEditing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class EditorVerifyKeyListener implements VerifyKeyListener {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashMap<IEditorPart, EditorVerifyKeyListener> installedOn = new HashMap<IEditorPart, EditorVerifyKeyListener>();
	
	private ArrayList<MultilineBox> boxList = new ArrayList<MultilineBox>();
	
	private boolean turnedOn = false;	//need to toggle this on setup
	
	private StyledText boxText;
	private BoxPaintListener boxPaint;
	
	public EditorVerifyKeyListener(IEditorPart editor) {
		this.installMe(editor);
	}
	
	public static void ensureInstalled(IEditorPart editor) {
		if (shouldInstall(editor)) {	//is it an ISA File?
			EditorVerifyKeyListener ekpl;
			if(installedOn.containsKey(editor)) {	  //Don't install if already installed on
				ekpl = installedOn.get(editor);
				System.out.println("Already Installed!");
			} else {
				ekpl = new EditorVerifyKeyListener(editor);	
				installedOn.put(editor, ekpl);
				
				//TODO:  Remove this, testing only!
				ekpl.boxList.add(new MultilineBox(2, 5));
				ekpl.boxList.add(new MultilineBox(7, 15, new Color(null, 255,100,100)));
				//ekpl.boxList.add(new MultilineBox(17, 17, new Color(null, 255,255,100)));
				//ekpl.boxList.add(new MultilineBox(19, 24, new Color(null, 255,100,255)));
				//ekpl.boxList.add(new MultilineBox(25, 27, new Color(null, 100,255,160)));
				
				IResource res = ResourceUtil.getResource(editor.getEditorInput());
				if (res != null) {	
					System.out.println("making inline");
					AllowEditing.createInLine(res, 10, 5, 25, "yeah");
					System.out.println("making multiline");
					AllowEditing.createMultiLine(res, 12, 16, "yeah2");
				}
			}
			ekpl.toggle();  //TODO: Move this to a logical place...  it toggles behavior on/off
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
			
			this.boxText = text;  //TODO Move this somewhere logical?
			
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
	
	
	
	
	//Intercept key presses, if turned on stop key presses
	//Bug:  Does NOT prevent pasting!
	@Override
	public void verifyKey(VerifyEvent event) {
		System.out.println("verifyKey called: " + event.character);
		if (turnedOn) {
			//TODO: Check if we're actually outside the typing bounds...
			event.doit = false;
		}
		//TODO: Call this somewhere better
		drawBox();
	}
	
	//TODO: Figure out if we need these listeners or not  (looks like it's working with just paintlistener)
	//private BoxKeyListener boxKey;
	//private BoxModifyListener boxModify;
	//private BoxMouseMoveListener boxMouseMove;
	//private BoxMouseTrackListener boxMouseTrack;
	//private BoxTextChangeListener boxTextChange;
	
	private void toggle() {
		if (!turnedOn) {
			decorate();
		} else {
			undecorate();
		}
		oldDrawParameters = null;  //hacky... force drawBox to redraw
		drawBox();
	}
	
	//install various listeners for drawing
	private void decorate() {
		if (turnedOn == true) {  //already on!
			return;
		}
		turnedOn = true;
		
		//Add Listeners - Think we only need paint!
        boxPaint = new BoxPaintListener();
        boxText.addPaintListener(boxPaint);
	}
	
	//stop random listening when turned off
	private void undecorate() {
		turnedOn = false;
		//Stop listeners
		boxText.removePaintListener(boxPaint);
	}
	
//	private class BoxKeyListener implements KeyListener {
//		public void keyPressed(KeyEvent e) { }
//		
//		public void keyReleased(KeyEvent e) {
//			drawBox();
//		}
//	}
//	
//	private class BoxModifyListener implements ModifyListener {		//Can we use modifylistener to fix ctrl+v?
//		//When text actually changes
//		public void modifyText(ModifyEvent e) {
//			drawBox();
//		}
//	}
//	
//	private class BoxMouseMoveListener implements MouseMoveListener {
//		public void mouseMove(MouseEvent e) {
//			drawBox();
//		}
//	}
	
	private class BoxPaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			drawBox();
		}
	}
	
	
	
	//ATTEMPT #1 AT BOX DRAWING
	//Most of this is stolen from editbox
	
	private LinkedList<Integer> oldDrawParameters;  //Stores some set of numbers related to things we're drawing, not for use outside Draw!
	
	//start and stop are line numbers to draw the box around, should probably just store them as instance variables...
	public void drawBox() {
		
		//big picture: create an image (newImage), edit with the gc, then set as styledtext background later
		Rectangle r0 = boxText.getClientArea();
		Image newImage = new Image(null, r0.width, r0.height);
        GC gc = new GC(newImage);
        
        
        
        //Do we need to redraw?
        LinkedList<Integer> params = new LinkedList<Integer>();
        params.add(r0.width);
        for (MultilineBox b : boxList) {
        	//params.add(boxText.getLocationAtOffset(boxText.getOffsetAtLine(b.start()-1)).y);
        	//params.add(boxText.getLocationAtOffset(boxText.getOffsetAtLine(b.stop())).y);
        	params.add(boxText.getLinePixel(b.start()-1));
        	params.add(boxText.getLinePixel(b.stop()));
        }
        if (params.equals(oldDrawParameters)) {
        	return;  //short circuit!
        }
        oldDrawParameters = params;  //save state of last draw
        
        
        
        if (turnedOn) {
        	
    		gc.setLineWidth(2);
        	
        	for (MultilineBox b : boxList) {
        		int startY = boxText.getLinePixel(b.start()-1);
        		int stopY = boxText.getLinePixel(b.stop());

        		gc.setForeground(b.color);
        		gc.drawRectangle(1, startY, r0.width - 4, stopY - startY);
        	}
        }
        
        Image oldImage = boxText.getBackgroundImage();  //(so we can null check)
        boxText.setBackgroundImage(newImage);	//draw our box!  :D
        if (oldImage != null)
                oldImage.dispose();   //if we had a box before, clean up after ourselves
        gc.dispose();
	}

}
