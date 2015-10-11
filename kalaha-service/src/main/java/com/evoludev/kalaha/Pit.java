package com.evoludev.kalaha;

/**
 * @author antonio
 * Represents any Pit - Store or House
 */
public abstract class Pit {

	protected Pit nextPit;
	protected int seeds;
	protected int ordinal;	

	public Pit(int ordinal, int seeds) {
		this.ordinal = ordinal;
		this.seeds = seeds;
	}
	
	public boolean isHouse() {
		return !isStore();
	}
	
	public Pit getNextPit() {
		return nextPit;
	}

	public void setNextPit(Pit nextPit) {
		this.nextPit = nextPit;
	}

	public void addSeeds(int seeds) {
		this.seeds += seeds;
	}

	public int getSeeds() {
		return seeds;
	}

	public boolean isEmpty() {
		return this.seeds == 0;
	}

	public int getOrdinal() {
		return ordinal;
	}	
	
	public abstract String getStringState();
	public abstract boolean isStore();
	
}