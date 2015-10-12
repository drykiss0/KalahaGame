package com.evoludev.kalaha.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
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
	
	public Player getPlayerToMove() {
		return playerToMove;
	}

	public List<Player> getPlayers() {
		return players;
	}

	private KalahaGame() {
	}
	
	/**
	 * Initialized the game. Adds users, their houses and stores,
	 * creates a circular linked pit list (initialized via setNextPit method),
	 * computes opposite pit and sets it up.
	 * @param player1name
	 * @param player2name
	 * @return <i>this</i>
	 */
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
		
		// Compute and link next and opposite pits
		for (int idx = 0; idx < allPits.size(); idx ++) {
			
			Pit pit = allPits.get(idx);
			pit.setNextPit(allPits.get((idx + 1) % allPits.size()));
			if (pit.isHouse()) {
				House house = (House) pit;
				house.setOppositeHouse((House) allPits.get(HOUSES_PER_PLAYER * NUM_PLAYERS - idx));
			}
		}
		// Link next players
		for (int pIdx = 0; pIdx < players.size(); pIdx++) {
			players.get(pIdx).setNextPlayer(players.get((pIdx + 1) % players.size()));
		}
		this.playerToMove = players.get(0);
		return this;
	}
	
	/**
	 * @return Player that wins (have won) current game or Optional.empty() if there is a draw
	 */
	public Optional<Player> getWinningPlayer() {
		boolean isDraw = players.size() != players.stream().map(p->p.getStore().getSeeds()).collect(Collectors.toSet()).size();
		return isDraw ? Optional.empty() : players.stream().max((p1, p2) -> Integer.compare(
				p1.getStore().getSeeds(), p2.getStore().getSeeds()));
	}

	/**
	 * Makes a move.
	 * @param houseNum number of current player's House from where sowing starts, starting from 0
	 * @return <i>this</i>
	 * @throws {@link IllegalStateException} - when makeMove is called, but the game has ended
	 * @thriws {@link IllegalArgumentException} - in case houseNumber is invalid (out-of-range, or empty)
	 */
	public KalahaGame makeMove(int houseNum) {
	
		/* House and game state checks */
		Preconditions.checkState(!isGameFinished(), "Game has finished. Please restart to play again");
		Preconditions.checkArgument(!playerToMove.isAllHousesEmpty(), "Invalid player turn or game finished. Player " + playerToMove + " has all empty houses!");
		/* Checks finished */
		
		Pit lastSowedPit = this.playerToMove.sowSeedsFromHouse(houseNum);

		if (lastSowedPit.getSeeds() == 1 && lastSowedPit.isHouse()) {
			House lastSowedHouse = (House) lastSowedPit;
			if (playerToMove.isOwnHouse(lastSowedHouse) && !lastSowedHouse.getOppositeHouse().isEmpty()) {
				int oppositeSeeds = lastSowedHouse.getOppositeHouse().retrieveSeeds();
				playerToMove.getStore().addSeeds(oppositeSeeds + lastSowedHouse.retrieveSeeds());				
			}
		}

		if (!lastSowedPit.equals(playerToMove.getStore())) {
			this.playerToMove = this.playerToMove.getNextPlayer();
		}

		if (isGameFinished()) {
			getPlayersWithEmptyHouses().get(0).getNextPlayer().moveAllOwnedSeedsToStore();
		}		
		
		return this;
	}
	
	private List<Player> getPlayersWithEmptyHouses() {
		return players.stream().filter(p -> p.isAllHousesEmpty()).collect(Collectors.toList());
	}

	public boolean isGameFinished() {
		return players.stream().anyMatch(p -> p.isAllHousesEmpty());		
	}
	
	/**
	 * @return Game state as String. Can be used to initialize arbitrary setup of seeds on board
	 */
	public String getBoardState() {
		Player firstPlayer = players.get(0);
		StringBuilder str = new StringBuilder(this.playerToMove.getIndex() + "|" 
				+ firstPlayer.getFirstHouse().getSeeds() + "|");
		for (Pit pit = firstPlayer.getFirstHouse().getNextPit(); pit != firstPlayer.getFirstHouse(); pit = pit.getNextPit()) {
			str.append(pit.getSeeds() + "|");
		}
		return str.deleteCharAt(str.length() - 1).toString();
	}

	/**
	 * Start a fresh game of Kalaha :)
	 * @param player1 Player1 name
	 * @param player2 Player2 name
	 */
	public static KalahaGame newGame(String player1, String player2) {
		return new KalahaGame().init(player1, player2);
	}
	
	/**
	 * Initializes Kalaha with given state. Allows to play the game with arbitrary setup of seeds.
	 * @param gameState state, as returned by {@link getBoardState()} method
	 * @param player1 Player1 name
	 * @param player2 Player2 name
	 */
	public static KalahaGame fromBoardState(String gameState, String player1, String player2) {

		// TODO: validate gameState - input length, sum of all seeds in 14 pits should be exactly 72 (for 6-seed 2 players game), etc
		List<Integer> boardState = Splitter.onPattern("\\|").splitToList(gameState.replaceAll("\\s", "")).stream()
				.map(s -> Integer.valueOf(s)).collect(Collectors.toList());
		KalahaGame game = newGame(player1, player2);
		game.playerToMove = game.players.get(boardState.get(0));
		Pit pit = game.players.get(0).getFirstHouse();
		for (int num = 1; num < boardState.size(); num++) {
			pit.setSeeds(boardState.get(num));
			pit = pit.getNextPit();
		}
		return game;
	}
}
