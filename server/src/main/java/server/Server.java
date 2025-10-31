package server;
import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
//import server.websocket.WebSocketHandler;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.UserService;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Server {
    private final UserService service;

    private final Javalin javalin;

    private void exceptionHandler(DataAccessException ex, Context ctx) {
        ctx.status(ex.getStatusCode());
        ctx.result(new Gson().toJson(Map.of("message",ex.getMessage())));
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        this.service = new UserService(new MemoryDataAccess());
        // Register your endpoints and exception handlers here.
        javalin.post("/user", this::RegisterHandler);
        // ADD SAME FOR EACH OTHER ENDPOINT FROM DIAGRAM
        javalin.post("/session", this::LoginHandler);
        javalin.delete("/session", this::LogoutHandler);
        javalin.get("/game", this::ListGamesHandler);
        javalin.post("/game", this::CreateGameHandler);
        javalin.put("/game", this::JoinGameHandler);
        javalin.delete("/db", this::ClearDataBaseHandler);
    }

    // Register Endpoint handler
    public void RegisterHandler(Context context) {
        var userInput = new Gson().fromJson(context.body(), UserData.class);
        try {
            context.result(new Gson().toJson(service.register(userInput)));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    public void LoginHandler( Context context) {
        var userInput = new Gson().fromJson(context.body(), UserData.class);
        try {
            context.result(new Gson().toJson(service.login(userInput)));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    public void LogoutHandler( Context context) {
        var userInput = new Gson().fromJson(context.header("authorization"), String.class);
        try {
            service.logout(userInput);
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    public void ListGamesHandler( Context context) {
        var userInput = new Gson().fromJson(context.header("authorization"), String.class);
        try {
            context.result(new Gson().toJson(new listGamesResult(service.listGames(userInput))));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    public void CreateGameHandler( Context context) {
        var userInputAuth = new Gson().fromJson(context.header("authorization"), String.class);
        createGameRequest gameRequest = new Gson().fromJson(context.body(), createGameRequest.class);
        try {
            context.result(new Gson().toJson(service.createGame(userInputAuth, gameRequest.gameName())));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    public void JoinGameHandler( Context context) {
        var userInputAuth = new Gson().fromJson(context.header("authorization"), String.class);
        joinGameRequest gameRequest = new Gson().fromJson(context.body(), joinGameRequest.class);
        try {
            context.result(new Gson().toJson(service.joinGame(userInputAuth, gameRequest.playerColor(), gameRequest.gameID())));
        } catch (DataAccessException e) {
            exceptionHandler(e, context); // some kind of syntax error for JSON comes up
        }
    }

    // Clear Database Handler
    public void ClearDataBaseHandler(Context context) {
        try {
            service.clearApp();
        } catch (DataAccessException e) {
            exceptionHandler(e, context);
        }
    }





    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}




//
//public class Server {
//    private final UserService service;
////    private final WebSocketHandler webSocketHandler;
//    private final Javalin httpHandler;
//
//    public Server() {
//        this(new UserService(new MemoryDataAccess()));
//    }
//
//    public Server(UserService service) {
//        this.service = service;
//
////        webSocketHandler = new WebSocketHandler();
//
//        httpHandler = Javalin.create(config -> config.staticFiles.add("public"))
//                .post("/pet", this::addPet)
//                .get("/pet", this::listPets)
//                .delete("/pet/{id}", this::deletePet)
//                .delete("/pet", this::deleteAllPets)
//                .exception(DataAccessException, this::exceptionHandler);
////                .ws("/ws", ws -> {
////                    ws.onConnect(webSocketHandler);
////                    ws.onMessage(webSocketHandler);
////                    ws.onClose(webSocketHandler);
////                });
//    }
//
//    public PetServer run(int port) {
//        httpHandler.start(port);
//        return this;
//    }
//
//    public int port() {
//        return httpHandler.port();
//    }
//
//    public void stop() {
//        httpHandler.stop();
//    }
//
//    private void exceptionHandler(ResponseException ex, Context ctx) {
//        ctx.status(ex.toHttpStatusCode());
//        ctx.result(ex.toJson());
//    }
//
//    private void addPet(Context ctx) throws ResponseException {
//        Pet pet = new Gson().fromJson(ctx.body(), Pet.class);
//        pet = service.addPet(pet);
////        webSocketHandler.makeNoise(pet.name(), pet.sound());
//        ctx.result(new Gson().toJson(pet));
//    }
//
//    private void listPets(Context ctx) throws ResponseException {
//        ctx.result(service.listPets().toString());
//    }
//
//    private void deletePet(Context ctx) throws ResponseException {
//        var id = Integer.parseInt(ctx.pathParam("id"));
//        Pet pet = service.getPet(id);
//        if (pet != null) {
//            service.deletePet(id);
////            webSocketHandler.makeNoise(pet.name(), pet.sound());
//            ctx.status(204);
//        } else {
//            ctx.status(404);
//        }
//    }
//
//    private void deleteAllPets(Context ctx) throws ResponseException {
//        service.deleteAllPets();
//        ctx.status(204);
//    }
//}


// COMPLETE LISTING OF SERVER CODE FOR HOSTING STATIC FILES AND
// name list  SERVICE ENDPOINTS
//    import com.google.gson.Gson;
//import io.javalin.Javalin;
//import io.javalin.http.Context;
//
//import java.util.ArrayList;
//import java.util.Map;
//
//public class SimpleNameServer {
//    private ArrayList<String> names = new ArrayList<>();
//
//    public static void main(String[] args) {
//        new SimpleNameServer().run();
//    }
//
//    private void run() {
//        Javalin.create(config -> config.staticFiles.add("web"))
//                .post("/name/{name}", this::addName)
//                .get("/name", this::listNames)
//                .delete("/name/{name}", this::deleteName)
//                .start(8080);
//    }
//
//    private void addName(Context ctx) {
//        if (authorized(ctx)) {
//            names.add(ctx.pathParam("name"));
//            listNames(ctx);
//        }
//    }
//
//    private void listNames(Context ctx) {
//        ctx.contentType("application/json");
//        ctx.result(new Gson().toJson(Map.of("name", names)));
//    }
//
//
//    private void deleteName(Context ctx) {
//        if (authorized(ctx)) {
//            names.remove(ctx.pathParam("name"));
//            listNames(ctx);
//        }
//    }
//
//    final private HashSet<String> validTokens = new HashSet<>(Set.of("secret1", "secret2"));
//
//    private boolean authorized(Context ctx) {
//        String authToken = ctx.header("Authorization");
//        if (!validTokens.contains(authToken)) {
//            ctx.contentType("application/json");
//            ctx.status(401);
//            ctx.result(new Gson().toJson(Map.of("msg", "invalid authorization")));
//            return false;
//        }
//        return true;
//    }
//}

// SERIALIZING REQUESTS AND RESPONSES
// JSON and GSON example of server with echo endpoint that
// parses request body into Java Map object and then serializes
// it back into the end point response
//import com.google.gson.Gson;
//import io.javalin.Javalin;
//import io.javalin.http.Context;
//
//import java.util.Map;
//
//public class EchoJsonServer {
//    public static void main(String[] args) {
//        new EchoJsonServer().run();
//    }
//
//    private void run() {
//        Javalin.create()
//                .post("/echo", this::echo)
//                .start(8080);
//    }
//
//    private void echo(Context context) {
//        // Convert body json to object
//        Map bodyObject = getBodyObject(context, Map.class);
//
//        // Convert bodyObject back to json and send to client
//        String json = new Gson().toJson(bodyObject);
//        context.json(json);
//    }
//
//    private static <T> T getBodyObject(Context context, Class<T> clazz) {
//        var bodyObject = new Gson().fromJson(context.body(), clazz);
//
//        if (bodyObject == null) {
//            throw new RuntimeException("missing required body");
//        }
//
//        return bodyObject;
//    }
//}

// HttpClient class from standard JDK java.net for client
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Locale;
//
//public class SimpleNameClient {
//    // Create an HttpClient for making requests
//    // This should be long-lived and shared, so a static final field is good here
//    private static final HttpClient httpClient = HttpClient.newHttpClient();
//
//    public static void main(String[] args) throws Exception {
//        new SimpleNameClient().get("localhost", 8080, "/name");
//    }
//
//    private void get(String host, int port, String path) throws Exception {
//        String urlString = String.format(Locale.getDefault(), "http://%s:%d%s", host, port, path);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI(urlString))
//                .timeout(java.time.Duration.ofMillis(5000))
//                .GET()
//                .build();
//
//        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
//            System.out.println(httpResponse.body());
//        } else {
//            System.out.println("Error: received status code " + httpResponse.statusCode());
//        }
//    }
//}

// USING HTTP HEADERS ON THE CLIENT
//var request = HttpRequest.newBuilder(uri)
//    .GET()
//    .header("Authorization", "secret1")
//    .build();
//
//var response = client.send(request, BodyHandlers.ofString());
//var headers = response.headers();
//OptionalLong length = response.firstValueAsLong("Content-Length");
//Optional<String> type = response.firstValue("Content-Type");

     //BODY HANDLERS AND PUBLISHERS
// OTHER HANDLER:
//var request = HttpRequest.newBuilder(uri)
//    .GET()
//    .build();
//
//var response = client.send(request, BodyHandlers.ofInputStream());
//InputStream body = response.body();

// OTHER PUBLISHER:  this one uses the class with factory functions
// var body = Map.of("name", "joe", "type", "cat");
// var jsonBody = new Gson().toJson(body);
//
// var request = HttpRequest.newBuilder(uri)
//    .POST(BodyPublishers.ofString(jsonBody))
//    .header("Content-Type", "application/json")
//    .build();

//THINGS TO UNDERSTAND
//1.  Writing the main Server class
//2.  Writing HTTP handlers for GET and POST requests
//3.  Implementing the Test Web Page using a FileHandler
//4.  Writing a web client
//5.  Server and client code examples



//import chess.*;
//
//public class Main {
//    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
//    }
//}



// OTHER EXAMPLE CODE CHUNKS
//    private void run() {
//    ...
//        Javalin.create()
//                .post("/name/{name}", new Handler() {
//                    public void handle(Context context) throws Exception {
//                        names.add(context.pathParam("name"));
//                        listNames(context);
//                    }
//                })
//    ...
//    }
//
//    private void listNames(Context context) {
//        String jsonNames = new Gson().toJson(Map.of("name", names));
//
//        context.contentType("application/json");
//        context.result(jsonNames);
//    }

// take above and simplify to below
//simplify the representation of our post handler in two ways:
// 1) by using a lambda function to call a method that implements
// the addName endpoint, and
// 2) by calling context.json(...) in the listNames method, which
// will set the Content-Type to application/json and set the response body.

//    private void run() {
//    ...
//    Javalin.create()
//        .post("/name{name}", context -> addName(context))  ***************
//    ...
//}
//
//private void addName(Context context) {
//    names.add(context.pathParam("name"));
//    listNames(context);
//}
//
//private void listNames(Context context) {
//    String jsonNames = new Gson().toJson(Map.of("name", names));
//    context.json(jsonNames);
//}

//Finally, since our lambda function is simply a passthrough to another
// function, we can replace it with the Java method reference syntax.
//    Javalin.create()
//    .post("/name{name}", this::addName) ****************

//    final private HashSet<String> validTokens = new HashSet<>(Set.of("secret1", "secret2"));
//
// pass info using HTTP headers with status codes for when not valid
//private boolean authorized(Context ctx) {
//    String authToken = ctx.header("authorization");
//    if (!validTokens.contains(authToken)) {
//        ctx.contentType("application/json");
//        ctx.status(401);
//        ctx.result(new Gson().toJson(Map.of("msg", "invalid authorization")));
//        return false;
//    }
//    return true;
//}
// wrapped secure endpoints with auth test
//    private void addName(Context ctx) {
//    if (authorized(ctx)) {
//        names.add(ctx.pathParam("name"));
//        listNames(ctx);
//    }
//}

// Serving static files
//Javalin javalinServer = Javalin.create(config -> config.staticFiles.add("web"));
// by using above: your server you can now make a request to the server
// with a path like /index.html and it will return the index.html file
// found in a directory named web that is found in a parent directory on
// your application's Classpath. In Intellij, the parent directory is
// typically any directory marked as a Rousources Root.