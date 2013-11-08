package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

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
	int startOffset() {
		return (model.getPosition(start).getOffset());
	}
	
	//getter for stop..  returns the offset (relative to start of file)
	int stopOffset() {
		return (model.getPosition(stop).getOffset());
	}
	
	//get start line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int startLine(StyledText st) {
		return st.getLineAtOffset(startOffset());
	}
	
	//get ending line number.  Pass in boxText if you're in EditorVerifyKeyListener.
	int stopLine(StyledText st) {
		return st.getLineAtOffset(stopOffset());
	}
	
	public String toString() {
		String toReturn;
		toReturn = "Start: " + start + ", Line: " + model.getPosition(start).getOffset();
		toReturn += "\nStop: " + stop + ", Line: " + model.getPosition(stop).getOffset();
		return toReturn;
	}
}
