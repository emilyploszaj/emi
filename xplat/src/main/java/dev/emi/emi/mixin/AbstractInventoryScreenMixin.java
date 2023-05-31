package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {
	@Unique
	private static boolean hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");
	
	private AbstractInventoryScreenMixin() { super(null, null, null); }

	@Shadow
	private Text getStatusEffectDescription(StatusEffectInstance effect) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void drawStatusEffectBackgrounds(MatrixStack matrices, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
		throw new UnsupportedOperationException();
	}
	
	@Shadow
	private void drawStatusEffectSprites(MatrixStack matrices, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean wide) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void drawStatusEffectDescriptions(MatrixStack matrices, int x, int height, Iterable<StatusEffectInstance> statusEffects) {
		throw new UnsupportedOperationException();
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/AbstractInventoryScreen.drawStatusEffectBackgrounds(Lnet/minecraft/client/util/math/MatrixStack;IILjava/lang/Iterable;Z)V"),
		method = "drawStatusEffects")
	private void drawStatusEffects(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo info) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			emi$drawCenteredEffects(matrices, mouseX, mouseY);
		}
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "java/util/Collection.size()I", ordinal = 0),
		method = "drawStatusEffects", ordinal = 0)
	private Collection<StatusEffectInstance> drawStatusEffects(Collection<StatusEffectInstance> original) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			return List.of();
		}
		return original;
	}

	private void emi$drawCenteredEffects(MatrixStack matrices, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		Collection<StatusEffectInstance> effects = Ordering.natural().sortedCopy(this.client.player.getStatusEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}
		boolean wide = size == 1;
		int y = this.y - 34;
		if (((Object) this) instanceof CreativeInventoryScreen || hasInventoryTabs) {
			y -= 28;
			if (((Object) this) instanceof CreativeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}
		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (this.backgroundWidth - 32) / (size - 1);
		}
		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = this.x + (this.backgroundWidth - width) / 2;
		StatusEffectInstance hovered = null;
		int restoreY = this.y;
		try {
			this.y = y;
			for (StatusEffectInstance inst : effects) {
				int ew = wide ? 120 : 32;
				List<StatusEffectInstance> single = List.of(inst);
				this.drawStatusEffectBackgrounds(matrices, x, 32, single, wide);
				this.drawStatusEffectSprites(matrices, x, 32, single, wide);
				if (wide) {
					this.drawStatusEffectDescriptions(matrices, x, 32, single);
				}
				if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
					hovered = inst;
				}
				x += xOff;
			}
		} finally {
			this.y = restoreY;
		}
		if (hovered != null && size > 1) {
			List<Text> list = List.of(this.getStatusEffectDescription(hovered),
				EmiPort.literal(StatusEffectUtil.durationToString(hovered, 1.0f)));
			this.renderTooltip(matrices, list, Optional.empty(), mouseX, Math.max(mouseY, 16));
		}
	}
	
	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 0)
	private boolean squishEffects(boolean original) {
		return !EmiConfig.effectLocation.compressed;
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 2)
	private int changeEffectSpace(int original) {
		return switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED -> original;
			case TOP -> this.x;
			case LEFT_COMPRESSED -> this.x - 2- 32;
			case LEFT -> this.x - 2 - 120;
		};
	}
}
