package server.websocket;


/**
 * Indicates there was an error connecting to the database
 */
public class MessageException extends Exception{
    private final int statusCode;


    public MessageException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public MessageException(String message, Throwable ex, int statusCode) {
        super(message, ex);
        this.statusCode = statusCode;
    }
    public MessageException(String message, Throwable ex) {
        super(message, ex);
        this.statusCode = -1;
    }


    public int getStatusCode() {
        return this.statusCode;
    }

}