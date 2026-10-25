package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
	@Unique
	private static boolean hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");
	
	private AbstractInventoryScreenMixin() { super(null, null, null); }

	@Shadow
	private Component getEffectName(MobEffectInstance effect) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void renderBackgrounds(GuiGraphics draw, int x, int height, Iterable<MobEffectInstance> statusEffects, boolean wide) {
		throw new UnsupportedOperationException();
	}
	
	@Shadow
	private void renderIcons(GuiGraphics draw, int x, int height, Iterable<MobEffectInstance> statusEffects, boolean wide) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void renderLabels(GuiGraphics draw, int x, int height, Iterable<MobEffectInstance> statusEffects) {
		throw new UnsupportedOperationException();
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen.renderBackgrounds(Lnet/minecraft/client/gui/GuiGraphics;IILjava/lang/Iterable;Z)V"),
		method = "renderEffects")
	private void drawStatusEffects(GuiGraphics draw, int mouseX, int mouseY, CallbackInfo info) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			emi$drawCenteredEffects(draw, mouseX, mouseY);
		}
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "java/util/Collection.size()I", ordinal = 0),
		method = "renderEffects", ordinal = 0)
	private Collection<MobEffectInstance> drawStatusEffects(Collection<MobEffectInstance> original) {
		if (EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN) {
			return List.of();
		}
		return original;
	}

	private void emi$drawCenteredEffects(GuiGraphics raw, int mouseX, int mouseY) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		Collection<MobEffectInstance> effects = Ordering.natural().sortedCopy(this.minecraft.player.getActiveEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}
		boolean wide = size == 1;
		int y = this.topPos - 34;
		if (((Object) this) instanceof CreativeModeInventoryScreen || hasInventoryTabs) {
			y -= 28;
			if (((Object) this) instanceof CreativeModeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}
		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (this.imageWidth - 32) / (size - 1);
		}
		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = this.leftPos + (this.imageWidth - width) / 2;
		MobEffectInstance hovered = null;
		int restoreY = this.topPos;
		try {
			this.topPos = y;
			for (MobEffectInstance inst : effects) {
				int ew = wide ? 120 : 32;
				List<MobEffectInstance> single = List.of(inst);
				this.renderBackgrounds(context.raw(), x, 32, single, wide);
				this.renderIcons(context.raw(), x, 32, single, wide);
				if (wide) {
					this.renderLabels(context.raw(), x, 32, single);
				}
				if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
					hovered = inst;
				}
				x += xOff;
			}
		} finally {
			this.topPos = restoreY;
		}
		if (hovered != null && size > 1) {
			List<Component> list = List.of(this.getEffectName(hovered), MobEffectUtil.formatDuration(hovered, 1.0f, minecraft.level.tickRateManager().tickrate()));
			context.raw().renderTooltip(minecraft.font, list, Optional.empty(), mouseX, Math.max(mouseY, 16));
		}
	}
	
	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "renderEffects", ordinal = 0)
	private boolean squishEffects(boolean original) {
		return !EmiConfig.effectLocation.compressed;
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "renderEffects", ordinal = 2)
	private int changeEffectSpace(int original) {
		return switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED, HIDDEN -> original;
			case TOP -> this.leftPos;
			case LEFT_COMPRESSED -> this.leftPos - 2- 32;
			case LEFT -> this.leftPos - 2 - 120;
		};
	}
}
