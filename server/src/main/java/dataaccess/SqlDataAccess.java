package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlDataAccess implements DataAccess {

    private int executeUpdate(Connection conn, String statement, Object... params) throws DataAccessException {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof ChessGame p) ps.setString(i + 1, new Gson().toJson(p));
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            } catch (SQLException e) {
            throw new DataAccessException(String.format("Error: unable to update database: %s, %s", statement, e.getMessage()),e, 500);
        }
    }


//    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var nUsername = rs.getString("username");
                    var password = rs.getString("password");
                    var email = rs.getString("email");
                    return new UserData(nUsername, password, email);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get user data", ex, 500);
        }
        return null;
    }

//    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auth WHERE authToken=?";
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var username = rs.getString("username");
                    var nAuthToken = rs.getString("authToken");
                    return new AuthData(username, nAuthToken);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get auth data", ex, 500);
        }
        return null;
    }

    private GameData parseGame(ResultSet rs) throws SQLException {
        var nGameID = rs.getInt("gameID");
        var whiteUser = rs.getString("whiteUsername");
        var blackUser = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var gameJson = rs.getString("gameJson");
        return new GameData(nGameID, whiteUser, blackUser, gameName, new Gson().fromJson(gameJson, ChessGame.class));
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson FROM game WHERE gameID=?";
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseGame(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: failed to get game data", ex, 500);
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {

            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJson FROM game";
            PreparedStatement ps = conn.prepareStatement(statement);
            try (ResultSet rs = ps.executeQuery()) {
                Collection<GameData> gameList = new ArrayList<>();
                while (rs.next()) {
                    gameList.add(parseGame(rs));
                }
                return gameList;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: failed to list game data", ex);
        }
    }

    @Override
    public void updateGame(int gameID, ChessGame.TeamColor parsedPlayerColor, String user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            GameData game = getGame(gameID);
            if (getGame(gameID).whiteUsername() == null && parsedPlayerColor == ChessGame.TeamColor.WHITE) {
                GameData newGame = new GameData(game.gameID(), user, game.blackUsername(), game.gameName(), game.game());
                    executeUpdate(conn, "UPDATE game SET whiteUsername=? WHERE gameID=?", newGame.whiteUsername(), newGame.gameID());
            } else if (getGame(gameID).blackUsername() == null && parsedPlayerColor == ChessGame.TeamColor.BLACK){
                GameData newGame = new GameData(game.gameID(), game.whiteUsername(), user, game.gameName(), game.game());
                executeUpdate(conn, "UPDATE game SET blackUsername=? WHERE gameID=?", newGame.blackUsername(), newGame.gameID());
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: failed to update game", ex);
        }

    }

    @Override
    public void createUser(UserData regReq) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()) {
            executeUpdate(conn, "INSERT INTO user (username, password, email) VALUES(?, ?, ?)", regReq.username(), regReq.password(), regReq.email());
        } catch (SQLException ex) {
        throw new DataAccessException("Error: failed to insert user data", ex, 500);
        }
    }

    @Override
    public void createAuth(AuthData newAuth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            executeUpdate(conn, "INSERT INTO auth (username, authToken) VALUES(?, ?)", newAuth.username(), newAuth.authToken());
        } catch (SQLException|DataAccessException ex) {
            throw new DataAccessException("Error: failed to insert auth data", ex, 500);
        }

    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        GameData newGame = new GameData(1, null, null, gameName, new ChessGame());
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameJson) VALUES(?, ?, ?, ?)";
            return executeUpdate(conn, statement, newGame.whiteUsername(), newGame.blackUsername(), newGame.gameName(), newGame.game());
        } catch (SQLException ex) {
            throw new DataAccessException("Error: failed to insert game data", ex, 500);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            executeUpdate(conn, "DELETE FROM auth WHERE authToken=?", authToken);
        } catch (SQLException ex) {
            throw new DataAccessException("Error: failed to delete auth entry", ex, 500);
        }
    }

    @Override
    public void clearAllData() throws DataAccessException {
        // TRUNCATE ALL TABLES
        try (var conn = DatabaseManager.getConnection()) {
            executeUpdate(conn, "TRUNCATE TABLE user");
            executeUpdate(conn, "TRUNCATE TABLE auth");
            executeUpdate(conn, "TRUNCATE TABLE game");
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to delete auth entry", e, 500);
        }
    }
}