package generator;

import java.io.File;
import java.util.List;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

public class Generator {
	public static void main(String[] args) {
//		File settings = new File ("settings.xml");
//		File data = new File ("source-data.tsv");
//		File output = new File ("output.txt");
		
		File settings = null;
		File data = null;
		File output = null;
		
		try {
			settings = new File(args[0]);
			data = new File(args[1]);
			output = new File(args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		TsvParserSettings tsvSettings = new TsvParserSettings();
		TsvParser parser = new TsvParser(tsvSettings);
		List<String[]> allRows = parser.parseAll(data, "UTF-16LE");

		XmlFileHandler xmlFile = new XmlFileHandler(settings);
		GeneratorLogic gl = new GeneratorLogic(xmlFile, allRows, output);

		gl.print();
	}

	

}
