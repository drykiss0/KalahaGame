package com.evoludev.kalaha;

public class House extends Pit {
	
	private House oppositeHouse;

	public House(int index, int seeds) {
		super(index, seeds);
	}

	/**
	 * @return number of seeds and removes all seeds from this house
	 */
	public int retrieveSeeds() {
		int retrievedSeeds = getSeeds();
		setSeeds(0);
		return retrievedSeeds;
	}
	
	public House getOppositeHouse() {
		return oppositeHouse;
	}

	public void setOppositeHouse(House oppositePit) {
		this.oppositeHouse = oppositePit;
	}

	@Override
	public boolean isStore() {
		return false;
	}

	@Override
	public String toString() {
		return "H[" + getSeeds() + "]";
	}
}
