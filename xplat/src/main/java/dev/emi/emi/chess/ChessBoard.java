package dev.emi.emi.chess;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.math.MathHelper;

class ChessBoard {
	public ChessPiece[] pieces = new ChessPiece[64];
	public ChessMove lastMove;
	public int castles;
	
	private ChessBoard() {
	}

	public ChessBoard copy() {
		ChessBoard c = new ChessBoard();
		c.pieces = pieces.clone();
		c.lastMove = lastMove;
		c.castles = castles;
		return c;
	}

	public ChessPiece get(int x, int y) {
		return get(x + y * 8);
	}

	public ChessPiece get(int position) {
		if (position < 0 || position >= pieces.length) {
			return null;
		}
		return pieces[position];
	}

	public void set(int x, int y, ChessPiece piece) {
		set(x + y * 8, piece);
	}

	public void set(int position, ChessPiece piece) {
		if (position < 0 || position > pieces.length) {
			return;
		}
		pieces[position] = piece;
	}

	// Loses a lot of state, not a reliable unmove
	public void unmove(ChessMove move) {
		ChessPiece piece = get(move.end());
		if (move.type() == 1) {
			set(move.end() + (piece.color() == PieceColor.WHITE ? 8 : -8), ChessPiece.of(PieceType.PAWN, piece.color().opposite()));
		} else if (move.type() == 2) {
			int mDiff = move.end() - move.start();
			int y = move.start() / 8;
			int rx = MathHelper.clamp(mDiff * 8, 0, 7);
			ChessPiece rook = ChessPiece.of(PieceType.ROOK, piece.color());
			set(rx, y, rook);
			set(move.start() + (mDiff / 2), null);
		} else if (move.type() > 2) {
			piece = ChessPiece.of(PieceType.PAWN, piece.color());
		}
		set(move.start(), piece);
		set(move.end(), null);
	}

	public void move(ChessMove move) {
		ChessPiece piece = get(move.start());
		set(move.start(), null);
		set(move.end(), piece);
		if (move.type() == 1) {
			set(lastMove.end(), null);
		} else if (move.type() == 2) {
			int mDiff = move.end() - move.start();
			int y = move.start() / 8;
			int rx = MathHelper.clamp(mDiff * 8, 0, 7);
			ChessPiece rook = get(rx, y);
			set(rx, y, null);
			set(move.start() + (mDiff / 2), rook);
		} else if (move.type() == 3) {
			set(move.end(), ChessPiece.of(PieceType.QUEEN, piece.color()));
		} else if (move.type() == 4) {
			set(move.end(), ChessPiece.of(PieceType.KNIGHT, piece.color()));
		} else if (move.type() == 5) {
			set(move.end(), ChessPiece.of(PieceType.ROOK, piece.color()));
		} else if (move.type() == 6) {
			set(move.end(), ChessPiece.of(PieceType.BISHOP, piece.color()));
		}
		lastMove = move;
		if (piece.type() == PieceType.KING) {
			int colorShift = piece.color() == PieceColor.WHITE ? 0 : 2;
			castles |= 0b0011 << colorShift;
		} else if (piece.type() == PieceType.ROOK) {
			int ex = move.end() % 8;
			int ey = move.end() / 8;
			if (ex == 0 && ey == 0) {
				castles |= 0b0010;
			} else if (ex == 7 && ey == 0) {
				castles |= 0b0001;
			} else if (ex == 0 && ey == 7) {
				castles |= 0b1000;
			} else if (ex == 7 && ey == 7) {
				castles |= 0b0100;
			}
		}
	}

	public int getKing(PieceColor color) {
		for (int i = 0; i < 64; i++) {
			ChessPiece p = get(i);
			if (p != null && p.type() == PieceType.KING && p.color() == color) {
				return i;
			}
		}
		return -1;
	}

	public boolean isChecked(PieceColor color) {
		int k = getKing(color);
		for (ChessMove move : getAllMoves(color.opposite())) {
			if (move.end() == k) {
				return true;
			}
		}
		return false;
	}

