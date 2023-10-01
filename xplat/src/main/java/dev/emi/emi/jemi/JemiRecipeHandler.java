package dev.emi.emi.jemi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiRecipeSlotsView;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.ScreenHandler;

public class JemiRecipeHandler<T extends ScreenHandler, R> implements EmiRecipeHandler<T> {
	private final RecipeType<R> type;
	//private IRecipeCategory<R> category;
	public IRecipeTransferHandler<T, R> handler;

	public JemiRecipeHandler(IRecipeTransferHandler<T, R> handler) {
		this.handler = handler;
		type = handler.getRecipeType();
		/*
		if (type != null) {
			List<IRecipeCategory<R>> categories = (List<IRecipeCategory<R>>) (Object) JemiPlugin.runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().limitTypes(List.of(type)).get().toList();
			if (!categories.isEmpty()) {
				category = categories.get(0);
			}
		}*/
	}

	@Override
	public boolean alwaysDisplaySupport(EmiRecipe recipe) {
		return type != null;
	}

	@Override
	public EmiPlayerInventory getInventory(HandledScreen<T> screen) {
		return new EmiPlayerInventory(List.of());
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return (type == null || getRawRecipe(recipe) != null) && recipe.supportsRecipeTree();
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
		IRecipeTransferError err = jeiCraft(recipe, context, false);
		return err == null || err.getType().allowsTransfer;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
		IRecipeTransferError err = jeiCraft(recipe, context, true);
		if (err == null || err.getType().allowsTransfer) {
			MinecraftClient.getInstance().setScreen(context.getScreen());
		}
		return err == null || err.getType().allowsTransfer;
	}

	@Override
	public void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, DrawContext raw) {
		EmiDrawContext draw = EmiDrawContext.wrap(raw);
		IRecipeTransferError err = jeiCraft(recipe, context, false);
		if (err != null) {
			if (err.getType() == IRecipeTransferError.Type.COSMETIC) {
				for (Widget widget : widgets) {
					if (widget instanceof RecipeFillButtonWidget) {
						Bounds b = widget.getBounds();
						draw.fill(b.left(), b.top(), b.width(), b.height(), err.getButtonHighlightColor());
					}
				}
			}
			/*R rawRecipe = getRawRecipe(recipe);
			JemiRecipeSlotsView view = createSlotsView(recipe, rawRecipe);
			if (view != null) {
				err.showError(raw, EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, view, 0, 0);
			}*/
		}
	}

	@SuppressWarnings("unchecked")
	private IRecipeTransferError jeiCraft(EmiRecipe recipe, EmiCraftContext<T> context, boolean craft) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			R rawRecipe = getRawRecipe(recipe);
			
			JemiRecipeSlotsView view = createSlotsView(recipe, rawRecipe);

			if (view == null) {
				return () -> IRecipeTransferError.Type.INTERNAL;
			}
			
			return handler.transferRecipe(context.getScreenHandler(), rawRecipe != null ? rawRecipe : (R) recipe, view, client.player, context.getAmount() > 1, craft);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return () -> IRecipeTransferError.Type.INTERNAL;
	}

	private JemiRecipeSlotsView createSlotsView(EmiRecipe recipe, R rawRecipe) {
		JemiRecipeLayoutBuilder builder = null;
		if (rawRecipe != null) {
			/*
			if (category != null) {
				builder = new JemiRecipeLayoutBuilder();
				category.setRecipe(builder, rawRecipe, JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
			}*/
		} else if (type != null) {
			return null;
		}

		if (builder == null) {
			builder = new JemiRecipeLayoutBuilder();
			addIngredients(builder, recipe.getOutputs(), RecipeIngredientRole.OUTPUT);
			addIngredients(builder, recipe.getInputs(), RecipeIngredientRole.INPUT);
			if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING) {
				for (int i = recipe.getInputs().size(); i < 9; i++) {
					addIngredients(builder, List.of(EmiStack.EMPTY), RecipeIngredientRole.INPUT);
				}
			}
			addIngredients(builder, recipe.getCatalysts(), RecipeIngredientRole.CATALYST);
		}

		return new JemiRecipeSlotsView(builder.slots.stream().map(JemiRecipeSlot::new).toList());
	}

	@SuppressWarnings("unchecked")
	private R getRawRecipe(EmiRecipe recipe) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			RecipeManager manager = client.world.getRecipeManager();
			if (type != null && type.getRecipeClass() != null) {
				if (recipe instanceof JemiRecipe jr && jr.recipe != null) {
					if (type.getRecipeClass().isAssignableFrom(jr.recipe.getClass())) {
						return type.getRecipeClass().cast(jr.recipe);
					}
				}
				if (manager != null) {
					Optional<? extends Recipe<?>> opt = manager.get(recipe.getId());
					if (opt.isPresent()) {
						Recipe<?> r = opt.get();
						if (type.getRecipeClass().isAssignableFrom(r.getClass())) {
							return type.getRecipeClass().cast(r);
						}
					}
				}
			}
			if (manager != null) {
				Optional<? extends Recipe<?>> opt = manager.get(recipe.getId());
				if (opt.isPresent()) {
					return (R) opt.get();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addIngredients(JemiRecipeLayoutBuilder builder, List<? extends EmiIngredient> stacks, RecipeIngredientRole role) {
		for (EmiIngredient ing : stacks) {
			IIngredientAcceptor acceptor = builder.addSlot(role, 0, 0);
			for (EmiStack stack : ing.getEmiStacks()) {
				Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(stack);
				if (opt.isPresent()) {
					ITypedIngredient<?> typed = opt.get();
					acceptor.addIngredient((IIngredientType) typed.getType(), typed.getIngredient());
				}
			}
		}
	}
}
