package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.Collection;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private int assignedGameID = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();
    final private HashMap<String, UserData> users = new HashMap<>();

    // Get Methods
    public UserData getUser(String username) {
        if (users.containsKey(username)) {
            return users.get(username);
        }
        return null;
    }
    public AuthData getAuth(String authToken) {
        if (authTokens.containsKey(authToken)) {
            return authTokens.get(authToken);
        }
        return null;
    }
    public GameData getGame(int gameID) {
        if (games.containsKey(gameID)) {
            return games.get(gameID);
        }
        return null;
    }

    // Game Methods
    public Collection<GameData> listGames() {
        return games.values();
    }
    public void updateGame(int gameID, ChessGame.TeamColor parsedPlayerColor, String user) {
        GameData game = getGame(gameID);
        if (getGame(gameID).whiteUsername() == null && parsedPlayerColor == ChessGame.TeamColor.WHITE) {
            GameData newGame = new GameData(game.gameID(), user, game.blackUsername(), game.gameName(), game.game());
            games.put(gameID, newGame);
        } else if (getGame(gameID).blackUsername() == null && parsedPlayerColor == ChessGame.TeamColor.BLACK){
            GameData newGame = new GameData(game.gameID(), game.whiteUsername(), user, game.gameName(), game.game());
            games.put(gameID, newGame);
        }
    }

    // Create Methods
    public void createUser(UserData registerRequest) {
        users.put(registerRequest.username(), registerRequest);
    }
    public void createAuth(AuthData newAuthToken) {
        authTokens.put(newAuthToken.authToken(), newAuthToken);
    }
    public int createGame(String gameName) {
        GameData newGame = new GameData(assignedGameID, null, null, gameName, new ChessGame());
        games.put(assignedGameID, newGame);
        assignedGameID++;
        return newGame.gameID();
    }

    // Delete Method
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    // Clear Method
    public void clearAllData() {
        games.clear();
        authTokens.clear();
        users.clear();
        assignedGameID = 1;
    }
}
