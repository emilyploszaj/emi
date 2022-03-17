package dev.emi.emi.search;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.registry.Registry;

public class ModQuery extends Query {
	private final String name;

	public ModQuery(String name) {
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		String namespace = Registry.ITEM.getId(stack.getItemStack().getItem()).getNamespace();
		return EmiUtil.getModName(namespace).toLowerCase().contains(name);
	}
}
