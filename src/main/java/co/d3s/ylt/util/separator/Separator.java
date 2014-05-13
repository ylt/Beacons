package co.d3s.ylt.util.separator;

public class Separator {
	private Boolean state = false;
	private String separator;
	public Integer length;

	public Separator(String separator) {
		this.separator = separator;
		this.length = separator.length();
	}

	@Override
	public String toString() {
		if (state == false) {
			state = true;
			return "";
		}
		return separator;
	}
}