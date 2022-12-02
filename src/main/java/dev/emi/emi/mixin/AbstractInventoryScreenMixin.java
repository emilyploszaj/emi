package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {
	
	private AbstractInventoryScreenMixin() { super(null, null, null); }

	@Shadow
	abstract Text getStatusEffectDescription(StatusEffectInstance effect);

	@Inject(at = @At("HEAD"), method = "drawStatusEffects", cancellable = true)
	private void drawStatusEffects(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo info) {
		if (EmiConfig.moveEffects) {
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			Collection<StatusEffectInstance> effects = this.client.player.getStatusEffects();
			int size = effects.size();
			if (size == 0) {
				info.cancel();
			}
			int y = this.y - 34;
			if (((Object) this) instanceof CreativeInventoryScreen) {
				y -= 28;
			}
			int xOff = 34;
			if (size == 1) {
				xOff = 122;
			} else if (size > 5) {
				xOff = (this.backgroundWidth - 32) / (size - 1);
			}
			int width = (size - 1) * xOff + (size == 1 ? 120 : 32);
			int x = this.x + (this.backgroundWidth - width) / 2;
			StatusEffectInstance hovered = null;
			for (StatusEffectInstance inst : effects) {
				RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
				StatusEffect effect = inst.getEffectType();
				Sprite sprite = this.client.getStatusEffectSpriteManager().getSprite(effect);
				int ew = size == 1 ? 120 : 32;
				this.drawTexture(matrices, x, y, 0, size == 1 ? 166 : 198, ew, 32);
				RenderSystem.setShaderTexture(0, sprite.getAtlasId());
				AbstractInventoryScreen.drawSprite(matrices, x + (size == 1 ? 6 : 7), y + 7, this.getZOffset(), 18, 18, sprite);
				if (size == 1) {
					AbstractInventoryScreen.drawSprite(matrices, x + 6, y + 7, this.getZOffset(), 18, 18, sprite);
					Text text = this.getStatusEffectDescription(inst);
					this.textRenderer.drawWithShadow(matrices, text, x + 28, y + 6, 0xFFFFFF);
					String string = StatusEffectUtil.durationToString(inst, 1.0f);
					this.textRenderer.drawWithShadow(matrices, string, x + 28, y + 16, 0x7F7F7F);
				}
				if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
					hovered = inst;
				}
				x += xOff;
			}
			if (hovered != null && size > 1) {
				List<Text> list = List.of(this.getStatusEffectDescription(hovered),
					EmiPort.literal(StatusEffectUtil.durationToString(hovered, 1.0f)));
				this.renderTooltip(matrices, list, Optional.empty(), mouseX, Math.max(mouseY, 16));
			}
			info.cancel();
			if (this instanceof RecipeBookProvider rbp) {
				RecipeBookWidget widget = rbp.getRecipeBookWidget();
				if (widget != null && widget.isOpen()) {
					info.cancel();
				}
			}
		}
	}
	
	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 0)
	private boolean squishEffects(boolean original) {
		return false;
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawStatusEffects", ordinal = 3)
	private int changeEffectSpace(int original) {
		if (!EmiConfig.moveEffects) {
			return original;
		}
		return this.x - 2;
	}
}
