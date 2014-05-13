package co.d3s.ylt.util.mlist;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;

import co.d3s.ylt.util.separator.Separator;

public class MList {
	public Separator separator;
	public String separator_string;

	public String start_prefix;
	public String line_prefix;

	public CommandSender sender;
	public List<String> message;

	public MList(CommandSender sender, String start_prefix, String line_prefix,
			String separator_string) {
		this.sender = sender;
		this.start_prefix = start_prefix;
		this.line_prefix = line_prefix;
		this.separator_string = separator_string;

		this.separator = new Separator(separator_string);
		this.message = new LinkedList<String>();
	}

	public void add(String element) {
		message.add(element);
	}

	public void flush() {
		Collections.sort(message, new StringSort());
		StringBuffer buffer = new StringBuffer(start_prefix);
		for (String element : message) {
			if ((buffer.toString() + separator_string + element).replaceAll(
					"&.", "").length() > 60) {
				sender.sendMessage(buffer.toString().replace("&", "\u00A7"));

				buffer = new StringBuffer(line_prefix);
				separator = new Separator(separator_string);
			}
			buffer.append(separator + element);
		}
		sender.sendMessage(buffer.toString().replace("&", "\u00A7"));
	}
}

class StringSort implements Comparator<String> {
	@Override
	public int compare(String arg0, String arg1) {
		if (arg0.replaceAll("&.", "").length() < arg1.replaceAll("&.", "")
				.length()) {
			return 1;
		}
		return -1;
	}
}