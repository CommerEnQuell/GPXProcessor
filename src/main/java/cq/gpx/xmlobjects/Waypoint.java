package cq.gpx.xmlobjects;

import java.math.BigDecimal;

import cq.gpx.Location;

public class Waypoint extends XMLObject implements Location {
	public BigDecimal lon;
	public BigDecimal lat;

	public Waypoint() {
		this("wpt");
	}
	
	public Waypoint(String xmlTag) {
		super(xmlTag);
	}

	@Override
	protected String nativeToXML() {
		StringBuffer buf = new StringBuffer("lon=\"");
		buf.append(lon).append("\" lat=\"");
		buf.append(lat).append("\"");
		return buf.toString();
	}
	
	public void setLon(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			lon = bd;
		} catch (Exception x) {
			x.printStackTrace();
			lon = null;
		}
	}
	
	public BigDecimal getLon() {
		return lon;
	}
	
	public void setLat(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			lat = bd;
		} catch (Exception x) {
			x.printStackTrace();
			lat = null;
		}
	}
	
	public BigDecimal getLat() {
		return lat;
	}

}
