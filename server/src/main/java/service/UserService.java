package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    // logout
    public void logout(String logoutRequest) throws DataAccessException {
        AuthData retrievedToken = dataAccess.getAuth(logoutRequest);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
            dataAccess.deleteAuth(logoutRequest);
        }
    }
    public Collection<GameData> listGames(String listGamesRequest) throws DataAccessException {
        AuthData retrievedToken = dataAccess.getAuth(listGamesRequest);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
                return dataAccess.listGames();
        }
    }

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

    public GameData joinGame(String joinGameReqAuth, String playerColor, int joinGameReqID) throws DataAccessException {
        if (joinGameReqAuth == null || joinGameReqID <= 0 || playerColor == null || !(playerColor.equals("WHITE") || playerColor.equals("BLACK"))) { // need better bad player cases
            throw new DataAccessException("Error: bad request", 400);
        }
        AuthData retrievedToken = dataAccess.getAuth(joinGameReqAuth);
        if (retrievedToken == null) {
            // UnauthorizedException
            throw new DataAccessException("Error: Unauthorized", 401);
        } else {
            ChessGame.TeamColor parsedPlayerColor;
            if (playerColor.equals("WHITE") && dataAccess.getGame(joinGameReqID).whiteUsername() == null) {
                parsedPlayerColor = ChessGame.TeamColor.WHITE;
            } else if (dataAccess.getGame(joinGameReqID).blackUsername() == null) {
                parsedPlayerColor = ChessGame.TeamColor.BLACK;
            } else {
                throw new DataAccessException("Error: Already taken", 403);
            }
                dataAccess.updateGame(joinGameReqID, parsedPlayerColor, retrievedToken.username());
            return dataAccess.getGame(joinGameReqID);
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

//public class UserService {
//
//    private final DataAccess dataAccess;
//
//    public ChessService(DataAccess dataAccess) {
//        this.dataAccess = dataAccess;
//    }
//
//    // Pet Shop is very simple.
//    // A more complicated application would do the business logic in the service.
//
//    public Pet addPet(Pet pet) throws DataAccessException {
//        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
//            throw new DataAccessException(DataAccessException.Code.ClientError, "Error: no dogs with fleas");
//        }
//        return dataAccess.addPet(pet);
//    }
//
//    public PetList listPets() throws DataAccessException {
//        return dataAccess.listPets();
//    }
//
//    public Pet getPet(int id) throws DataAccessException {
//        validateId(id);
//        return dataAccess.getPet(id);
//    }
//
//    public void deletePet(Integer id) throws DataAccessException {
//        validateId(id);
//        dataAccess.deletePet(id);
//    }
//
//    public void deleteAllPets() throws DataAccessException {
//        Collection<Pet> pets = dataAccess.listPets();
//        if (!pets.isEmpty()) {
//            dataAccess.deleteAllPets();
//        }
//    }

//    private void validateId(int id) throws DataAccessException {
//        if (id <= 0) {
//            throw new DataAccessException(DataAccessException.Code.ClientError, "Error: invalid pet ID");
//        }
//    }
//}