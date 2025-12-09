package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String errorMessage;
    ServerMessage.ServerMessageType error;
    public ErrorMessage(ServerMessage.ServerMessageType err, String errorMessage) {
        this.error = err;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
