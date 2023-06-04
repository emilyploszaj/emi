package dev.emi.emi.jemi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.util.math.Rect2i;
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

	@Override
	public Identifier getPluginUid() {
		return new Identifier("emi:jemi");
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
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(s);
			if (opt.isPresent()) {
				return !runtime.getIngredientVisibility().isIngredientVisible(opt.get());
			}
			return false;
		});

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
			EmiLog.info("[JEMI] Collecing data for " + c.getTitle().getString());
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

		EmiReloadManager.step(EmiPort.literal("Processing JEI subtypes..."), 5_000);
		parseSubtypes(registry);
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
					sv.visit((style, string) -> {
						return Optional.of(EmiPort.literal(string, style));
					}, Style.EMPTY).ifPresent(t -> text.append(" ").append(t));
				}
			}
			identical.computeIfAbsent(text, k -> Lists.newArrayList()).addAll(group.getKey());
		}
		
		for (Text text : identical.keySet()) {
			registry.addRecipe(new EmiInfoRecipe(identical.get(text).stream().map(s -> (EmiIngredient) s).toList(), List.of(text), null));
		}
	}

	@SuppressWarnings({"unchecked"})
	private void parseSubtypes(EmiRegistry registry) {
		if (subtypeManager != null) {
			IIngredientManager im = runtime.getIngredientManager();
			List<IIngredientType<?>> types = Lists.newArrayList(im.getRegisteredIngredientTypes());
			for (IIngredientType<?> type : types) {
				if (type instanceof IIngredientTypeWithSubtypes iitws) {
					List<Object> ings = Lists.newArrayList(im.getAllIngredients(type));
					for (Object o : ings) {
						String info = subtypeManager.getSubtypeInfo(iitws, o, UidContext.Recipe);
						if (info != IIngredientSubtypeInterpreter.NONE) {
							if (type == VanillaTypes.ITEM_STACK) {
								registry.setDefaultComparison(iitws.getBase(o), Comparison.of((a, b) -> {
									return subtypeManager.getSubtypeInfo(a.getItemStack(), UidContext.Recipe)
										.equals(subtypeManager.getSubtypeInfo(b.getItemStack(), UidContext.Recipe));
								}));
							} else if (type == JemiUtil.getFluidType()) {
								registry.setDefaultComparison(iitws.getBase(o), Comparison.of((a, b) -> {
									ITypedIngredient<?> ta = JemiUtil.getTyped(a).orElse(null);
									ITypedIngredient<?> tb = JemiUtil.getTyped(b).orElse(null);
									if (ta != null && tb != null) {
										return subtypeManager.getSubtypeInfo(iitws, ta.getIngredient(), UidContext.Recipe)
											.equals(subtypeManager.getSubtypeInfo(iitws, tb.getIngredient(), UidContext.Recipe));
									}
									return false;
								}));
							} else {
								registry.setDefaultComparison(iitws.getBase(o), Comparison.of((a, b) -> {
									if (a instanceof JemiStack ja && b instanceof JemiStack jb) {
										return subtypeManager.getSubtypeInfo(iitws, ja.ingredient, UidContext.Recipe)
											.equals(subtypeManager.getSubtypeInfo(iitws, jb.ingredient, UidContext.Recipe));
									}
									return false;
								}));
							}
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
}
