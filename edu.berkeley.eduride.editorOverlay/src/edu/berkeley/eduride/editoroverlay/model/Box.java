package edu.berkeley.eduride.editoroverlay.model;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Color;

public class Box {

	
	public IAnnotationModel model;
	public Annotation start;
	public Color color;

	public Box(IAnnotationModel m, Annotation start) {
		this.model = m;
		this.start = start;
		this.color = getColor();
	}
	
	
	//can do static purple or semi-random purples
	protected Color getColor() {
		return new Color(null, 200, 120, 255);
		//return new Color(null, 170 + (int)(60 * Math.random()), 90 + (int)(60 * Math.random()), 205 + (int)(40 * Math.random()));
	}
	
	
	public Position getStyledTextPosition() {
		return model.getPosition(start);
	}
	
	//getter for start..  returns the offset (relative to start of file)
	public int getStartStyledTextOffset() {
		Position pos = getStyledTextPosition();
		return (getStartStyledTextOffset(pos));
	}

	public int getStartStyledTextOffset(Position pos) {
		return pos.getOffset();
	}
	
	//getter for stop..  returns the offset (relative to start of file)
	public int getStopStyledTextOffset() {
		Position pos = getStyledTextPosition();
		return (getStopStyledTextOffset(pos));
	}
	
	int getStopStyledTextOffset(Position pos) {
		return (pos.getOffset() + pos.getLength());
	}
	
	

}