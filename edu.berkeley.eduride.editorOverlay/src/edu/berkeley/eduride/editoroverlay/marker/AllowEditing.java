package edu.berkeley.eduride.editoroverlay.marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.berkeley.eduride.editoroverlay.EditorOverlayActivator;

public class AllowEditing {

	public static final int STOP = 0;
	public static final int START = 1;
	public static final int INLINE = 2;

	private static final String MARKER_ID = EditorOverlayActivator.PLUGIN_ID
			+ ".allowediting";

	public static boolean deleteAll(IResource resource) {
		try {
			resource.deleteMarkers(MARKER_ID, false, IResource.DEPTH_ZERO);
			return true;
		} catch (CoreException e) {
			// whoopsee
			System.err.println(e);
			return false;
		}
	}

	public static IMarker[] getInlineMarkers(IResource res) {
		IMarker[] ret = new IMarker[0];
		int ri = 0;
		try {
			IMarker[] marks = res.findMarkers(MARKER_ID, false,
					IResource.DEPTH_ZERO);
			for (int mi = 0; mi < marks.length; mi++) {
				String thetype = (marks[mi].getAttribute("thetype", null));
				if (thetype != null && thetype.equals(INLINE)) {
					ret[ri] = marks[mi];
					ri++;
				}
			}
			return ret;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ret;
		}
	}

	// returns an array of inner arrays where START marker is [0] and END
	// marker is [1]
	public static IMarker[][] getMultilineMarkers(IResource res) {
		IMarker[][] ret = new IMarker[0][0];
		int ri = 0;	
		try {
			HashMap<String, IMarker[]> hm = new HashMap<String, IMarker[]>();
			IMarker[] marks = res.findMarkers(MARKER_ID, false, IResource.DEPTH_ZERO);
			for(int mi=0; mi < marks.length; mi++){
				int thetype = (marks[mi].getAttribute("thetype", -1));
				String id = (marks[mi].getAttribute("id", ""));
				if (!(hm.containsKey(id))) {
					hm.put(id,  new IMarker[2]);
				}
				if (thetype == START) {
					hm.get(id)[0] = marks[mi];
				} else if (thetype == STOP) {
					hm.get(id)[1] = marks[mi];
				}
			}
			
			// build  IMarker[][] from HashMap
			for (IMarker[] m : hm.values()) {
				if (m[0] != null && m[1] != null) {
					ret[ri] = m;
					ri++;
				} else {
					String id = "unknown";
					if (m[0] != null) {
						id = m[0].getAttribute("id", "unknown");
					} else if (m[1] != null) {
						id = m[0].getAttribute("id", "unknown");
					}
					System.err.println("ShiBe, only got one AllowEditing marker for id '" + id + "' and I expected two!");
				}
			}
			
			return ret;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ret;
		}
	}

	public static int getLineNumber(IMarker mark) {
		return mark.getAttribute(IMarker.LINE_NUMBER, -1);
	}

	public static int getCharStart(IMarker mark) {
		return mark.getAttribute(IMarker.CHAR_START, -1);
	}

	public static int getCharEnd(IMarker mark) {
		return mark.getAttribute(IMarker.CHAR_END, -1);
	}

	// ///////////////////

	private static boolean deleteWithId(IResource res, String id) {
		// TODO test this method
		try {
			IMarker[] marks = res.findMarkers(MARKER_ID, false,
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

	public static boolean createMultiLine(IResource resource, int lineStart,
			int lineStop, String id) {
		try {
			IMarker mstart = resource.createMarker(MARKER_ID);
			IMarker mstop = resource.createMarker(MARKER_ID);
			deleteWithId(resource, id); // delete old ones once we've made these

			mstart.setAttribute("thetype", START);
			mstart.setAttribute(IMarker.LINE_NUMBER, lineStart);
			mstop.setAttribute("thetype", STOP);
			mstop.setAttribute(IMarker.LINE_NUMBER, lineStop);

			List<IMarker> markers = new ArrayList<IMarker>();
			markers.add(mstart);
			markers.add(mstop);
			for (IMarker m : markers) {
				m.setAttribute("id", id);
				m.setAttribute(IMarker.USER_EDITABLE, false);
				m.setAttribute(IMarker.MESSAGE, "AllowEditing:" + id);
			}
			return true;
		} catch (CoreException e) {
			System.err.println(e);
			return false;
		}

	}

	public static boolean createInLine(IResource resource, int line,
			int charStart, int charStop, String id) {
		try {
			IMarker m = resource.createMarker(MARKER_ID);
			deleteWithId(resource, id); // delete old ones once we've made these

			m.setAttribute("thetype", INLINE);
			m.setAttribute(IMarker.LINE_NUMBER, line);
			m.setAttribute(IMarker.CHAR_START, charStart);
			m.setAttribute(IMarker.CHAR_END, charStop);
			m.setAttribute("id", id);
			m.setAttribute(IMarker.MESSAGE, "AllowEditing:" + id);
			m.setAttribute(IMarker.USER_EDITABLE, false);
			return true;
		} catch (CoreException e) {
			System.err.println(e);
			return false;
		}
	}

}
