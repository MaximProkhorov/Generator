package generator;

public class Column {
	private String title;
	private int width;

	public Column(String title, int width) {
		this.title = title;
		this.width = width;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
