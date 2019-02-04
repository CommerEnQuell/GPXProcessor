package cq.gpx;

import java.math.BigDecimal;

public class Coordinates {
	private BigDecimal latitude;
	private BigDecimal longitude;

	public Coordinates(BigDecimal latitude, BigDecimal longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Coordinates(Location location) {
		this(location.getLat(), location.getLon());
	}
	
	public BigDecimal getLat() {
		return latitude;
	}
	
	public BigDecimal getLon() {
		return longitude;
	}
}
