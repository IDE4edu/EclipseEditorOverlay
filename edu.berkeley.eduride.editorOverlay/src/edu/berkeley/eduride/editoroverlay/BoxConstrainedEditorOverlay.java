package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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

// one of these per editor that can be constrained by our boxes!
public class BoxConstrainedEditorOverlay  {

	private ITextEditor editor;
	private IDocument doc;
	private ITextSelection sel;
	private static HashMap<IEditorPart, BoxConstrainedEditorOverlay> installedOn = new HashMap<IEditorPart, BoxConstrainedEditorOverlay>();
    ISourceViewer srcViewer;
    ITextViewerExtension5 srcViewerE5;
		

	
	
	private ArrayList<MultilineBox> multilineBoxes = new ArrayList<MultilineBox>();
	
	private boolean turnedOn = false;	//need to toggle this on setup
	
	private StyledText styledText;
	private BoxPaintListener boxPaint;
	private CaretListener caretListener;
	private EditorOverlayVerifyKeyListener verifyKeyListener;
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
					Util.createInlineMarker(ekpl.res, 10, 5, 25, "yeah");
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
			srcViewerE5 = (ITextViewerExtension5)srcViewer;   //we can use this to get line numbers w/ folding
				
		 
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
		
		// TODO removed the hack to force redraw.  Will this work?
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
		// Bug: Does NOT prevent pasting!
		@Override
		public void verifyKey(VerifyEvent event) {
			System.out.println("verifyKey called: " + event.character);
			if (turnedOn) {
				boolean allowed = false;
				int offset = caretOffset;
				for (MultilineBox b : multilineBoxes) {
					// TODO off by one error with ending offset -- you can edit
					// at the start of the line below the MLBox
					// TODO does this work with folding?
					if ((offset >= b.getStartStyledTextOffset())
							&& (offset <= b.getStopStyledTextOffset())) {
						allowed = true;
						break;
					}
				}
				event.doit = allowed;
			}
		}

	}
	
	
	
	
	
	//Make multilineBoxes and inlineBpoxes based on annotations in editor viewer
	private void createBoxes() {
		ISourceViewer viewer = ((CompilationUnitEditor)editor).getViewer();
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		
		// MULTILINE
		List<Annotation[]> multiline = Util.getMultilineAnnotations(annotationModel);
		MultilineBox b;
		
		Iterator<Annotation> annotations = annotationModel.getAnnotationIterator();
		while (annotations.hasNext()) {
			System.out.println(annotations.next().getType());
		}
		
		for (int i = 0; i < multiline.size(); i++) {
			b = new MultilineBox(annotationModel, multiline.get(i)[0], multiline.get(i)[1]);
			multilineBoxes.add(b);
			//System.out.println("No Crash");
			//System.out.println(b);
		}
		
		
		// INLINE
		//List<IMarker> inline = Util.getInlineMarkers(res);
		//TODO: Storage and drawing for inline markers
	}
	
	
	
	
	
	
	
	////////////////
	
	
	//private LinkedList<Integer> oldDrawParameters;  //Stores some set of numbers related to things we're drawing, not for use outside Draw!
	
	private int oldEditorWidth = 0;
	
	//draws boxes from boxList
	public  void drawBoxes() {
		//big picture: create an image (newImage), edit with the gc, then set as styledtext background later
		Rectangle editorRectangle = styledText.getClientArea();
		
        //LinkedList params = new LinkedList<Integer>();   DELETE ME
		
        //Do we need to redraw?
		boolean redraw = false;
		
		// if the editor window gets wider or narrower we've got to redraw it no matter what
		if (editorRectangle.width != oldEditorWidth) {
			redraw = true;
			oldEditorWidth = editorRectangle.width;
		}

		// collect all the drawing locations for all the boxes.
        for (MultilineBox b : multilineBoxes) {
        	//params.add(boxText.getLocationAtOffset(boxText.getOffsetAtLine(b.start()-1)).y);
        	//params.add(boxText.getLinePixel(b.start()-1));
        	
        	//params.add(boxText.getLocationAtOffset(b.startOffset()).y);
        	//params.add(boxText.getLocationAtOffset(b.stopOffset()).y);
        	
        	int startWidgetOffset, stopWidgetOffset;
        	int startPixelY = -1, stopPixelY = -1;
        	
        	startWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(b.getStartStyledTextOffset());
        	if (startWidgetOffset != -1) {
        		startPixelY = styledText.getLocationAtOffset(startWidgetOffset).y;
        	}
        	stopWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(b.getStopStyledTextOffset());
        	if (stopWidgetOffset != -1) {
        		stopPixelY = styledText.getLocationAtOffset(stopWidgetOffset).y;
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
        
        // TODO compare old drawing locations with new locations.  If unchanged, simply return.
        if (!redraw) {
        	return;  //short circuit if we don't need to redraw
        }

        
        //// OKAY, we need to draw
		
		// TODO speed keep old size around and only recreate when necessary?
		Image newImage = new Image(null, editorRectangle.width, editorRectangle.height);
        GC gc = new GC(newImage);
        
////////////////////////////////////////////////////
        
        System.out.println(Arrays.toString(srcViewerE5.getCoveredModelRanges(srcViewerE5.getModelCoverage())));
        IRegion[] visibleRegions = srcViewerE5.getCoveredModelRanges(srcViewerE5.getModelCoverage());
        
        if (turnedOn) {
    		gc.setLineWidth(2);
        	
        	for (MultilineBox b : multilineBoxes) {
        		//int startY = boxText.getLinePixel(b.start()-1);  //for dealing with line numbers, not offsets
        		//int stopY = boxText.getLinePixel(b.stop());
        		
        		//int startY = boxText.getLocationAtOffset(b.startOffset()).y;  //start is offset, not line number
        		//int stopY = boxText.getLocationAtOffset(b.stopOffset()).y; // + boxText.getLineHeight();

    			int startWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(b.getStartStyledTextOffset());
    			int stopWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(b.getStopStyledTextOffset());

    			System.out.println("start: " + startWidgetOffset + ", stop: " + stopWidgetOffset);
    			
    			int index = 0;
    			
    			if (startWidgetOffset == -1) {   //start marker is folded, recompute
    				System.out.println("new start");
    				startWidgetOffset = b.getStartStyledTextOffset();
    				while ((index < visibleRegions.length) && (startWidgetOffset >= visibleRegions[index].getOffset())) {
    					index++;
    				}
    				
    				startWidgetOffset = visibleRegions[index].getOffset();
    				if (b.getStopStyledTextOffset() < startWidgetOffset) {	//no region to draw, stop found before start
    					System.out.println("full box folded");
    					continue;
    				}
    				startWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(startWidgetOffset);
    			}
    			
    			if (stopWidgetOffset == -1) {  //stop marker is folded, recompute
    				System.out.println("new end");
    				stopWidgetOffset = b.getStopStyledTextOffset();
    				while ((index < visibleRegions.length) && (stopWidgetOffset < visibleRegions[index].getOffset() - visibleRegions[index].getLength())) {
    					index++;
    				}
    				index++;
    				stopWidgetOffset = visibleRegions[index].getOffset();
    				stopWidgetOffset = srcViewerE5.modelOffset2WidgetOffset(stopWidgetOffset);
    			}

    			int startY = styledText.getLocationAtOffset(startWidgetOffset).y;
    			int stopY = styledText.getLocationAtOffset(stopWidgetOffset).y;
    			
    			gc.setForeground(b.color);
        		gc.drawRectangle(1, startY, editorRectangle.width - 4, stopY - startY);
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

	

