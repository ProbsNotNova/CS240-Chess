package server.websocket;

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

    public void forfeit(String authToken, int gameID, Session session) {

    }

    public void move(String authToken, int gameID, ChessMove move, Session session) throws MessageException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

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



   ///
    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(String authToken, Session session) throws IOException {
        int gameID = 1; // ** temp variable ** //
        connections.add(session, gameID);
        Server server = new Server();
        // line above is due to idea for forming message, which should
        // send the user's name, not their authToken. The AuthToken can
        // be used to retrieve the user data though unless better option available.
        var message = String.format("%s is in the shop", authToken);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        Collection<Session> includeSessions = new ArrayList<>();
        includeSessions.add(session);
        connections.broadcast(includeSessions, serverMessage);
    }

    private void exit(String authToken, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var serverMessage = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        int gameID = 1; // ** temp variable ** //
        connections.remove(session, gameID);
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