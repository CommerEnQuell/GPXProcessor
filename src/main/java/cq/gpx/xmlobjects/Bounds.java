package cq.gpx.xmlobjects;

import java.math.BigDecimal;

public class Bounds extends XMLObject {
	private BigDecimal minlon;
	private BigDecimal minlat;
	private BigDecimal maxlon;
	private BigDecimal maxlat;

	public Bounds() {
		this("bounds");
	}
	
	public Bounds(String xmlTag) {
		super(xmlTag);
	}
	
	public void setMinlon(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.minlon = bd;
		} catch (Exception x) {
			this.minlon = null;
		}
	}
	
	public BigDecimal getMinlon() {
		return minlon;
	}
	
	public void setMinlat(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.minlat = bd;
		} catch (Exception x) {
			this.minlat = null;
		}
	}
	
	public BigDecimal getMinlat() {
		return minlat;
	}
	
	public void setMaxlon(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.maxlon = bd;
		} catch (Exception x) {
			this.maxlon = null;
		}
	}
	
	public BigDecimal getMaxlon() {
		return maxlon;
	}
	
	public void setMaxlat(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			this.maxlat = bd;
		} catch (Exception x) {
			this.maxlat = null;
		}
	}
	
	public BigDecimal getMaxlat() {
		return maxlat;
	}

	@Override
	protected String nativeToXML() {
		StringBuffer buf = new StringBuffer();
		buf.append("minlon=\"").append(minlon).append("\" minlat=\"");
		buf.append(minlat).append("\" maxlon=\"");
		buf.append(maxlon).append("\" maxlat=\"");
		buf.append(maxlat).append("\"");
		return buf.toString();
	}

}
