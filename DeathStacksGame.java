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

	// new, contains Strings of the board representations throughout the game
	private List<String> boardHistory = new LinkedList<String>();

	// TODO: internal representation of the game state

	/************************
	 * constructors
	 ***********************/

	public DeathStacksGame() throws Exception {
		super();
//		this.addPlayer(redPlayer); 
//		this.addPlayer(bluePlayer); 
//		setNextPlayer(redPlayer);
		// isRedNext(); // bei Erstellung fängt Rot an
		setBoardHistory("rr,rr,rr,rr,rr,rr/,,,,,/,,,,,/,,,,,/,,,,,/bb,bb,bb,bb,bb,bb"); 

		// TODO: Initialization, if necessary
	}

	public String getType() {
		return "deathstacks";
	}

	public List<String> getBoardHistory() {
		return boardHistory;
	}

	public void setBoardHistory(String board) {
		boardHistory.add(board); 
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

	// übergibt Spielzustand für Testfälle
	// muss nicht auf Validität geprüft werden
	@Override
	public void setBoard(String state) {
		boardHistory.add(state);
	}
	

	// Abruf des Spielzustands
	@Override
	public String getBoard() {
		return ((LinkedList<String>) this.boardHistory).getLast().toString();
	}

	/*
	 * checks Move and possibly executes it
	 */
	@Override
	public boolean tryMove(String moveString, Player player) {

//		 Benutzer könnte System angreifen oder schummeln, indem er die Anfragen an den Server manipuliert
//		draw? 	WIE KANN SPIELER DRAW REQUESTEN?
		if (player == nextPlayer && checkMoveFormat(moveString)) {

			String[] array = moveString.split("-");
			String startField = array[0];
			Integer steps = Integer.parseInt(array[1]);
			String endField = array[2];

			if ( ! startField.equals(endField) 
					&& getStack(startField, getBoard()).startsWith(nextPlayerString())
					&& ( (tooTall(getBoard()).isEmpty() || tooTall(getBoard()).contains(getStack(startField, getBoard()))) )
			) {
				
				String newBoard = updateBoard(startField, steps, endField);

//				if (getStack(startField, newBoard).length() <= 4) {

					setBoardHistory(newBoard);
					history.add(new Move(moveString, getBoard(), player)); // board before

					if (!(repeatingState() || winCheck(player)))
						changeNextPlayer();
					// return aktuellen spielstand
					return true;
				}
//			}
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
		if (isRedNext())
			setNextPlayer(bluePlayer);
		else
			setNextPlayer(redPlayer); // ? oder mit nextPlayer == redPlayer
	}

	/*
	 * updates board with the move (changes stacks)
	 */
	private String updateBoard(String startField, Integer steps, String endField) {

	//	if(getStack(startField, getBoard()).length() >= steps) {
		// warning if length of stack > steps ??


		String oldboard = getBoard(); 
		String[] rows = oldboard.split("/");
		String changedStones = getStack(startField, oldboard).substring(0, steps);

		String tempBoard = changeStartField(startField, steps, rows);
		String[] tempRows = tempBoard.split("/"); 

		String finBoard = changeEndField(endField, changedStones, steps, tempRows);
		return finBoard; 
	}
	

	private String changeStartField(String startField, Integer steps, String[] rows) {
		StringBuffer newBoard = new StringBuffer();
		int xr = 6;
		for (String r : rows) {
			StringBuffer newRow = new StringBuffer();
			String[] rowFields = r.split(",", -1);
			int xs = 0;

			for (String f : rowFields) {

				if (xr == (startField.charAt(1) - '0') && xs == getIndex(startField)) {
					newRow.append(f.substring(steps) + ",");
				} else {
					newRow.append(f + ",");
				}
				xs++;
			}
			newRow.setLength(newRow.length() - 1);
			newBoard.append(newRow + "/");
			xr--;
		}
		newBoard.setLength(newBoard.length() - 1);
		return newBoard.toString();
	}
	
	private String changeEndField(String endField, String changedStones, Integer steps, String[] rows) {

		StringBuffer newBoard = new StringBuffer();
		int xr = 6;
		
		for (String r : rows) {
			
			StringBuffer newRow = new StringBuffer();
			String[] rowFields = r.split(",", -1);
			int xs = 0;

			for (String f : rowFields) {
				if (xr == (endField.charAt(1) - '0') && xs == getIndex(endField)) {
					newRow.append(changedStones + f + ",");
				} else {
					newRow.append(f + ",");
				}
				xs++;
			}
			newRow.setLength(newRow.length() - 1);
			newBoard.append(newRow + "/");
			xr--;
		}
		newBoard.setLength(newBoard.length() - 1);
		return newBoard.toString();
	}
	

	/*
	 * returns the current stack of the given field
	 */
	private String getStack(String field, String board) {

		String[] rows = board.split("/");
		String[] stacks = rows[(6 - Integer.parseInt(field.substring(1)))].split(",", -1);				 
		int i = getIndex(field); 
		
		return stacks[i]; 
	}
	
	/*
	 * returns the index to look for in an array based on the first character of a given field
	 */
	private Integer getIndex(String startField) {

		char f = startField.charAt(0);
		int i;

		switch (f) {
		case 'a':
			i = 0; break;
		case 'b':
			i = 1; break;
		case 'c':
			i = 2; break;
		case 'd':
			i = 3; break;
		case 'e':
			i = 4; break;
		case 'f':
			i = 5; break;
		default:
			i = 0; break;
		}
		return i;
	}

	/*
	 * returns an ArrayList containing all stacks of that current player that are
	 * higher than 4
	 */
	private ArrayList<String> tooTall(String board) {

		ArrayList<String> tallStacks = new ArrayList<String>();
		String[] rows = board.split("/"); 
		
		for (String r : rows) {
			String[] rowFields = r.split(",", -1);
			for (String f : rowFields) {
				if (f.length() > 4 && (f.startsWith(nextPlayerString())))
					tallStacks.add(f.toString());
			}
		}
		return tallStacks;
	}

	/*
	 * checks whether a board state exists for the third time and ends game if so
	 */
	private boolean repeatingState() {

		if (boardHistory.stream().filter(i -> Collections.frequency(boardHistory, i) > 2).collect(Collectors.toSet())
				.isEmpty())
			return false;
		else
			return (!finishRepeatingState());
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

	
	/*
	 * checks if all stacks have one color on top
	 */
		private boolean winCheck(Player player) {

			Set<String> res = new HashSet<String>();
			for (String row : getBoard().split("/")) {
				for (String field : row.split(",", -1)) {
					if (field.startsWith("b"))
						res.add("b");
					else if (field.startsWith("r"))
						res.add("r");
				}
			}
			if (res.size() <= 1) {
				return finish(player);
			} else
				return false;
		}
}
