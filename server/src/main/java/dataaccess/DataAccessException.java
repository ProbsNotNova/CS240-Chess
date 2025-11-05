package dataaccess;


/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    private final int statusCode;


    public DataAccessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public DataAccessException(String message, Throwable ex, int statusCode) {
        super(message, ex);
        this.statusCode = statusCode;
    }
    public DataAccessException(String message, Throwable ex) {
        super(message, ex);
        this.statusCode = -1;
    }


    public int getStatusCode() {
        return this.statusCode;
    }

}