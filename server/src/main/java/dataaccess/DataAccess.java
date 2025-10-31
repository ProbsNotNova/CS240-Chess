package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.Collection;

public interface DataAccess {

    // Get Methods
    UserData getUser(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;

    // Game Methods
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(int gameID, ChessGame.TeamColor parsedPlayerColor, String user) throws DataAccessException;

    // Create Methods
    void createUser(UserData registerRequest) throws DataAccessException;
    void createAuth(AuthData newAuthToken) throws DataAccessException;
    int createGame(String gameName) throws DataAccessException;

    // Delete Method
    void deleteAuth(String authToken) throws DataAccessException;

    // Clear Method
    void clearAllData() throws DataAccessException;

}
