package dev.emi.emi.screen;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class FakeScreen extends Screen {
	private static MethodHandle handle;
	public static final FakeScreen INSTANCE = new FakeScreen();
	
	static {
		try {
			String name = FabricLoader.getInstance().getMappingResolver()
				.mapMethodName("intermediary", "net.minecraft.class_437", "method_32635",
					"(Ljava/util/List;Lnet/minecraft/class_5632;)V");
			Method m = Screen.class.getDeclaredMethod(name, List.class, TooltipData.class);
			m.setAccessible(true);
			handle = MethodHandles.lookup().unreflect(m);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected FakeScreen() {
		super(new LiteralText(""));
		this.init(MinecraftClient.getInstance(), 99999, 99999);
	}

	public List<TooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
		List<TooltipComponent> list = this.getTooltipFromItem(stack)
			.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
		Optional<TooltipData> data = stack.getTooltipData();
		if (data.isPresent()) {
			try {
				handle.invokeWithArguments(list, data.get());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
