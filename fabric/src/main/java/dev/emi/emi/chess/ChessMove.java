package dev.emi.emi.chess;

record ChessMove(int start, int end, int type) {	
	private static final ChessMove[] COMPUTED = new ChessMove[64 * 64];

	public static ChessMove of(int start, int end, int type) {
		if (type == 0) {
			int i = start + (end << 6);
			ChessMove m = COMPUTED[i];
			if (m != null) {
				return m;
			} else {
				m = new ChessMove(start, end, type);
				COMPUTED[i] = m;
				return m;
			}
		} else {
			return new ChessMove(start, end, type);
		}
	}
}
