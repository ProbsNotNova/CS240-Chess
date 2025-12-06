package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import model.SessionInfo;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final HashMap<String, SessionInfo> authInfo = new HashMap<>();
    private final SqlDataAccess sqlDataAccess = new SqlDataAccess();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }


    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            /// move keeps coming in as null here for some reason. is it not ChessMove????

            // Trying here, otherwise only in Enter, or in all methods individually ew
            if (sqlDataAccess.getAuth(userGameCommand.getAuthToken()) == null) {
                var serverMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid Auth");
                connections.rootBroadcast(ctx.session, serverMessage);
                return;
            } else if (userGameCommand.getGameID() > sqlDataAccess.listGames().size()) {
                var serverMessage = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid GameID");
                connections.rootBroadcast(ctx.session, serverMessage);
                return;
            }

            switch (userGameCommand.getCommandType()) {
                case CONNECT -> enter(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
                case MAKE_MOVE -> move(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.makeMove(), ctx.session);
                case LEAVE -> exit(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
                case RESIGN -> forfeit(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    ///
    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    //Connect command method maybe both player and observer
    private void enter(String authToken, int gameID, Session session) throws MessageException {
        try {
            SessionInfo sessionInfo;
            if (sqlDataAccess.getGame(gameID).whiteUsername().equals(sqlDataAccess.getAuth(authToken).username())) {
                sessionInfo = new SessionInfo(gameID, sqlDataAccess.getAuth(authToken).username(), "WHITE", session);
            } else if (sqlDataAccess.getGame(gameID).blackUsername().equals(sqlDataAccess.getAuth(authToken).username())) {
                sessionInfo = new SessionInfo(gameID, sqlDataAccess.getAuth(authToken).username(), "BLACK", session);
            } else {
                sessionInfo = new SessionInfo(gameID, sqlDataAccess.getAuth(authToken).username(), "OBSERVE", session);
            }
            authInfo.put(authToken, sessionInfo);
            connections.add(gameID, sessionInfo);

            // Message Root Client LOADGAME
            var ldGmMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, sqlDataAccess.getGame(gameID).game());
            connections.rootBroadcast(session, ldGmMsg);

            // Message Others Notification
            var message = String.format("%s joined as %s", sessionInfo.username(), sessionInfo.teamColor());
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(session, gameID, serverMessage);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    // make move command method
    public void move(String authToken, int gameID, ChessMove move, Session session) throws MessageException, IOException {
        try {
            if (!sqlDataAccess.getGame(gameID).game().getGameOver()) {
                ChessGame updatedGame = sqlDataAccess.getGame(gameID).game();
                updatedGame.makeMove(move);
                sqlDataAccess.updateChessGame(updatedGame, gameID);

                // Message All LOADGAME
                var ldGmMsg =
                        new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, sqlDataAccess.getGame(gameID).game());
                connections.broadcast(null, gameID, ldGmMsg);

                // Message Others Notification
                ChessPiece mvdPc = sqlDataAccess.getGame(gameID).game().getBoard().getPiece(move.getEndPosition());
                var message =
                        String.format("%s moved %s from %s to %s", authInfo.get(authToken).username(), mvdPc, move.getStartPosition(), move.getEndPosition());
                var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(session, gameID, serverMessage);

                // Message All for Check Checkmate Stalemate
                checkMateStaleCheck(gameID, session);

            } else {
                var svrMsg = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: Game Over You Lost!");
                connections.rootBroadcast(session, svrMsg);
            }
        } catch (InvalidMoveException ex) {
            var svrMsg = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: Game Over You Lost!");
            connections.rootBroadcast(session, svrMsg);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }

    }

    // resign command method
    public void forfeit(String authToken, int gameID, Session session) throws MessageException {
        try {
            // mark game as over
//            sqlDataAccess.getGame(gameID).game().setGameOver();
            ChessGame updatedGame = sqlDataAccess.getGame(gameID).game();
            updatedGame.setGameOver();
            sqlDataAccess.updateChessGame(updatedGame, gameID);
            // update game in database accordingly???????????????


            var message = String.format("%s resigned the game", authInfo.get(authToken).username());
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(null, gameID, serverMessage);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    // leave command method for both player and observer
    private void exit(String authToken, int gameID, Session session) throws MessageException {
        try {
        if (authInfo.get(authToken).teamColor().equals("WHITE")) {
            sqlDataAccess.updateGame(gameID, ChessGame.TeamColor.WHITE, null);
        } else {
            sqlDataAccess.updateGame(gameID, ChessGame.TeamColor.BLACK, null);
        }

        var message = String.format("%s left the game", authInfo.get(authToken).username());
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, gameID, serverMessage);

        connections.remove(gameID, authInfo.get(authToken));
        authInfo.remove(authToken);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    // Check, Checkmate, Stalemate Notifications
    private void checkMateStaleCheck(int gameID, Session session) throws DataAccessException, IOException {
        String player1 = "";
        String player2 = "";
        String condition = "";

        if (sqlDataAccess.getGame(gameID).game().isInCheck(ChessGame.TeamColor.WHITE)) {
            player1 = sqlDataAccess.getGame(gameID).blackUsername();
            player2 = sqlDataAccess.getGame(gameID).whiteUsername();
            condition = "Checked";
        } else if (sqlDataAccess.getGame(gameID).game().isInCheck(ChessGame.TeamColor.BLACK)) {
            player1 = sqlDataAccess.getGame(gameID).whiteUsername();
            player2 = sqlDataAccess.getGame(gameID).blackUsername();
            condition = "Checked";
        }
        if (sqlDataAccess.getGame(gameID).game().isInCheckmate(ChessGame.TeamColor.WHITE) ||
            sqlDataAccess.getGame(gameID).game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
            condition = "Checkmated";
//            ChessGame updatedGame = sqlDataAccess.getGame(gameID).game();
//            updatedGame.setGameOver();
//            sqlDataAccess.updateChessGame(updatedGame, gameID);

        }
        if (sqlDataAccess.getGame(gameID).game().isInStalemate(ChessGame.TeamColor.WHITE) ||
            sqlDataAccess.getGame(gameID).game().isInStalemate(ChessGame.TeamColor.BLACK)) {
            player1 = "This game";
            player2 = "Stalemate";
            condition = "reached";
//            ChessGame updatedGame = sqlDataAccess.getGame(gameID).game();
//            updatedGame.setGameOver();
//            sqlDataAccess.updateChessGame(updatedGame, gameID);
        }
        if (player1.isEmpty()) {
            return;
        }
        if (session.isOpen()) {
            var message = String.format("%s has %s %s", player1, condition, player2);
            var svrMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(null, gameID, svrMsg);
        }
//        ChessGame updatedGame = sqlDataAccess.getGame(gameID).game();
//        updatedGame.setGameOver();
//        sqlDataAccess.updateChessGame(updatedGame, gameID);
    }
}