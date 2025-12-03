package server.websocket;

import model.SessionInfo;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Collection<SessionInfo>> connections = new ConcurrentHashMap<>();

//    // Create sessionGroup for new gameID
//    public void create(int gameID) {
//        Collection<Session> sessionGroup = new ArrayList<>();
//        connections.put(gameID, sessionGroup);
//    }

    // Add a session to the gameID sessionGroup
    public void add(int gameID, SessionInfo sessionInfo) {
        Collection<SessionInfo> sessionGroup = new ArrayList<>();
        if (connections.get(gameID) !=null) {
            sessionGroup = connections.get(gameID);
        }
        sessionGroup.add(sessionInfo);
        connections.put(gameID, sessionGroup);
    }

    // Remove a session from the gameID sessionGroup
    public void remove(int gameID, SessionInfo sessionInfo) {
        Collection<SessionInfo> sessionGroup = connections.get(gameID);
        sessionGroup.remove(sessionInfo);
        connections.put(gameID, sessionGroup);
    }

    public void rootBroadcast(Session session, ServerMessage serverMessage) throws IOException {
        String msg = serverMessage.toString();
        if (session.isOpen()) {
            session.getRemote().sendString(msg);
        }
    }


        public void broadcast(Session excludeSession, int gameID, ServerMessage serverMessage) throws IOException {
        String msg = serverMessage.toString();
        for (SessionInfo sessionInfo : connections.get(gameID)) {
                if (sessionInfo.savedSession().isOpen()) {
                    if (!sessionInfo.savedSession().equals(excludeSession)) {
                        sessionInfo.savedSession().getRemote().sendString(msg);
                    }
                }


        }
    }
}