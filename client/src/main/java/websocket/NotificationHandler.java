package websocket;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage notification, boolean error);
    void loadGame(ChessGame inputGame);
}