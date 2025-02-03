package dev.emi.emi.chess;

enum PieceType {
	PAWN('P', 0),
	ROOK('R', 16),
	KNIGHT('N', 32),
	BISHOP('B', 48),
	QUEEN('Q', 64),
	KING('K', 80),
	;

	public final char abbreviation;
	public final int u;

	private PieceType(char abbreviation, int u) {
		this.abbreviation = abbreviation;
		this.u = u;
	}
}
