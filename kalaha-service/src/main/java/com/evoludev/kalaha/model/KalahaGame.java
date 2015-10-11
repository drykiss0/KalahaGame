package com.evoludev.kalaha.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class KalahaGame {
	
	public static final class TurnOutcome {
		
		public static final TurnOutcome OK = new TurnOutcome(true, null);
		private final boolean success;
		private final String message;
		
		public TurnOutcome(boolean success, String message) {
			this.success = success;
			this.message = message;
		}
		
		public boolean isSuccess() {
			return success;
		}
		public String getMessage() {
			return message;
		}
	}
	
	public static final int INITIAL_SEEDS_COUNT = 6;
	public static final int NUM_PLAYERS = 2;
	public static final int HOUSES_PER_PLAYER = 6;
	
	private List<Player> players = new ArrayList<>();
	private Player playerToMove;
	private boolean gameFinished;
	
	public static int getNumPlayers() {
		return NUM_PLAYERS;
	}

	public static int getNumHouses() {
		return HOUSES_PER_PLAYER;
	}

	public Player getPlayerToMove() {
		return playerToMove;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	private KalahaGame() {
	}
	
	private KalahaGame init(String player1name, String player2name) {
		List<Pit> allPits = new ArrayList<Pit>();
		List<String> playerNames = Lists.newArrayList(player1name, player2name);
		for (int playerIdx = 0; playerIdx < NUM_PLAYERS; playerIdx++) {
			
			List<House> playerHouses = new ArrayList<>();
			final int playerPitStartIdx = playerIdx * (HOUSES_PER_PLAYER + 1);
			final int playerPitEndIdx = playerPitStartIdx + HOUSES_PER_PLAYER;
			for (int pitIdx = playerPitStartIdx; pitIdx < playerPitEndIdx; pitIdx++) {
				playerHouses.add(new House(pitIdx, INITIAL_SEEDS_COUNT));
			}

			Player player = new Player(playerHouses, new Store(playerPitEndIdx, 0), playerIdx);
			player.setName(playerNames.get(playerIdx));
			players.add(player);

			allPits.addAll(playerHouses);
			allPits.add(playerPitEndIdx, player.getStore());
		}
		
		// Initialize next and opposite pits
		for (int idx = 0; idx < allPits.size(); idx ++) {
			
			Pit pit = allPits.get(idx);
			pit.setNextPit(allPits.get((idx + 1) % allPits.size()));
			if (pit.isHouse()) {
				House house = (House) pit;
				house.setOppositeHouse((House) allPits.get(HOUSES_PER_PLAYER * NUM_PLAYERS - idx));
			}
		}
		// Initialize next players
		for (int pIdx = 0; pIdx < players.size(); pIdx++) {
			players.get(pIdx).setNextPlayer(players.get((pIdx + 1) % players.size()));
		}
		this.playerToMove = players.get(0);
		return this;
	}
	
	public static KalahaGame newGame(String player1, String player2) {
		return new KalahaGame().init(player1, player2);
	}
	
	public TurnOutcome makeMove(int houseNum) {

		//Preconditions.checkState(!this.isGameFinished(), "Game has finished. Please restart to play again");
		// TODO: Validate houseNum
		Pit lastSowedPit = this.playerToMove.makeMove(houseNum);
		
		// TODO: Game rules could use refactoring, returns in the middle are ugly
		if (this.playerToMove.isAllHousesEmpty()) {
			this.playerToMove.getNextPlayer().moveAllOwnedSeedsToStore();
			this.gameFinished = true;
			return TurnOutcome.OK;
		}		
		if (lastSowedPit.equals(playerToMove.getStore())) {
			return TurnOutcome.OK;
		}
		if (lastSowedPit.isHouse() && playerToMove.isOwnHouse(lastSowedPit)) {
			House lastSowedHouse = (House) lastSowedPit;
			if (!lastSowedHouse.getOppositeHouse().isEmpty()) {
				int oppositeSeeds = lastSowedHouse.getOppositeHouse().retrieveSeeds();
				playerToMove.getStore().addSeeds(oppositeSeeds + lastSowedHouse.retrieveSeeds());				
			}
		}
		this.playerToMove = this.playerToMove.getNextPlayer();
		return TurnOutcome.OK;
	}
	
	public Player getWinningPlayer() {
		return players.stream().max((p1, p2) -> Integer.compare(
				p1.getStore().getSeeds(), p2.getStore().getSeeds())).get();
	}

	public boolean isGameFinished() {
		return gameFinished;
	}
	
	public String getBoardStateAsString() {
		Player firstPlayer = players.get(0);
		StringBuilder str = new StringBuilder("[" + this.playerToMove.getIndex() + "]" 
				+ firstPlayer.getFirstHouse().getStringState());
		for (Pit pit = firstPlayer.getFirstHouse().getNextPit(); pit != firstPlayer.getFirstHouse(); pit = pit.getNextPit()) {
			str.append(pit.getStringState());
		}
		return str.toString();
	}
}
