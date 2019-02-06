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

//	public List<String> getBoardHistory() {
//		return boardHistory;
//	}

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

	// uebergibt Spielzustand für Testfaelle
	// muss nicht auf Validitaet geprüft werden
	@Override
	public void setBoard(String state) {
		boardHistory.add(state);
	}

	// Abruf des Spielzustands
	@Override
	public String getBoard() {
		return ((LinkedList<String>) this.boardHistory).getLast().toString();
	}

	/**
	 * checks: - is it the player's turn - move format - start field and end field
	 * differ - top stone on the start field is player's stone - valid step (can be
	 * performed diagonally, horizontally, vertically) - no stack of the player that
	 * is too tall exists or player is moving this stack
	 * 
	 * updates board
	 * 
	 * checks: - no more than 4 remaining stones on the start field
	 * 
	 * updates board history
	 * 
	 * checks: - repeating state - win else next player's turn
	 * 
	 * @param move   String representation of move
	 * @param player The player that tries the move
	 * @return true if the move was performed
	 */
	@Override
	public boolean tryMove(String moveString, Player player) {

//	Benutzer könnte System angreifen oder schummeln, indem er die Anfragen an den Server manipuliert
//		draw? 	WIE KANN SPIELER DRAW REQUESTEN?
		if (player == nextPlayer && checkMoveFormat(moveString)) {

			String[] array = moveString.split("-");
			String startField = array[0];
			Integer steps = Integer.parseInt(array[1]);
			String endField = array[2];

			if (!startField.equals(endField) && getStack(startField, getBoard()).startsWith(nextPlayerString())
					&& checkValidStep(startField, steps, endField) && ((tooTall(getBoard()).isEmpty()
							^ tooTall(getBoard()).contains(getStack(startField, getBoard()))))) {

				String newBoard = updateBoard(startField, steps, endField);

				if (getStack(startField, newBoard).length() <= 4) { // Höhe des Stapels des Startfields des Moves <= 4

					setBoardHistory(newBoard);
					history.add(new Move(moveString, getBoard(), player)); // board before
 
					if (winCheck()) {// falls Spieler gewonnen
							finish(player); // Spiel beenden
						return true; 
				}
//					else if (repeatingState()) {// falls Status zum dritten Mal gleich
//						finishRepeatingState();
//						return true;
//					}
					else {
						changeNextPlayer(); // zum nächsten Spieler wechseln
						return true;
					}
					// return aktuellen spielstand
				} else
					return false;
			} else
				return false;
		}
		return false;
	}

	/**
	 * checks the format of the given move <start>-<steps>-<end> (d2-3-e3)
	 * 
	 * @param moveString
	 * @return true if move matches format
	 */
	private boolean checkMoveFormat(String moveString) {
		return moveString.matches("[a-f][1-6]-(\\d+)-[a-f][1-6]");
	}
	
	

	/**
	 * updates which player's turn it is
	 */
	private void changeNextPlayer() {
		if (isRedNext())
			setNextPlayer(bluePlayer);
		else
			setNextPlayer(redPlayer);
	}

	/**
	 * updates board with the move (changes stacks)
	 * 
	 * @param startField, steps, endField
	 * @return updated board
	 */
	private String updateBoard(String startField, Integer steps, String endField) {

		// if(getStack(startField, getBoard()).length() >= steps) {
		// warning if length of stack > steps ??

		String oldboard = getBoard();
		String[] rows = oldboard.split("/");
		String changedStones = getStack(startField, oldboard).substring(0, steps);

		String tempBoard = changeStartField(startField, steps, rows);
		String[] tempRows = tempBoard.split("/");

		String finBoard = changeEndField(endField, changedStones, steps, tempRows);
		return finBoard;
	}

	/**
	 * updates the board by changing the start field (removing stones)
	 * 
	 * @param startField
	 * @param steps
	 * @param rows
	 * @return temporary board with changed start field
	 */
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

	/**
	 * updates the board by changing the end field (adding stones)
	 * 
	 * @param endField
	 * @param changedStones
	 * @param steps
	 * @param rows
	 * @return updated board with changed end field
	 */
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

	/**
	 * @param field
	 * @param board
	 * @return current stack of the given field
	 */
	private String getStack(String field, String board) {

		String[] rows = board.split("/");
		String[] stacks = rows[(6 - Integer.parseInt(field.substring(1)))].split(",", -1);
		int i = getIndex(field);

		return stacks[i];
	}

	private Boolean checkValidStep(String startField, int steps, String endField) {
		return checkVertical(startField, steps, endField) 
				|| checkDiagonal(startField, steps, endField)
				|| checkHorizontal(startField, steps, endField);
	}

	/**
	 * @param startField
	 * @param steps
	 * @param endField
	 * @return true if the move was performed vertical
	 */
	private Boolean checkVertical(String startField, int steps, String endField) {
		int s = Character.getNumericValue(startField.charAt(1));
		int e = Character.getNumericValue(endField.charAt(1));

		int sPlus = s + steps;
		int sMinus = s - steps;

		if (sPlus == e 
				|| sMinus == e)
			return true;

		else if (sPlus > 6 && (sPlus - 2 * (sPlus - 6) == e))
			return true;

		else if (sMinus < 1 && (sMinus + 2 * (1 - sMinus)) == e)
			return true;

		else return false;

	}

	/**
	 * @param startField
	 * @param steps
	 * @param endField
	 * @return true if the move was performed horizontal
	 */
	private Boolean checkHorizontal(String startField, int steps, String endField) {
		int s = getIndex(startField) + 1;
		int e = getIndex(endField) + 1;

		int sPlus = s + steps;
		int sMinus = s - steps;

		if (sPlus == e 
				|| sMinus == e)
			return true;

		else if (sPlus > 6 && (sPlus - 2 * (sPlus - 6)) == e)
			return true;

		else if (sMinus < 1 && (sMinus + 2 * (1 - sMinus)) == e)
			return true;

		else return false;
	}

	/**
	 * @param startField
	 * @param steps
	 * @param endField
	 * @return true if the move was performed diagonal
	 */
	private Boolean checkDiagonal(String startField, int steps, String endField) {
		int sD = getIndex(startField); // 3
		int s6 = Character.getNumericValue(startField.charAt(1));

		int eB = getIndex(endField); // 1
		int e4 = Character.getNumericValue(endField.charAt(1));

		int sDLinks = sD - steps;
		int sDRechts = sD + steps;
		int s6Oben = s6 + steps;
		int s6Unten = s6 - steps;

		if ((sDLinks == eB && s6Oben == e4) 
				|| (sDLinks == eB && s6Unten == e4) 
				|| (sDRechts == eB && s6Oben == e4)
				|| (sDRechts == eB && s6Unten == e4))
			return true;

		if (sDLinks < 0 
				&& ((sDLinks + 2 * (1 - sDLinks)) == eB) 
				&& (s6Unten == e4 || s6Oben == e4))
			return true;

		if (sDRechts > 5 
				&& (sDRechts - 2 * (sDRechts - 5) == eB) 
				&& (s6Unten == e4 || s6Oben == e4))
			return true;

		if (s6Oben > 6
				&& (s6Oben - 2 * (s6Oben - 6) == e4) 
				&& (sDRechts == eB || sDLinks == eB))
			return true;

		if (s6Unten < 1 
				&& (s6Unten + 2 * (1 - s6Unten) == e4) 
				&& (sDRechts == eB || sDLinks == eB))
			return true;

		else return false;
	}

	/**
	 * returns the index to look for in an array based on the first character of a
	 * given field ("a" to 0, "b" to 1, ...)
	 * 
	 * @param field
	 * @return index
	 */
	private Integer getIndex(String field) {

		int i = 0;

		switch (field.charAt(0)) {
		case 'a':
			i = 0;
			break;
		case 'b':
			i = 1;
			break;
		case 'c':
			i = 2;
			break;
		case 'd':
			i = 3;
			break;
		case 'e':
			i = 4;
			break;
		case 'f':
			i = 5;
			break;
		}
		return i;
	}

	/*
	 * 
	 */
	/**
	 * looks for all stacks of the current player that are higher than 4
	 * 
	 * @param board
	 * @return list with stacks
	 */
	private ArrayList<String> tooTall(String board) {

		ArrayList<String> tallStacks = new ArrayList<String>();
		String[] rows = board.split("/");

		for (String r : rows) {
			String[] rowFields = r.split(",", -1);
			for (String f : rowFields) {
				if (f.length() > 4 
						&& (f.startsWith(nextPlayerString())))
					tallStacks.add(f.toString());
			}
		}
		return tallStacks;
	}

	/**
	 * checks the repeating state rule
	 * 
	 * @return true if board state exists for the third time
	 */
	private boolean repeatingState() {

		if (boardHistory.stream().filter(i -> Collections.frequency(boardHistory, i) > 2).collect(Collectors.toSet())
				.isEmpty())
			return false;
		else {
			finishRepeatingState();
			return true;
		}
			
		
	}

	/**
	 * finishes a game based on the repating state rule
	 * 
	 * @return
	 */
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

	/**
	 * checks if all stacks have one color (one player's string) on top
	 * 
	 * @return true if a player has won
	 */
	private boolean winCheck() {

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
			return true;
		} else
			return false;
	}
}
