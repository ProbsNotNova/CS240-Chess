package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
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
                try {
                    dataAccess.createUser(registerRequest);
                    AuthData authToken = new AuthData(registerRequest.username(), generateToken());
                    dataAccess.createAuth(authToken);
                    return authToken;
                } catch (DataAccessException e) {
                    throw new DataAccessException(e.getMessage(), 500);
                }
            }
    }

    // Login
    public AuthData login(UserData loginRequest) throws DataAccessException {
        if ((loginRequest.username() == null) || (loginRequest.password() == null)) {
            // Bad Request Exception
            throw new DataAccessException("Error: bad request", 400);
        } else {
            try {
                UserData retrievedData = dataAccess.getUser(loginRequest.username());
                 if (!loginRequest.password().equals(retrievedData.password())) {
                    // UnauthorizedException
                    throw new DataAccessException("Error: Unauthorized", 401);
                 } else {
                     AuthData authToken = new AuthData(loginRequest.username(), generateToken());
                     dataAccess.createAuth(authToken);
                     return authToken;
                 }
            } catch (DataAccessException e) {
                throw new DataAccessException(e.getMessage(), 500);
            }
        }
    }

    // Clear
    public void clearApp() throws DataAccessException {
        try {
        dataAccess.clearUserData();
        dataAccess.clearAuthData();
        dataAccess.clearGameData();
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage(), 500);
        }
    }


//    public LoginResult login(LoginRequest loginRequest) {
//
//    }
//    public void logout(LogoutRequest logoutRequest) {
//
//    }



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