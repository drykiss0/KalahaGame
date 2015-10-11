package com.evoludev.kalaha;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player {

	private final House firstHouse;
	private final Store store;
	private final List<House> houses;
	private final Set<House> housesSet;
	private final int index;
	private String name;
	private Player nextPlayer;
	
	public Player(List<House> houses, Store store, int index) {
		this.houses = houses;
		this.index = index;
		this.housesSet = new HashSet<>(houses);
		this.firstHouse = houses.get(0);
		this.store = store;
	}
	
	public Pit makeMove(int houseNum) {
		int seeds = houses.get(houseNum).retrieveSeeds();
		return sowSeeds(houses.get(houseNum).getNextPit(), seeds);
	}
	
	/**
	 * Sows seeds pits (seeds get propagated to next pit until no seeds left) 
	 * @param pit Pit where seed is added (or skipped if not a player'a store)
	 * @param seedsToSow propagated seeds
	 * @return Pit that received last seed
	 */
	private Pit sowSeeds(Pit pit, int seedsToSow) {
		if (!pit.isStore() || (pit.equals(getStore()))) {
			pit.addSeeds(seedsToSow--);		
		}
		if (seedsToSow == 0) {
			return pit;
		}
		return sowSeeds(pit.getNextPit(), seedsToSow);
	}

	public boolean isAllHousesEmpty() {
		return this.houses.stream().allMatch(h -> h.getSeeds() == 0);
	}

	public void moveAllOwnedSeedsToStore() {
		for (House house: houses) {
			getStore().addSeeds(house.retrieveSeeds());
		}
	}

	public boolean isOwnHouse(Pit lastSowedHouse) {
		return housesSet.contains(lastSowedHouse);
	}

	/* Getter,Setters */
	
	public House getFirstHouse() {
		return firstHouse;
	}

	public Store getStore() {
		return store;
	}
	
	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Player getNextPlayer() {
		return nextPlayer;
	}

	public void setNextPlayer(Player nextPlayer) {
		this.nextPlayer = nextPlayer;
	}
}