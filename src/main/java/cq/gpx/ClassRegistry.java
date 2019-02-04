package cq.gpx;

import java.util.HashMap;
import java.util.Map;

import cq.gpx.xmlobjects.GPX;
import cq.gpx.xmlobjects.GenericXMLObject;
import cq.gpx.xmlobjects.Trackpoint;
import cq.gpx.xmlobjects.Waypoint;
import cq.gpx.xmlobjects.XMLObject;

public class ClassRegistry {
	private static ClassRegistry instance = null;
	private Map<String, Class<? extends XMLObject>> registry = new HashMap<String, Class<? extends XMLObject>>();

	private ClassRegistry() {
		registry.put("gpx", GPX.class);
		registry.put("wpt", Waypoint.class);
		registry.put("trkpt", Trackpoint.class);
	}
	
	public Class<? extends XMLObject> getObjectClass(String xmlTag) {
		Class<? extends XMLObject> retval;
		if (registry.containsKey(xmlTag)) {
			retval = registry.get(xmlTag);
		} else {
			retval = GenericXMLObject.class;
		}
		return retval;
	}
	
	public static ClassRegistry getInstance() {
		if (instance == null) {
			instance = new ClassRegistry();
		}
		return instance;
	}
}