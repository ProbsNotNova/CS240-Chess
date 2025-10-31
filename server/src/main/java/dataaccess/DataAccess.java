package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
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

    void deleteAuth(String authToken) throws DataAccessException;

    // Clear Methods
    void clearAllData() throws DataAccessException;



//    GameData addData(GameData game) throws DataAccessException;
//
//    Gamelist listGames() throws DataAccessException;
//
//    Pet getPet(int id) throws DataAccessException;
//
//    void deletePet(Integer id) throws DataAccessException;
//
//    void deleteAllData() throws DataAccessException;
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


//package model;
//
//import com.google.gson.*;
//
//public record Pet(int id, String name, PetType type) {
//
//    public String sound() {
//        return switch (type) {
//            case DOG -> "bark";
//            case CAT -> "meow";
//            case FISH -> "bubbles";
//            case FROG -> "ribbit";
//            case BIRD -> "tweet";
//            case RAT -> "squeak";
//            case ROCK -> "roll";
//        };
//    }
//
//    public Pet setId(int id) {
//        return new Pet(id, this.name, this.type);
//    }
//
//    public String toString() {
//        return new Gson().toJson(this);
//    }
//}