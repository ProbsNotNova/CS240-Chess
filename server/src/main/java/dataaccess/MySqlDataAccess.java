package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlDataAccess implements DataAccess {
    private int assignedGameID = 1;

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    int insertData(Object type, ArrayList<String> vals) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            if (type.equals(UserData.class)) {
                try (var preparedStatement = conn.prepareStatement("INSERT INTO  (username, password, email) VALUES(?, ?, ?)", RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, vals.get(0));
                    preparedStatement.setString(2, vals.get(1));
                    preparedStatement.setString(3, vals.get(2));

                    preparedStatement.executeUpdate();

                    //                var resultSet = preparedStatement.getGeneratedKeys();
                    //                var ID = 0;
                    //                if (resultSet.next()) {
                    //                    ID = resultSet.getInt(1);
                    //                }

                    return getUser();
                }
            } else if (type.equals(AuthData.class)) {
                try (var preparedStatement = conn.prepareStatement("INSERT INTO  (username, authToken) VALUES(?, ?)", RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, vals.get(0));
                    preparedStatement.setString(2, vals.get(1)); // authToken

                    preparedStatement.executeUpdate();

                    var resultSet = preparedStatement.getGeneratedKeys();
                    var ID = 0;
                    if (resultSet.next()) {
                        ID = resultSet.getInt(2);
                    }

                    return ID;
                }
            } else if (type.equals(GameData.class)) {
                try (var preparedStatement = conn.prepareStatement("INSERT INTO  (gameID, whiteUsername, blackUsername, gameName, game) VALUES(?, ?, ?, ?, ?)", RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, vals.get(0));
                    preparedStatement.setString(2, vals.get(1));
                    preparedStatement.setString(3, vals.get(2));
                    preparedStatement.setString(4, vals.get(3));
                    preparedStatement.setString(5, vals.get(4));

                    preparedStatement.executeUpdate();

                    var resultSet = preparedStatement.getGeneratedKeys();
                    var ID = 0;
                    if (resultSet.next()) {
                        ID = resultSet.getInt(1);
                    }

                    return ID;
                }
            } else {
                throw new DataAccessException("Error: Invalid type, Bad Request", 400);
            }
        } catch (SQLException|DataAccessException ex) {
            throw new DataAccessException("failed to insert data", ex, ex.getErrorCode());
        }
    }
/// MODIFY THIS INTO CHESS ONE AND PULL FROM INSERTDATA CURRENT CODE
    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    void updateData(Connection conn, int petID, String name) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("UPDATE pet SET name=? WHERE id=?")) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, petID);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to update data", ex, ex.getErrorCode());

        }
    }

    void deleteData(Connection conn, int petID) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("DELETE FROM pet WHERE id=?")) {
            preparedStatement.setInt(1, petID);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete data", ex, ex.getErrorCode());

        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor parsedPlayerColor, String user) throws DataAccessException {

    }

    @Override
    public void createUser(UserData registerRequest) throws DataAccessException {
        ArrayList<String> vals = new ArrayList<>();
        vals.add(registerRequest.username());
        vals.add(registerRequest.password());
        vals.add(registerRequest.email());
        insertData(DatabaseManager.getConnection(), registerRequest, vals);
    }

    @Override
    public void createAuth(AuthData newAuthToken) throws DataAccessException {
        ArrayList<String> vals = new ArrayList<>();
        vals.add(newAuthToken.username());
        vals.add(newAuthToken.authToken());
        insertData(DatabaseManager.getConnection(), newAuthToken, vals);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        GameData newGame = new GameData(assignedGameID, null, null, gameName, new ChessGame());
        ArrayList<String> vals = new ArrayList<>();
        vals.add(String.valueOf(newGame.gameID()));
        vals.add(newGame.whiteUsername());
        vals.add(newGame.blackUsername());
        vals.add(newGame.gameName());
        vals.add(new Gson().toJson(newGame.game()));
        insertData(DatabaseManager.getConnection(), newGame, vals);
        assignedGameID++;
        return 0;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clearAllData() throws DataAccessException {

        assignedGameID = 1;
    }


    void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var createDbStatement = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS chess");
            createDbStatement.executeUpdate();

            conn.setCatalog("chess");

            var createUserTable = """
            CREATE TABLE  IF NOT EXISTS user (
                username VARCHAR(255) NOT NULL PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
            )""";
            var createAuthTable = """
            CREATE TABLE  IF NOT EXISTS auth (
                username VARCHAR(255) NOT NULL,
                authToken VARCHAR(255) NOT NULL PRIMARY KEY,
            )""";
            var createGameTable = """
            CREATE TABLE  IF NOT EXISTS game (
                gameID INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                whiteUsername VARCHAR(255) NOT NULL,
                blackUsername VARCHAR(255) NOT NULL,
                gameName VARCHAR(255) NOT NULL,
                gameJson TEXT NOT NULL
            )""";


            try (var createTableStatement = conn.prepareStatement(createUserTable)) {
                createTableStatement.executeUpdate();
            }
            try (var createTableStatement = conn.prepareStatement(createAuthTable)) {
                createTableStatement.executeUpdate();
            }
            try (var createTableStatement = conn.prepareStatement(createGameTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to configure database", ex, ex.getErrorCode());
        }
    }
}


