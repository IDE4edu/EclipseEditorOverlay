package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.Position;

import edu.berkeley.eduride.editoroverlay.marker.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// one of these per editor that can be constrained by our boxes!
public class BoxConstrainedEditorOverlay  {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashMap<IEditorPart, BoxConstrainedEditorOverlay> installedOn = new HashMap<IEditorPart, BoxConstrainedEditorOverlay>();
    ISourceViewer srcViewer;
    IAnnotationModel annotationModel;
    ITextViewerExtension5 txtViewerExt;
		
	
	private ArrayList<MultilineBox> multilineBoxes = new ArrayList<MultilineBox>();
	private ArrayList<InlineBox> inlineBoxes = new ArrayList<InlineBox>();
	
	private boolean turnedOn = false;	//need to toggle this on setup
	
	private StyledText styledText;
	private BoxPaintListener boxPaint;
	private CaretListener caretListener;
	private EditorOverlayVerifyKeyListener verifyKeyListener;
	private BCEOAnnotationModelListener annotationModelListener;
	IResource res;
	int caretOffset = 0;
	
	public BoxConstrainedEditorOverlay(IEditorPart editor) {
		this.installMe(editor);
	}
	
	
	public static BoxConstrainedEditorOverlay ensureInstalled(IEditorPart editor) {
		if (shouldInstall(editor)) {	//is it an ISA File?
			BoxConstrainedEditorOverlay ekpl;
			if(installedOn.containsKey(editor)) {	  //Don't install if already installed on
				ekpl = installedOn.get(editor);
				System.out.println("Already Installed!");
			} else {
				ekpl = new BoxConstrainedEditorOverlay(editor);	
				installedOn.put(editor, ekpl);
				
				ekpl.res = ResourceUtil.getResource(editor.getEditorInput());
				if (ekpl.res != null) {	
					System.out.println("making inline");
					Util.createInlineMarker(ekpl.res, ekpl.styledText.getOffsetAtLine(8)+5, ekpl.styledText.getOffsetAtLine(8)+25, "yeah");
					System.out.println("making multiline");
					Util.createMultiLine(ekpl.res, 12, 20, "yeah2");
					Util.createMultiLine(ekpl.res, 22, 25, "box3");
				}
			}
			return ekpl;
		}
		return null;
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
			text = (StyledText) editor.getAdapter(Control.class);
			this.styledText = text;
			
	    } 
		if (text != null) {
//			log("loggerInstall", "KeyPressInEditor installed in " + editor.getTitle());
			this.editor = (ITextEditor) editor;
			IDocumentProvider dp = this.editor.getDocumentProvider();
			this.doc = dp.getDocument(editor.getEditorInput());
			ISelectionProvider sp = this.editor.getSelectionProvider();
			this.sel = (ITextSelection) sp.getSelection();
			sp.addSelectionChangedListener(new selChanged(this));
			
		    srcViewer = ((CompilationUnitEditor) editor).getViewer();
		    annotationModel = srcViewer.getAnnotationModel();
			txtViewerExt = (ITextViewerExtension5)srcViewer;   //we can use this to get line numbers w/ folding
				
		 
		} else {
			System.out.println("loggerInstall " + "EditorVerifyKeyListener failed to installed in " + editor.getTitle());
		}

	}
	

	
	// This keeps the last selection around, but it is currently unused.
	private class selChanged implements ISelectionChangedListener {

		BoxConstrainedEditorOverlay ed;
		
		public selChanged(BoxConstrainedEditorOverlay kpie) {
			this.ed = kpie;
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ed.sel = (ITextSelection) event.getSelection();
		}
		
	}
	
	
	////////////////////
	/////////// OVERLAY CONTROLS
	
	
	
	public void toggle() {
		if (!turnedOn) {
			createBoxes();
			decorate();
		} else {
			undecorate();
		}
	}
	
	

	
	//install any listeners for drawing
	private void decorate() {
		if (turnedOn == true) {  //already on!
			return;
		}
		turnedOn = true;
		
		//Add Listeners - 
        boxPaint = new BoxPaintListener();
        styledText.addPaintListener(boxPaint);
        caretListener = new CaretPositionListener();
        styledText.addCaretListener(caretListener);
        verifyKeyListener = new EditorOverlayVerifyKeyListener();
		styledText.addVerifyKeyListener(verifyKeyListener);
		annotationModelListener = new BCEOAnnotationModelListener();
		annotationModel.addAnnotationModelListener(annotationModelListener);
		
		drawBoxes();
	}

	
	// stop listening when turned off
	private void undecorate() {
		if (turnedOn == true) {
			turnedOn = false;
			// Stop listeners
			styledText.removePaintListener(boxPaint);
			styledText.removeCaretListener(caretListener);
			styledText.removeVerifyKeyListener(verifyKeyListener);
			clearBackground();
		}
	}

	
	
	private class BoxPaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			drawBoxes();
		}
	}
	
	private class CaretPositionListener implements CaretListener {

		@Override
		public void caretMoved(CaretEvent event) {
			caretOffset = event.caretOffset;
		}
	}
	

	
	private class EditorOverlayVerifyKeyListener implements VerifyKeyListener {

		// Intercept key presses, if turned on stop key presses
		
		//TODO Kim's consolidated To Do List:
		//1. BAD CRASH: Highlight a region s.t. it covers a marker and the cursor is in a box, press delete or any key.
		//    Annotation gets deleted, code explodes from null pointer.  Does get caught by keypress event, how do we handle it?
		//2. detect/catch/stop paste events outside of boxes
		
		@Override
		public void verifyKey(VerifyEvent event) {
			char character = event.character;
			System.out.println("verifyKey called: " + character + ", int: " + ((int)character));
			if ((int)character == 0) {  //hacky, allow non-character keys like arrows.  Couldn't find a proper method to detect arrows.
				event.doit = true;
				return;
			}
			if (turnedOn) {
				boolean allowed = false;
				
				int offset = caretOffset;
				offset = txtViewerExt.widgetOffset2ModelOffset(offset);  //account for folding
				
				for (MultilineBox b : multilineBoxes) {
					
					// TODO move this to a helper function...
					if ((offset > b.getStartStyledTextOffset()) && (offset < b.getStopStyledTextOffset() - 1)) {
						allowed = true;
						if ((int)character == 8) {	//backspace
							if (offset == b.getStartStyledTextOffset() + 1) {	//are we at the start of a box?
								allowed = false;
							}
						} if ((int)character == 127) {  //user pressed delete
							System.out.println("Delete pressed");
							if (offset == b.getStopStyledTextOffset() - 2) {  //end of box
								System.out.println("end of box");
								allowed = false;
							}
						}
						break;
					}
				}
				for (InlineBox b : inlineBoxes) {
					// TODO use that helper function!
				}
				event.doit = allowed;
			}
		}

	}
	
	
	
	
	
	
	public class BCEOAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		@Override
		public void modelChanged(AnnotationModelEvent event) {
			//System.err.println ("Hello.  Got a annotation model change!");
			
			if (event.isValid() && !event.isEmpty()) {
				boolean uhOh = false;
				Annotation[] removedAnnotations = event.getRemovedAnnotations();
				for (Annotation removedAnnotation : removedAnnotations) {
					if (Util.isBCEOAnnoation(removedAnnotation)) {
						uhOh = true;
					}
				}
				if (uhOh) {
					// seems overly dramatic, but hey
					createBoxes();
				}
			}
			
		}

		@Override
		public void modelChanged(IAnnotationModel model) {
			//System.err.println("I don't care about this model Changed event");
			// this is around just so we can add this listener, sigh
		}
		
		
		

	}
	
	
	///////////////////////////
	
	
	
	
	//Make multilineBoxes and inlineBpoxes based on annotations in editor viewer
	private void createBoxes() {
//		ISourceViewer viewer = ((CompilationUnitEditor)editor).getViewer();
//		IAnnotationModel annotationModel =  viewer.getAnnotationModel();
		
		// MULTILINE
		List<Annotation[]> multiline = Util.getMultilineAnnotations(annotationModel);
		MultilineBox mb;
		multilineBoxes.clear();
		
		for (int i = 0; i < multiline.size(); i++) {
			mb = new MultilineBox(annotationModel, multiline.get(i)[0], multiline.get(i)[1]);
			multilineBoxes.add(mb);
		}
		
		
		// INLINE
		//TODO: Storage and drawing for inline markers
		List<Annotation> inline = Util.getInlineAnnotations(annotationModel);
		InlineBox ib;
		inlineBoxes.clear();
		
		for (Annotation ann : inline) {
			ib = new InlineBox(annotationModel, ann);
			inlineBoxes.add(ib);
		}
	}
	
	
	
	
	
	
	
	////////////////
	
	
	//private LinkedList<Integer> oldDrawParameters;  //Stores some set of numbers related to things we're drawing, not for use outside Draw!
	
	private int oldEditorWidth = 0;
	
	//draws boxes from boxList
	public  void drawBoxes() {
		//big picture: create an image (newImage), edit with the gc, then set as styledtext background later
		Rectangle editorRectangle = styledText.getClientArea();
		
        //Do we need to redraw?
		boolean redraw = false;
		boolean somethingIsFolded = false;
		
		// if the editor window gets wider or narrower we've got to redraw it no matter what...  don't out here!
		if (editorRectangle.width != oldEditorWidth) {
			redraw = true;
			oldEditorWidth = editorRectangle.width;
		}

		// collect all the drawing locations for all the boxes.
        for (MultilineBox b : multilineBoxes) {

        	int startWidgetOffset, stopWidgetOffset;
        	int startPixelY = -1, stopPixelY = -1;
        	
        	startWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(b.getStartStyledTextOffset());
        	if (startWidgetOffset != -1) {
        		startPixelY = styledText.getLocationAtOffset(startWidgetOffset).y;
        	} else {
        		somethingIsFolded = true;
        	}
        	
        	stopWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(b.getStopStyledTextOffset());
        	if (stopWidgetOffset != -1) {
        		stopPixelY = styledText.getLocationAtOffset(stopWidgetOffset).y;
        	} else {
        		somethingIsFolded = true;
        	}

        	// no need to check/store widget offsets... right?
        	if (b.getStartPixelY() != startPixelY) {
        		redraw = true;
        		b.setStartPixelY(startPixelY);
        	}	
        	if (b.getStopPixelY() != stopPixelY) {
        		redraw = true;
        		b.setStopPixelY(stopPixelY);
        	}
        	
        }
        
        for (InlineBox b : inlineBoxes) {
        	
        	int startWidgetOffset, stopWidgetOffset;
     	
        	Position stPos = b.getStyledTextPosition();
        	
        	startWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(stPos.getOffset());
        	int x = -1;
        	int y = -1;
        	int width = -1;
        	if (startWidgetOffset != -1) {
        		Point startLoc = styledText.getLocationAtOffset(startWidgetOffset);
        		x = startLoc.x;
        		y = startLoc.y;
        	} else {
        		somethingIsFolded = true;
        	}
        	
        	stopWidgetOffset = startWidgetOffset + stPos.getLength();
        	if (stopWidgetOffset != -1) {
        		width = styledText.getLocationAtOffset(stopWidgetOffset).x - x;
        	} else {
        		somethingIsFolded = true;
        	}

        	// no need to check/store widget offsets... right?
        	if (b.x != x) {
        		redraw = true;
        		b.x = x;
        	}	
        	if (b.y != y) {
        		redraw = true;
        		b.y = y;
        	}
        	if (b.width != width) {
        		redraw = true;
        		b.width = width;
        	}
        	
        }
        
        
        //compare old drawing locations with new locations.  If unchanged, simply return.
        if (!redraw) {
        	return;  //short circuit if we don't need to redraw
        }

        
        
        
        //// OKAY, we need to draw
		
		// TODO speed keep old size around and only recreate when necessary?
		Image newImage = new Image(null, editorRectangle.width, editorRectangle.height);
        GC gc = new GC(newImage);
        
        IRegion[] visibleRegions = txtViewerExt.getCoveredModelRanges(txtViewerExt.getModelCoverage());
        
        if (turnedOn) {
        	gc.setLineWidth(2);

        	
        	for (InlineBox b : inlineBoxes) {
        		if (b.x != -1) {
        			// its visible (not folded up)
        			gc.setForeground(b.color);
        			gc.drawRectangle(b.x, b.y, b.width, styledText.getLineHeight());
        		}
        		
        	}
        	
        	if (!somethingIsFolded) {   //Don't go through big slow ifs/loops when no folding problems (aka correct use of boxes)
        		for (MultilineBox b : multilineBoxes) {
        			int startY = b.getStartPixelY();
        			int stopY = b.getStopPixelY();
        		
        			gc.setForeground(b.color);
            		gc.drawRectangle(1, startY, editorRectangle.width - 4, stopY - startY);
        		}

        	} else {  //something is folded, so we need to check starts/ends and recalculate
	        	for (MultilineBox b : multilineBoxes) {
	        		int startY = b.getStartPixelY();
	        		int stopY = b.getStopPixelY();
	        		
	        		int index = 0;
	    			
	    			if (startY == -1) {   //start marker is folded, recompute
	    				startY = b.getStartStyledTextOffset();
	    				while ((index < visibleRegions.length) && (startY >= visibleRegions[index].getOffset())) {
	    					index++;
	    				}
	    				
	    				startY = visibleRegions[index].getOffset();
	    				
	    				if (b.getStopStyledTextOffset() < startY) {	//no region to draw, stop found before start
	    					continue;
	    				}
	    				startY = txtViewerExt.modelOffset2WidgetOffset(startY);
	    				startY = styledText.getLocationAtOffset(startY).y;
	    			}
	    			
	    			if (stopY == -1) {  //stop marker is folded, recompute
	    				stopY = b.getStopStyledTextOffset();
	    				
	    				while ((index < visibleRegions.length) && (stopY > visibleRegions[index].getOffset() )) {
	    					index++;
	    				}
	    				
	    				stopY = visibleRegions[index].getOffset() + visibleRegions[index].getLength() - 1;
	    				stopY = txtViewerExt.modelOffset2WidgetOffset(stopY);
	    				stopY = styledText.getLocationAtOffset(stopY).y;
	    			}
	        		
	    			gc.setForeground(b.color);
	        		gc.drawRectangle(1, startY, editorRectangle.width - 4, stopY - startY);
        		}
        	}
        }
        
        Image oldImage = styledText.getBackgroundImage();  //(so we can null check)
        styledText.setBackgroundImage(newImage);	//draw our box!  :D
        if (oldImage != null)
                oldImage.dispose();   //if we had a box before, clean up after ourselves
        gc.dispose();
	}

	
	
	private void clearBackground() {
		Image oldImage = styledText.getBackgroundImage();
        styledText.setBackgroundImage(null);
        if (oldImage != null)
            oldImage.dispose();
	}
	
}

	

