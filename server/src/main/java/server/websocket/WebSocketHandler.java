package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import server.Server;
import server.websocket.MessageException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (userGameCommand.getCommandType()) {
                case CONNECT -> enter(userGameCommand.getAuthToken(), ctx.session);
                case MAKE_MOVE -> move(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.makeMove(), ctx.session);
                case LEAVE -> exit(userGameCommand.getAuthToken(), ctx.session);
                case RESIGN -> forfeit(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }


    //Connect command method maybe both player and observer
    private void enter(String authToken, Session session) throws IOException {
        int gameID = 1; // ** temp variable ** //
        connections.add(session, gameID);
        Server server = new Server();
        // line above is due to idea for forming message, which should
        // send the user's name, not their authToken. The AuthToken can
        // be used to retrieve the user data though unless better option available.
        var message = String.format("%s joined as %s", authToken, /*ChessGame.TeamColor*/);
        // message alternative for joining as observer. maybe third option for TEAM color
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        Collection<Session> includeSessions = new ArrayList<>();
        includeSessions.add(session);
        connections.broadcast(includeSessions, serverMessage);
    }

    // make move command method
    public void move(String authToken, int gameID, ChessMove move, Session session) throws MessageException {
        try {
            var message = String.format("%s moved %s to %s", playerName, pieceType, move.getEndPosition());
            // message must be sent with corresponding board update
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            Collection<Session> includeSessions = new ArrayList<>();
            includeSessions.add(session);
            connections.broadcast(includeSessions, serverMessage);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
//try {
//            var message = String.format("%s says %s", petName, sound);
//            var serverMessage = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast(null, notification);
//        } catch (Exception ex) {
//            throw new MessageException(ex.getMessage(), 500);
//        }

    }

    // resign command method
    public void forfeit(String authToken, int gameID, Session session) throws MessageException {
        try {
            var message = String.format("%s resigned the game", playerName);
            // message must be sent with corresponding board update
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            Collection<Session> includeSessions = new ArrayList<>();
            includeSessions.add(session);
            connections.broadcast(includeSessions, serverMessage);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    // leave command method
    private void exit(String authToken, Session session) throws IOException {
        var message = String.format("%s left the game", playerName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        Collection<Session> includeSessions = new ArrayList<>();
        includeSessions.add(session);
        connections.broadcast(includeSessions, serverMessage);
        int gameID = 1; // ** temp variable ** //
        connections.remove(session, gameID);
    }

    // Player is in check notif method


    // Player is in checkmate notif method


   ///
    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }



    public void makeNoise(String petName, String sound) throws MessageException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var serverMessage = new Notification(Notification.Type.NOISE, message);
            connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }
}