package de.tuberlin.sese.swtpp.gameserver.test.deathstacks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.sese.swtpp.gameserver.control.GameController;
import de.tuberlin.sese.swtpp.gameserver.model.Player;
import de.tuberlin.sese.swtpp.gameserver.model.User;
import de.tuberlin.sese.swtpp.gameserver.model.deathstacks.DeathStacksGame;

public class TryMoveTest {

	User user1 = new User("Alice", "alice");
	User user2 = new User("Bob", "bob");
	
	Player redPlayer = null;
	Player bluePlayer = null;
	DeathStacksGame game = null;
	GameController controller;
	
	String gameType ="deathstacks";
	
	@Before
	public void setUp() throws Exception {
		controller = GameController.getInstance();
		controller.clear();
		
		int gameID = controller.startGame(user1, "", gameType);
		
		game = (DeathStacksGame) controller.getGame(gameID);
		redPlayer = game.getPlayer(user1);

	}
	
	public void startGame(String initialBoard, boolean redNext) {
		controller.joinGame(user2, gameType);		
		bluePlayer = game.getPlayer(user2);
		
		game.setBoard(initialBoard);
		game.setNextPlayer(redNext? redPlayer:bluePlayer);
	}
	
	public void assertMove(String move, boolean red, boolean expectedResult) {
		if (red)
			assertEquals(expectedResult, game.tryMove(move, redPlayer));
		else 
			assertEquals(expectedResult,game.tryMove(move, bluePlayer));
	}
	
	public void assertGameState(String expectedBoard, boolean redNext, boolean finished, boolean draw, boolean redWon) {
		String board = game.getBoard();
				
		assertEquals(expectedBoard,board);
		assertEquals(finished, game.isFinished());
		if (!game.isFinished()) {
			assertEquals(redNext, game.isRedNext());
		} else {
			assertEquals(draw, game.isDraw());
			if (!draw) {
				assertEquals(redWon, redPlayer.isWinner());
				assertEquals(!redWon, bluePlayer.isWinner());
			}
		}
	}

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 *******************************************/
	
	@Test
	public void exampleTest() {
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true);
		assertMove("d6-1-d4",true,false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true,false,false,false);
	}

	
	//TODO: implement test cases of same kind as example here

	@Test
	public void ourTests() {
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false);		
		
		
//		assertMove(move, red, expectedResult) 
 
		assertMove("d6-1-d4",true,false);	
		assertMove("d6-1-d4",false,false);
		
		assertMove("d9-1-d2",true,false); 
		assertMove("d9-1-d2",false,false); 
		
		assertMove("d6-10-d6",true,false); 
		assertMove("d6-10-d6",false,false); 

		assertMove("a6-1-a5",false,false); 
		
//		assertMove("d6-1-d5",false,true);
//		assertMove("d6-1-d5",false,true);

		
//		assertGameState(expectedBoard, redNext, finished, draw, redWon)
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,false,false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,true,false);		
	}
	
}
