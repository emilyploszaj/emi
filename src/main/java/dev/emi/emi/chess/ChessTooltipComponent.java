package dev.emi.emi.chess;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

class ChessTooltipComponent implements TooltipComponent {
	private final ChessPiece dragged, hovered;
	private final Text description;
	
	public ChessTooltipComponent(ChessPiece dragged, ChessPiece hovered, Text description) {
		this.dragged = dragged;
		this.hovered = hovered;
		this.description = description;
	}

	@Override
	public int getHeight() {
		return 30;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return Math.max(textRenderer.getWidth(description), 48);
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		matrices.push();
		matrices.translate(0, 0, 100);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.PIECES);
		DrawableHelper.drawTexture(matrices, x, y + 14, 100, dragged.type().u, dragged.color() == PieceColor.BLACK ? 0 : 16, 16, 16, 256, 256);
		DrawableHelper.drawTexture(matrices, x + 32, y + 14, 100, hovered.type().u, hovered.color() == PieceColor.BLACK ? 0 : 16, 16, 16, 256, 256);
		matrices.pop();
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		textRenderer.draw(description, x, y + 4, 0xffffff, true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		textRenderer.draw("->", x + 18, y + 19, 0xffffff, true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}
}
