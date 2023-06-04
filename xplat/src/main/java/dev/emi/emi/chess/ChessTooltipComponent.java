package dev.emi.emi.chess;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.tooltip.EmiTooltipComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

class ChessTooltipComponent implements EmiTooltipComponent {
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
	public void drawTooltip(EmiDrawContext context, TooltipRenderData tooltip) {
		context.drawTexture(EmiRenderHelper.PIECES, 0, 14, 100, dragged.type().u, dragged.color() == PieceColor.BLACK ? 0 : 16, 16, 16);
		context.drawTexture(EmiRenderHelper.PIECES, 32, 14, 100, hovered.type().u, hovered.color() == PieceColor.BLACK ? 0 : 16, 16, 16);
	}

	@Override
	public void drawTooltipText(TextRenderData text) {
		text.draw(description, 0, 4, 0xffffff, true);
		text.draw("->", 18, 19, 0xffffff, true);
	}
}
