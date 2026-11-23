package dev.emi.emi.api.stack;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;

import dev.emi.emi.EmiPort;

@ApiStatus.Internal
public class EmptyEmiStack extends EmiStack {
	private static final Identifier ID = EmiPort.id("emi", "empty");

	@Override
	public EmiStack getRemainder() {
		return EMPTY;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return List.of(EMPTY);
	}

	@Override
	public EmiStack setRemainder(EmiStack stack) {
		throw new UnsupportedOperationException("Cannot mutate an empty stack");
	}

	@Override
	public EmiStack copy() {
		return EMPTY;
	}
	
	public EmiStack setAmount(long amount) {
		return this;
	}
	
	public EmiStack setChance(float chance) {
		return this;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public DataComponentPatch getComponentChanges() {
		return DataComponentPatch.EMPTY;
	}

	@Override
	public Object getKey() {
		return Items.AIR;
	}

	@Override
	public ItemStack getItemStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public boolean isEqual(EmiStack stack) {
		return stack == EMPTY;
	}

	@Override
	public void render(GuiGraphicsExtractor draw, int x, int y, float delta, int flags) {
	}

	@Override
	public List<Component> getTooltipText() {
		return List.of();
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		return List.of();
	}

	@Override
	public Component getName() {
		return EmiPort.literal("");
	}

	static class EmptyEntry {
	}
}