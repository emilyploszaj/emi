package dev.emi.emi.registry;

import java.util.function.Predicate;

import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiHidden;

public class EmiInitRegistryImpl implements EmiInitRegistry {

	@Override
	public <T extends EmiIngredient> void addIngredientSerializer(Class<T> clazz, EmiIngredientSerializer<T> serializer) {
		EmiIngredientSerializers.BY_CLASS.put(clazz, serializer);
		EmiIngredientSerializers.BY_TYPE.put(serializer.getType(), serializer);
	}

	@Override
	public void disableStacks(Predicate<EmiStack> predicate) {
		EmiHidden.pluginDisabledFilters.add(predicate);
	}

	@Override
	public void disableStack(EmiStack stack) {
		EmiHidden.pluginDisabledStacks.add(stack);
	}

	@Override
	public void addRegistryAdapter(EmiRegistryAdapter<?> adapter) {
		EmiTags.ADAPTERS_BY_CLASS.map().put(adapter.getBaseClass(), adapter);
		EmiTags.ADAPTERS_BY_REGISTRY.put(adapter.getRegistry(), adapter);
	}
}
