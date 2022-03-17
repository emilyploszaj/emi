package dev.emi.emi.screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.emi.emi.mixin.accessor.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class FakeScreen extends Screen {
	public static final FakeScreen INSTANCE = new FakeScreen();

	protected FakeScreen() {
		super(new LiteralText(""));
		this.init(MinecraftClient.getInstance(), 99999, 99999);
	}

	public List<TooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
		List<TooltipComponent> list = this.getTooltipFromItem(stack)
			.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
		Optional<TooltipData> data = stack.getTooltipData();
		if (data.isPresent()) {
			((ScreenAccessor) this).invokeMethod_32635(list, data.get());
		}
		return list;
	}
}
