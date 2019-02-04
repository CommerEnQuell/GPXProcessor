package cq.gpx.utils;

import java.math.BigDecimal;

import cq.gpx.Coordinates;
import cq.gpx.HeadingAndDistance;

public class GPXUtils {

	private GPXUtils() {}
	
	public static HeadingAndDistance calculateHeadingAndDistance(Coordinates pointFrom, Coordinates pointTo) {
		BigDecimal prevLat = pointFrom.getLat();
		BigDecimal prevLon = pointFrom.getLon();
		BigDecimal lat = pointTo.getLat();
		BigDecimal lon = pointTo.getLon();
		BigDecimal deltaLat = lat.subtract(prevLat);
		BigDecimal deltaLon = lon.subtract(prevLon);
		double latRadians = Math.PI * lat.doubleValue() / 180.0d;
		double distDegLat = 20000000.0d / 180.0d;
		double distDegLon = 40000000.0d * Math.cos(latRadians) / 360.0d;
		double distLat = deltaLat.doubleValue() * distDegLat;
		double distLon = deltaLon.doubleValue() * distDegLon;
		int quarter = 0;
		if (distLat >= 0 && distLon >= 0) {
			quarter = 1;
		} else if (distLat >= 0 && distLon < 0) {
			quarter = 2;
		} else if (distLat < 0 && distLon <= 0) {
			quarter = 3;
		} else {
			quarter = 4;
		}
		double dist = Math.sqrt(distLat * distLat + distLon * distLon);
		double heading = -400.0d;
		if (deltaLon.doubleValue() == 0.0d) {
			if (deltaLat.compareTo(new BigDecimal(0)) < 0) {
				heading = 180.0d;
			} else {
				heading = 0.0d;
			}
		} else {
			double acotRad = Math.atan(distLat / distLon);
			double angleDeg = acotRad * 180 / Math.PI;
			switch (quarter) {
			case 1:
				heading = 90.0d - angleDeg;
				break;
			case 2:
				heading = 360.0d + angleDeg;
				break;
			case 3:
				heading = 270.0d - angleDeg;
				break;
			case 4:
				heading = 90.0d - angleDeg;
				break;
			}
		}
		
		HeadingAndDistance retval = new HeadingAndDistance(pointFrom, pointTo, heading, dist);
		return retval;
		
	}
	
	public static String formatInteger(int i, int size) {
		StringBuffer buf = new StringBuffer();
		buf.append(i);
		int theSize = (i < 0 ? size + 1 : size);
		int thePos  = (i < 0 ? 1 : 0);
		while (buf.length() < theSize) {
			buf.insert(thePos, "0");
		}
		return buf.toString();
	}
}
