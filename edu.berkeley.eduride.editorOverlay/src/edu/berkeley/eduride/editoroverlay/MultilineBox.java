package edu.berkeley.eduride.editoroverlay;

import org.eclipse.swt.graphics.Color;

public class MultilineBox {
	//start and stop are the line numbers corresponding to where user can type
	private int start;
	private int stop;
	Color color = new Color(null, 200, 120, 255);
	
	public MultilineBox (int Start, int Stop) {
		start = Start;
		stop = Stop;
	}
	
	public MultilineBox (int Start, int Stop, Color  boxColor) {
		this(Start, Stop);
		color = boxColor;
	}

	//getter for start
	int start() {
		return start;
	}
	
	//getter for stop
	int stop() {
		return stop;
	}
}
