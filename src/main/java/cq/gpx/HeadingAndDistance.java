package cq.gpx;

public class HeadingAndDistance {
	private Coordinates pointFrom;
	private Coordinates pointTo;
	private double heading = -1.0d;
	private double distance = -1.0d;

	public HeadingAndDistance(Coordinates pointFrom, Coordinates pointTo, double heading, double distance) {
		this.pointFrom = pointFrom;
		this.pointTo = pointTo;
		this.heading = heading;
		this.distance = distance;
	}
	
	public Coordinates getPointFrom() {
		return pointFrom;
	}
	
	public Coordinates getPointTo() {
		return pointTo;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public double getDistance() {
		return distance;
	}

}
