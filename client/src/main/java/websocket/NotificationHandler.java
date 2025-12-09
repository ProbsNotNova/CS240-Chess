package websocket;

import chess.ChessGame;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage notification);
    void loadGame(ChessGame inputGame);
    void errNotify(ErrorMessage errorMessage);
}