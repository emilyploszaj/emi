package dev.emi.emi.data;

import java.util.List;

import dev.emi.emi.api.stack.EmiIngredient;

public record EmiAlias(List<EmiIngredient> stacks, List<String> keys) {
}
