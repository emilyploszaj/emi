package dev.emi.emi.chess;

import java.util.Random;

abstract class MoveGenerator {
	public static final Random RANDOM = new Random();
	public PieceColor color;
	
	public MoveGenerator(PieceColor color) {
		this.color = color;
	}

	public abstract void ponderMove(ChessBoard board);

	public abstract boolean determinedMove();

	public abstract ChessMove getMove();
}
