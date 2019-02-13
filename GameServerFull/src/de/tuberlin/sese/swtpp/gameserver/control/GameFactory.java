package de.tuberlin.sese.swtpp.gameserver.control;

import de.tuberlin.sese.swtpp.gameserver.model.Game;
import de.tuberlin.sese.swtpp.gameserver.model.HaskellBot;
import de.tuberlin.sese.swtpp.gameserver.model.User;
import de.tuberlin.sese.swtpp.gameserver.model.deathstacks.DeathStacksGame;

public class GameFactory {
	
	//TODO: change path to bot executable if desired
	public static final String DEATHSTACKS_BOT_PATH = "C:\\tmp\\deathstacks\\";
	public static final String DEATHSTACKS_BOT_COMMAND = "Main.exe";
	
	public static Game createGame(String gameType) {
		try {
			switch(gameType) {
				case "deathstacks": return new DeathStacksGame();
			}
		} catch (Exception e)  {
			e.printStackTrace();
		}
		
		return null;
	}

	public static User createBot(String type, Game game) {
		switch(type) {
			case "haskell": 
				switch(game.getClass().getName().substring(game.getClass().getName().lastIndexOf(".")+1)) {
					case "DeathStacksGame": return new HaskellBot(game, DEATHSTACKS_BOT_PATH, DEATHSTACKS_BOT_COMMAND);
					default: return null;
				}
			default: return null;
		}
	}
	
}
