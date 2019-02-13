package de.tuberlin.sese.swtpp.gameserver.test.deathstacks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.sese.swtpp.gameserver.control.GameController;
import de.tuberlin.sese.swtpp.gameserver.model.Player;
import de.tuberlin.sese.swtpp.gameserver.model.User;
import de.tuberlin.sese.swtpp.gameserver.model.deathstacks.DeathStacksGame;

public class DeathStacksGameTest {
	
	User user1 = new User("Alice", "alice");
	User user2 = new User("Bob", "bob");
	User user3 = new User("Eve", "eve");
	
	Player redPlayer = null;
	Player bluePlayer = null;
	DeathStacksGame game = null;
	GameController controller;
	
	String gameType = "deathstacks";
	
	@Before
	public void setUp() throws Exception {
		controller = GameController.getInstance();
		controller.clear();
		
		int gameID = controller.startGame(user1, "", gameType);
		
		game = (DeathStacksGame) controller.getGame(gameID);
		redPlayer = game.getPlayer(user1);
	}
	
	public void startGame() {
		controller.joinGame(user2,gameType);
		bluePlayer = game.getPlayer(user2);
	}

	
	@Test
	public void testWaitingGame() {
		assertTrue(game.getStatus().equals("Wait"));
		assertTrue(game.gameInfo().equals(""));
	}
	
	@Test
	public void testGameStarted() {
		assertEquals(game.getGameID(), controller.joinGame(user2, gameType));
		assertEquals(false, game.addPlayer(new Player(user3, game))); // no third player
		assertEquals("Started", game.getStatus());
		assertTrue(game.gameInfo().equals(""));
		assertTrue(game.isRedNext());
		assertFalse(game.didRedDraw());
		assertFalse(game.didBlueDraw());
		assertFalse(game.redGaveUp());
		assertFalse(game.blueGaveUp());
	}

	@Test
	public void testSetNextPlayer() {
		startGame();
		assertEquals("r",game.nextPlayerString());
		
		game.setNextPlayer(bluePlayer);
		
		assertEquals("b",game.nextPlayerString());
		
		assertFalse(game.isRedNext());
	}

	
	@Test
	public void testCallDraw() {	
		// call draw before start
		assertFalse(game.callDraw(redPlayer));

		startGame();
		
		controller.callDraw(user1, game.getGameID());
		assertTrue(game.didRedDraw());
		assertFalse(game.didBlueDraw());
		assertEquals("red called draw", game.gameInfo());
		
		controller.callDraw(user2, game.getGameID());
		assertTrue(game.didBlueDraw());

		assertEquals("Draw", game.getStatus());
		assertEquals("draw game", game.gameInfo());
		
		// call draw after finish
		assertFalse(game.callDraw(redPlayer));
	}
	
	@Test
	public void testCallDrawBlue() {
		startGame();
		
		controller.callDraw(user2, game.getGameID());
		assertFalse(game.didRedDraw());
		assertTrue(game.didBlueDraw());
		assertEquals("blue called draw", game.gameInfo());
	}

	@Test
	public void testGiveUp() {
		// try before start 
		assertFalse(game.giveUp(redPlayer));
		assertFalse(game.giveUp(bluePlayer));
		
		startGame();
		
		controller.giveUp(user1, game.getGameID());
		
		assertEquals("Surrendered", game.getStatus());
		assertEquals("red gave up", game.gameInfo());
		
		// try after finish
		assertFalse(game.giveUp(redPlayer));
		assertFalse(game.giveUp(bluePlayer));

	}
	
	@Test
	public void testGiveUpBlue() {
		startGame();
		
		controller.giveUp(user2, game.getGameID());
		
		assertEquals("Surrendered", game.getStatus());
		assertEquals("blue gave up", game.gameInfo());
	}

	@Test
	public void testGetMinPlayers() {
		assertEquals(2, game.getMinPlayers());
	}
	
	@Test
	public void testGetMaxPlayers() {
		assertEquals(2, game.getMaxPlayers());
	}

	@Test
	public void testFinish() {
		// test before start
		assertFalse(game.finish(redPlayer));

		startGame();
		
		assertTrue(game.finish(redPlayer));
		assertEquals("Finished", game.getStatus());
		assertEquals("red won", game.gameInfo());
		
		// test after finish
		assertFalse(game.finish(redPlayer));
	}
	
	@Test
	public void testFinishBlue() {
		startGame();
		
		assertTrue(game.finish(bluePlayer));
		assertEquals("Finished", game.getStatus());
		assertEquals("blue won", game.gameInfo());
	}

	@Test
	public void testError() {
		assertFalse(game.isError());
		game.setError(true);
		assertTrue(game.isError());
		assertEquals("Error", game.getStatus());
		game.setError(false);
		assertFalse(game.isError());
	}
}
