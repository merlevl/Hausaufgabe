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
//		startGame(initiaBoard, redNext)
//		assertMove(move, red, expectedResult) 
//		assertGameState(expectedBoard, redNext, finished, draw, redWon)

/*
  		//draw wegen repeatingstate
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true);		 
		assertMove("d6-1-d5",true,true);	
		assertMove("d1-1-d2",false,true);
		assertMove("d5-1-d6",true,true);	
		assertMove("d2-1-d1",false,true);
		assertMove("d6-1-d4",true,true);	
		assertMove("d1-1-d2",false,true);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,true,false);
*/
				
		
		//Feld nicht auf Board
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false);		 
		assertMove("a9-1-d2",false,false); 
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,false,false);
		
		//Rot bewegt, ist aber nicht dran
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false);		 
		assertMove("a6-1-b6",true,false); 
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,false,false);

		//keine Änderung des Boardstates, da 10 Schritte
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false);		 
		assertMove("c6-10-d6",true,false); 
		assertMove("e6-10-d6",false,false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,false,false,false);
		
		//startfield == endfield
		startGame("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,",false);		 
		assertMove("f1-1-f1",false,false); 
		assertGameState("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,",false,false,false,false);
		
		//blue will Feld von rot bewegen
		startGame(",,,,,/,,,,,/,,,,,/,,,,,/r,,,,,/rbb,rrbb,rrbb,rrbb,rrbb,rrbb",false);		 
		assertMove("a2-1-a1",false,false); 
		assertGameState(",,,,,/,,,,,/,,,,,/,,,,,/r,,,,,/rbb,rrbb,rrbb,rrbb,rrbb,rrbb",false,false,false,false);
		
		//rot will Feld von blau bewegen
//		startGame(",,,,,/,,,,,/,,,,,/,,,,,/br,,,,,/rb,rrbb,rrbb,rrbb,rrbb,rrbb",true);		 
//		assertMove("a2-1-a1",true,false); 
//		assertGameState(",,,,,/,,,,,/,,,,,/,,,,,/br,,,,,/rb,rrbb,rrbb,rrbb,rrbb,rrbb",false,false,false,false);
//		
		//game already finished
		startGame("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,",false);		 
		assertMove("a2-1-a1",false,false); 
		assertGameState("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,",false,true,false,false);

		
		//redWon, finished
		startGame(",,,,,/,,,,,/,,,,,/,,,,,/r,,,,,/brb,rrbb,rrbb,rrbb,rrbb,rrbb",true);		 
		assertMove("a2-1-a1",true,true); 
		assertGameState(",,,,,/,,,,,/,,,,,/,,,,,/,,,,,/rbrb,rrbb,rrbb,rrbb,rrbb,rrbb",false,true,false,true);

		//blueWon, finished
		startGame("bbrr,bbrr,bbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,",false);		 
		assertMove("f5-1-f6",false,true); 
		assertGameState("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,",false,true,false,false);

		//getIndex für alle Felder
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true);
		assertMove("a6-1-a5",true,true);
		assertMove("c6-1-d5",true,true);
		assertMove("e6-1-f5",true,true);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true,false,false,false);
		
		
		
		
		
//		assertMove("d6-1-d5",false,true);
//		assertMove("d6-1-d5",false,true);
	}
	
}
