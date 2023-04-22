package dev.emi.emi.chess;

import java.util.List;
import java.util.UUID;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.screen.EmiScreenManager;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

// Yes, this is a thing that exists
public class EmiChess {
	public static final List<EmiStack> SIDEBAR;
	private static final EmiChess chess = new EmiChess();
	public UUID opponent = null;
	public UUID pending = null;
	public boolean started = false;
	public ChessBoard board = ChessBoard.setupBoard();
	public MoveGenerator generator = new StandardMoveGenerator(PieceColor.BLACK);
	public PieceColor turn = PieceColor.WHITE;
	public int lastChecked = -1;
	public IntSet targets = new IntOpenHashSet();
	public int promotionStart = -1;
	public int pendingPromotion = -1;

	public boolean isTarget(int position) {
		if (EmiScreenManager.draggedStack instanceof ChessEmiStack c) {
			if (c.position != lastChecked) {
				lastChecked = c.position;
				targets.clear();
				for (ChessMove move : board.getLegal(board.getMoves(lastChecked))) {
					targets.add(move.end());
				}
			}
			return targets.contains(position);
		}
		return false;
	}

	public boolean isPlayerTurn() {
		return turn != generator.color;
	}

	public void update() {
		if (!isPlayerTurn()) {
			if (generator.determinedMove()) {
				ChessMove move = generator.getMove();
				if (board.getLegal(board.getAllMoves(turn)).contains(move)) {
					doMove(generator.getMove());
				}
			}
		}
	}

	public void doMove(ChessMove move) {
		getBoard().move(move);
		PieceColor op = turn.opposite();
		if (isPlayerTurn()) {
			generator.ponderMove(board.copy());
			if (generator instanceof NetworkedMoveGenerator nmg) {
				sendNetwork(opponent, move.type(), move.start(), move.end());
			}
		}
		turn = op;
	}

	public static EmiChess get() {
		return chess;
	}

	public static ChessBoard getBoard() {
		return chess.board;
	}

	public static void restart() {
		EmiChess chess = get();
		chess.pending = null;
		chess.turn = PieceColor.WHITE;
		chess.board = ChessBoard.setupBoard();
		chess.started = false;
		chess.generator = new StandardMoveGenerator(PieceColor.BLACK);
	}

	public static void interact(EmiIngredient hovered, int button) {
		if (hovered instanceof ChessEmiStack c) {
			int position = c.position;
			EmiChess chess = get();
			ChessPiece piece = chess.board.get(position);
			if (!chess.started && piece != null) {
				if (piece.type() == PieceType.KING && piece.color() == PieceColor.BLACK) {

				}
			}
			if (piece != null && piece.type() == PieceType.KING && button == 1) {
				if (piece.color() == PieceColor.WHITE || chess.pending != null) {
					if (chess.generator instanceof NetworkedMoveGenerator nmg) {
						sendNetwork(chess.opponent, -3, 0, 0);
					}
					restart();
				}
			} else {
				int pending = chess.pendingPromotion;
				int dir = position > 31 ? -8 : 8;
				if (pending != -1) {
					if (position == pending) {
						chess.doMove(new ChessMove(chess.promotionStart, chess.pendingPromotion, 3));
					} else if (position == pending + dir) {
						chess.doMove(new ChessMove(chess.promotionStart, chess.pendingPromotion, 4));
					} else if (position == pending + dir * 2) {
						chess.doMove(new ChessMove(chess.promotionStart, chess.pendingPromotion, 5));
					} else if (position == pending + dir * 3) {
						chess.doMove(new ChessMove(chess.promotionStart, chess.pendingPromotion, 6));
					}
					chess.pendingPromotion = -1;
				}
			}
		}
	}

