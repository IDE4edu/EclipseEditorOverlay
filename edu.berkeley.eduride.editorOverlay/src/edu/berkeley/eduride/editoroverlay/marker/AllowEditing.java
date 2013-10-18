package edu.berkeley.eduride.editoroverlay.marker;

import java.util.ArrayList;
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

	private static boolean deleteWithId(IResource resource, String id) {
		// TODO delete existing markers with same id;
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
				m.setAttribute(IMarker.TEXT, "AllowEditing:" + id);
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
			m.setAttribute(IMarker.USER_EDITABLE, false);
			m.setAttribute(IMarker.TEXT, "AllowEditing:" + id);
			return true;
		} catch (CoreException e) {
			System.err.println(e);
			return false;
		}
	}

}
