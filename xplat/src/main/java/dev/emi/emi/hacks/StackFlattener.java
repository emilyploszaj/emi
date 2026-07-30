package dev.emi.emi.hacks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiLog;

public class StackFlattener {
	private static final Map<EmiStack, List<ImmutableEmiStack>> IMMUTABLES = Maps.newHashMap();
	private static final Map<Class<?>, Intruder<?>> INTRUDERS = Maps.newHashMap();

	public static ImmutableEmiStack getImmutable(EmiStack stack, int discriminator) {
		List<ImmutableEmiStack> list = IMMUTABLES.computeIfAbsent(stack, s -> Lists.newArrayList());
		while (discriminator >= list.size()) {
			list.add(new ImmutableEmiStack(stack.copy()));
		}
		return list.get(discriminator);
	}

	private static <I extends EmiIngredient> List<I> immutablize(List<I> list) {
		ImmutableList.Builder<I> builder = ImmutableList.builderWithExpectedSize(list.size());
		for (I stack : list) {
			builder.add(immutablize(stack));
		}
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private static <I extends EmiIngredient> I immutablize(I ingredient) {
		if (ingredient instanceof EmiStack stack) {
			if (stack.getChance() == 1 && stack.getRemainder().isEmpty()) {
				return (I) StackFlattener.getImmutable(stack, 0);
			} else {
				EmiLog.info(stack.toString());
			}
		}
		return ingredient;
	}

	@SuppressWarnings("unchecked")
	private static final <T extends EmiRecipe> Intruder<T> getIntruder(T recipe) {
		return (Intruder<T>) INTRUDERS.computeIfAbsent(recipe.getClass(), c -> new Intruder<T>(recipe));
	}
	
	public static void flattenRecipe(EmiRecipe recipe) {
		getIntruder(recipe).flatten(recipe);
	}

	public static void printImpact() {
		Map<EmiIngredient, Integer> map = new IdentityHashMap<>();
		long countIng = 0;
		long countIngDeep = 0;
		long countStack = 0;
		long countImmut = 0;
		long uniqImmut = 0;
		for (EmiRecipe recipe : EmiRecipes.manager.getRecipes()) {
			for (EmiIngredient i : Iterables.concat(recipe.getInputs(), recipe.getCatalysts(), recipe.getOutputs())) {
				boolean unique = true;
				if (map.containsKey(i)) {
					unique = false;
				}
				map.put(i, 0);
				if (i instanceof ImmutableEmiStack) {
					countImmut++;
					if (unique) {
						uniqImmut++;
					}
				} else if (i instanceof EmiStack) {
					countStack++;
				} else {
					countIng++;
					countIngDeep += i.getEmiStacks().size();
				}
			}
		}
		long countTotal = countIng + countIngDeep + countStack + countImmut;
		EmiLog.info("Ingredient memory analysis:"
			+ "\nImmutable Stacks: " + countImmut + " (" + countImmut * 100 / countTotal + "%) [" + uniqImmut + " uniq]" 
			+ "\nNormal Stacks:    " + countStack + " (" + countStack * 100 / countTotal + "%)"
			+ "\nIngredients:      " + countIng + " (" + countIng * 100 / countTotal + "%)"
			+ "\n- Inner Stacks:   " + countIngDeep + " (" + countIngDeep * 100 / countTotal + "%)");
		StringBuilder irBuilder = new StringBuilder();
		for (Intruder<?> intruder : INTRUDERS.values()) {
			irBuilder.append("\n  ");
			if (intruder.inputApplicator instanceof ListApplicator) {
				irBuilder.append("I");
			} else if (intruder.inputApplicator instanceof FloatingApplicator) {
				irBuilder.append("i");
			} else {
				irBuilder.append("-");
			}
			if (intruder.outputApplicator instanceof ListApplicator) {
				irBuilder.append("O");
			} else if (intruder.outputApplicator instanceof FloatingApplicator) {
				irBuilder.append("o");
			} else {
				irBuilder.append("-");
			}
			if (intruder.catalystApplicator instanceof ListApplicator) {
				irBuilder.append("C");
			} else if (intruder.catalystApplicator instanceof FloatingApplicator) {
				irBuilder.append("c");
			} else {
				irBuilder.append("-");
			}
			irBuilder.append("  ").append(intruder.clazz);
		}
		EmiLog.info("Intruder report:" + irBuilder.toString());
	}

	public static class Intruder<T extends EmiRecipe> {
		private final Class<?> clazz;
		private Applicator inputApplicator, outputApplicator, catalystApplicator;

		public Intruder(T donor) {
			Class<?> clazz = donor.getClass();
			this.clazz = clazz;
			List<EmiIngredient> inputs = donor.getInputs();
			List<EmiIngredient> catalysts = donor.getCatalysts();
			List<EmiStack> outputs = donor.getOutputs();
			do {
				for (Field field : clazz.getDeclaredFields()) {
					Class<?> type = field.getType();
					if ((field.getModifiers() & Modifier.STATIC) != 0) {
						continue;
					}
					if (type.isAssignableFrom(ImmutableList.class)) {
						field.setAccessible(true);
						try {
							Object o = field.get(donor);
							if (o == inputs) {
								this.inputApplicator = new ListApplicator(MethodHandles.lookup().unreflectSetter(field));
							} else if (o == catalysts) {
								this.catalystApplicator = new ListApplicator(MethodHandles.lookup().unreflectSetter(field));
							} else if (o == outputs) {
								this.outputApplicator = new ListApplicator(MethodHandles.lookup().unreflectSetter(field));
							}
						} catch (Throwable t) {
						}
					} else if (type.isAssignableFrom(EmiStack.class)) {
						field.setAccessible(true);
						try {
							Object o = field.get(donor);
							if (inputs.size() > 0 && o == inputs.get(0)) {
								this.inputApplicator = new FloatingApplicator(MethodHandles.lookup().unreflectSetter(field));
							} else if (catalysts.size() > 0 && o == catalysts.get(0)) {
								this.catalystApplicator = new FloatingApplicator(MethodHandles.lookup().unreflectSetter(field));
							} else if (outputs.size() > 0 && o == outputs.get(0)) {
								this.outputApplicator = new FloatingApplicator(MethodHandles.lookup().unreflectSetter(field));
							}
						} catch (Throwable t) {
						}
					}
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null && clazz != Object.class);
		}

		public void flatten(T recipe) {
			if (inputApplicator != null) {
				inputApplicator.flatten(recipe, recipe.getInputs());
			}
			if (outputApplicator != null) {
				outputApplicator.flatten(recipe, recipe.getOutputs());
			}
			if (catalystApplicator != null) {
				catalystApplicator.flatten(recipe, recipe.getCatalysts());
			}
		}
	}

	private static abstract class Applicator {

		public abstract void flatten(EmiRecipe recipe, List<? extends EmiIngredient> stacks);
	}

	private static class ListApplicator extends Applicator {
		private final MethodHandle listSetter;

		public ListApplicator(MethodHandle handle) {
			this.listSetter = handle;
		}
		
		public void flatten(EmiRecipe recipe, List<? extends EmiIngredient> stacks) {
			try {
				listSetter.invoke(recipe, immutablize(stacks));
			} catch (Throwable e) {
				EmiLog.error("Unexpected error flattening recipe ", e);
			}
		}
	}

	private static class FloatingApplicator extends Applicator {
		private final MethodHandle fieldSetter;

		public FloatingApplicator(MethodHandle handle) {
			this.fieldSetter = handle;
		}
		
		public void flatten(EmiRecipe recipe, List<? extends EmiIngredient> stacks) {
			try {
				if (stacks.size() > 0) {
					fieldSetter.invoke(recipe, immutablize(stacks.get(0)));
				}
			} catch (Throwable e) {
				EmiLog.error("Unexpected error flattening recipe ", e);
			}
		}
	}
}
