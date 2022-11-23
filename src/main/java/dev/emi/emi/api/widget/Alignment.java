package dev.emi.emi.api.widget;

public enum Alignment {
	START, CENTER, END;

	public int offset(int length) {
		return switch (this) {
			case START -> 0;
			case CENTER -> -(length / 2);
			case END -> -length;
		};
	}
}
