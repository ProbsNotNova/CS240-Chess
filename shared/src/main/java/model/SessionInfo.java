package model;


import org.eclipse.jetty.server.session.Session;

public record SessionInfo(int gameID, String username, String teamColor, Session savedSession) {
}
