package edu.berkeley.eduride.editoroverlay;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Color;

import edu.berkeley.eduride.editoroverlay.marker.Util;

public class MultilineBox {
	//start and stop are the line numbers corresponding to where user can type
	private IMarker start;
	private IMarker stop;
	Color color = new Color(null, 200, 120, 255);
	
	public MultilineBox (IMarker Start, IMarker Stop) {
		start = Start;
		stop = Stop;
	}
	
	public MultilineBox (IMarker Start, IMarker Stop, Color  boxColor) {
		this(Start, Stop);
		color = boxColor;
	}

	//getter for start
	int start() {
		return Util.getLineNumber(start);	//TODO: make sure this works with AllowEditing's line number fetching
	}
	
	//getter for stop
	int stop() {
		return Util.getLineNumber(stop);
	}
	
	public String toString() {
		String toReturn;
		toReturn = "Start: " + start + ", Line: " + Util.getLineNumber(start);
		toReturn += "\nStop: " + stop + ", Line: " + Util.getLineNumber(stop);
		return toReturn;
	}
}
