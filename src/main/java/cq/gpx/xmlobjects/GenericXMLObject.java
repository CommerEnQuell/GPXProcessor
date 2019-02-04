package cq.gpx.xmlobjects;

import java.util.Map;

public class GenericXMLObject extends XMLObject {
	public GenericXMLObject(String xmlTag) {
		super(xmlTag);
	}

	@Override
	protected String nativeToXML() {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (Map.Entry<String, String> me : getProperties().entrySet()) {
			if ("".equals(me.getKey())) {
				continue;
			}
			if (!first) {
				buf.append(" ");
			}
			buf.append(me.getKey()).append("=\"").append(me.getValue()).append("\"");
			first = false;
		}
		return buf.toString();
	}

}
