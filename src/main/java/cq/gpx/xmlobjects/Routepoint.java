package cq.gpx.xmlobjects;

import java.math.BigDecimal;

import cq.gpx.Coordinates;
import cq.gpx.Location;

public class Routepoint extends XMLObject implements Location {
	private BigDecimal lon;
	private BigDecimal lat;
	
	public Routepoint() {
		this("rtept");
	}
	
	public Routepoint(String xmlTag) {
		super(xmlTag);
	}
	
	public void setLon(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.lon = bd;
		} catch (Exception x) {
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
			this.lat = null;
		}
	}
	
	public BigDecimal getLat() {
		return lat;
	}
	
	public void setCoordinates(Coordinates coord) {
		this.lat = coord.getLat();
		this.lon = coord.getLon();
	}

	@Override
	protected String nativeToXML() {
		StringBuffer buf = new StringBuffer("lon=\"");
		buf.append(lon).append("\" lat=\"");
		buf.append(lat).append("\"");
		return buf.toString();
	}

}
