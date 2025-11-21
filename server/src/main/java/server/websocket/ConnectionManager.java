package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Collection<Session>> connections = new ConcurrentHashMap<>();

    // Create sessionGroup for new gameID
    public void create(int gameID) {
        Collection<Session> sessionGroup = new ArrayList<>();
        connections.put(gameID, sessionGroup);
    }

    // Add a session to the gameID sessionGroup
    public void add(Session session, int gameID) {
        Collection<Session> sessionGroup = connections.get(gameID);
        sessionGroup.add(session);
        connections.put(gameID, sessionGroup);
    }

    // Remove a session from the gameID sessionGroup
    public void remove(Session session, int gameID) {
        Collection<Session> sessionGroup = connections.get(gameID);
        sessionGroup.remove(session);
        connections.put(gameID, sessionGroup);
    }

    // Delete the gameID sessionGroup entirely
    public void delete(int gameID) {
        connections.remove(gameID);
    }

    public void broadcast(Collection<Session> includeSessions, ServerMessage serverMessage) throws IOException {
        String msg = serverMessage.toString();
        for (Collection<Session> sessionGroup : connections.values()) {
            for (Session c : sessionGroup) {
                if (c.isOpen()) {
                    if (includeSessions.contains(c)) {
                        c.getRemote().sendString(msg);
                    }
                }
            }

        }
    }
}