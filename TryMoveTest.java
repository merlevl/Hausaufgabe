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

	String gameType = "deathstacks";

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
		game.setNextPlayer(redNext ? redPlayer : bluePlayer);
	}

	public void assertMove(String move, boolean red, boolean expectedResult) {
		if (red)
			assertEquals(expectedResult, game.tryMove(move, redPlayer));
		else
			assertEquals(expectedResult, game.tryMove(move, bluePlayer));
	}

	public void assertGameState(String expectedBoard, boolean redNext, boolean finished, boolean draw, boolean redWon) {
		String board = game.getBoard();

		assertEquals(expectedBoard, board);
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
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", true);
		assertMove("d6-1-d4", true, false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", true, false, false, false);
	}

	// TODO: implement test cases of same kind as example here

	@Test
	public void ourTests() {
//		startGame(initiaBoard, redNext)
//		assertMove(move, red, expectedResult) 
//		assertGameState(expectedBoard, redNext, finished, draw, redWon)

		// Feld nicht auf Board
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false);
		assertMove("a9-1-d2", false, false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false, false, false, false);

		// Rot bewegt, ist aber nicht dran
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false);
		assertMove("a6-1-b6", true, false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false, false, false, false);

		// keine Änderung des board states, da 10 Schritte
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false);
		assertMove("c6-10-d6", true, false);
		assertMove("e6-10-d6", false, false);
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", false, false, false, false);

		// startfield == endfield
		startGame("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("f1-1-f1", false, false);
		assertGameState("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,", false, false, false, false);

		// blue will feld von rot bewegen
		startGame(",,,,,/,,,,,/,,,,,/,,,,,/r,,,,,/rbb,rrbb,rrbb,rrbb,rrbb,rrbb", false);
		assertMove("a2-1-a1", false, false);
		assertGameState(",,,,,/,,,,,/,,,,,/,,,,,/r,,,,,/rbb,rrbb,rrbb,rrbb,rrbb,rrbb", false, false, false, false);

		// rot will feld von blau bewegen
		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb", true);
		assertMove("a1-1-a2",true,false); 
		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true,false,false,false);

		// tooTall, ungültig, da kein valid move
		startGame("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("c6-3-c5", false, false);
		assertGameState("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false, false, false, false);

		// tooTall, ungültig, da updated startfield zu hoch
		startGame("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("c6-1-c5", false, false);
		assertGameState("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false, false, false, false);

		// tooTall, gültig, da zu hohes field bewegt wird (blau)
		startGame("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("c6-2-e6", false, true);
		assertGameState("bbrr,bbrr,rbrr,bbrr,bbbbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", true, false, false, false);
		
		// tooTall, gültig, da zu hohes field bewegt wird (rot)
		startGame("rrbb,rrbb,rrbrbb,rrbb,rrbb,brb/,,,,,r/,,,,,/,,,,,/,,,,,/,,,,,", true);
		assertMove("c6-2-a4", true, true);
		assertGameState("rrbb,rrbb,brbb,rrbb,rrbb,brb/,,,,,r/rr,,,,,/,,,,,/,,,,,/,,,,,", false, false, false, false);
		
		// tooTall, gültig, da zu hohes field von anderem spieler (rot)
		startGame("rrbb,rrbb,rrbrbb,rrbb,rrbb,brb/,,,,,r/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("f6-2-f4", false, true);
		assertGameState("rrbb,rrbb,rrbrbb,rrbb,rrbb,b/,,,,,r/,,,,,br/,,,,,/,,,,,/,,,,,", true, false, false, false);
				
		// tooTall, gültig, da zu hohes field bewegt wird
		// blau gewinnt
		startGame("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("c6-3-f6", false, true);
		assertGameState("bbrr,bbrr,brr,bbrr,bbrr,bbrrbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false, true, false, false);

		// tooTall, gültig, da zu hohes field bewegt wird
		// rot gewinnt
		startGame("rrbb,rrbb,rrbrbb,rrbb,rrbb,brb/,,,,,r/,,,,,/,,,,,/,,,,,/,,,,,", true);
		assertMove("c6-3-f6", true, true);
		assertGameState("rrbb,rrbb,rbb,rrbb,rrbb,rrbbrb/,,,,,r/,,,,,/,,,,,/,,,,,/,,,,,", true, true, false, false);	//muss redWon true sein
		
		// blueWon, finished
		startGame("bbrr,bbrr,bbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("f5-1-f6", false, true);
		assertGameState("bbrr,bbrr,bbrr,bbrr,bbrr,brbr/,,,,,/,,,,,/,,,,,/,,,,,/,,,,,", false, true, false, false);

		// draw, wegen repeating state
//		startGame("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",true);		 
//		assertMove("d6-1-d5",true,true);	
//		assertMove("d1-1-d2",false,true);
//		assertMove("d5-1-d6",true,true);	
//		assertMove("d2-1-d1",false,true);
//		assertMove("d6-1-d5",true,true);	
//		assertMove("d1-1-d2",false,true);
//		assertMove("d5-1-d6",true,true);
//		assertMove("d2-1-d1",false,true);
//		assertGameState("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb",false,true,true,false);
		

		startGame(",bbrr,bbrr,bbrr,bbrr,rbr/bbrr,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
		assertMove("a5-3-a4", false, true);
//		vertic raus oben, korrekt
//		",bbrr,bbrr,bbrr,bbrr,rbr/r,,,,,b/bbr,,,,,/,,,,,/,,,,,/,,,,," -> rot dran
		
		assertMove("f6-1-f5", true, true); 
//		vertic sminus
//		",bbrr,bbrr,bbrr,bbrr,br/r,,,,,rb/bbr,,,,,/,,,,,/,,,,,/,,,,,"
		
		assertMove("e6-1-f6", false, true); 
//		horiz splus
//		",bbrr,bbrr,bbrr,brr,bbr/r,,,,,rb/bbr,,,,,/,,,,,/,,,,,/,,,,,"
		
		assertMove("a5-1-a6", true, true);
//		vertic plus
//		"r,bbrr,bbrr,bbrr,brr,bbr/,,,,,rb/bbr,,,,,/,,,,,/,,,,,/,,,,,"
		
		assertMove("b6-3-c6", false, true); 
//		horiz minus raus
//		"r,r,bbrbbrr,bbrr,brr,bbr/,,,,,rb/bbr,,,,,/,,,,,/,,,,,/,,,,,"
		
		assertMove("b6-1-a6", true, true); 
//		horiz minus
//		"rr,,bbrbbrr,bbrr,brr,bbr/,,,,,rb/bbr,,,,,/,,,,,/,,,,,/,,,,,"
		
		assertMove("c6-6-c2", false, true); 
//		vertic sminus raus
//		"rr,,r,bbrr,brr,bbr/,,,,,rb/bbr,,,,,/,,,,,/,,bbrbbr,,,/,,,,,"
		
		assertMove("a6-2-c4", true, true);
//		diag rechts unten
//		",,r,bbrr,brr,bbr/,,,,,rb/bbr,,rr,,,/,,,,,/,,bbrbbr,,,/,,,,,"
		
		assertMove("c2-4-e4",false, true); 
//		diag: an rand unten von links dann nach rechts hoch; seite von unten nach links oben
//		",,r,bbrr,brr,bbr/,,,,,rb/bbr,,rr,,bbrb,/,,,,,/,,br,,,/,,,,,"
		
		assertMove("c4-2-c6",true,true); 
//		",,rrr,bbrr,brr,bbr/,,,,,rb/bbr,,,,bbrb,/,,,,,/,,br,,,/,,,,,"
		
		assertMove("e6-3-d6",false, true);
//		",,rrr,brrbbrr,,bbr/,,,,,rb/bbr,,,,bbrb,/,,,,,/,,br,,,/,,,,,"
		
		assertMove("c6-3-b3",true,true);
//		diag: nach links an seite, dann nach rechts unten
//		",,,brrbbrr,,bbr/,,,,,rb/bbr,,,,bbrb,/,rrr,,,,/,,br,,,/,,,,,"
		
		assertMove("d6-7-a3",false,true);
//		diag: an seite von rechts oben, dann weiter nach links unten, von rechts oben an rand unten, dann nach links hoch
//		",,,,,bbr/,,,,,rb/bbr,,,,bbrb,/brrbbrr,rrr,,,,/,,br,,,/,,,,,"
		
		assertMove("b3-3-c6",true,true);
//		diag: an linke seite von rechts unten, dann nach rechts oben weiter
//		",,rrr,,,bbr/,,,,,rb/bbr,,,,bbrb,/brrbbrr,,,,,/,,br,,,/,,,,,"
		
		assertMove("a3-4-e5",false,true);
//		diag: von links unten an rand oben, dann nach rechts unten weiter
//		",,rrr,,,bbr/,,,,brrb,rb/bbr,,,,bbrb,/brr,,,,,/,,br,,,/,,,,,"
		
		assertMove("c6-1-b5",true,true);
//		diag: rechts runter
//		",,rr,,,bbr/,r,,,brrb,rb/bbr,,,,bbrb,/brr,,,,,/,,br,,,/,,,,,"
		
		assertMove("e5-3-b4",false,true);
//		diag: von links unten an rand oben, dann nach rechts unten weiter
//		",,rr,,,bbr/,r,,,b,rb/bbr,brr,,,bbrb,/brr,,,,,/,,br,,,/,,,,,"
		
		assertMove("b5-1-a6",true,true);
//		diag: nach links oben
		
		assertMove("e4-1-f5",false,true);
//		diag: nach rechts oben
		
		assertGameState("r,,rr,,,bbr/,,,,b,brb/bbr,brr,,,brb,/brr,,,,,/,,br,,,/,,,,,", true, true, false, false);

	
	
		// tooTall, gültig
//		startGame("bbrr,bbrr,bbrbrr,bbrr,bbrr,rbr/,,,,,b/,,,,,/,,,,,/,,,,,/,,,,,", false);
//		assertMove("c6-3-c5", false, true);
//		assertGameState("bbrr,bbrr,brr,bbrr,bbrr,rbr/,,bbr,,,b/,,,,,/,,,,,/,,,,,/,,,,,", true, true, false, false);



	}

}
