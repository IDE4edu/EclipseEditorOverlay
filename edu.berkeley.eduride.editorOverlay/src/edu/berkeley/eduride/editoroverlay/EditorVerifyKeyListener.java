package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
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

import edu.berkeley.eduride.editoroverlay.marker.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EditorVerifyKeyListener implements VerifyKeyListener {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashMap<IEditorPart, EditorVerifyKeyListener> installedOn = new HashMap<IEditorPart, EditorVerifyKeyListener>();
	
	private ArrayList<MultilineBox> boxList = new ArrayList<MultilineBox>();
	
	private boolean turnedOn = false;	//need to toggle this on setup
	
	private StyledText boxText;
	private BoxPaintListener boxPaint;
	IResource res;
	int caretOffset = 0;
	
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
				
				ekpl.res = ResourceUtil.getResource(editor.getEditorInput());
				if (ekpl.res != null) {	
					System.out.println("making inline");
					Util.createInlineMarker(ekpl.res, 10, 5, 25, "yeah");
					System.out.println("making multiline");
					Util.createMultiLine(ekpl.res, 12, 20, "yeah2");
					Util.createMultiLine(ekpl.res, 22, 25, "box3");
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
			
			this.boxText = text;
			
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
			System.out.println("loggerInstall " + "EditorVerifyKeyListener failed to installed in " + editor.getTitle());
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
			boolean allowed = false;
			int offset = caretOffset;
			for (MultilineBox b : boxList) {
				if ((offset >= b.startOffset()) && (offset <= b.stopOffset())) {
					allowed = true;
					break;
				}
			}
			event.doit = allowed;
		}
	}
	
	private void toggle() {
		if (!turnedOn) {
			createBoxListFromMarkers();
			decorate();
		} else {
			undecorate();
		}
		oldDrawParameters = null;  //hacky... force drawBox to redraw
		drawBox();
	}
	
	//install any listeners for drawing
	private void decorate() {
		if (turnedOn == true) {  //already on!
			return;
		}
		turnedOn = true;
		
		//Add Listeners - Think we only need paint!
        boxPaint = new BoxPaintListener();
        boxText.addPaintListener(boxPaint);
        CaretListener caretListener = new CaretPositionListener();
        boxText.addCaretListener(caretListener);
	}
	
	//stop listening when turned off
	private void undecorate() {
		turnedOn = false;
		//Stop listeners
		boxText.removePaintListener(boxPaint);
	}
	
	private class BoxPaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			drawBox();
		}
	}
	
	private class CaretPositionListener implements CaretListener {

		@Override
		public void caretMoved(CaretEvent event) {
			caretOffset = event.caretOffset;
		}
	}
	
	
	//Find all markers on res, store them as pairs in boxes in boxList
	private void createBoxListFromMarkers() {
		ISourceViewer viewer = ((CompilationUnitEditor) editor).getViewer();
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		
		//viewer.getVerticalRuler();  //i want a ruler  :(
		
		List<Annotation[]> multiline = Util.getMultilineAnnotations(annotationModel);
		MultilineBox b;
		
		Iterator<Annotation> annotations = annotationModel.getAnnotationIterator();
		while (annotations.hasNext()) {
			System.out.println(annotations.next().getType());
		}
		
		for (int i = 0; i < multiline.size(); i++) {
			b = new MultilineBox(annotationModel, multiline.get(i)[0], multiline.get(i)[1]);
			boxList.add(b);
			
			System.out.println("No Crash");
			System.out.println(b);
		}
		System.out.println("Done with for loop");
		
		List<IMarker> inline = Util.getInlineMarkers(res);
		//TODO: Storage and drawing for inline markers
	}
	
	
	
	
	//ATTEMPT #1 AT BOX DRAWING
	//Most of this is stolen from editbox
	
	private LinkedList<Integer> oldDrawParameters;  //Stores some set of numbers related to things we're drawing, not for use outside Draw!
	
	//draws boxes from boxList
	public void drawBox() {
		//big picture: create an image (newImage), edit with the gc, then set as styledtext background later
		Rectangle r0 = boxText.getClientArea();
		Image newImage = new Image(null, r0.width, r0.height);
        GC gc = new GC(newImage);
        
        
        ISourceViewer v = ((CompilationUnitEditor) editor).getViewer();
		ITextViewerExtension5 itve5 = (ITextViewerExtension5)v;   //we can use this to get line numbers w/ folding
		
        
        //Do we need to redraw?
		//Idea: Throw a bunch of numbers in some list, and see if it changes from one to another
        LinkedList<Integer> params = new LinkedList<Integer>();
        params.add(r0.width);
        for (MultilineBox b : boxList) {
        	//params.add(boxText.getLocationAtOffset(boxText.getOffsetAtLine(b.start()-1)).y);
        	//params.add(boxText.getLinePixel(b.start()-1));
        	
        	//params.add(boxText.getLocationAtOffset(b.startOffset()).y);
        	//params.add(boxText.getLocationAtOffset(b.stopOffset()).y);
        	try {
        		int startWidgetOffset = itve5.modelOffset2WidgetOffset(b.startOffset());
        		params.add(boxText.getLocationAtOffset(startWidgetOffset).y);
        		int stopWidgetOffset = itve5.modelOffset2WidgetOffset(b.stopOffset());
        		params.add(boxText.getLocationAtOffset(stopWidgetOffset).y);
        	} catch (Exception e) {
        		params.add(-1);  //one of the endpoints is folded
        	}
        }
        if (params.equals(oldDrawParameters)) {
        	return;  //short circuit if we don't need to redraw
        }
        oldDrawParameters = params;  //save state of last draw
        
        
        System.out.println(Arrays.toString(itve5.getCoveredModelRanges(itve5.getModelCoverage())));
        
        if (turnedOn) {
    		gc.setLineWidth(2);
        	
        	for (MultilineBox b : boxList) {
        		//int startY = boxText.getLinePixel(b.start()-1);  //for dealing with line numbers, not offsets
        		//int stopY = boxText.getLinePixel(b.stop());
        		
        		//int startY = boxText.getLocationAtOffset(b.startOffset()).y;  //start is offset, not line number
        		//int stopY = boxText.getLocationAtOffset(b.stopOffset()).y; // + boxText.getLineHeight();

        		try {
        			int startWidgetOffset = itve5.modelOffset2WidgetOffset(b.startOffset());
        			int startY = boxText.getLocationAtOffset(startWidgetOffset).y;
        		
        			int stopWidgetOffset = itve5.modelOffset2WidgetOffset(b.stopOffset());
        			int stopY = boxText.getLocationAtOffset(stopWidgetOffset).y;
        			
        			gc.setForeground(b.color);
            		gc.drawRectangle(1, startY, r0.width - 4, stopY - startY);
        		} catch (Exception e) {
        			System.out.println("Need to handle folding here");
        			//TODO: Folding edge cases, one or both markers inside of folded regions
        			//System.out.println(Arrays.toString(itve5.getCoveredModelRanges(itve5.getModelCoverage())));
        		}
        	}
        }
        
        Image oldImage = boxText.getBackgroundImage();  //(so we can null check)
        boxText.setBackgroundImage(newImage);	//draw our box!  :D
        if (oldImage != null)
                oldImage.dispose();   //if we had a box before, clean up after ourselves
        gc.dispose();
	}

}
