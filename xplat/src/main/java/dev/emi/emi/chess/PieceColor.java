package dev.emi.emi.chess;

enum PieceColor {
	WHITE,
	BLACK,
	;

	public PieceColor opposite() {
		return this == WHITE ? BLACK : WHITE;
	}
}
