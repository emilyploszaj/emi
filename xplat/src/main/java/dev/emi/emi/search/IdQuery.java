package dev.emi.emi.search;

import dev.emi.emi.api.stack.EmiStack;

public class IdQuery extends Query {
    private final String name;

    public IdQuery(String name) {
        this.name = name.toLowerCase();
    }

    @Override
    public boolean matches(EmiStack stack) {
        return stack.getId().getPath().contains(name);
    }
}
