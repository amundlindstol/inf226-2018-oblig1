package inf226;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class Message {
	public final String sender, recipient, message;
	
	Message(final String sender, final String recipient, final String message) throws Invalid {
		this.sender = sender;
		this.recipient = recipient;
		if (!valid(message))
			throw new Invalid(message);
		this.message = message;
	}

	public static boolean valid(String message) {
		char[] messsageChar = message.toCharArray();
		for (char c: messsageChar) {
			if (Character.isISOControl(c) && c != '\n') {
				return false;
			} else if (c == '.') { //TODO: fix
			}
		}
		return message.equals(Jsoup.clean(message, Whitelist.basic()));
	}

	public static class Invalid extends Exception {
		private static final long serialVersionUID = -3451435075806445718L;

		public Invalid(String msg) {
			super("Invalid string: " + msg);
		}
	}
}
