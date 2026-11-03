package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.mixin.accessor.CraftingResultSlotAccessor;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.runtime.ProxyRecipeManager;

public class EmiStackProviders {
	public static Map<Class<?>, List<EmiStackProvider<?>>> fromClass = Maps.newHashMap();
	public static List<EmiStackProvider<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiStackInteraction getStackAt(Screen screen, int x, int y, boolean notClick) {
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiStackProvider provider : fromClass.get(screen.getClass())) {
				EmiStackInteraction stack = provider.getStackAt(screen, x, y);
				if (!stack.isEmpty() && (notClick || stack.isClickable())) {
					return stack;
				}
			}
		}
		for (EmiStackProvider handler : generic) {
			EmiStackInteraction stack = handler.getStackAt(screen, x, y);
			if (!stack.isEmpty() && (notClick || stack.isClickable())) {
				return stack;
			}
		}
		if (notClick && screen instanceof HandledScreenAccessor handled) {
			Slot s = handled.getFocusedSlot();
			if (s != null) {
				ItemStack stack = s.getItem();
				if (!stack.isEmpty()) {
					if (s instanceof ResultSlot craf) {
						// Emi be making assumptions
						try {
							CraftingContainer inv = ((CraftingResultSlotAccessor) craf).getInput();
							CraftingInput input = CraftingInput.of(inv.getWidth(), inv.getHeight(), inv.getItems());
							CraftingRecipe crafting = ProxyRecipeManager.getFirst(RecipeType.CRAFTING, input);
							if (crafting != null) {
								Identifier id = ProxyRecipeManager.getId(crafting);
								EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
								if (recipe != null) {
									return new EmiStackInteraction(EmiStack.of(stack), recipe, false);
								}
							}
						} catch (Exception e) {
						}
					}
					return new EmiStackInteraction(EmiStack.of(stack));
				}
			}
		}
		return EmiStackInteraction.EMPTY;
	}
}
