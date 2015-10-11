package com.evoludev.kalaha;

/**
 * @author antonio
 * Represents Store in Kalaha
 */
public class Store extends Pit {

	public Store(int ordinal, int seeds) {
		super(ordinal, seeds);
	}

	public String getStringState() {
		return "|" + getSeeds() + "|";
	}
	
	@Override
	public String toString() {
		return "Store [" + getSeeds() + "]";
	}

	@Override
	public boolean isStore() {
		return true;
	}
}
