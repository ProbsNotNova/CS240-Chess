package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public interface MySqlDataAccess {
    int insertData(Connection conn, String name, String type) throws DataAccessException {

        try (var preparedStatement = conn.prepareStatement("INSERT INTO pet (name, type) VALUES(?, ?)", RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, type);

            preparedStatement.executeUpdate();

            var resultSet = preparedStatement.getGeneratedKeys();
            var ID = 0;
            if (resultSet.next()) {
                ID = resultSet.getInt(1);
            }

            return ID;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to insert data", ex, ex.getErrorCode());
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
//
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
//
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