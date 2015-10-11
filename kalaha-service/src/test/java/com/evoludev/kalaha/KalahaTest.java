package com.evoludev.kalaha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class KalahaTest {

	private KalahaGame kalaha;

	@Before
	public void initKalaha() {
		kalaha = KalahaGame.newGame("Me", "You");
	}
	
	@Test
	public void testSanity() {

		assertNotNull(kalaha.getPlayerToMove());
		assertTrue(kalaha.getPlayers().size() == KalahaGame.NUM_PLAYERS);
		assertEquals(KalahaGame.INITIAL_SEEDS_COUNT, kalaha.getPlayerToMove().getFirstHouse().getSeeds());
		assertEquals(0, kalaha.getPlayerToMove().getStore().getSeeds());
		assertEquals(kalaha.getPlayerToMove(), kalaha.getWinningPlayer());
		assertFalse(kalaha.isGameFinished());
	}

	@Test
	public void testPitsInitialized() {
		
		Pit pitToCheck = kalaha.getPlayerToMove().getFirstHouse();
		do {
			assertNotNull(pitToCheck.getNextPit());
			if (pitToCheck.isHouse()) {
				assertNotNull(((House)pitToCheck).getOppositeHouse());
				assertEquals(KalahaGame.INITIAL_SEEDS_COUNT, pitToCheck.getSeeds());
			} else {
				assertEquals(0, pitToCheck.getSeeds());
			}
			pitToCheck = pitToCheck.getNextPit();
		} while (pitToCheck != kalaha.getPlayerToMove().getFirstHouse());
	}
	
	@Test 
	public void testBoardState() {
		String strNumeric = kalaha.getBoardState().replaceAll("[^0-9]", "");
		assertEquals("066666606666660", strNumeric);
	}
	
	@Test
	public void testFromBoardState() {
		String state = "1|0|12|0|12|6|3|3|6|6|6|6|4|4|4";
		KalahaGame kalaha2 = KalahaGame.fromBoardState(state, "Me", "You");
		String state2 = kalaha2.getBoardState();
		assertEquals(state, state2);
	}
}
