package edu.berkeley.eduride.editoroverlay.marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;

import edu.berkeley.eduride.editoroverlay.EditorOverlayActivator;

/**
 * Methods to create and manipulate eduride resource markers and editor
 * annotations
 * 
 */
public class Util {

	private static final String INLINE_MARKERID = EditorOverlayActivator.PLUGIN_ID
			+ ".inline";
	private static final String START_MARKERID = EditorOverlayActivator.PLUGIN_ID
			+ ".start";
	private static final String STOP_MARKERID = EditorOverlayActivator.PLUGIN_ID
			+ ".stop";
	private static final String INLINE_ANNOTATIONID = INLINE_MARKERID;
	private static final String START_ANNOTATIONID = START_MARKERID;
	private static final String STOP_ANNOTATIONID = STOP_MARKERID;

	
	// 
	
	public static boolean deleteAllMarkers(IResource resource) {
		try {
			resource.deleteMarkers(INLINE_MARKERID, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(START_MARKERID, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(STOP_MARKERID, false, IResource.DEPTH_ZERO);
			return true;
		} catch (CoreException e) {
			// whoopsee
			System.err.println(e);
			return false;
		}
	}



	// probably should separate inline from multiline?
	public static boolean deleteInlineMarkersWithId(IResource res, String id) {
		// TODO test this method
		try {
			IMarker[] marks = res.findMarkers(INLINE_MARKERID, false,
					IResource.DEPTH_ZERO);

			for (int i = 0; i < marks.length; i++) {
				String an_id = (marks[i].getAttribute("id", null));
				if (an_id != null && an_id.equals(id)) {
					marks[i].delete();
				}
			}
			return true;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	// probably should separate inline from multiline?
	public static boolean deleteMultilineMarkersWithId(IResource res, String id) {
		// TODO test this method
		try {
			IMarker[] marks = concatArrays(res.findMarkers(START_MARKERID,
					false, IResource.DEPTH_ZERO), res.findMarkers(
					STOP_MARKERID, false, IResource.DEPTH_ZERO));

			for (int i = 0; i < marks.length; i++) {
				String an_id = (marks[i].getAttribute("id", null));
				if (an_id != null && an_id.equals(id)) {
					marks[i].delete();
				}
			}
			return true;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// from
	// http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
	private static IMarker[] concatArrays(IMarker[] first, IMarker[] second) {
		IMarker[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
		  
	
	
	//////

	public static int getLineNumber(IMarker mark) {
		return mark.getAttribute(IMarker.LINE_NUMBER, -1);
	}

	public static int getCharStart(IMarker mark) {
		return mark.getAttribute(IMarker.CHAR_START, -1);
	}

	public static int getCharEnd(IMarker mark) {
		return mark.getAttribute(IMarker.CHAR_END, -1);
	}

	// ok, this is pretty lame.
	public static Position getPosition(Annotation ann, IAnnotationModel am) {
		return am.getPosition(ann);
	}
	
	
	//////////// 
	///// inline

	public static List<IMarker> getInlineMarkers(IResource res) {
		try {
			IMarker[] marks = res.findMarkers(INLINE_MARKERID, false,
					IResource.DEPTH_ZERO);
			return new ArrayList<IMarker>(Arrays.asList(marks));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static List<Annotation> getInlineAnnotations(IAnnotationModel am) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		Iterator<Annotation> it = am.getAnnotationIterator();
		while (it.hasNext()) {
			Annotation annotation = it.next();
			if (annotation.getType().equals(INLINE_ANNOTATIONID)) {
				annotations.add(annotation);
			}
		}
		return annotations;
	}

	// returns a List of arrays where START marker is [0] and END
	// marker is [1]
	public static List<IMarker[]> getMultilineMarkers(IResource res) {
		ArrayList<IMarker[]> ret = new ArrayList<IMarker[]>();
		IMarker[] startmarks;
		try {
			startmarks = res.findMarkers(START_MARKERID, false,
					IResource.DEPTH_ZERO);
			IMarker[] stopmarks = res.findMarkers(STOP_MARKERID, false,
					IResource.DEPTH_ZERO);
			for (IMarker startmark : startmarks) {
				String startmarkid = startmark.getAttribute("id", "");
				for (IMarker stopmark : stopmarks) {
					if (stopmark.getAttribute("id", "") == startmarkid) {
						IMarker[] arr = new IMarker[2];
						arr[0] = startmark;
						arr[1] = stopmark;
						ret.add(arr);
						break;
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ret;
		}

		return ret;

	}


	public static boolean createInlineMarker(IResource resource, int line,
			int charStart, int charStop, String id) {
		try {
			IMarker m = resource.createMarker(INLINE_MARKERID);
			deleteInlineMarkersWithId(resource, id); // delete old ones once we've made this

			m.setAttribute(IMarker.LINE_NUMBER, line);
			m.setAttribute(IMarker.CHAR_START, charStart);
			m.setAttribute(IMarker.CHAR_END, charStop);
			m.setAttribute("id", id);
			m.setAttribute(IMarker.MESSAGE, id); // text of the annotation
			m.setAttribute(IMarker.USER_EDITABLE, true);
			return true;
		} catch (CoreException e) {
			System.err.println(e);
			return false;
		}
	}
	
	
	//////////// 
	// MultiLine

	public static List<Annotation[]> getMultilineAnnotations(IAnnotationModel am) {
		ArrayList<Annotation> startannotations = new ArrayList<Annotation>();
		ArrayList<Annotation> stopannotations = new ArrayList<Annotation>();
		ArrayList<Annotation[]> annotations = new ArrayList<Annotation[]>();
		Iterator<Annotation> it = am.getAnnotationIterator();
		while (it.hasNext()) {
			Annotation annotation = it.next();
			if (annotation.getType().equals(START_ANNOTATIONID)) {  //kim changed == to .equals
				startannotations.add(annotation);
			} else if (annotation.getType().equals(STOP_ANNOTATIONID)) {  //== to .equals
				stopannotations.add(annotation);
			}
		}
		for (Annotation startannotation : startannotations) {
			String startannid = startannotation.getText();
			for (Annotation stopannotation : stopannotations) {
				if (stopannotation.getText().equals(startannid)) {   //== to .equals
					Annotation[] arr = new Annotation[2];
					arr[0] = startannotation;
					arr[1] = stopannotation;
					annotations.add(arr);
					break;
				}
			}
		}
		return annotations;

	}


	public static boolean createMultiLine(IResource resource, int lineStart,
			int lineStop, String id) {
		try {
			IMarker mstart = resource.createMarker(START_MARKERID);
			IMarker mstop = resource.createMarker(STOP_MARKERID);
			deleteMultilineMarkersWithId(resource, id); // delete old ones once we've made these

			mstart.setAttribute(IMarker.LINE_NUMBER, lineStart);
			//  should we make the length of the start marker to the stop marker?"
			mstop.setAttribute(IMarker.LINE_NUMBER, lineStop);
			mstart.setAttribute("id", id);
			mstop.setAttribute("id", id);
			mstart.setAttribute(IMarker.USER_EDITABLE, true);
			mstop.setAttribute(IMarker.USER_EDITABLE, true);
			mstart.setAttribute(IMarker.MESSAGE, id);
			mstop.setAttribute(IMarker.MESSAGE, id);
			return true;
		} catch (CoreException e) {
			System.err.println(e);
			return false;
		}
	}


	// //////////////

	public static void DEBUGMarkersToConsole(IResource res) {
		List<IMarker[]> ala = new ArrayList<IMarker[]>();
		List<IMarker> al = new ArrayList<IMarker>();
		// TODO Auto-generated method stub
		ala = getMultilineMarkers(res);
		for (IMarker[] ms : ala) {
			try {
				System.out.println("Multiline Marker "
						+ ms[0].getAttribute("id") + ": start loc=("
						+ getLineNumber(ms[0]) + "); endloc=("
						+ getLineNumber(ms[1]) + ")");
			} catch (CoreException e) {
				System.out.println("kabang");
			}
		}
		al = getInlineMarkers(res);
		for (IMarker m : al) {
			try {
				System.out.println("InLine Marker " + m.getAttribute("id")
						+ ":  loc=(" + getLineNumber(m) + "); charstart=("
						+ getCharStart(m) + "); charend=(" + getCharEnd(m)
						+ ")");
			} catch (CoreException e) {
				System.out.println("kabang");
			}
		}

	}

}
