package server;
import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.SqlDataAccess;
import model.*;
//import server.websocket.WebSocketHandler;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import server.websocket.WebSocketHandler;
import service.UserService;
import io.javalin.*;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Map;

public class Server {
    private UserService service;
    private final WebSocketHandler webSocketHandler;
    private final Javalin javalin;

    // Exception Handler
    private void exceptionHandler(DataAccessException ex, Context ctx) {
        ctx.status(ex.getStatusCode());
        ctx.result(new Gson().toJson(Map.of("message",ex.getMessage())));
    }

    public Server() {
        webSocketHandler = new WebSocketHandler();
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            DatabaseManager.configureDatabase();
            this.service = new UserService(new SqlDataAccess());
        } catch (SQLException e) {
            this.service = new UserService(new MemoryDataAccess());
        }

        // Server Endpoints
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);
        javalin.delete("/db", this::clearDataBaseHandler);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });
    }

    // Server Endpoint Handler Methods
    public void registerHandler(Context context) {
        var userInput = new Gson().fromJson(context.body(), UserData.class);
        try {
            context.result(new Gson().toJson(service.register(userInput)));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void loginHandler( Context context) {
        var userInput = new Gson().fromJson(context.body(), UserData.class);
        try {
            context.result(new Gson().toJson(service.login(userInput)));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void logoutHandler( Context context) {
        var userInput = context.header("authorization");
        try {
            service.logout(userInput);
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void listGamesHandler( Context context) {
        var userInput = context.header("authorization");
        try {
            context.result(new Gson().toJson(new ListGamesResult(service.listGames(userInput))));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void createGameHandler( Context context) {
        var userInputAuth = context.header("authorization");
        CreateGameRequest gameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);
        try {
            context.result(new Gson().toJson(Map.of("gameID", service.createGame(userInputAuth, gameRequest.gameName()))));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void joinGameHandler( Context context) {
        var userInputAuth = context.header("authorization");
        JoinGameRequest gameRequest = new Gson().fromJson(context.body(), JoinGameRequest.class);
        try {
            context.result(new Gson().toJson(service.joinGame(userInputAuth, gameRequest.playerColor(), gameRequest.gameID())));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }
    public void clearDataBaseHandler(Context context) {
        try {
            service.clearApp();
        } catch (DataAccessException e) {
            exceptionHandler(e, context);
        }
    }

    // Server Methods
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
