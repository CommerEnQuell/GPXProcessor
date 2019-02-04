package cq.gpx.xmlobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GPX extends XMLObject {
	private String xmlns;
	private String xsi_schemaLocation;
	private String xmlns_xsi;
	private String creator;
	private String version;

	public GPX() {
		this("gpx");
	}
	
	public GPX(String xmlTag) {
		super(xmlTag);
	}

	@Override
	protected String nativeToXML() {
		StringBuffer buf = new StringBuffer();
		buf.append("xmlns=\"").append(xmlns).append("\" xsi:schemaLocation=\"");
		buf.append(xsi_schemaLocation).append("\" xmlns:xsi=\"");
		buf.append(xmlns_xsi).append("\" creator=\"").append(creator);
		buf.append("\" version=\"").append(version).append("\"");
		for (Map.Entry<String, String> me : getProperties().entrySet()) {
			buf.append(" ").append(me.getKey()).append("=\"");
			buf.append(me.getValue()).append("\"");
		}
		return buf.toString();
	}
	
	public void setXmlns(String s) {
		this.xmlns = s;
	}
	
	public String getXmlns() {
		return xmlns;
	}
	
	public void setXsi_schemaLocation(String s) {
		this.xsi_schemaLocation = s;
	}
	
	public String getXsi_schemaLocation() {
		return xsi_schemaLocation;
	}
	
	public void setXmlns_xsi(String s) {
		this.xmlns_xsi = s;
	}
	
	public String getXmlns_xsi() {
		return xmlns_xsi;
	}

	public void setCreator(String s) {
		this.creator = s;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setVersion(String s) {
		this.version = s;
	}
	
	public String getVersion() {
		return version;
	}
	
	public List<Waypoint> getAllWaypoints() {
		List<Waypoint> retval = new ArrayList<Waypoint>();
		getWaypointsRecursively(this, retval);
		return retval;
	}
	
	public void getWaypointsRecursively(XMLObject parent, List<Waypoint> waypoints) {
		for (XMLObject subObject : parent.getSubObjects()) {
			if (subObject instanceof Waypoint) {
				Waypoint wp = (Waypoint) subObject;
				waypoints.add(wp);
			} else {
				getWaypointsRecursively(subObject, waypoints);
			}
		}
	}
	
	public List<Trackpoint> getAllTrackpoints() {
		List<Trackpoint> retval = new ArrayList<Trackpoint>();
		getTrackpointsRecursively(this, retval);
		System.out.println(retval.size() + " trackpoints have been found");
		return retval;
	}
	
	private void getTrackpointsRecursively(XMLObject parent, List<Trackpoint> trackpoints) {
		for (XMLObject subObject : parent.getSubObjects()) {
			if (subObject instanceof Trackpoint) {
				Trackpoint tp = (Trackpoint) subObject;
				trackpoints.add(tp);
			} else {
				getTrackpointsRecursively(subObject, trackpoints);
			}
		}
	}
}
