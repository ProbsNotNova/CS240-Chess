package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String errorMessage;
    ServerMessage.ServerMessageType ERROR;
    public ErrorMessage(ServerMessage.ServerMessageType err, String errorMessage) {
        this.ERROR = err;
        this.errorMessage = errorMessage;
    }
}
