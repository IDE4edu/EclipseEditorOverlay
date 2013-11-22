package edu.berkeley.eduride.editoroverlay;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import edu.berkeley.eduride.editoroverlay.marker.Util;

public class MultilineBox {
	//start and stop are the line numbers corresponding to where user can type
	private IAnnotationModel model;
	private Annotation start;
	private Annotation stop;
	Color color = new Color(null, 200, 120, 255);
	
	public MultilineBox (IAnnotationModel m, Annotation Start, Annotation Stop) {
		model = m;
		start = Start;
		stop = Stop;
	}
	
	public MultilineBox (IAnnotationModel m, Annotation Start, Annotation Stop, Color  boxColor) {
		this(m, Start, Stop);
		color = boxColor;
	}

	//getter for start..  returns the offset (relative to start of file)
	int getStartStyledTextOffset() {
		return (model.getPosition(start).getOffset());
	}
	
	//getter for stop..  returns the offset (relative to start of file)
	int getStopStyledTextOffset() {
		return (model.getPosition(stop).getOffset());
	}
	
	
	private int startWidgetOffset = -1;
	int getStartWidgetOffset() {
		return startWidgetOffset;
	}
	void setStartWidgetOffset(int off) {
		startWidgetOffset=off;
	}
	
	
	private int stopWidgetOffset = -1;
	int getStopWidgetOffset() {
		return stopWidgetOffset;
	}
	void setStopWidgetOffset(int off) {
		stopWidgetOffset=off;
	}
	
	
	private int startPixelY = -1;
	int getStartPixelY() {
		return startPixelY;
	}
	void setStartPixelY(int y) {
		startPixelY=y;
	}
	
	
	private int stopPixelY = -1;
	int getStopPixelY() {
		return stopPixelY;
	}
	void setStopPixelY(int y) {
		stopPixelY=y;
	}
	
	
	
	//get start line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int startLine(StyledText st) {
		return st.getLineAtOffset(getStartStyledTextOffset());
	}
	
	//get ending line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int stopLine(StyledText st) {
		return st.getLineAtOffset(getStopStyledTextOffset());
	}
	
	public String toString() {
		String toReturn;
		toReturn = "Start: " + start + ", Line: " + model.getPosition(start).getOffset();
		toReturn += "\nStop: " + stop + ", Line: " + model.getPosition(stop).getOffset();
		return toReturn;
	}
	
	
	
	
	
	
	


	
}
