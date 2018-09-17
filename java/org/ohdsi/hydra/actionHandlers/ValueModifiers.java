package org.ohdsi.hydra.actionHandlers;

public class ValueModifiers {
	public static String convertToFileName(String string) {
		StringBuilder fileName = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (Character.isLetterOrDigit(ch) || ch == '_')
				fileName.append(ch);
			else if (ch == ' ')
				fileName.append('_');
		}
		return fileName.toString();
	}
}
