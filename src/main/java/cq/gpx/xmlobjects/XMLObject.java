package cq.gpx.xmlobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class XMLObject {
	private String xmlTag;
	private Map<String, String> properties = new HashMap<String, String>();
	private List<XMLObject> subObjects = new ArrayList<XMLObject>();
	private boolean cData = false;
	
	protected XMLObject(String xmlTag) {
		this.xmlTag = xmlTag;
	}
	
	public void addProperty(String key, String value) {
		properties.put(key, value);
	}
	
	protected String removeProperty(String key) {
		String retval = null;
		if (properties.containsKey(key)) {
			retval = properties.remove(key);
		}
		return retval;
	}
	
	public final void setValue(String value) {
		properties.put("", value);
	}
	
	public final String getValue() {
		String retval = null;
		String key = cData ? "CDATA" : "";
		if (properties.containsKey(key)) {
			retval = properties.get(key);
		}
		return retval;
	}
	
	public String getXmlTag() {
		return xmlTag;
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	public void addSubObject(XMLObject xmlObject) {
		subObjects.add(xmlObject);
	}
	
	public List<XMLObject> getSubObjects() {
		return Collections.unmodifiableList(subObjects);
	}
	
	public final void setCDATA(String value) {
		this.cData = true;
		properties.put("CDATA", value);
	}
	
	public final boolean isCDATA() {
		return cData;
	}
	
	protected abstract String nativeToXML();
	
	public String toXML(String indent) {
		StringBuffer buf = new StringBuffer();
		buf.append(indent);
		if (cData) {
			buf.append("<").append(xmlTag).append(">\n");
			buf.append(indent).append("   ");
			buf.append("<![CDATA[").append(properties.get("CDATA")).append("]]>\n");
			buf.append(indent).append("</").append(xmlTag).append(">\n");
			return buf.toString();
		}
		buf.append("<").append(xmlTag);
		String nativeProps = nativeToXML();
		if (nativeProps != null && nativeProps.length() > 0) {
			buf.append(" ").append(nativeProps);
		}
		String value = "";
		if (properties.containsKey("")) {
			value = properties.get("");
		}
		if (value.length() > 0) {
			buf.append(">").append(value).append("</").append(xmlTag);
		} else {
			if (!properties.isEmpty()) {
				buf.append(" ");
				for (Map.Entry<String, String> property : properties.entrySet()) {
					buf.append(property.getKey()).append("=\"").append(property.getValue()).append("\"");
				}
			}
		}
		if (!subObjects.isEmpty()) {
			buf.append(">\n");
			String subIndent = indent + "  ";
			for (XMLObject subObject : subObjects) {
				buf.append(subObject.toXML(subIndent));
			}
			buf.append(indent).append("</").append(xmlTag).append(">\n");
		} else {
			if (value.length() > 0) {
				buf.append(">\n");
			} else {
				buf.append("/>").append("\n");
			}
		}
		return buf.toString();
	}
	
	public String toXML() {
		return toXML("");
	}
}
