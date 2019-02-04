package cq.gpx.xmlobjects;

import java.math.BigDecimal;

import cq.gpx.Location;

public class Trackpoint extends XMLObject implements Location {
	private BigDecimal lon;
	private BigDecimal lat;

	public Trackpoint() {
		this("trkpt");
	}
	
	public Trackpoint(String xmlTag) {
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
			this.lon = bd;
		} catch (Exception x) {
			x.printStackTrace();
			this.lon = null;
		}
	}
	
	public BigDecimal getLon() {
		return lon;
	}
	
	public void setLat(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.lat = bd;
		} catch (Exception x) {
			x.printStackTrace();
			this.lat = null;
		}
	}
	
	public BigDecimal getLat() {
		return lat;
	}
}
