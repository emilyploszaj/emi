package dev.emi.emi.search;

import dev.emi.emi.api.stack.EmiStack;

// Wrapper to use identity comparison in searching
public class SearchStack {
	public final EmiStack stack;

	public SearchStack(EmiStack stack) {
		this.stack = stack;
	}
}
