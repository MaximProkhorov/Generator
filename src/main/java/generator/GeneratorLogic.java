package generator;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Предисловие: много тафтологии (строка - запись - записывание), поэтому на примере:
//[1, 25/11, Павлов Дмитрий] - это запись таблицы
//| 1        | 25/11   | Павлов  | - это 1ая строка записи таблицы
//|          |         | Дмитрий | - это 2ая

public class GeneratorLogic {
	private XmlFileHandler xmlFile;
	private List<String[]> allRows;
	private PrintWriter pw;
	private File output;
	private Pattern pattern = Pattern.compile("[^\\p{L}\\d]"); // ограничение - любой знак, кроме букв и цифр
	private int lineNumber; // номер строки на текущей странице - для создания новой страницы
	private StringBuilder title; // содержимое заголовков колонок запоминается, чтобы не получать его заново на
									// каждой странице
	private int titleHeight; // аналогично, высота заголовков

	public GeneratorLogic(XmlFileHandler xmlFile, List<String[]> allRows, File output) {
		this.xmlFile = xmlFile;
		this.allRows = allRows;
		this.output = output;
	}

	public void print() {
		try {
			pw = new PrintWriter(output, "UTF-16LE"); // использую PrintWriter вместо BufferedWriter, т.к. запись в файл
														// производится редко, т.к. запись таблицы сначала записывается
														// в StringBuilder, а потом только в файл (это затем, чтобы
														// можно было переносить запись на новую страницу,
														// если она не помещается на старой)
		} catch (Exception e) {
			e.printStackTrace();
		}
		title = printTitle(); // запоминаются заголовки
		titleHeight += lineNumber; // ...и их высота
		pw.print(title.toString());
		printEntries();
		pw.close();
	}

	// все входные данные переводятся из String в StringBuilder, чтобы после
	// записывания одной строки записи таблицы проще было удалять написанное
	// из записи (и сами String записи оставались неизменными)

	private StringBuilder printTitle() {
		StringBuilder[] row = new StringBuilder[xmlFile.getColumns().size()];
		for (int i = 0; i < row.length; i++) {
			row[i] = new StringBuilder(xmlFile.getColumns().get(i).getTitle());
		}
		return printRow(row, true);
	}

	private void printEntries() {
		StringBuilder[][] entries = new StringBuilder[allRows.size()][xmlFile.getColumns().size()];
		for (int i = 0; i < entries.length; i++) {
			for (int j = 0; j < entries[i].length; j++) {
				entries[i][j] = new StringBuilder(allRows.get(i)[j]);
			}
		}
		for (int i = 0; i < entries.length; i++) {
			StringBuilder[] row = entries[i];
			pw.print(printRow(row).toString());
		}
	}

	private StringBuilder printDashes() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xmlFile.getPageWidth(); i++) {
			sb.append("-");
		}
		sb.append("\n");
		return sb;
	}

	private void printNewPage() {
		lineNumber += titleHeight;
		pw.print("~\n");
		pw.print(title.toString());
	}

	private StringBuilder printRow(StringBuilder[] row) {
		return printRow(row, false);
	}

	private StringBuilder printRow(StringBuilder[] row, boolean newPage) {
		StringBuilder superLine = new StringBuilder(); // сюда записывается запись таблицы (это нужно для переноса целой
														// записи на новую страницу, если она не помещается на старую)
		int currentNumberOfLines = 0; // количество строк, занимаемых текущей записью таблицы - для переноса
										// ЦЕЛОЙ записи на новую страницу, если она не помещается на предыдущей
		if (!newPage) { // ограничение, не дающее печатать тире в начале страницы (до заголовков)
			currentNumberOfLines++;
			superLine.append(printDashes()); // строка тире до записи считается частью записи
		}
		while (true) { // цикл будет печатать строки текущей записи в superLine, пока есть, что
						// печатать
			currentNumberOfLines++;
			StringBuilder line = new StringBuilder(); // сюда записывает строчка таблицы
			boolean repeat = false;
			for (int i = 0; i < row.length; i++) { // проход по столбцам
				StringBuilder subLine = new StringBuilder(); // сюда записывается содержимое текущего столбца (нужно для
																// разбиения содержимого по специальным символам)
				line.append("| ");
				while (true) { // цикл разбивает значение текущего столбца до тех пор, пока разбитые значения
								// можно уместить В ОДНУ СТРОКУ
					int splitIndex = splitIndex(row[i].toString());
					if (splitIndex == -1 || splitIndex + subLine.length() > xmlFile.getColumns().get(i).getWidth()
							|| (splitIndex == 0 && subLine.length() == xmlFile.getColumns().get(i).getWidth())) {
						// 3-е условие - чтобы избежать зацикливания после того, как символ уже
						// перенесли на новую строку
						break; // если разбить нельзя, или значение не поместится в эту же строку, break
					}
					char splitChar = row[i].charAt(splitIndex);
					int k = 1; // чтобы символ, по которому проводится разбиение, оставался на этой строке
					int l = 1; // аналогично
					if (splitIndex + subLine.length() == xmlFile.getColumns().get(i).getWidth()) {
						k--; // если символ, по которому проводится разбиение, попадает на границу стобца,
								// разрешается перенести его на следующую строку
						if (splitChar != ' ') {
							l--; // ...а если это пробел, то вместо переноса он удаляется
						}

					}
					subLine.append(row[i], 0, splitIndex + k);
					row[i].delete(0, splitIndex + l); // здесь и далее - когда значение пишется в строку, оно удаляется
				}
				if (row[i].length() > xmlFile.getColumns().get(i).getWidth() - subLine.length()) {
					if (subLine.length() == 0) { // если слово/цифра не умещается на текущей строчке полностью, то оно
													// начинается с новой
						subLine.append(row[i], 0, xmlFile.getColumns().get(i).getWidth());
						row[i].delete(0, xmlFile.getColumns().get(i).getWidth());
					}
					repeat = true; // сообщает, что остались недописанные символы в записи, и надо пройтись по
									// циклу ещё раз
				} else {
					subLine.append(row[i]);
					row[i] = new StringBuilder("");
				}
				for (int k = 0; k < xmlFile.getColumns().get(i).getWidth() - subLine.length() + k; k++) {
					subLine.append(" ");
				}

				line.append(subLine).append(" ");
			}
			line.append("|\n");
			superLine.append(line);
			if (!repeat) { // если все символы записи переписаны, цикл прерывается
				break;
			}
		}
		if (currentNumberOfLines + titleHeight > xmlFile.getPageHeight()) {
			throw new RuntimeException("Величина строки больше высоты страницы, напечатать невозможно");
		}
		if (lineNumber + currentNumberOfLines > xmlFile.getPageHeight()) { // если запись полностью не помещается, создаётся
																		// новая страница
			lineNumber = 0;
			printNewPage();
		}
		lineNumber += currentNumberOfLines;
		return superLine;
	}

	private int splitIndex(String s) {
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			return matcher.start(); // если находит символ, по которому проводится разбиение, возвращается его
									// индекс
		}
		return -1;
	}
}
