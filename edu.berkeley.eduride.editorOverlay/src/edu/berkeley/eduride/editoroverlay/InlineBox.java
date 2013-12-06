package edu.berkeley.eduride.editoroverlay;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

public class InlineBox {
	//start and stop are the line numbers corresponding to where user can type
	private IAnnotationModel model;
	private Annotation start;
	Color color = purple();
	
	public InlineBox (IAnnotationModel m, Annotation Start) {
		model = m;
		start = Start;
	}
	
	public InlineBox (IAnnotationModel m, Annotation Start, Color  boxColor) {
		this(m, Start);
		color = boxColor;
	}
	
	// TODO move to BCEO, I think
	//can do static purple or semi-random purples
	private Color purple() {
		return new Color(null, 200, 120, 255);
		//return new Color(null, 170 + (int)(60 * Math.random()), 90 + (int)(60 * Math.random()), 205 + (int)(40 * Math.random()));
	}

	
	public Position getStyledTextPosition() {
		return model.getPosition(start);
	}
	
	//getter for start..  returns the offset (relative to start of file)
	int getStartStyledTextOffset() {
		return (model.getPosition(start).getOffset());
	}
	
	//getter for stop..  returns the offset (relative to start of file)
	int getStopStyledTextOffset() {
		return (getStartStyledTextOffset() + model.getPosition(start).getLength());
	}
	
	
//	private int startWidgetOffset = -1;
//	int getStartWidgetOffset() {
//		return startWidgetOffset;
//	}
//	void setStartWidgetOffset(int off) {
//		startWidgetOffset=off;
//	}
//	
//	
//	private int stopWidgetOffset = -1;
//	int getStopWidgetOffset() {
//		return stopWidgetOffset;
//	}
//	void setStopWidgetOffset(int off) {
//		stopWidgetOffset=off;
//	}

	
	// top left point
	public int x = -1;
	public int y = -1;

	public int width = -1;

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
		toReturn = "INLINE pos: " + getStyledTextPosition();
		return toReturn;
	}
	
	
	
	
	
	
	


	
}
