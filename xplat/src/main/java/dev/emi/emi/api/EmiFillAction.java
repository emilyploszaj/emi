package dev.emi.emi.api;

public enum EmiFillAction {
	/**
	 * Move the ingredients to where they belong.
	 */
	FILL(0),
	/**
	 * Move the ingredients to where they belong.
	 * Then, attempt to immediately grab the result and place in cursor.
	 * If this action is not supported, this should do nothing more than FILL.
	 */
	CURSOR(1),
	/**
	 * Move the ingredients to where they belong.
	 * Then, attempt to immediately quick move the result, likely into the player's inventory.
	 * If this action is not supported, this should do nothing more than FILL.
	 */
	QUICK_MOVE(2),
	;

	public final int id;

	private EmiFillAction(int id) {
		this.id = id;
	}
}