	public List<ChessMove> getLegal(List<ChessMove> moves) {
		if (moves.isEmpty()) {
			return moves;
		}
		ChessPiece piece = get(moves.get(0).start());
		PieceColor color = piece.color();
		return moves.stream().filter(m -> {
			ChessBoard copy = this.copy();
			copy.move(m);
			return !copy.isChecked(color);
		}).toList();
	}

	public List<ChessMove> getAllMoves(PieceColor color) {
		List<ChessMove> list = Lists.newArrayList();
		for (int i = 0; i < 64; i++) {
			ChessPiece p = get(i);
			if (p != null && p.color() == color) {
				list.addAll(getMoves(i));
			}
		}
		return list;
	}

	// Can contain moves that will ignore check rules
	public List<ChessMove> getMoves(int position) {
		ChessPiece self = get(position);
		int x = position % 8;
		int y = position / 8;
		if (self != null) {
			List<ChessMove> list = Lists.newArrayList();
			switch (self.type()) {
				case PAWN:
					int pOff = self.color() == PieceColor.BLACK ? 1 : -1;
					if (((pOff == -1 && y == 6) || (pOff == 1 && y == 1)) && get(x, y + pOff) == null && get(x, y + pOff * 2) == null) {
						addMove(list, self, position, x, y + pOff * 2, 0);
					}
					int moveType = 0;
					if (y + pOff == 0 || y + pOff == 7) {
						moveType = 3;
					}
					if (get(x, y + pOff) == null) {
						addMove(list, self, position, x, y + pOff, moveType);
					}
					if (get(x - 1, y + pOff) != null) {
						addMove(list, self, position, x - 1, y + pOff, moveType);
					}
					if (get(x + 1, y + pOff) != null) {
						addMove(list, self, position, x + 1, y + pOff, moveType);
					}
					if (lastMove != null) {
						ChessPiece lastPiece = get(lastMove.end());
						if (lastPiece != null && lastPiece.type() == PieceType.PAWN && lastMove.end() / 8 == y && lastMove.start() / 8 == y + pOff * 2) {
							int lx = lastMove.end() % 8;
							if (lx == x - 1) {
								addMove(list, self, position, x - 1, y + pOff, 1);
							} else if (lx == x + 1) {
								addMove(list, self, position, x + 1, y + pOff, 1);
							}
						}
					}
					break;
				case ROOK:
					addRookMoves(list, self, position, x, y);
					break;
				case KNIGHT:
					addMove(list, self, position, x + 1, y + 2, 0);
					addMove(list, self, position, x + 1, y - 2, 0);
					addMove(list, self, position, x - 1, y + 2, 0);
					addMove(list, self, position, x - 1, y - 2, 0);
					addMove(list, self, position, x + 2, y + 1, 0);
					addMove(list, self, position, x + 2, y - 1, 0);
					addMove(list, self, position, x - 2, y + 1, 0);
					addMove(list, self, position, x - 2, y - 1, 0);
					break;
				case BISHOP:
					addBishopMoves(list, self, position, x, y);
					break;
				case QUEEN:
					addRookMoves(list, self, position, x, y);
					addBishopMoves(list, self, position, x, y);
					break;
				case KING:
					addMove(list, self, position, x - 1, y - 1, 0);
					addMove(list, self, position, x, y - 1, 0);
					addMove(list, self, position, x + 1, y - 1, 0);
					addMove(list, self, position, x - 1, y, 0);
					addMove(list, self, position, x + 1, y, 0);
					addMove(list, self, position, x - 1, y + 1, 0);
					addMove(list, self, position, x, y + 1, 0);
					addMove(list, self, position, x + 1, y + 1, 0);
					int colorShift = self.color() == PieceColor.WHITE ? 0 : 2;
					int castles = this.castles >> colorShift;
					// Did you know you could put labels there?
					if ((castles & 0b10) == 0) leftCastle: {
						for (int cx = 1; cx < 4; cx++) {
							if (get(cx, y) != null) {
								break leftCastle;
							}
						}
						for (int cx = 5; cx > 2; cx--) {
							ChessBoard board = this.copy();
							board.move(new ChessMove(position, y * 8 + cx, 0));
							if (board.isChecked(self.color())) {
								break leftCastle;
							}
						}
						addMove(list, self, position, x - 2, y, 2);
					}
					if ((castles & 0b01) == 0) rightCastle: {
						for (int cx = 5; cx < 7; cx++) {
							if (get(cx, y) != null) {
								break rightCastle;
							}
						}
						for (int cx = 4; cx < 7; cx++) {
							ChessBoard board = this.copy();
							board.move(new ChessMove(position, y * 8 + cx, 0));
							if (board.isChecked(self.color())) {
								break rightCastle;
							}
						}
						addMove(list, self, position, x + 2, y, 2);
					}
					break;
			}
			return list;
		}
		return List.of();
	}

