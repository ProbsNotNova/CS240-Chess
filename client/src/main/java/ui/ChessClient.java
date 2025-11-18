package ui;

import backend.ServerFacade;
import com.google.gson.Gson;
import model.GameData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class ChessClient {
    private String visitorName = null;
    private State state = State.SIGNEDOUT;
    private String sessionAuth = null;
    public String currentPlayerColor = null;


    private final ServerFacade server = new ServerFacade();
    private final BoardPrinter bdPrint = new BoardPrinter();
    //    private final WebSocketFacade ws;


//    public ChessClient(String serverUrl) throws ResponseException {
//        server = new ServerFacade(serverUrl);
//        ws = new WebSocketFacade(serverUrl, this);
//    }

//    public void notify(Notification notification) {
//        System.out.println(RED + notification.message());
//        printPrompt();
//    }
    // E definition of REPL loop
    public String eval(String input) throws IOException {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (state == State.SIGNEDOUT) {
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                case "help" -> help();
                default -> help();
            };
        }
        return switch (cmd) {
            case "create" -> createGame(params);
            case "list" -> listGames(params); // ASK TA for help on game joins staying despite logout or quitting.
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "logout" -> logout();
            case "help" -> help();
            default -> help();
        };
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - login <username> <password>
                    - register <username> <password> <email>
                    - help - with possible commands
                    - quit - the application
                    """;
        }
        return """
                - create <gameName> - a game
                - list - games
                - join <ID> [WHITE|BLACK]
                - observe <ID> - a game
                - logout - when you are done
                - help - with possible commands
                """;
    }

    /// SIGNEDOUT State Methods
    public String register(String... params) throws IOException {
        if (params.length != 3) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        String auth = server.register(params[0], params[1], params[2]);
        state = State.SIGNEDIN;
        visitorName = params[0];
        sessionAuth = auth;
        return "Registered Successfully";
    }
    public String login(String... params) throws IOException {
        if (params.length != 2) {
            throw new IOException("Invalid Parameters, Check help()");
        }

        String auth = server.login(params[0], params[1]);
        state = State.SIGNEDIN;
        visitorName = params[0];
        sessionAuth = auth;
        return String.format("Signed in as %s", visitorName);
    }

    /// SIGNEDIN State Methods
    public String createGame(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 1) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        int gameID = server.createGame(params[0], sessionAuth);
        return String.format("Created game named %s with ID %s", params[0], gameID);
    }
    public String listGames(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        Collection<GameData> games = server.listGames(sessionAuth);
        var result = new StringBuilder();
        for (GameData game : games) {
            result.append("GameID: ").append(game.gameID()).append(" | Gamename: ").append(game.gameName());
            result.append(" | WHITE: ").append(game.whiteUsername()).append(" | BLACK: ").append(game.blackUsername()).append('\n');
        }
        return result.toString();
    }
    public String joinGame(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 2) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        GameData game = server.joinGame(params[1], parseInt(params[0]), sessionAuth);
        currentPlayerColor = params[1];
        bdPrint.printBoard(params[1], null);
        return String.format("Joined game %s as %s team", game.gameName(), params[1]);
    }
    public String observeGame(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 1) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        Collection<GameData> games = server.listGames(sessionAuth);
        GameData game = (GameData) games.toArray()[parseInt(params[0])-1];
        bdPrint.printBoard("WHITE", game.game().getBoard());
        return String.format("Observing game with ID %s", params[0]);
    }
    public String logout(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        server.logout(sessionAuth);
        state = State.SIGNEDOUT;
        visitorName = null;
        return "Logging Out";
    }

    private void assertSignedIn() throws IOException {
        if (state == State.SIGNEDOUT) {
            throw new IOException("You must login or register");
        }
    }
    public void clear() throws IOException {
        server.clear();
    }
}