package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.RecipeScreen;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.core.config.IClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;


/**
 * The extension of main work class instead of implementing interface is needed, as JEI do not have build in overwrite
 * functions yet. A lot of rely on implementation class.
 */
public class JemiRecipesGui extends RecipesGui {

	/**
	 * Gives easier access to recipe transfer manager instnace.
	 */
	private final RecipeTransferManager recipeTransferManager;

	public JemiRecipesGui(RecipeManager recipeManager,
		RecipeTransferManager recipeTransferManager,
		RegisteredIngredients registeredIngredients,
		IModIdHelper modIdHelper,
		IClientConfig clientConfig,
		Textures textures,
		IIngredientVisibility ingredientVisibility,
		IKeyBindings keyBindings) {
		super(recipeManager,
			recipeTransferManager,
			registeredIngredients,
			modIdHelper,
			clientConfig,
			textures,
			ingredientVisibility,
			keyBindings);

		this.recipeTransferManager = recipeTransferManager;
	}


	@Override
	public void show(List<IFocus<?>> focuses) {
		for (IFocus<?> focus : focuses) {
			EmiStack stack = JemiUtil.getStack(focus.getTypedValue());
			if (!stack.isEmpty()) {
				RecipeIngredientRole role = focus.getRole();
				if (role == RecipeIngredientRole.OUTPUT) {
					EmiApi.displayRecipes(stack);
				} else {
					EmiApi.displayUses(stack);
				}
			}
		}
	}


	@Override
	public <V> void show(IFocus<V> focus) {
		EmiStack stack = JemiUtil.getStack(focus.getTypedValue());
		if (!stack.isEmpty()) {
			RecipeIngredientRole role = focus.getRole();
			if (role == RecipeIngredientRole.OUTPUT) {
				EmiApi.displayRecipes(stack);
			} else {
				EmiApi.displayUses(stack);
			}
		}
	}


	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {
		for (RecipeType<?> type : recipeTypes) {
			for (EmiRecipeCategory category : EmiApi.getRecipeManager().getCategories()) {
				if (category.getId().equals(type.getUid())) {
					EmiApi.displayRecipeCategory(category);
				}
			}
		}
	}

	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof RecipeScreen screen) {
			EmiIngredient stack = screen.getHoveredStack();
			if (!stack.isEmpty()) {
				Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(stack.getEmiStacks().get(0));
				if (opt.isPresent()) {
					return opt.get().getIngredient(ingredientType).orElse(null);
				}
			}
		}
		return null;
	}


	@Override
	public void showCategories(List<Identifier> recipeCategoryUids) {
	}


	//@Override
	public Optional<Screen> getParentScreen() {
		return Optional.empty();
	}


    public RecipeTransferManager getRecipeTransferManager() {
        return this.recipeTransferManager;
    }


// ---------------------------------------------------------------------
// Section: Disabled methods
// ---------------------------------------------------------------------


	/**
	 * Disables RecipesGui screen initialization
	 */
	@Override
	public void init() {
	}


	/**
	 * Disables RecipesGui screen rendering
	 */
	@Override
	public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
	}


	/**
	 * Prevents mouse over detection for RecipeGui
	 */
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return false;
	}


	/**
	 * Prevents mouse scrolling detection for RecipeGui
	 */
	@Override
	public boolean mouseScrolled(double scrollX, double scrollY, double scrollDelta) {
		return false;
	}


	/**
	 * Prevents mouse click detection for RecipeGui
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}


	/**
	 * Prevents key press detection for RecipeGui
	 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		return false;
	}


	@Override
	public void close() {
	}
}
