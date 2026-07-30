package dev.emi.emi.hacks;

import java.util.List;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.ComponentChanges;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ImmutableEmiStack extends EmiStack {
	private final EmiStack underlying;

	public ImmutableEmiStack(EmiStack underlying) {
		this.underlying = underlying;
	}

	@Override
	public long getAmount() {
		return underlying.getAmount();
	}

	@Override
	public float getChance() {
		return underlying.getChance();
	}

	@Override
	public EmiStack setAmount(long amount) {
		// No op
		return this;
	}

	@Override
	public EmiStack setChance(float chance) {
		// No op
		return this;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		underlying.render(draw, x, y, delta, flags);
	}

	@Override
	public EmiStack copy() {
		return underlying.copy();
	}

	@Override
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	@Override
	public ComponentChanges getComponentChanges() {
		return underlying.getComponentChanges();
	}

	@Override
	public Object getKey() {
		return underlying.getKey();
	}

	@Override
	public Identifier getId() {
		return underlying.getId();
	}

	@Override
	public List<Text> getTooltipText() {
		return underlying.getTooltipText();
	}

	@Override
	public Text getName() {
		return underlying.getName();
	}
}
