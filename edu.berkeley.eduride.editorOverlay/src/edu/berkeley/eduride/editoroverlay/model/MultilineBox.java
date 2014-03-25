package edu.berkeley.eduride.editoroverlay.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import edu.berkeley.eduride.base_plugin.util.Console;

public class MultilineBox extends Box {
	//start and stop are the line numbers corresponding to where user can type

	
	private Annotation stop;

	
	public MultilineBox (IAnnotationModel m, Annotation start, Annotation stop) {
		super (m, start);
		this.stop = stop;
	}
	
	public MultilineBox (IAnnotationModel m, Annotation Start, Annotation Stop, Color  boxColor) {
		this(m, Start, Stop);
		color = boxColor;
	}
	
	
	
	// styled text position -- need to override Stop position 
	public int getStartStyledTextOffset() {
		Position pos = getStyledTextPosition();
		return (getStartStyledTextOffset(pos));
	}

	public int getStartStyledTextOffset(Position pos) {
		return pos.getOffset() + 1;
	}

	
	@Override
	public int getStopStyledTextOffset() {
		return (model.getPosition(stop).getOffset());
	}
	
	// assume this is the position of the stop annotation... I guess...
	@Override
	public int getStopStyledTextOffset(Position stopPos) {
		return stopPos.getOffset();
	}
	
	
	////// storage for use during drawBox()
	
	private int startWidgetOffset = -1;
	public int getStartWidgetOffset() {
		return startWidgetOffset;
	}
	void setStartWidgetOffset(int off) {
		startWidgetOffset=off;
	}
	
	
	private int stopWidgetOffset = -1;
	public int getStopWidgetOffset() {
		return stopWidgetOffset;
	}
	public void setStopWidgetOffset(int off) {
		stopWidgetOffset=off;
	}
	
	
	private int startPixelY = -1;
	public int getStartPixelY() {
		return startPixelY;
	}
	public void setStartPixelY(int y) {
		startPixelY=y;
	}
	
	
	private int stopPixelY = -1;
	public int getStopPixelY() {
		return stopPixelY;
	}
	public void setStopPixelY(int y) {
		stopPixelY=y;
	}
	
	
	
	//get start line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int startLine(StyledText st) {
		return st.getLineAtOffset(getStartStyledTextOffset()) + 1;
	}
	
	//get ending line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int stopLine(StyledText st) {
		return st.getLineAtOffset(getStopStyledTextOffset());
	}
	
	public void delete() {
		super.delete();
		try {
			((SimpleMarkerAnnotation)stop).getMarker().delete();
		} catch (CoreException e) {
			Console.err("Couldn't delete multiline-box marker, uh oh: " + e.getMessage());
		}
		model.removeAnnotation(stop);
	}
	
	
	public String toString() {
		String toReturn;
		toReturn = "Start: " + start + ", Line: " + model.getPosition(start).getOffset();
		toReturn += "\nStop: " + stop + ", Line: " + model.getPosition(stop).getOffset();
		return toReturn;
	}
	
}
