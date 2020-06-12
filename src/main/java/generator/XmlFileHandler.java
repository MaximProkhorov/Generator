package generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFileHandler {
	private int pageHeight;
	private int pageWidth;
	private List<Column> columns = new ArrayList<Column>();

	public XmlFileHandler(File settings) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Node page = document.getElementsByTagName("page").item(0);
		Element pageElement = (Element) page;
		pageWidth = getIntValue(pageElement, "width");
		pageHeight = getIntValue(pageElement, "height");

		NodeList nodeColumns = document.getElementsByTagName("column");
		for (int i = 0; i < nodeColumns.getLength(); i++) {
			Element elementColumns = (Element) nodeColumns.item(i);
			columns.add(createColumn(elementColumns));
		}
	}

	private Column createColumn(Element e) {
		String title = getTextValue(e, "title");
		int columnWidth = getIntValue(e, "width");
		return new Column(title, columnWidth);
	}

	private String getTextValue(Element e, String tagName) {
		return e.getElementsByTagName(tagName).item(0).getTextContent();
	}

	private int getIntValue(Element e, String tagName) {
		return Integer.parseInt(getTextValue(e, tagName));
	}

	public int getPageHeight() {
		return pageHeight;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public List<Column> getColumns() {
		return columns;
	}
}
