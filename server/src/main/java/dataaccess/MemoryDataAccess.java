package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;

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
//        else {
//            return null;
//        }

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

    // Delete Methods
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

//MAKE IT CRUD:
//Create objects in the data store
//Read objects from the data store
//Update objects already in the data store
//Delete objects from the data store


// EXAMPLE DATA ACCESS METHODS
//clear: A method for clearing all data from the database. This is used during testing.
//createUser: Create a new user.
//getUser: Retrieve a user with the given username.
//createGame: Create a new game.
//getGame: Retrieve a specified game with the given game ID.
//listGames: Retrieve all games.
//updateGame: Updates a chess game. It should replace the chess game string corresponding to a given gameID. This is used when players join a game or when a move is made.
//createAuth: Create a new authorization.
//getAuth: Retrieve an authorization given an authToken.
//deleteAuth: Delete an authorization so that it is no longer valid.
