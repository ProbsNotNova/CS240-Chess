package websocket;

import com.google.gson.Gson;

import jakarta.websocket.*;
import model.SessionInfo;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws MessageException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connectToGame (String authToken, int gameID, String username, String teamColor) throws MessageException {
        try {
            sessionInfo = new SessionInfo(gameID, username, teamColor, session);
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, sessionInfo, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

    public void leavePetShop(String visitorName) throws MessageException {
        try {
            var action = new Action(Action.Type.EXIT, visitorName);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new MessageException(ex.getMessage(), 500);
        }
    }

}
