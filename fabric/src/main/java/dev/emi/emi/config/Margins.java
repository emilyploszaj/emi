package dev.emi.emi.config;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;

public class Margins extends IntGroup {

	public Margins(int top, int right, int bottom, int left) {
		super("emi.sidebar.margins.", List.of("top", "right", "bottom", "left"), IntList.of(top, right, bottom, left));
	}
	
	public int top() {
		return values.getInt(0);
	}

	public int right() {
		return values.getInt(1);
	}

	public int bottom() {
		return values.getInt(2);
	}

	public int left() {
		return values.getInt(3);
	}
}
