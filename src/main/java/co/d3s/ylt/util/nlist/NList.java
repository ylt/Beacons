package co.d3s.ylt.util.nlist;

import java.lang.reflect.Method;

import org.bukkit.command.CommandSender;

import co.d3s.ylt.util.separator.Separator;

public class NList {
	// Object sender;
	String seps;
	Separator sep;
	StringBuilder message;
	Method method;
	CommandSender sender;
	String prefix;

	public NList(CommandSender sender, String fprefix, String aprefix,
			String Separator) {
		this.sender = sender;

		message = new StringBuilder(fprefix);
		sep = new Separator(Separator);

		prefix = aprefix;
		seps = Separator;
	}

	public void add(String msg) {
		if ((message.toString() + seps + msg).replaceAll("&.", "").length() > 65) {
			flush();
		}
		message.append(sep + msg);
	}

	public void flush() {
		/*
		 * try { method.invoke(object, new Object[] {message.toString()}); }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
		sender.sendMessage(message.toString().replace("&", "\u00A7"));
		sep = new Separator(seps);
		message = new StringBuilder(prefix);
	}
}
