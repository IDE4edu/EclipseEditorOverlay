package edu.berkeley.eduride.editoroverlay;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

public class MultilineBox {
	//start and stop are the line numbers corresponding to where user can type
	private IAnnotationModel model;
	private Annotation start;
	private Annotation stop;
	Color color = purple();
	
	public MultilineBox (IAnnotationModel m, Annotation Start, Annotation Stop) {
		model = m;
		start = Start;
		stop = Stop;
	}
	
	public MultilineBox (IAnnotationModel m, Annotation Start, Annotation Stop, Color  boxColor) {
		this(m, Start, Stop);
		color = boxColor;
	}
	
	//can do static purple or semi-random purples
	private Color purple() {
		return new Color(null, 200, 120, 255);
		//return new Color(null, 170 + (int)(60 * Math.random()), 90 + (int)(60 * Math.random()), 205 + (int)(40 * Math.random()));
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
