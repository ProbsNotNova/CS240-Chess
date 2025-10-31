package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // generate new Auth Token
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    // Register Request and Result
    public AuthData register(UserData registerRequest) throws DataAccessException {
            if ((registerRequest.username() == null) || (registerRequest.password() == null) || (registerRequest.email() == null)) {
                throw new DataAccessException("Error: bad request", 400);
            } else if (dataAccess.getUser(registerRequest.username()) != null) {
                // AlreadyTakenException
                throw new DataAccessException("Error: username already taken", 403);
            } else {
                dataAccess.createUser(registerRequest);
                AuthData authToken = new AuthData(registerRequest.username(), generateToken());
                dataAccess.createAuth(authToken);
                return authToken;
            }
    }

    // Login
    public AuthData login(UserData loginRequest) throws DataAccessException {
        if ((loginRequest.username() == null) || (loginRequest.password() == null)) {
            // Bad Request Exception
            throw new DataAccessException("Error: bad request", 400);
        } else {
                UserData retrievedData = dataAccess.getUser(loginRequest.username());
                if (retrievedData == null || !loginRequest.password().equals(retrievedData.password())) {
                    // UnauthorizedException
                    throw new DataAccessException("Error: Unauthorized", 401);
                } else {
                    AuthData authToken = new AuthData(loginRequest.username(), generateToken());
                    dataAccess.createAuth(authToken);
                    return authToken;
                }
        }
    }

    // Logout
    public void logout(String logoutRequest) throws DataAccessException {
        AuthData retrievedToken = dataAccess.getAuth(logoutRequest);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
            dataAccess.deleteAuth(logoutRequest);
        }
    }
    // List All Games
    public Collection<GameData> listGames(String listGamesRequest) throws DataAccessException {
        AuthData retrievedToken = dataAccess.getAuth(listGamesRequest);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
                return dataAccess.listGames();
        }
    }

    // Create Game
    public int createGame(String createGameReqAuth, String createGameReq) throws DataAccessException {
        if (createGameReqAuth == null || createGameReq == null) {
            throw new DataAccessException("Error: bad request", 400);
        }
        AuthData retrievedToken = dataAccess.getAuth(createGameReqAuth);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
                return dataAccess.createGame(createGameReq);
        }
    }

    // Join Game
    public GameData joinGame(String ReqAuth, String playerColor, int ReqID) throws DataAccessException {
        if (ReqAuth==null || ReqID<=0 || playerColor==null || !(playerColor.equals("WHITE") || playerColor.equals("BLACK"))) {
            throw new DataAccessException("Error: bad request", 400);
        }
        AuthData retrievedToken = dataAccess.getAuth(ReqAuth);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
            ChessGame.TeamColor parsedPlayerColor;
            if (playerColor.equals("WHITE") && dataAccess.getGame(ReqID).whiteUsername() == null) {
                parsedPlayerColor = ChessGame.TeamColor.WHITE;
            } else if (dataAccess.getGame(ReqID).blackUsername() == null) {
                parsedPlayerColor = ChessGame.TeamColor.BLACK;
            } else {
                throw new DataAccessException("Error: Already taken", 403);
            }
                dataAccess.updateGame(ReqID, parsedPlayerColor, retrievedToken.username());
            return dataAccess.getGame(ReqID);
        }
    }

    // Clear
    public void clearApp() throws DataAccessException {
        try {
            dataAccess.clearAllData();
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage(), e.getStatusCode());
        }
    }

}
