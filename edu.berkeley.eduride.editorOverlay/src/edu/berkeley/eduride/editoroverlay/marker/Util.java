package edu.berkeley.eduride.editoroverlay.marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import edu.berkeley.eduride.base_plugin.util.Console;
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
			Console.err(e);
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
			Console.err(e);
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
			Console.err(e);
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
	
	
	public static boolean isBCEOAnnoation(Annotation ann) {
		String type = ann.getType();
		return (type.equals(INLINE_ANNOTATIONID) ||
				type.equals(START_ANNOTATIONID) || 
				type.equals(STOP_ANNOTATIONID));
	}
	
	//////////// 
	///// inline

	public static List<IMarker> getInlineMarkers(IResource res) {
		try {
			IMarker[] marks = res.findMarkers(INLINE_MARKERID, false,
					IResource.DEPTH_ZERO);
			return new ArrayList<IMarker>(Arrays.asList(marks));
		} catch (CoreException e) {
			Console.err(e);;
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
			Console.err(e);
			return ret;
		}

		return ret;

	}


	public static boolean createInlineMarker(IResource resource, 
			int offsetStart, int offsetStop, String id) {
		try {
			IMarker m = resource.createMarker(INLINE_MARKERID);
			deleteInlineMarkersWithId(resource, id); // delete old ones once we've made this

			//TODO -- this ignores the line number when CHAR_START is used...
			//m.setAttribute(IMarker.LINE_NUMBER, line);
			
			m.setAttribute(IMarker.CHAR_START, offsetStart);
			m.setAttribute(IMarker.CHAR_END, offsetStop);
			m.setAttribute("id", id);
			m.setAttribute(IMarker.MESSAGE, id); // text of the annotation
			m.setAttribute(IMarker.USER_EDITABLE, true);
			return true;
		} catch (CoreException e) {
			Console.err(e);
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
			if (!(annotation.isMarkedDeleted())) {
				if (annotation.getType().equals(START_ANNOTATIONID)) { 
					startannotations.add(annotation);
				} else if (annotation.getType().equals(STOP_ANNOTATIONID)) {
					stopannotations.add(annotation);
				}
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


	public static boolean createMultiLine(IResource resource, int startPos,
			int lineStop, String id) {
		try {
			IMarker mstart = resource.createMarker(START_MARKERID);
			IMarker mstop = resource.createMarker(STOP_MARKERID);
			deleteMultilineMarkersWithId(resource, id); // delete old ones once we've made these

			//mstart.setAttribute(IMarker.LINE_NUMBER, lineStart);
			mstart.setAttribute(IMarker.CHAR_START, startPos);
			mstart.setAttribute(IMarker.CHAR_END, startPos + 1);
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
			Console.err(e);
			return false;
		}
	}
	
	///////////////////
	
	//Move stop annotations to the start of the line, move start annotations to the end
	public static void fixMultiLine(IAnnotationModel annotationModel, StyledText st) {
		
		Display.getDefault().asyncExec(new FixMultilineTask(annotationModel, st));
		
	}
	
	private static class FixMultilineTask implements Runnable {
		private IAnnotationModel annotationModel;
		private StyledText st;

		public FixMultilineTask(IAnnotationModel annotationModel, StyledText styledText) {
			this.annotationModel = annotationModel;
			st = styledText;
		}

		public void run() {

			Iterator<Annotation> it = annotationModel.getAnnotationIterator();
			while (it.hasNext()) {
				Annotation annotation = it.next();
				if (!(annotation.isMarkedDeleted())) {
					if (annotation.getType().equals(STOP_ANNOTATIONID)) {
						int offset = (annotationModel.getPosition(annotation).getOffset());  //get old offset
						int newOff = st.getOffsetAtLine(st.getLineAtOffset(offset));  //push back to start of line
						if (offset != newOff) {
							(annotationModel.getPosition(annotation)).setOffset(newOff);  //change position
							//((AnnotationModel)annotationModel).modifyAnnotationPosition(annotation, new Position(newOff));
						}
					} else if (annotation.getType().equals(START_ANNOTATIONID)) {
						int offset = (annotationModel.getPosition(annotation).getOffset());  //get old offset
						int newOff = st.getOffsetAtLine(st.getLineAtOffset(offset) + 1) - 1;  //push back to start of line
						if (offset != newOff) {
							(annotationModel.getPosition(annotation)).setOffset(newOff);  //change position
						}
					}
				}
			}
		}
	}


	// //////////////

	public static void DEBUGMarkersToConsole(IResource res) {
		List<IMarker[]> ala = new ArrayList<IMarker[]>();
		List<IMarker> al = new ArrayList<IMarker>();
		
		ala = getMultilineMarkers(res);
		for (IMarker[] ms : ala) {
			try {
				Console.msg("Multiline Marker "
						+ ms[0].getAttribute("id") + ": start loc=("
						+ getLineNumber(ms[0]) + "); endloc=("
						+ getLineNumber(ms[1]) + ")");
			} catch (CoreException e) {
				Console.msg("kabang");
			}
		}
		al = getInlineMarkers(res);
		for (IMarker m : al) {
			try {
				Console.msg("InLine Marker " + m.getAttribute("id")
						+ ":  loc=(" + getLineNumber(m) + "); charstart=("
						+ getCharStart(m) + "); charend=(" + getCharEnd(m)
						+ ")");
			} catch (CoreException e) {
				Console.msg("kabang");
			}
		}

	}
	
	
	
	
	/////////////////  Kim's color things
	
	public static String colorToString (Color c) {
		return "{" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "}";
	}
	
	public static Color stringToColor (String s) {
		//if (s.matches("\\{\\d+,\\d+,\\d+\\}")) {  //blurgh
		try {
			s = s.substring(s.indexOf("{") + 1, s.length() - 1);
			String[] colors = s.split(",");
			int red = Integer.parseInt(colors[0]);
			int green = Integer.parseInt(colors[1]);
			int blue = Integer.parseInt(colors[2]);
			return new Color(null, red, green, blue);
		} catch (Exception e) {
			return new Color(null, 200, 120, 255);
		}
	}
	
	
	///////////////  Cram annotations into XML, and unpack later
	
	public static void exportMarkers (IResource res) {
		Console.msg(generateXML(res));
	}
	
	public static String generateXML (IResource res) {
		
		//general info
		String xml = "<annotationinfo>\n";
		String path = (res.getLocation()).toPortableString();  //should return OS independent path?
		//String path = (res.getLocationURI()).toString();  //URI version
		xml += "\t<file>" + path + "</file>\n";
		
		//inline markers
		List<IMarker> inline = getInlineMarkers(res);
		for (IMarker ann : inline) {
			String tempXML = "\t<inlinePB>";
			try {
				tempXML += ann.getAttribute(IMarker.MESSAGE) + " ";
				tempXML += ann.getAttribute(IMarker.CHAR_START) + " ";
				tempXML += ann.getAttribute(IMarker.CHAR_END) + "</inlinePB>\n";
				
				xml += tempXML;
			} catch (Exception e) {
				System.out.println("problem generating xml");
			}
		}
		
		//multiline markers
		List<IMarker[]> multiline = getMultilineMarkers(res);
		for (IMarker[] ann : multiline) {
			String tempXML = "\t<multilinePB>";  //try/catch required...  make sure we can build full string before adding it in
			try {
				tempXML += ann[0].getAttribute(IMarker.MESSAGE) + " ";
				tempXML += ann[0].getAttribute(IMarker.CHAR_START) + " ";
				tempXML += ann[1].getAttribute(IMarker.LINE_NUMBER) + "</multilinePB>\n";
				
				xml += tempXML;
			} catch (CoreException e) {
				System.out.println("something broke while generating xml");
			}
		}
		
		String base64 = stringToBase64(getContents((IFile)res));
		xml += "\t<base64>" + base64 + "</base64>\n";
		
		System.out.println(base64ToString(base64));
		//System.out.println(base64.length());  Can we make the base64 xml prettier?  add newlines + tabs, maybe?
		
		xml += "</annotationinfo>\n";
		return xml;
	}
	
	
	/** Get contents of an Eclipse resource file as string. */
	public static String getContents(IFile file) {
		try {
			InputStream input = file.getContents();
			
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append('\n');
			}
			
			String contents = buffer.toString();
			input.close();
			return contents;
		} catch (Exception e) {
			System.out.println("Problem reading file contents.");
			return null;
		}
	}

	
	//input: some string of text
	//output: a string representing the base64 encoding
	public static String stringToBase64(String data) {
		return DatatypeConverter.printBase64Binary(data.getBytes());
	}
	
	
	//input: A string of base64 encoded data.  Do NOT include the <base64> tags!
	//output: A string represented the decoded base64
	//extra whitespace/newlines in the xml should be ignored
	public static String base64ToString(String base64) {
		
		base64 = base64.replaceAll("\\s+","");  //maybe?
		
		byte[] undo = DatatypeConverter.parseBase64Binary(base64);
        try {
            return new String(undo, "UTF-8");
        } catch (Exception e) {
            System.out.println("Error parsing base64");
            return null;
        }
	}


	
	
	//Take a path (as a string) and turn it back into a resource
	public static IResource getResFromPath(String path) {
		//http://www.eclipsezone.com/eclipse/forums/m92221730.html
		IPath location = new Path(path);
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		IFile file = workspace.getRoot().getFileForLocation(location); 
		return file;
	}
	
	//expected: id start stop
	//do NOT include <inlinePB> tags!
	//to get a resource from a filepath, use getResFromPath(String path)
	public static void importInlineXML (String xml, IResource res) {
		try {
			xml = xml.trim();
			String[] pieces = xml.split(" ");
			int start = Integer.parseInt(pieces[1]);
			int stop = Integer.parseInt(pieces[2]);
			createInlineMarker(res, start, stop, pieces[0]);
		} catch (Exception e) {
			System.out.println("Could not parse: " + xml);
		}
	}
	
	//expected: id start stop
	//do NOT include <inlinePB> tags!
	//to get a resource from a filepath, use getResFromPath(String path)
	public static void importMultilineXML (String xml, IResource res) {
		try {
			xml = xml.trim();
			String[] pieces = xml.split(" ");
			int startPos = Integer.parseInt(pieces[1]);
			int stopLine = Integer.parseInt(pieces[2]);
			createMultiLine(res, startPos, stopLine, pieces[0]);
		} catch (Exception e) {
			System.out.println("Could not parse: " + xml);
		}
	}
}
