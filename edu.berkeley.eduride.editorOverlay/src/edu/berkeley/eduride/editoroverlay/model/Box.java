package edu.berkeley.eduride.editoroverlay.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import edu.berkeley.eduride.base_plugin.util.Console;
import edu.berkeley.eduride.editoroverlay.marker.Util;

public class Box {

	
	public IAnnotationModel model;
	public Annotation start;
	public Color color;

	public Box(IAnnotationModel m, Annotation start) {
		this.model = m;
		this.start = start;
		this.color = Util.stringToColor(start.getText());  //cram color into text when creating, restore color here
	}
	
	
	//can do static purple or semi-random purples
	protected Color getDefaultColor() {
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
	
	public void delete() {
		try {
			((SimpleMarkerAnnotation)start).getMarker().delete();
		} catch (CoreException e) {
			Console.err("Couldn't delete marker, uh oh: " + e.getMessage());
		}
		model.removeAnnotation(start);
		
	}

}