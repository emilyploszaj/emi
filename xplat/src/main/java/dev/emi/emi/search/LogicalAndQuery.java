package dev.emi.emi.search;

import java.util.List;

import dev.emi.emi.api.stack.EmiStack;

public class LogicalAndQuery extends Query {
	private final List<Query> queries;

	public LogicalAndQuery(List<Query> queries) {
		this.queries = queries;
	}

	@Override
	public boolean matches(EmiStack stack) {
		for (int i = 0; i < queries.size(); i++) {
			Query q = queries.get(i);
			boolean failure = q.negated;
			if (q.matches(stack) == failure) {
				return false;
			}
		}
		return true;
	}
}
