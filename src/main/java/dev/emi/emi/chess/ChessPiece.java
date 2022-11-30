package dev.emi.emi.chess;

import java.util.List;

record ChessPiece(PieceType type, PieceColor color) {
	private static final List<ChessPiece> PIECES = List.of(
		new ChessPiece(PieceType.PAWN, PieceColor.WHITE),
		new ChessPiece(PieceType.ROOK, PieceColor.WHITE),
		new ChessPiece(PieceType.KNIGHT, PieceColor.WHITE),
		new ChessPiece(PieceType.BISHOP, PieceColor.WHITE),
		new ChessPiece(PieceType.QUEEN, PieceColor.WHITE),
		new ChessPiece(PieceType.KING, PieceColor.WHITE),
		new ChessPiece(PieceType.PAWN, PieceColor.BLACK),
		new ChessPiece(PieceType.ROOK, PieceColor.BLACK),
		new ChessPiece(PieceType.KNIGHT, PieceColor.BLACK),
		new ChessPiece(PieceType.BISHOP, PieceColor.BLACK),
		new ChessPiece(PieceType.QUEEN, PieceColor.BLACK),
		new ChessPiece(PieceType.KING, PieceColor.BLACK)
	);

	public static ChessPiece of(PieceType type, PieceColor color) {
		return PIECES.get(type.ordinal() + color.ordinal() * 6);
	}
}
