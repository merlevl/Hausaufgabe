package de.tuberlin.sese.swtpp.gameserver.model.deathstacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.tuberlin.sese.swtpp.gameserver.model.Game;
import de.tuberlin.sese.swtpp.gameserver.model.Move;
import de.tuberlin.sese.swtpp.gameserver.model.Player;

public class DeathStacksGame extends Game {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3053592017994489843L;
	/************************
	 * member
	 ***********************/

	// just for better comprehensibility of the code: assign blue and red player
	private Player bluePlayer;
	private Player redPlayer;

	// new
	private List<String> boardHistory = new LinkedList<String>();

	// TODO: internal representation of the game state

	/************************
	 * constructors
	 ***********************/

	public DeathStacksGame() throws Exception {
		super();
		nextPlayer = redPlayer;
		// isRedNext(); // bei Erstellung fängt Rot an
		this.boardHistory.add("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb");

		// TODO: Initialization, if necessary
	}

	public String getType() {
		return "deathstacks";
	}

	public List<String> getBoardHistory() {
		return boardHistory;
	}

	public void setBoardHistory(List<String> boardHistory) {
		this.boardHistory = boardHistory;
	}

	/*******************************************
	 * Game class functions already implemented
	 ******************************************/

	@Override
	public boolean addPlayer(Player player) {
		if (!started) {
			players.add(player);

			if (players.size() == 2) {
				started = true;
				this.redPlayer = players.get(0);
				this.bluePlayer = players.get(1);
				nextPlayer = this.redPlayer;
			}
			return true;
		}

		return false;
	}

	@Override
	public String getStatus() {
		if (error)
			return "Error";
		if (!started)
			return "Wait";
		if (!finished)
			return "Started";
		if (surrendered)
			return "Surrendered";
		if (draw)
			return "Draw";

		return "Finished";
	}

	@Override
	public String gameInfo() {
		String gameInfo = "";

		if (started) {
			if (blueGaveUp())
				gameInfo = "blue gave up";
			else if (redGaveUp())
				gameInfo = "red gave up";
			else if (didRedDraw() && !didBlueDraw())
				gameInfo = "red called draw";
			else if (!didRedDraw() && didBlueDraw())
				gameInfo = "blue called draw";
			else if (draw)
				gameInfo = "draw game";
			else if (finished)
				gameInfo = bluePlayer.isWinner() ? "blue won" : "red won";
		}

		return gameInfo;
	}

	@Override
	public String nextPlayerString() {
		return isRedNext() ? "r" : "b";
	}

	@Override
	public int getMinPlayers() {
		return 2;
	}

	@Override
	public int getMaxPlayers() {
		return 2;
	}

	@Override
	public boolean callDraw(Player player) {

		// save to status: player wants to call draw
		if (this.started && !this.finished) {
			player.requestDraw();
		} else {
			return false;
		}

		// if both agreed on draw:
		// game is over
		if (players.stream().allMatch(p -> p.requestedDraw())) {
			this.finished = true;
			this.draw = true;
			redPlayer.finishGame();
			bluePlayer.finishGame();
		}
		return true;
	}

	@Override
	public boolean giveUp(Player player) {
		if (started && !finished) {
			if (this.redPlayer == player) {
				redPlayer.surrender();
				bluePlayer.setWinner();
			}
			if (this.bluePlayer == player) {
				bluePlayer.surrender();
				redPlayer.setWinner();
			}
			finished = true;
			surrendered = true;
			redPlayer.finishGame();
			bluePlayer.finishGame();

			return true;
		}

		return false;
	}

	/*******************************************
	 * Helpful stuff
	 ******************************************/

	/**
	 * 
	 * @return True if it's white player's turn
	 */
	public boolean isRedNext() {
		return nextPlayer == redPlayer;
	}

	/**
	 * Finish game after regular move (save winner, move game to history etc.)
	 * 
	 * @param player
	 * @return
	 */
	public boolean finish(Player player) {
		// public for tests
		if (started && !finished) {
			player.setWinner();
			finished = true;
			redPlayer.finishGame();
			bluePlayer.finishGame();

			return true;
		}
		return false;
	}

	public boolean didRedDraw() {
		return redPlayer.requestedDraw();
	}

	public boolean didBlueDraw() {
		return bluePlayer.requestedDraw();
	}

	public boolean redGaveUp() {
		return redPlayer.surrendered();
	}

	public boolean blueGaveUp() {
		return bluePlayer.surrendered();
	}

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 ******************************************/

	// übergibt Spielzustand
	// muss nicht auf Validität geprüft werden
	@Override
	public void setBoard(String state) {
		this.boardHistory.add(state);

	}

	// Abruf des Spielzustands
	@Override
	public String getBoard() {
		return ((LinkedList<String>) this.boardHistory).getLast().toString();
	}