//package dataaccess;
//
//import com.google.gson.Gson;
//import exception.ResponseException;
//import model.*;
//
//import java.sql.*;
//
//import static java.sql.Statement.RETURN_GENERATED_KEYS;
//import static java.sql.Types.NULL;
//
//
//public class MySqlDataAccess implements DataAccess {
//
//    public MySqlDataAccess() throws ResponseException {
//        configureDatabase();
//    }
/// /////////// IMPORTANT
//    public Pet addPet(Pet pet) throws ResponseException {
//        var statement = "INSERT INTO pet (name, type, json) VALUES (?, ?, ?)";
//        String json = new Gson().toJson(pet);
//        int id = executeUpdate(statement, pet.name(), pet.type(), json);
//        return new Pet(id, pet.name(), pet.type());
//    }
//
//    public Pet getPet(int id) throws ResponseException {
//        try (Connection conn = DatabaseManager.getConnection()) {
//            var statement = "SELECT id, json FROM pet WHERE id=?";
//            try (PreparedStatement ps = conn.prepareStatement(statement)) {
//                ps.setInt(1, id);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        return readPet(rs);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to read data: %s", e.getMessage()));
//        }
//        return null;
//    }
//
//    public PetList listPets() throws ResponseException {
//        var result = new PetList();
//        try (Connection conn = DatabaseManager.getConnection()) {
//            var statement = "SELECT id, json FROM pet";
//            try (PreparedStatement ps = conn.prepareStatement(statement)) {
//                try (ResultSet rs = ps.executeQuery()) {
//                    while (rs.next()) {
//                        result.add(readPet(rs));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to read data: %s", e.getMessage()));
//        }
//        return result;
//    }
//
//    public void deletePet(Integer id) throws ResponseException {
//        var statement = "DELETE FROM pet WHERE id=?";
//        executeUpdate(statement, id);
//    }
//
//    public void deleteAllPets() throws ResponseException {
//        var statement = "TRUNCATE pet";
//        executeUpdate(statement);
//    }
//
//    private Pet readPet(ResultSet rs) throws SQLException {
//        var id = rs.getInt("id");
//        var json = rs.getString("json");
//        Pet pet = new Gson().fromJson(json, Pet.class);
//        return pet.setId(id);
//    }
/// /////// IMPORTANT TO LOOK AT
//    private int executeUpdate(String statement, Object... params) throws ResponseException {
//        try (Connection conn = DatabaseManager.getConnection()) {
//            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
//                for (int i = 0; i < params.length; i++) {
//                    Object param = params[i];
//                    if (param instanceof String p) ps.setString(i + 1, p);
//                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
//                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
//                    else if (param == null) ps.setNull(i + 1, NULL);
//                }
//                ps.executeUpdate();
//
//                ResultSet rs = ps.getGeneratedKeys();
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//
//                return 0;
//            }
//        } catch (SQLException e) {
//            throw new ResponseException(ResponseException.Code.ServerError, String.format("unable to update database: %s, %s", statement, e.getMessage()));
//        }
//    }
//
//    private final String[] createStatements = {
//            """
//            CREATE TABLE IF NOT EXISTS  pet (
//              `id` int NOT NULL AUTO_INCREMENT,
//              `name` varchar(256) NOT NULL,
//              `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
//              `json` TEXT DEFAULT NULL,
//              PRIMARY KEY (`id`),
//              INDEX(type),
//              INDEX(name)
//            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
//            """
//    };
//
//
//    private void configureDatabase() throws ResponseException {
//        DatabaseManager.createDatabase();
//        try (Connection conn = DatabaseManager.getConnection()) {
//            for (String statement : createStatements) {
//                try (var preparedStatement = conn.prepareStatement(statement)) {
//                    preparedStatement.executeUpdate();
//                }
//            }
//        } catch (SQLException ex) {
//            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
//        }
//    }
//}