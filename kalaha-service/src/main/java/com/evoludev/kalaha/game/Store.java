package com.evoludev.kalaha.game;

/**
 * @author antonio
 * Represents Store in Kalaha
 */
public class Store extends Pit {

	public Store(int ordinal, int seeds) {
		super(ordinal, seeds);
	}

	@Override
	public String toString() {
		return "S[" + getSeeds() + "]";
	}

	@Override
	public boolean isStore() {
		return true;
	}
}