	/*
	 * checks Move and possibly executes it
	 * 
	 * 			draw? 	WIE KANN SPIELER DRAW REQUESTEN?
			no: 
	    	   	if tootall: höchstens 4 Figuren verbleiben auf dem startField 
				board updaten -- String boardUpdate = neuen String mit Änderungen
				board state speichern -- setBoard(boardUpdate)
				move speichern -- this.history.add(new Move(...))
				check if repeating state or sme won
						yes: finished, gameStatus updaten, return aktuellen spielstand
						no: anderer Spieler dran, return aktuellen spielstand
		 
	 */
	@Override
	public boolean tryMove(String moveString, Player player) {
		
//		 Benutzer könnte System angreifen oder schummeln, indem er die Anfragen an den Server manipuliert

		if (player == nextPlayer && checkMoveFormat(moveString)) {

			String[] array = moveString.split("-");
			String startField = array[0];
			Integer steps = Integer.parseInt(array[1]);
			String endField = array[2];

			if (startField != endField
					&& getStack(startField, getBoard()).startsWith(nextPlayerString())
					&& ( (tooTall(getBoard()).isEmpty() || tooTall(getBoard()).contains(startField)) )) {
						
					String newBoard = updateBoard(startField, steps, endField);
					
					if( getStack(startField, newBoard).length() <= 4 ) {
						
					setBoard(newBoard);
					this.history.add(new Move(moveString, getBoard(), player));	//board before

					if (! (repeatingState() || winCheck(player))) 
						changeNextPlayer();
						//return aktuellen spielstand
					return true; 
					}
			}
		}
		return false; 
	}
	
	/*
	 * checks the format of the given move <start>-<steps>-<end> (d2-3-e3)
	 */
	private boolean checkMoveFormat(String moveString) {
		return moveString.matches("[a-f][1-6]-(\\d+)-[a-f][1-6]");
	}

	/*
	 * updates which player's turn it is
	 */
	private void changeNextPlayer() {
		if(isRedNext())
			setNextPlayer(bluePlayer); 
		else 
			setNextPlayer(redPlayer); //? oder mit nextPlayer == redPlayer
	}


	/*
	 * updates board with the move (changes stacks)
	 */
	private String updateBoard(String startField, Integer steps, String endField) {
		//warning if length of stack > steps
		
		//ex.: a6, 1, f1  (rr, 1, bb) 
		//res.: r, 1, rbb
		String startStack = getStack(startField, getBoard()); 
			//rr
		String endStack = getStack(endField, getBoard()); 
			//bb
		String changedStones = startStack.substring(0,steps); 
			//r
		String newStart = startStack.substring(steps, startStack.length()); 
			//r
		String newEnd = changedStones.concat(endStack); 
			//rbb
		
		String[] rows = getBoard().split("/"); 		
//		a6,b6,c6,d6,e6,f6
//		a5,b5,c5,d5,e5,f5
//		a4,b4,c4,d4,e4,f4
//		a3,b3,c3,d3,e3,f3
//		a2,b2,c2,d2,e2,f2
//		a1,b1,c1,d1,e1,f1
		String[] stacksStartField = rows[(6 - Integer.parseInt(startField.substring(1)))].split(",");
//		a6
//		b6
//		c6
//		d6
//		e6
//		f6
		String[] stacksEndField = rows[(6 - Integer.parseInt(endField.substring(1)))].split(",");
		String newBoard = "";  
		return newBoard;
	}

	
	// checks if all stacks have one color on top
	private boolean winCheck(Player player) {
		
		Set<String> res = new HashSet<String>();  
		for (String row : getBoard().split("/")) {
			for (String field : row.split(",")) {
				if (field.startsWith("b")) 
					res.add("b"); 
				else if (field.startsWith("b")) 
					res.add("r"); 
			}
		}
		if(res.size() <= 1) {
			return finish(player);
		}
		else 
			return false;
	}

	

	/*
	 * returns the current stack of the given field
	 * a6,b6,c6,d6,e6,f6/a5,b5,c5,d5,e5,f5/a4,b4,c4,d4,e4,f4/a3,b3,c3,d3,e3,f3/a2,b2,c2,d2,e2,f2/a1,b1,c1,d1,e1,f1
	 */
	private String getStack(String field, String board) {

		String[] rows = board.split("/");
		String[] stacks = rows[(6 - Integer.parseInt(field.substring(1)))].split(",");
		switch (field.substring(0)) {
			case "a":
				return stacks[0];
			case "b":
				return stacks[1];
			case "c":
				return stacks[2];
			case "d":
				return stacks[3];
			case "e":
				return stacks[4];
			case "f":
				return stacks[5];
			default:
				return "";
		}
	}

	/*
	 * returns an ArrayList containing all stacks of that current player that are higher than 4
	 */
	private ArrayList<String> tooTall(String board) {
		ArrayList<String> tallStacks = new ArrayList<String>();

		for (String row : board.split("/")) {
			for (String field : row.split(",")) {
				if (field.length() > 4 && (field.startsWith(nextPlayerString())))
					tallStacks.add(field.toString());
			}
		}
		return tallStacks;
	}

	/*
	 * checks whether a board state exists for the third time
	 * and ends game if so
	 */
	private boolean repeatingState() {

		if (boardHistory.stream().filter(i -> Collections.frequency(boardHistory, i) > 2).collect(Collectors.toSet())
				.isEmpty())
			return false;
		else 
			return (! finishRepeatingState()); 
	}
	
	private boolean finishRepeatingState() {
		if (started && !finished) {
			finished = true;
			draw = true; 
			redPlayer.finishGame();
			bluePlayer.finishGame();
			return true;
		}
		return false;
	}

}
