package dev.emi.emi.chess;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

class StandardMoveGenerator extends MoveGenerator {
	public ChessEvaluation eval = new ChessEvaluation();
	public ChessMove chosenMove;
	public Thread worker;

	public StandardMoveGenerator(PieceColor color) {
		super(color);
	}

	@Override
	public void ponderMove(ChessBoard board) {
		chosenMove = null;
		worker = new Thread(new Worker(board));
		worker.start();
	}

	@Override
	public boolean determinedMove() {
		return worker != null && !worker.isAlive();
	}

	@Override
	public ChessMove getMove() {
		return chosenMove;
	}

	public int evaluate(ChessBoard board, PieceColor turn, int depth, int alpha, int beta) {
		List<ChessMove> moves = board.getAllMoves(turn);
		for (int i = 0; i < moves.size(); i++) {
			ChessMove move = moves.get(i);
			int rCastles = board.castles;
			ChessMove rLast = board.lastMove;
			ChessPiece captured = board.get(move.end());
			board.move(move);
			int value;
			if (depth <= 0) {
				value = eval.evaluate(board);
				if (turn == PieceColor.BLACK) {
					value = -value;
				}
			} else {
				value = -evaluate(board, turn.opposite(), depth - 1, -beta, -alpha);
			}
			board.unmove(move);
			board.castles = rCastles;
			board.lastMove = rLast;
			board.set(move.end(), captured);
			if (value >= beta) {
				return value;
			} else if (value > alpha) {
				alpha = value;
			}
		}
		return alpha;
	}
	
	class Worker implements Runnable {
		private ChessBoard board;

		public Worker(ChessBoard board) {
			this.board = board;
		}

		public void run() {
			int bestEval = Integer.MIN_VALUE + 1;
			ChessMove bestMove = null;
			List<ChessMove> legal = Lists.newArrayList(board.getLegal(board.getAllMoves(color)).iterator());
			Collections.shuffle(legal);
			for (ChessMove move : legal) {
				ChessBoard copy = board.copy();
				copy.move(move);
				int eval = -evaluate(copy, color.opposite(), 4, Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
				if (eval > bestEval) {
					bestEval = eval;
					bestMove = move;
				}
			}
			chosenMove = bestMove;
		}
	}
}
