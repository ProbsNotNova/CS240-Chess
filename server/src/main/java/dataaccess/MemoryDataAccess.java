package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
//    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();
    final private HashMap<String, UserData> users = new HashMap<>();

    public UserData getUser(String username) {
        if (users.containsKey(username)) {
            return users.get(username);
        }
        return null;
    }
    public void createUser(UserData registerRequest) {
        users.put(registerRequest.username(), registerRequest);
    }
    public void createAuth(AuthData newAuthToken) {
        authTokens.put(newAuthToken.username(), newAuthToken);
    }



    // DB Clear Methods
    public void clearUserData() {
        users.clear();
    }
    public void clearAuthData() {
        authTokens.clear();
    }
    public void clearGameData() {
        games.clear();
    }
    //
//    public GameData createGame(ChessGame game) {
//        game = new Pet(nextId++, pet.name(), pet.type());
//
//        games.put(game.id(), game);
//        return game;
//    }
//
//    public DataAccess listPets() {
//        return new DataAccess(pets.values());
//    }
//
//
//    public DataAccess getGame(int id) {
//        return games.get(id);
//    }
//    public AuthData getAuthToken(int id) {
//        return authTokens.get(id);
//    }
//    public UserData getUser(int id) {
//        return users.get(id);
//    }

//    public void deletePet(Integer id) {
//        pets.remove(id);
//    }

    public void deleteAllData() {
        games.clear();
        authTokens.clear();
        users.clear();

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
