package model;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;

public record SessionInfo(int gameID, String username, String teamColor, Session savedSession) {
}