	private void addRookMoves(List<ChessMove> list, ChessPiece self, int position, int x, int y) {
		for (int i = 0; i < 4; i++) {
			int xm = 0, ym = 0;
			if (i < 2) {
				xm = i * 2 - 1;
			} else {
				ym = (i - 2) * 2 - 1;
			}
			int xo = x + xm;
			int yo = y + ym;
			while (true) {
				if (xo < 0 || xo >= 8 || yo < 0 || yo >= 8) {
					break;
				}
				addMove(list, self, position, xo, yo, 0);
				if (get(xo, yo) != null) {
					break;
				}
				xo += xm;
				yo += ym;
			}
		}
	}

	private void addBishopMoves(List<ChessMove> list, ChessPiece self, int position, int x, int y) {
		for (int i = 0; i < 4; i++) {
			int xm = (i & 1) * 2 - 1, ym = (i & 2) - 1;
			int xo = x + xm;
			int yo = y + ym;
			while (true) {
				if (xo < 0 || xo >= 8 || yo < 0 || yo >= 8) {
					break;
				}
				addMove(list, self, position, xo, yo, 0);
				if (get(xo, yo) != null) {
					break;
				}
				xo += xm;
				yo += ym;
			}
		}
	}

	private void addMove(List<ChessMove> list, ChessPiece self, int position, int x, int y, int type) {
		ChessPiece target = get(x, y);
		if (x < 0 || x >= 8 || y < 0 || y >= 8 || (target != null && target.color() == self.color())) {
			return;
		}
		list.add(ChessMove.of(position, x + y * 8, type));
		if (type == 3) {
			list.add(new ChessMove(position, x + y * 8, 4));
			list.add(new ChessMove(position, x + y * 8, 5));
			list.add(new ChessMove(position, x + y * 8, 6));
		}
	}

	public static ChessBoard setupBoard() {
		ChessBoard board = new ChessBoard();
		for (int x = 0; x < 8; x++) {
			board.set(x, 1, ChessPiece.of(PieceType.PAWN, PieceColor.BLACK));
			board.set(x, 6, ChessPiece.of(PieceType.PAWN, PieceColor.WHITE));
		}
		for (int i = 0; i < 2; i++) {
			int y = i * 7;
			PieceColor color = i == 0 ? PieceColor.BLACK : PieceColor.WHITE;
			board.set(0, y, ChessPiece.of(PieceType.ROOK, color));
			board.set(1, y, ChessPiece.of(PieceType.KNIGHT, color));
			board.set(2, y, ChessPiece.of(PieceType.BISHOP, color));
			board.set(3, y, ChessPiece.of(PieceType.QUEEN, color));
			board.set(4, y, ChessPiece.of(PieceType.KING, color));
			board.set(5, y, ChessPiece.of(PieceType.BISHOP, color));
			board.set(6, y, ChessPiece.of(PieceType.KNIGHT, color));
			board.set(7, y, ChessPiece.of(PieceType.ROOK, color));
		}
		return board;
	}
}