	public static void drop(EmiIngredient dragged, EmiIngredient hovered) {
		if (dragged instanceof ChessEmiStack cDragged && hovered instanceof ChessEmiStack cHovered) {
			EmiChess chess = get();
			ChessPiece piece = get().board.get(cDragged.position);
			ChessPiece end = get().board.get(cHovered.position);
			if (!chess.started && end != null) {
				if (end.type() == PieceType.KING && end.color() == PieceColor.BLACK) {
					if (piece.color() == PieceColor.BLACK) {
						if (piece.type() == PieceType.PAWN) {
							invitePlayer();
						}
					} else {
						if (piece.type() == PieceType.KING) {
							if (chess.pending != null) {
								chess.opponent = chess.pending;
								chess.pending = null;
								restart();
								sendNetwork(chess.opponent, -2, 0, 0);
								chess.generator = new NetworkedMoveGenerator(PieceColor.WHITE);
							}
						}
					}
				}
			}
			if (piece != null && piece.color() == chess.turn && chess.isPlayerTurn()) {
				move(cDragged.position, cHovered.position);
			}
		}
	}

	public static void move(int start, int end) {
		ChessBoard board = getBoard();
		for (ChessMove move : board.getLegal(board.getMoves(start))) {
			if (move.end() == end) {
				if (move.type() > 2) {
					get().promotionStart = move.start();
					get().pendingPromotion = move.end();
				} else {
					chess.doMove(move);
				}
				get().started = true;
				break;
			}
		}
	}

	private static void invitePlayer() {
		MinecraftClient client = MinecraftClient.getInstance();
		String name = EmiScreenManager.search.getText();
		for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
			if (player.getName().getString().equals(name)) {
				get().opponent = player.getUuid();
				sendNetwork(player.getUuid(), -1, 0, 0);
			}
		}
	}

	private static void sendNetwork(UUID uuid, int type, int start, int end) {
		EmiNetwork.sendToServer(new EmiChessPacket.C2S(uuid, (byte) type, (byte) start, (byte) end));
	}

	public static void receiveNetwork(UUID uuid, int type, int start, int end) {
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity player = client.world.getPlayerByUuid(uuid);
		if (player == null) {
			return;
		}
		EmiChess chess = get();
		if (type == -1) {
			if (EmiScreenManager.hasSidebarAvailable(SidebarType.CHESS)) {
				chess.pending = uuid;
				client.player.sendMessage(EmiPort.translatable("emi.chess.multiplayer.invited", player.getDisplayName()), false);
			} else {
				sendNetwork(uuid, -4, 0, 0);
			}
		} else if (type == -2) {
			if (chess.started) {
				sendNetwork(uuid, -3, 0, 0);
			} else {
				if (uuid.equals(chess.opponent)) {
					client.player.sendMessage(EmiPort.translatable("emi.chess.multiplayer.accepted", player.getDisplayName()), false);
					chess.generator = new NetworkedMoveGenerator(PieceColor.BLACK);
				}
			}
		} else if (type == -3) {
			if (uuid.equals(chess.opponent)) {
				client.player.sendMessage(EmiPort.translatable("emi.chess.multiplayer.cancelled", player.getDisplayName()), false);
				restart();
			}
		} else if (type == -4) {
			if (uuid.equals(chess.opponent)) {
				client.player.sendMessage(EmiPort.translatable("emi.chess.multiplayer.unavailable", player.getDisplayName()), false);
			}
		} else if (chess.generator instanceof NetworkedMoveGenerator nmg && chess.opponent.equals(uuid)) {
			ChessMove desired = ChessMove.of(start, end, type);
			if (chess.turn == chess.generator.color) {
				ChessBoard board = getBoard();
				for (ChessMove move : board.getLegal(board.getMoves(start))) {
					if (move.equals(desired)) {
						nmg.move = desired;
						return;
					}
				}
			}
		}
	}

	static {
		List<EmiStack> list = Lists.newArrayList();
		for (int i = 0; i < 64; i++) {
			list.add(new ChessEmiStack(i));
		}
		SIDEBAR = ImmutableList.copyOf(list);
	}
}
