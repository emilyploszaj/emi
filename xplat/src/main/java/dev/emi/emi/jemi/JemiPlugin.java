package dev.emi.emi.jemi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.runtime.JemiBookmarkOverlay;
import dev.emi.emi.jemi.runtime.JemiDragDropHandler;
import dev.emi.emi.jemi.runtime.JemiIngredientFilter;
import dev.emi.emi.jemi.runtime.JemiIngredientListOverlay;
import dev.emi.emi.jemi.runtime.JemiRecipesGui;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.EmiReloadManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.load.registration.SubtypeRegistration;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@JeiPlugin
public class JemiPlugin implements IModPlugin, EmiPlugin {
	private static final Map<EmiRecipeCategory, IRecipeCategory<?>> CATEGORY_MAP = Maps.newHashMap();
	private static ISubtypeManager subtypeManager;
	public static IJeiRuntime runtime;
	public static BiPredicate<IIngredientTypeWithSubtypes<? extends Object, ? extends Object>, Object> hasSubtype = (a, b) -> true;

	@Override
	public Identifier getPluginUid() {
		return EmiPort.id("emi:jemi");
	}

	public void registerItemSubtypes(ISubtypeRegistration registration) {
		try {
			if (((SubtypeRegistration) registration).getInterpreters() != null) {
				hasSubtype = (type, ingredient) -> {
					@SuppressWarnings("unchecked")
					IIngredientTypeWithSubtypes<Object, Object> castedType = (IIngredientTypeWithSubtypes<Object, Object>) type;
					SubtypeInterpreters interpreters = ((SubtypeRegistration) registration).getInterpreters();
					return interpreters.contains(castedType, castedType.getBase(ingredient));
				};
			}
			return;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		hasSubtype = (type, ingredient) -> {
			@SuppressWarnings("unchecked")
			IIngredientTypeWithSubtypes<Object, Object> castedType = (IIngredientTypeWithSubtypes<Object, Object>) type;
			return subtypeManager.getSubtypeInfo(castedType, ingredient, UidContext.Recipe) != IIngredientSubtypeInterpreter.NONE;
		};
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		subtypeManager = registration.getSubtypeManager();
	}

	@Override
	public void registerRuntime(IRuntimeRegistration registration) {
		registration.setIngredientListOverlay(new JemiIngredientListOverlay());
		registration.setBookmarkOverlay(new JemiBookmarkOverlay());
		registration.setRecipesGui(new JemiRecipesGui());
		registration.setIngredientFilter(new JemiIngredientFilter());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		JemiPlugin.runtime = runtime;
	}

	@Override
	public void onRuntimeUnavailable() {
		JemiPlugin.runtime = null;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void register(EmiRegistry registry) {
		EmiLog.info("[JEMI] Waiting for JEI to finish reloading...");
		EmiReloadManager.step(EmiPort.literal("Waiting for JEI to finish..."), 20_000);
		try {
			while (true) {
				if (runtime != null) {
					break;
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {
			return;
		}
		EmiLog.info("[JEMI] JEI reloaded!");
		Set<String> handledNamespaces = EmiAgnos.getPlugins().stream().map(EmiPluginContainer::id).collect(Collectors.toSet());

		EmiReloadManager.step(EmiPort.literal("Loading information from JEI..."), 5_000);
		registry.addGenericExclusionArea((screen, consumer) -> {
			if (runtime != null && runtime.getScreenHelper() != null) {
				List<Rect2i> areas = runtime.getScreenHelper().getGuiExclusionAreas(screen).toList();
				for (Rect2i r : areas) {
					if (r != null) {
						consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
					}
				}
			}
		});

		registry.addGenericStackProvider((screen, x, y) -> {
			return new EmiStackInteraction(runtime.getScreenHelper().getClickableIngredientUnderMouse(screen, x, y)
					.map(IClickableIngredient::getTypedIngredient).map(JemiUtil::getStack).findFirst().orElse(EmiStack.EMPTY), null, false);
		});

		registry.addGenericDragDropHandler(new JemiDragDropHandler());

		registry.addIngredientSerializer(JemiStack.class, new JemiStackSerializer(runtime.getIngredientManager()));

		EmiReloadManager.step(EmiPort.literal("Processing JEI stacks..."), 5_000);
		for (IIngredientType<?> type : runtime.getIngredientManager().getRegisteredIngredientTypes()) {
			if (type == JemiUtil.getFluidType() || type == VanillaTypes.ITEM_STACK) {
				continue;
			}
			for (Object o : runtime.getIngredientManager().getAllIngredients(type)) {
				EmiStack stack = JemiUtil.getStack(type, o);
				if (!stack.isEmpty()) {
					registry.addEmiStack(stack);
				}
			}
		}

		registry.removeEmiStacks(s -> {
			try {
				Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(s);
				if (opt.isPresent()) {
					return !runtime.getIngredientVisibility().isIngredientVisible(opt.get());
				}
			} catch (Throwable t) {
			}
			return false;
		});
		EmiReloadManager.step(EmiPort.literal("Processing JEI subtypes..."), 5_000);
		safely("subtype comparison", () -> parseSubtypes(registry));

		EmiReloadManager.step(EmiPort.literal("Processing JEI recipes..."), 5_000);
		Set<Identifier> existingCategories = EmiRecipes.categories.stream().map(EmiRecipeCategory::getId).collect(Collectors.toSet());
		Map<RecipeType, EmiRecipeCategory> categoryMap = Maps.newHashMap();
		categoryMap.put(RecipeTypes.CRAFTING, VanillaEmiRecipeCategories.CRAFTING);
		categoryMap.put(RecipeTypes.SMELTING, VanillaEmiRecipeCategories.SMELTING);
		categoryMap.put(RecipeTypes.BLASTING, VanillaEmiRecipeCategories.BLASTING);
		categoryMap.put(RecipeTypes.SMOKING, VanillaEmiRecipeCategories.SMOKING);
		categoryMap.put(RecipeTypes.CAMPFIRE_COOKING, VanillaEmiRecipeCategories.CAMPFIRE_COOKING);
		categoryMap.put(RecipeTypes.STONECUTTING, VanillaEmiRecipeCategories.STONECUTTING);
		categoryMap.put(RecipeTypes.SMITHING, VanillaEmiRecipeCategories.SMITHING);
		categoryMap.put(RecipeTypes.ANVIL, VanillaEmiRecipeCategories.ANVIL_REPAIRING);
		categoryMap.put(RecipeTypes.BREWING, VanillaEmiRecipeCategories.BREWING);
		categoryMap.put(RecipeTypes.FUELING, VanillaEmiRecipeCategories.FUEL);
		categoryMap.put(RecipeTypes.COMPOSTING, VanillaEmiRecipeCategories.COMPOSTING);
		categoryMap.put(RecipeTypes.INFORMATION, VanillaEmiRecipeCategories.INFO);
		
		CATEGORY_MAP.clear();
		EmiRecipeFiller.extraHandlers = JemiPlugin::getRecipeHandler;

		List<IRecipeCategory<?>> categories = runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get().toList();
		for (IRecipeCategory<?> c : categories) {
			EmiLog.info("[JEMI] Collecting data for " + c.getTitle().getString());
			EmiReloadManager.step(EmiPort.literal("Loading JEI data for ").append(c.getTitle()), 5_000);
			try {
				RecipeType type = c.getRecipeType();
				Identifier id = type.getUid();
				List<EmiStack> catalysts = runtime.getRecipeManager().createRecipeCatalystLookup(type).includeHidden().get().map(JemiUtil::getStack).toList();
				if (categoryMap.containsKey(type)) {
					EmiRecipeCategory category = categoryMap.get(type);
					CATEGORY_MAP.put(category, c);
					for (EmiStack catalyst : catalysts) {
						if (!catalyst.isEmpty()) {
							registry.addWorkstation(category, catalyst);
						}
					}
					if (type == RecipeTypes.INFORMATION) {
						addInfoRecipes(registry, (IRecipeCategory<IJeiIngredientInfoRecipe>) c);
					} else if (type == RecipeTypes.CRAFTING) {
						addCraftingRecipes(registry, (IRecipeCategory<CraftingRecipe>) c);
					}
					continue;
				}
				if (handledNamespaces.contains(id.getNamespace())) {
					EmiLog.info("[JEMI] Skipping recipe category " + id + " because mod is already handled");
					continue;
				}
				if (existingCategories.contains(id)) {
					EmiLog.info("[JEMI] Skipping recipe category " + id + " because native EMI recipe category already exists");
					continue;
				}
				EmiRecipeCategory category = new JemiCategory(c);
				CATEGORY_MAP.put(category, c);
				registry.addCategory(category);
				for (EmiStack catalyst : catalysts) {
					if (!catalyst.isEmpty()) {
						registry.addWorkstation(category, catalyst);
					}
				}
				List<?> recipes = runtime.getRecipeManager().createRecipeLookup(type).includeHidden().get().toList();
				for (Object r : recipes) {
					try {
						registry.addRecipe(new JemiRecipe(category, c, r));
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void addInfoRecipes(EmiRegistry registry, IRecipeCategory<IJeiIngredientInfoRecipe> category) {
		List<IJeiIngredientInfoRecipe> recipes = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.INFORMATION).includeHidden().get().toList();
		Map<List<EmiStack>, List<IJeiIngredientInfoRecipe>> grouped = Maps.newHashMap();
		for (IJeiIngredientInfoRecipe recipe : recipes) {
			grouped.computeIfAbsent(recipe.getIngredients().stream().map(JemiUtil::getStack).toList(), k -> Lists.newArrayList()).add(recipe);
		}
		Map<Text, List<EmiStack>> identical = Maps.newHashMap();
		for (Map.Entry<List<EmiStack>, List<IJeiIngredientInfoRecipe>> group : grouped.entrySet()) {
			MutableText text = EmiPort.literal("");
			for (IJeiIngredientInfoRecipe recipe : group.getValue()) {
				for (StringVisitable sv : recipe.getDescription()) {
					MutableText current = EmiPort.literal("");
					sv.visit((style, string) -> {
						current.append(EmiPort.literal(string, style));
						return Optional.empty();
					}, Style.EMPTY);
					if (!current.getString().isBlank()) {
						if (!text.getString().isEmpty()) {
							text.append(" ");
						}
						text.append(current);
					}
				}
			}
			identical.computeIfAbsent(text, k -> Lists.newArrayList()).addAll(group.getKey());
		}
		
		for (Text text : identical.keySet()) {
			registry.addRecipe(new EmiInfoRecipe(identical.get(text).stream().map(s -> (EmiIngredient) s).toList(), List.of(text), null));
		}
	}

	private void addCraftingRecipes(EmiRegistry registry, IRecipeCategory<CraftingRecipe> category) {
		Set<Identifier> replaced = Sets.newHashSet();
		Set<EmiRecipe> replacements = Sets.newHashSet();
		List<CraftingRecipe> recipes = Stream.concat(
			runtime.getRecipeManager().createRecipeLookup(category.getRecipeType()).includeHidden().get(),
			registry.getRecipeManager().listAllOfType(net.minecraft.recipe.RecipeType.CRAFTING).stream()
				.filter(r -> r instanceof SpecialCraftingRecipe)
		).distinct().toList();
		for (CraftingRecipe recipe : recipes) {
			try {
				if (category.isHandled(recipe)) {
					JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
					category.setRecipe(builder, recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
					List<EmiIngredient> inputs = Lists.newArrayList();
					List<EmiStack> outputs = Lists.newArrayList();
					for (JemiIngredientAcceptor acceptor : builder.ingredients) {
						EmiIngredient stack = acceptor.build();
						if (acceptor.role == RecipeIngredientRole.INPUT) {
							inputs.add(stack);
						} else if (acceptor.role == RecipeIngredientRole.CATALYST) {
							inputs.add(stack);
						} else if (acceptor.role == RecipeIngredientRole.OUTPUT) {
							outputs.addAll(stack.getEmiStacks());
						}
					}
					if (inputs.stream().anyMatch(i -> !i.isEmpty()) && outputs.stream().anyMatch(o -> !o.isEmpty())) {
						EmiRecipe replacement;
						if (outputs.size() > 1) {
							replacement = new EmiPatternCraftingRecipe(inputs, EmiStack.EMPTY, category.getRegistryName(recipe), builder.shapeless) {

								@Override
								public List<EmiStack> getOutputs() {
									return outputs;
								}
								
								@Override
								public SlotWidget getInputWidget(int slot, int x, int y) {
									if (slot <= inputs.size()) {
										return new SlotWidget(inputs.get(slot), x, y);
									} else {
										return new SlotWidget(EmiStack.EMPTY, x, y);
									}
								}

								@Override
								public SlotWidget getOutputWidget(int x, int y) {
									return new GeneratedSlotWidget(r -> outputs.get(r.nextInt(outputs.size())), recipe.hashCode(), x, y);
								}
								
							};
						} else {
							replacement = new EmiCraftingRecipe(inputs, outputs.get(0), category.getRegistryName(recipe), builder.shapeless);
						}
						if (replacement.getId() != null) {
							replaced.add(replacement.getId());
						}
						replacements.add(replacement);
						registry.addRecipe(replacement);
					}
				}
			} catch (Throwable t) {
				EmiLog.error("[JEMI] Exception thrown setting JEI crafting recipe");
			}
		}
		registry.removeRecipes(r -> r instanceof EmiCraftingRecipe && replaced.contains(r.getId()) && !replacements.contains(r));
	}

	@SuppressWarnings({"unchecked"})
	private void parseSubtypes(EmiRegistry registry) {
		if (subtypeManager != null) {
			IIngredientManager im = runtime.getIngredientManager();
			List<IIngredientType<?>> types = Lists.newArrayList(im.getRegisteredIngredientTypes());
			for (Item item : EmiPort.getItemRegistry()) {
				if (hasSubtype.test(VanillaTypes.ITEM_STACK, item.getDefaultStack())) {
					registry.setDefaultComparison(item, Comparison.compareData(stack -> {
						return subtypeManager.getSubtypeInfo(stack.getItemStack(), UidContext.Recipe);
					}));
				}
			}
			for (Fluid fluid : EmiPort.getFluidRegistry()) {
				IIngredientTypeWithSubtypes<Object, Object> type = (IIngredientTypeWithSubtypes<Object, Object>) JemiUtil.getFluidType();
				if (hasSubtype.test(type, JemiUtil.getFluidHelper().create(fluid, 1000))) {
					registry.setDefaultComparison(fluid, Comparison.compareData(stack -> {
						ITypedIngredient<?> typed = JemiUtil.getTyped(stack).orElse(null);
						if (typed != null) {
							return subtypeManager.getSubtypeInfo(type, typed.getIngredient(), UidContext.Recipe);
						}
						return null;
					}));
				}
			}
			for (IIngredientType<?> type : types) {
				if (type == VanillaTypes.ITEM_STACK || type == JemiUtil.getFluidType()) {
					continue;
				}
				if (type instanceof IIngredientTypeWithSubtypes iitws) {
					List<Object> ings = Lists.newArrayList(im.getAllIngredients(type));
					for (Object o : ings) {
						try {
							if (hasSubtype.test(iitws, o)) {
								registry.setDefaultComparison(iitws.getBase(o), Comparison.compareData(stack -> {
									if (stack instanceof JemiStack jemi) {
										return subtypeManager.getSubtypeInfo(iitws, jemi.ingredient, UidContext.Recipe);
									}
									return null;
								}));
							}
						} catch (Throwable t) {
							EmiReloadLog.warn("Exception adding default comparison for JEI ingredient");
							EmiReloadLog.error(t);
						}
					}
				}
			}
		}
	}

	private static EmiRecipeHandler<?> getRecipeHandler(ScreenHandler handler, EmiRecipe recipe) {
		IRecipeCategory<?> category = CATEGORY_MAP.getOrDefault(recipe.getCategory(), null);
		if (category != null) {
			return runtime.getRecipeTransferManager().getRecipeTransferHandler(handler, category).map(JemiRecipeHandler::new).orElse(null);
		}
		return null;
	}

	private static void safely(String name, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			EmiReloadLog.warn("Exception thrown when reloading " + name  + " step in JEMI plugin");
			EmiReloadLog.error(t);
		}
	}
}
