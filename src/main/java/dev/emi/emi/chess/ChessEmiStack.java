package dev.emi.emi.chess;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

class ChessEmiStack extends EmiStack {
	public final int position;

	public ChessEmiStack(int position) {
		this.position = position;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
		ChessPiece piece = EmiChess.getBoard().get(position);
		RenderSystem.enableDepthTest();
		EmiChess chess = EmiChess.get();
		if (chess.pendingPromotion != -1) {
			PieceType type = null;
			int dir = chess.pendingPromotion > 31 ? -8 : 8;
			if (position == chess.pendingPromotion) {
				type = PieceType.QUEEN;
			} else if (position == chess.pendingPromotion + dir) {
				type = PieceType.KNIGHT;
			} else if (position == chess.pendingPromotion + dir * 2) {
				type = PieceType.ROOK;
			} else if (position == chess.pendingPromotion + dir * 3) {
				type = PieceType.BISHOP;
			}
			if (type != null) {
				matrices.push();
				matrices.translate(0, 0, 10);
				DrawableHelper.fill(matrices, x - 1, y - 1, x + 17, y + 17, 0x55000000);
				matrices.translate(0, 0, 90);
				RenderSystem.setShaderTexture(0, EmiRenderHelper.PIECES);
				DrawableHelper.drawTexture(matrices, x, y, 100, type.u, chess.pendingPromotion > 31 ? 0 : 16, 16, 16, 256, 256);
				matrices.pop();
				return;
			}
		}
		matrices.push();
		matrices.translate(0, 0, 10);
		if (chess.isTarget(position)) {
			DrawableHelper.fill(matrices, x - 1, y - 1, x + 17, y + 17, 0x5555ff00);
		}
		boolean dragging = !EmiScreenManager.draggedStack.isEmpty();
		ChessMove move = chess.board.lastMove;
		if (!dragging &&move != null && (move.start() == position || move.end() == position)) {
			DrawableHelper.fill(matrices, x - 1, y - 1, x + 17, y + 17, 0x55aaaa00);
		}
		if (!dragging && piece != null && piece.type() == PieceType.KING && chess.board.isChecked(piece.color())) {
			DrawableHelper.fill(matrices, x - 1, y - 1, x + 17, y + 17, 0x55ff0000);
		}
		matrices.pop();
		if (piece != null) {
			matrices.push();
			matrices.translate(0, 0, 100);
			RenderSystem.setShaderTexture(0, EmiRenderHelper.PIECES);
			DrawableHelper.drawTexture(matrices, x, y, 100, piece.type().u, piece.color() == PieceColor.BLACK ? 0 : 16, 16, 16, 256, 256);
			matrices.pop();
		}
	}

	@Override
	public EmiStack copy() {
		return this;
	}

	@Override
	public boolean isEmpty() {
		return EmiChess.get().board.get(position) == null;
	}

	@Override
	public NbtCompound getNbt() {
		return null;
	}

	@Override
	public Object getKey() {
		return position;
	}

	@Override
	public Identifier getId() {
		return new Identifier("emi:/chess/" + position);
	}

	@Override
	public List<Text> getTooltipText() {
		return List.of();
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		ChessPiece piece = EmiChess.getBoard().get(position);
		if (piece != null) {
			List<TooltipComponent> list = Lists.newArrayList();
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.chess.piece."
				+ piece.color().toString().toLowerCase() + "_" + piece.type().toString().toLowerCase()))));
			MinecraftClient client = MinecraftClient.getInstance();
			if (!EmiChess.get().started) {
				if (piece.type() == PieceType.KING) {
					list.add(new ChessTooltipComponent(
						ChessPiece.of(PieceType.PAWN, PieceColor.BLACK),
						ChessPiece.of(PieceType.KING, PieceColor.BLACK),
						EmiPort.translatable("emi.chess.tooltip.invite")));
					if (EmiChess.get().pending != null) {
						PlayerEntity player = client.world.getPlayerByUuid(EmiChess.get().pending);
						if (player != null) {
							list.add(new ChessTooltipComponent(
								ChessPiece.of(PieceType.KING, PieceColor.WHITE),
								ChessPiece.of(PieceType.KING, PieceColor.BLACK),
								EmiPort.translatable("emi.chess.tooltip.accept", player.getName())));
							list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.chess.tooltip.decline", player.getName()))));
						}
					}
				}
			} else {
				if (piece.type() == PieceType.KING && piece.color() == PieceColor.WHITE) {
					list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.chess.tooltip.restart"))));
				}
			}
			return list;
		}
		return List.of();
	}

	@Override
	public Text getName() {
		return EmiPort.literal("Chess Piece");
	}
}
