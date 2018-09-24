package org.ohdsi.hydra.actionHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;

public class ConditionEvaluator {

	private String		condition;
	private JSONObject	studySpecs;

	public static void main(String[] args) {
		JSONObject object = new JSONObject("{\"foo\": \"bar\"}") ;
		System.out.println(new ConditionEvaluator("(1 == 1) & (true) & ('x' != 'y') & (foo == 'bar2')", object).evaluate());
	}

	public ConditionEvaluator(String condition, JSONObject studySpecs) {
		this.condition = condition;
		this.studySpecs = studySpecs;
	}

	public boolean evaluate() {
		return evaluateCondition(condition);
	}

	private boolean evaluateCondition(String str) {
		str = str.trim();
		List<Span> spans = findParentheses(str);
		// Spans are in order of closing parenthesis, so if we go from first to last we'll always process nested parentheses first
		for (Span span : spans)
			if (!precededByIn(span.start, str)) {
				boolean evaluation = evaluateBooleanCondition(str.substring(span.start + 1, span.end - 1));
				str = replaceCharAt(str, span.start, evaluation ? '1' : '0');
				str = replace(str, spans, span.start, span.end, span.start, span.start);
			}
		return evaluateBooleanCondition(str);
	}

	private List<Span> findParentheses(String str) {
		Stack<Integer> starts = new Stack<Integer>();
		List<Span> spans = new ArrayList<Span>();
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '(') {
				starts.push(i);
			} else if (str.charAt(i) == ')') {
				if (!starts.empty()) {
					spans.add(new Span(starts.pop(), i + 1));
				}
			}
		}
		return spans;
	}

	private boolean evaluateBooleanCondition(String str) {
		str = str.trim();
		int found = str.indexOf("&");
		if (found != -1) {
			String[] parts = str.split("&");
			for (String part : parts)
				if (!evaluatePrimitiveCondition(part))
					return false;

			return true;
		}
		found = str.indexOf("|");
		if (found != -1) {
			String[] parts = str.split("\\|");
			for (String part : parts)
				if (evaluatePrimitiveCondition(part))
					return true;
			return false;
		}
		return evaluatePrimitiveCondition(str);
	}

	private boolean precededByIn(int start, String str) {
		str = str.toLowerCase();
		int matched = 0;
		for (int i = start - 1; i >= 0; i--) {
			if (!Character.isWhitespace(str.charAt(i))) {
				if (matched == 0 && str.charAt(i) == 'n')
					matched++;
				else if (matched == 1 && str.charAt(i) == 'i')
					matched++;
				else
					return false;
			} else if (matched == 2)
				return true;
		}
		return false;
	}

	private String resolveValue(String s) {
		if (s.length() > 1 && ((s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'') || (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"')))
			return s.substring(1, s.length() - 1);
		else if (isNumber(s))
			return s;
		else {
			return JsonUtilities.getViaPath(studySpecs, s).toString();
		}
	}

	public boolean isNumber(String string) {
		try {
			Double.parseDouble(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean evaluatePrimitiveCondition(String str) {
		str = str.trim();
		String str_lc = resolveValue(str).toLowerCase();
		if (str_lc.equals("false") || str_lc.equals("0") || str_lc.equals("!true") || str_lc.equals("!1"))
			return false;
		if (str_lc.equals("true") || str_lc.equals("1") || str_lc.equals("!false") || str_lc.equals("!0"))
			return true;

		int found = str.indexOf("==");
		if (found != -1) {
			String left = str.substring(0, found);
			left = left.trim();
			left = resolveValue(left);
			String right = str.substring(found + 2, str.length());
			right = right.trim();
			right = resolveValue(right);
			return (left.equals(right));
		}
		found = str.indexOf("!=");
		if (found == -1)
			found = str.indexOf("<>");
		if (found != -1) {
			String left = str.substring(0, found);
			left = left.trim();
			left = resolveValue(left);
			String right = str.substring(found + 2, str.length());
			right = right.trim();
			right = resolveValue(right);
			return (!left.equals(right));
		}
		found = str_lc.indexOf(" in ");
		if (found != -1) {
			String left = str.substring(0, found);
			left = left.trim();
			left = resolveValue(left);
			String right = str.substring(found + 4, str.length());
			right = right.trim();
			if (right.length() > 2 && right.charAt(0) == '(' && right.charAt(right.length() - 1) == ')') {
				right = right.substring(1, right.length() - 1);
				String[] parts = right.split(",");
				for (String part : parts) {
					String partString = resolveValue(part);
					if (left.equals(partString))
						return true;
				}
				return false;
			}
		}
		throw new RuntimeException("Error parsing boolean condition: \"" + str + "\"");
	}

	public String replaceCharAt(String string, int pos, char ch) {
		return string.substring(0, pos) + ch + string.substring(pos + 1);
	}

	public String replace(String string, int start, int end, String replacement) {
		if (end > string.length())
			return string.substring(0, start) + replacement;
		else
			return string.substring(0, start) + replacement + string.substring(end);
	}

	private String replace(String str, List<Span> spans, int toReplaceStart, int toReplaceEnd, int replaceWithStart, int replaceWithEnd) {
		String replaceWithString = str.substring(replaceWithStart, replaceWithEnd + 1);
		str = replace(str, toReplaceStart, toReplaceEnd, replaceWithString);
		for (Span span : spans)
			if (span.valid) {
				if (span.start > toReplaceStart) {
					if (span.start >= replaceWithStart && span.start < replaceWithEnd) {
						int delta = toReplaceStart - replaceWithStart;
						span.start += delta;
						span.end += delta;
					} else if (span.start > toReplaceEnd) {
						int delta = toReplaceStart - toReplaceEnd + replaceWithString.length();
						span.start += delta;
						span.end += delta;
					} else {
						span.valid = false;
					}
				} else if (span.end > toReplaceEnd) {
					int delta = toReplaceStart - toReplaceEnd + replaceWithString.length();
					span.end += delta;
				}
			}
		return str;
	}

	private class Span {
		public int		start;
		public int		end;
		public boolean	valid;

		public Span(int start, int end) {
			this.start = start;
			this.end = end;
			this.valid = true;
		}
	}

}
