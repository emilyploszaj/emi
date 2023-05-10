package dev.emi.emi.jemi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.runtime.JemiBookmarkOverlay;
import dev.emi.emi.jemi.runtime.JemiIngredientFilter;
import dev.emi.emi.jemi.runtime.JemiIngredientListOverlay;
import dev.emi.emi.jemi.runtime.JemiRecipesGui;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@JeiPlugin
public class JemiPlugin implements IModPlugin, EmiPlugin {
	public static IJeiRuntime runtime;

	@Override
	public Identifier getPluginUid() {
		return new Identifier("emi:emi");
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void register(EmiRegistry registry) {
		EmiLog.info("[JEMI] Waiting for JEI to finish reloading...");
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

		Set<String> handledNamespaces = Stream.concat(EmiAgnos.getPlugins().stream().map(EmiPluginContainer::id), Stream.of("minecraft", "jei"))
			.distinct().collect(Collectors.toSet());
		Set<Identifier> existingCategories = EmiRecipes.categories.stream().map(EmiRecipeCategory::getId).collect(Collectors.toSet());

		List<IRecipeCategory<?>> categories = runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get().toList();
		for (IRecipeCategory<?> c : categories) {
			try {
				Identifier id = c.getRecipeType().getUid();
				if (c.getRecipeType() == RecipeTypes.INFORMATION) {
					try {
						addInfoRecipes(registry, (IRecipeCategory<IJeiIngredientInfoRecipe>) c);
					} catch (Throwable t) {
						t.printStackTrace();
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
				registry.addCategory(category);
				List<EmiStack> catalysts = runtime.getRecipeManager().createRecipeCatalystLookup(c.getRecipeType()).includeHidden().get().map(JemiUtil::getStack).toList();
				for (EmiStack catalyst : catalysts) {
					if (!catalyst.isEmpty()) {
						registry.addWorkstation(category, catalyst);
					}
				}
				List<?> recipes = runtime.getRecipeManager().createRecipeLookup(c.getRecipeType()).includeHidden().get().toList();
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
}
