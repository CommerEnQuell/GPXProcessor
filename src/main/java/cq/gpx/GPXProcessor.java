package cq.gpx;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.FormAction;

import cq.gpx.utils.GPXUtils;
import cq.gpx.xmlobjects.Bounds;
import cq.gpx.xmlobjects.GPX;
import cq.gpx.xmlobjects.GenericXMLObject;
import cq.gpx.xmlobjects.Routepoint;
import cq.gpx.xmlobjects.Trackpoint;
import cq.gpx.xmlobjects.Waypoint;
import cq.gpx.xmlobjects.XMLObject;

public class GPXProcessor {
	private String infile;
	private String outfile;
	private String routeName;
	private String header = null;
	
	public GPXProcessor(String infile, String outfile, String routeName) {
		this.infile = infile;
		this.outfile = outfile;
		this.routeName = routeName;
	}
	
	public void process() throws Exception {
		File inputFile = null;
		BufferedReader reader = null;
		StringBuffer buf = new StringBuffer();
		try {
			inputFile = new File(infile);
			reader = new BufferedReader(new FileReader(inputFile));
			String line = reader.readLine();
			while (line != null) {
				buf.append(line).append("\n");
				line = reader.readLine();
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception x) {}
			}
		}
		GPX root = (GPX) processInternal(buf);
		List<Waypoint> waypoints = root.getAllWaypoints();
		List<Trackpoint> trackpoints = root.getAllTrackpoints();
		List<HeadingAndDistance> hands = getTrackpointDetails(trackpoints);
		List<Routepoint> route = processRoute(hands, waypoints);
		routepointsOverview(route);
		route = reprocessRoute(route);
		routepointsOverview(route);
		writeOutfile(waypoints, route);
	}
	
	private XMLObject processInternal(StringBuffer buf) throws Exception {
		XMLObject retval = null;
		int offset = buf.indexOf("<");
		while (offset >= 0) {
			int pos = offset + 1;
			char firstChar = buf.charAt(pos);
			int end = buf.indexOf(">", offset);
			char lastChar = buf.charAt(end - 1);
			if (firstChar == '?') {
				if (lastChar == '?') {
					if (header == null) {
						header = buf.substring(offset, end + 1);
					}
				}
				offset = buf.indexOf("<", end);
				continue;
			}
			String elem = buf.substring(offset, end + 1);
			if (lastChar == '/') {
				processSingleLine(elem, null);
				offset = buf.indexOf("<", end);
				continue;
			}
			String content = elem.substring(1, elem.length() - 2);
			int spcPos = content.indexOf(' ');
			String xmlTag;
			if (spcPos > 0) {
				xmlTag = content.substring(0, spcPos);
			} else {
				xmlTag = content;
			}
			String searchStr = "/" + xmlTag + ">";
			pos = buf.indexOf(searchStr);
			end = buf.indexOf(">", pos);
			String xmlRoot = buf.substring(offset, end + 1);
			retval = processXMLObject(xmlRoot, null);
			offset = buf.indexOf("<", pos);
		}
		return retval;
	}
	
	private XMLObject processXMLObject(String xml, XMLObject parent) throws Exception {
		int endTag = xml.indexOf(">");
		int spcPos = xml.indexOf(" ");
		String xmlTag;
		String properties = "";
		if (spcPos > 0 && spcPos < endTag) {
			xmlTag = xml.substring(1,  spcPos);
			properties = xml.substring(spcPos + 1, endTag);
		} else {
			if (xml.charAt(endTag - 1) == '/') {
				xmlTag = xml.substring(1, endTag - 1);
			} else {
				xmlTag = xml.substring(1, endTag);
			}
		}
		Map<String, String> propertyMap = processProperties(properties);
		XMLObject retval = instantiate(xmlTag, propertyMap);
		if (parent != null) {
			parent.addSubObject(retval);
		}
		
		int offset = xml.indexOf("<", endTag);
		while (offset > 0) {
			if (xml.charAt(offset + 1) == '/') {
				String subs = xml.substring(endTag, offset);
				if (subs != null && subs.trim().length() > 0) {
					retval.addProperty("", subs);
				}
			}
			endTag = xml.indexOf(">", offset);
			spcPos = xml.indexOf(" ", offset);
			if (xml.substring(offset + 1).startsWith("![CDATA[")) {
				int start = offset + 1 + "![CDATA[".length();
				int end   = xml.indexOf("]]", start);
				retval.setCDATA(xml.substring(start, end));
				offset = xml.indexOf("<", endTag);
				continue;
			}
			if (xml.charAt(offset + 1) == '/') {
				break;
			}
			if (spcPos > 0 && spcPos < endTag) {
				xmlTag = xml.substring(offset + 1,  spcPos);
				int theEndTag = endTag;
				if (xml.charAt(endTag - 1) == '/')  {
					theEndTag = endTag - 1;
				}
				properties = xml.substring(spcPos + 1, theEndTag);
			} else {
				if (xml.charAt(endTag - 1) == '/') {
					xmlTag = xml.substring(offset + 1, endTag - 1);
				} else {
					xmlTag = xml.substring(offset + 1, endTag);
				}
			}
			if (xml.charAt(endTag - 1) == '/') {
				xmlTag = xml.substring(offset + 1, endTag - 1);
				propertyMap = processProperties(properties);
				XMLObject obj = instantiate(xmlTag, propertyMap);
				retval.addSubObject(obj);
				offset = xml.indexOf("<", endTag);
				continue;
			}
			String searchStr = "/" + xmlTag + ">";
			endTag = xml.indexOf(searchStr, offset) + searchStr.length();
			String subXml = xml.substring(offset, endTag);
			XMLObject subObject = processXMLObject(subXml, retval);
			offset = xml.indexOf("<", endTag);
		}
		
		
		return retval;
	}
	
	private XMLObject processSingleLine(String line, XMLObject parent) {
		return null;	// TODO For the time being...
	}
	
	private Map<String, String> processProperties(String properties) {
		Map<String, String> retval = new HashMap<String, String>();
		int offset = 0;
		int eqPos = properties.indexOf('=');
		while (eqPos >= 0) {
			String lValue = properties.substring(offset, eqPos);
			int nextEqPos = properties.indexOf("=", eqPos + 1);
			int endPos = properties.length();
			if (nextEqPos > 0) {
				endPos = properties.substring(0, nextEqPos).lastIndexOf(' ');
			}
			String rValue = properties.substring(eqPos + 1, endPos);
			if (rValue.startsWith("\"") && rValue.endsWith("\"")) {
				rValue = rValue.substring(1, rValue.length() - 1);
			}
			retval.put(lValue, rValue);
			offset = endPos + 1;
			eqPos = nextEqPos;
		}
		return retval;
	}
	
	private XMLObject instantiate(String xmlTag, Map<String, String> properties) throws Exception {
		XMLObject retval = null;
		Class<? extends XMLObject> cls = ClassRegistry.getInstance().getObjectClass(xmlTag);
		Constructor<? extends XMLObject> c = cls.getConstructor(String.class);
		retval = c.newInstance(xmlTag);
		for (Map.Entry<String, String> me : properties.entrySet()) {
			setProperty(retval, me.getKey(), me.getValue());
		}
		
		return retval;
	}
	
	private void setProperty(XMLObject xmlObject, String key, String value) {
		Method m = null;
		String methodName = "set" + key.substring(0,  1).toUpperCase() + key.substring(1).replace(':', '_');
		Class<? extends XMLObject> cls = xmlObject.getClass();

		try {
			m = cls.getMethod(methodName, String.class);
			m.invoke(xmlObject, value);
		} catch (NoSuchMethodException nsmx) {
			xmlObject.addProperty(key, value);
		} catch (Exception x) {
			x.printStackTrace();
			xmlObject.addProperty(key, value);
		}
	}
	
	private List<HeadingAndDistance> getTrackpointDetails(List<Trackpoint> trackpoints) {
		BigDecimal prevLat = null;
		BigDecimal prevLon = null;
		double totalDist = 0;
		Coordinates pointFrom = null;
		List<HeadingAndDistance> retval = new ArrayList<HeadingAndDistance>();
		for (Trackpoint tp : trackpoints ) {
			if (pointFrom == null) {
				pointFrom = new Coordinates(tp.getLat(), tp.getLon());
				prevLat = tp.getLat();
				prevLon = tp.getLon();
				System.out.println("Lat=" + prevLat + ", Lon=" + prevLon);
				continue;
			}
			Coordinates pointTo = new Coordinates(tp.getLat(), tp.getLon());
			HeadingAndDistance hand = GPXUtils.calculateHeadingAndDistance(pointFrom, pointTo);
			retval.add(hand);
			totalDist += hand.getDistance();
			System.out.println("Lat=" + pointTo.getLat() + ", Lon=" + pointTo.getLon() + ", dist=" + hand.getDistance() + " m, heading=" + hand.getHeading() + ", Elapsed: " + totalDist + " m");
			pointFrom = pointTo;
		}
		System.out.println("Total distance: " + totalDist + " m");
		return retval;
	}
	
	private List<Routepoint> processRoute(List<HeadingAndDistance> hands, List<Waypoint> waypoints) {
		List<Routepoint> retval = new ArrayList<Routepoint>();
		HeadingAndDistance prevHand = null;
		HeadingAndDistance firstHand = null;
		HeadingAndDistance lastHand = null;
		int seqno = 0;
		double dist = 0.0d;
		for (HeadingAndDistance hand : hands) {
			if (prevHand == null) {
				seqno++;
				Routepoint r = new Routepoint();
				r.setCoordinates(hand.getPointFrom());
				checkWithWaypoints(r, waypoints, seqno);
				prevHand = hand;
				firstHand = hand;
				retval.add(r);
				continue;
			}
			dist += hand.getDistance();
			double diff = hand.getHeading() - prevHand.getHeading();
			if (diff < -340.0d) {
				diff += 360.0d;
			} else if (diff > 340.00d) {
				diff -= 360.0d;
			}
			if (diff < 0.0d) {
				diff *= -1;
			}
			if (diff > 20.0d && hand.getDistance() > 80.0d) {
				seqno++;
				Routepoint r = new Routepoint();
				r.setCoordinates(lastHand.getPointTo());
				checkWithWaypoints(r, waypoints, seqno);
				r.setCoordinates(hand.getPointFrom());
				retval.add(r);
				firstHand = hand;
				dist = 0.0d;
			}
			
			lastHand = hand;
		}
		seqno++;
		Routepoint r = new Routepoint();
		r.setCoordinates(lastHand.getPointTo());
		checkWithWaypoints(r, waypoints, seqno);

		retval.add(r);
		
		System.out.println(retval.size() + " routepoints added");
		return retval;
	}
	
	private List<Routepoint> reprocessRoute(List<Routepoint> route) {
		Routepoint prevRpt = null;
		List<Routepoint> retval = new ArrayList<Routepoint>();
		double prevHeading = -1.0d;
		int seqno = 0;
		for (Routepoint rpt : route) {
			seqno++;
			if (prevRpt == null) {
				retval.add(rpt);
				prevRpt = rpt;
				continue;
			}
			Coordinates pointFrom = new Coordinates(prevRpt);
			Coordinates pointTo = new Coordinates(rpt);
			HeadingAndDistance hand = GPXUtils.calculateHeadingAndDistance(pointFrom, pointTo);
			if (prevHeading < 0.0d) {
				prevHeading = hand.getHeading();
			} else {
				double heading = hand.getHeading();
				double diff = Math.abs(heading - prevHeading);
				if (diff >= 356.0d) {
					diff = 360.0d - diff;
				}
				String name = "";
				for (XMLObject o : prevRpt.getSubObjects()) {
					if ("name".equals(o.getXmlTag())) {
						name = o.getValue();
					}
				}
				if (diff <= 4.0d && name.length() <= 3) {
					seqno--;
					retval.remove(prevRpt);
				} else {
					prevHeading = -1.0d;
				}
			}
			modifyName(rpt, seqno);
			retval.add(rpt);
			prevRpt = rpt;
		}
		if (route.size() == retval.size()) {
			System.out.println("Routepoints not reduced");
		} else {
			System.out.println("Routepoints reduced from " + route.size() + " to " + retval.size());
		}
		return retval;
	}
	
	private void modifyName(Routepoint rpt, int seqno) {
		String seqnoStr = format(seqno, 3, '0');
		for (XMLObject o : rpt.getSubObjects()) {
			if ("name".equals(o.getXmlTag())) {
				StringBuffer buf = new StringBuffer(o.getValue());
				buf.replace(0, 3, seqnoStr);
				o.setValue(buf.toString());
			}
		}
	}
	
	private void routepointsOverview(List<Routepoint> routepoints) {
		System.out.println("\nRoutepoint overview:");
		Routepoint prevRpt = null;
		int seqno = 0;
		for (Routepoint rpt : routepoints) {
			seqno++;
			System.out.print(format(seqno, 3, ' ') + ". lat=" + format(rpt.getLat(), 5) + " lon=" + format(rpt.getLon(), 5));
			if (prevRpt == null) {
				System.out.println("");
				prevRpt = rpt;
				continue;
			}
			Coordinates pointTo = new Coordinates(rpt.getLat(), rpt.getLon());
			Coordinates pointFrom = new Coordinates(prevRpt.getLat(), prevRpt.getLon());
			HeadingAndDistance hand = GPXUtils.calculateHeadingAndDistance(pointFrom, pointTo);
			System.out.print(" heading=" + format(hand.getHeading(), 3, '0') + "°");
			System.out.println(" distance=" + format(hand.getDistance(), 4, '0') + " m");
			XMLObject comment = null;
			for (XMLObject o : prevRpt.getSubObjects()) {
				if ("cmt".equals(o.getXmlTag())) {
					comment = o;
					break;
				}
			}
			if (comment == null) {
				comment = new GenericXMLObject("cmt");
				prevRpt.addSubObject(comment);
			}
			comment.setValue("Heading=" + format(hand.getHeading(), 3, '0') + " deg, Distance=" + format(hand.getDistance(), 4,  '0') + " m");
			prevRpt = rpt;
		}
	}
	
	private String format(int n, int len, char pad) {
		String s = Integer.valueOf(n).toString();
		int theLen = len + (n < 0 ? 1 : 0);
		StringBuffer buf = new StringBuffer(s);
		int i = s.length();
		while (i < theLen) {
			buf.insert(n < 0 ? 1 : 0, pad);
			i++;
		}
		return buf.toString();
	}
	
	private String format(BigDecimal bd, int scale) {
		BigDecimal bdWork = new BigDecimal(bd.toString());
		bdWork.setScale(scale);
		StringBuffer buf = new StringBuffer(bdWork.toString());
		int pos = buf.indexOf(".");
		if (pos < 0) {
			pos = buf.length();
			buf.append(".");
		}
		int len = pos + scale;
		while (buf.length() <= len) {
			buf.append('0');
		}
		return buf.toString();
	}
	
	private String format(double d, int len, char pad) {
		int n = Double.valueOf(d + 0.5d).intValue();
		return format(n, len, pad);
	}
	
	private void checkWithWaypoints(Routepoint r, List<Waypoint> waypoints, int seqno) {
		String name = GPXUtils.formatInteger(seqno, 3);
		boolean match = false;
		for (Waypoint w : waypoints) {
			Coordinates coordR = new Coordinates(r.getLat(), r.getLon());
			Coordinates coordW = new Coordinates(w.getLat(), w.getLon());
			HeadingAndDistance hand = GPXUtils.calculateHeadingAndDistance(coordR, coordW);
			if (hand.getDistance() < 50.0d) {
				for (XMLObject subObject : w.getSubObjects()) {
					if ("name".equals(subObject.getXmlTag()) && subObject.getValue() != null) {
						name = name + ". " + subObject.getValue();
						break;
					}
				}
				match = true;
			}
			if (match) {
				break;
			}
		}
		XMLObject theName = new GenericXMLObject("name");
		theName.setValue(name);
		r.addSubObject(theName);
		r.addSubObject(new GenericXMLObject("cmt"));
		r.addSubObject(new GenericXMLObject("desc"));
	}
	
	private void writeOutfile(List<Waypoint> waypoints, List<Routepoint> routepoints) throws Exception {
		GPX outRoot = new GPX();
		outRoot.setCreator("Commer &amp; Quell");
		outRoot.setVersion("1.1");
		outRoot.setXmlns("http://www.topografix.com/GPX/1/1");
		outRoot.setXmlns_xsi("http://www.w3.org/2001/XMLSchema-instance");
		outRoot.setXsi_schemaLocation("http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.topografix.com/GPX/gpx_overlay/0/3 http://www.topografix.com/GPX/gpx_overlay/0/3/gpx_overlay.xsd");
		addMetadata(outRoot, waypoints, routepoints);
		for (Waypoint wpt : waypoints) {
			outRoot.addSubObject(wpt);
		}
		int seqno = 0;
		XMLObject rte = new GenericXMLObject("rte");
		outRoot.addSubObject(rte);
		for (Routepoint rpt : routepoints) {
			int rem = seqno % 250;
			rte.addSubObject(rpt);
			if (rem == 0 && seqno > 0) {
				rte = new GenericXMLObject("rte");
				outRoot.addSubObject(rte);
				rte.addSubObject(rpt);
			}
			seqno++;
		}
		String xml = outRoot.toXML();
		File output = null;
		BufferedWriter writer = null;
		try {
			output = new File(outfile);
			writer = new BufferedWriter(new FileWriter(output));
			writer.write(header);
			writer.newLine();
			int offset = 0;
			int pos = xml.indexOf("\n");
			while (pos > 0) {
				String line = xml.substring(offset, pos);
				writer.write(line);
				writer.newLine();
				offset = pos + 1;
				pos = xml.indexOf("\n", offset);
			}
			if (offset < xml.length()) {
				pos = offset + 1;
				if (pos < xml.length() || xml.charAt(offset) != '\n') {
					String lastLine = xml.substring(offset);
					writer.write(lastLine);
					writer.newLine();
				} else {
					writer.newLine();
				}
			}
			System.out.println("XML successfully written to " + outfile);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception x) {}
			}
		}
		
	}
	
	private void addMetadata(GPX gpx, List<Waypoint> waypoints, List<Routepoint> routepoints) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		StringBuffer buf = new StringBuffer();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		buf.append(calendar.get(Calendar.YEAR)).append("-");
		int month = calendar.get(Calendar.MONTH) + 1;
		buf.append(month < 10 ? "0" + month : month).append("-");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		buf.append(day < 10 ? "0" + day : day).append("T");
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		buf.append(hour < 10 ? "0" + hour : hour).append(":");
		int min = calendar.get(Calendar.MINUTE);
		buf.append(min < 10 ? "0" + min : min).append(":");
		int sec = calendar.get(Calendar.SECOND);
		buf.append(sec < 10 ? "0" + sec : sec);
		XMLObject metadata = new GenericXMLObject("metadata");
		gpx.addSubObject(metadata);
		XMLObject time = new GenericXMLObject("time");
		time.setValue(buf.toString());
		metadata.addSubObject(time);
		Bounds bounds = new Bounds();
		BigDecimal minlon = new BigDecimal(181);
		BigDecimal maxlon = new BigDecimal(-181);
		BigDecimal minlat = new BigDecimal(91);
		BigDecimal maxlat = new BigDecimal(-91);
		for (Waypoint wpt : waypoints) {
			BigDecimal lon = wpt.getLon();
			BigDecimal lat = wpt.getLat();
			if (lon.compareTo(minlon) < 0) {
				minlon = lon;
			}
			if (lon.compareTo(maxlon) > 0) {
				maxlon = lon;
			}
			if (lat.compareTo(minlat) < 0) {
				minlat = lat;
			}
			if (lat.compareTo(maxlat) > 0) {
				maxlat = lat;
			}
		}
		for (Routepoint rpt : routepoints) {
			BigDecimal lon = rpt.getLon();
			BigDecimal lat = rpt.getLat();
			if (lon.compareTo(minlon) < 0) {
				minlon = lon;
			}
			if (lon.compareTo(maxlon) > 0) {
				maxlon = lon;
			}
			if (lat.compareTo(minlat) < 0) {
				minlat = lat;
			}
			if (lat.compareTo(maxlat) > 0) {
				maxlat = lat;
			}
		}
		bounds.setMinlon(minlon.toPlainString());
		bounds.setMaxlon(maxlon.toPlainString());
		bounds.setMinlat(minlat.toPlainString());
		bounds.setMaxlat(maxlat.toPlainString());
		metadata.addSubObject(bounds);
	}

	public static void main(String[] args) {
		String infile;
		String outfile;
		String name = "";
		if (args.length < 1) {
			System.out.println("Usage: GPXProcessor infile [outfile]");
		}
			
		infile  = args[0];
		String filename = "";
		String extension = "";
		int idx = infile.lastIndexOf(".");
		if (idx > 0) {
			filename = infile.substring(0, idx);
			extension = infile.substring(idx + 1);
		} else {
			filename = infile;
			extension = "gpx";
		}
		outfile = filename + "_route" + "." + extension;
		if (args.length > 1) {
			for (int i = 1; i < args.length; i++) {
				if (args[i].startsWith("name=")) {
					name = args[i].substring("name=".length());
				}
				if (args[i].startsWith("outfile=")) {
					outfile=args[i].substring("outfile=".length());
				}
			}
		}

		try {
			new GPXProcessor(infile, outfile, name).process();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
