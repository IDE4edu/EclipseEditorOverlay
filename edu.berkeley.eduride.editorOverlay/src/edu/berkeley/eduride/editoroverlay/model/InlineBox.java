package edu.berkeley.eduride.editoroverlay.model;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

public class InlineBox extends Box {
	
	
	public InlineBox (IAnnotationModel m, Annotation start) {
		super(m, start);
	}
	
	public InlineBox (IAnnotationModel m, Annotation start, Color boxColor) {
		super(m, start);
		color = boxColor;
	}
	

	// styled Text position stuff is straight from Box.
	
	
	
	
	
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
