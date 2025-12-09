package ui;

import backend.ServerFacade;
import chess.*;
import model.GameData;
import websocket.MessageException;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;

import static java.lang.Integer.parseInt;
import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;

public class ChessClient implements NotificationHandler {
    private String visitorName = null;
    private State state = State.SIGNEDOUT;
    private String sessionAuth = null;
    public String currentPlayerColor = null;
    private int currentGameID = 0;
    private final Map<Integer, Integer> mappedID = new HashMap<>();

    private ServerFacade server = new ServerFacade(8080);
    private final BoardPrinter bdPrint = new BoardPrinter();
    private final WebSocketFacade ws;
    private ChessGame currentGame;

    public ChessClient(int serverPort, String serverUrl) throws IOException, MessageException {
        server = new ServerFacade(serverPort);
        ws = new WebSocketFacade(serverUrl, this);
    }

    public void notify(ServerMessage serverMessage) {
        System.out.println(SET_TEXT_COLOR_BLUE + serverMessage.getMessage());
        printPrompt();
    }

    public void errNotify(ErrorMessage errorMessage) {
        System.out.println(SET_TEXT_COLOR_RED + errorMessage.getErrorMessage());
        printPrompt();
    }

    public void loadGame(ChessGame inputGame) {
        System.out.println();
        bdPrint.printBoard(currentPlayerColor, inputGame, null);
        printPrompt();
        currentGame = inputGame;
    }

    public void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }


    // E definition of REPL loop
    public String eval(String input) throws IOException, MessageException {
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
        } else if (state == State.SIGNEDIN) {
            return switch (cmd) {
                case "create" -> createGame(params);
                case "list" -> listGames(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "help" -> help();
                default -> help();
            };
        } else if (state == State.INGAME) {
            return switch (cmd) {
                case "highlight" -> highlight(params);
                case "move" -> makeMove(params);
                case "redraw" -> redrawBoard();
                case "resign" -> resignGame();
                case "leave" -> leaveGame();
                case "help" -> help();
                default -> help();
            };
        } else if (state == State.OBSERVE) {
            return switch (cmd) {
                case "redraw" -> redrawBoard();
                case "leave" -> leaveGame();
                case "help" -> help();
                default -> help();
            };
        }
        return "FATAL ERROR";
    }

    public String help() {
        return switch (state) {
            case SIGNEDOUT -> """
                    - login <username> <password>
                    - register <username> <password> <email>
                    - help - with possible commands
                    - quit - the application
                    """;
            case SIGNEDIN -> """
                    - create <gameName> - a game
                    - list - games
                    - join <ID> [WHITE|BLACK]
                    - observe <ID> - a game
                    - logout - when you are done
                    - help - with possible commands
                    """;
            case INGAME -> """
                    - highlight <piece> <square> - a piece's valid moves
                    - move <piece> <start square> <end square> {<Promote pawn to QUEEN, ROOK, BISHOP, or KNIGHT>} - a piece
                    - redraw - the board
                    - resign - the game
                    - leave - the game
                    - help - with possible commands
                    """;
            case OBSERVE -> """
                    - redraw - the board
                    - leave - the game
                    - help - with possible commands
                    """;
        };
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
        server.createGame(params[0], sessionAuth);
        return String.format("Created game named %s", params[0]);
    }
    public String listGames(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }

        Collection<GameData> games = server.listGames(sessionAuth);
        var result = new StringBuilder();
        int cnt = 1;
        mappedID.clear();
        for (GameData game : games) {
            mappedID.put(cnt, game.gameID());
            result.append("GameID: ").append(game.gameID()).append(" | Gamename: ").append(game.gameName());
            result.append(" | WHITE: ").append(game.whiteUsername()).append(" | BLACK: ").append(game.blackUsername()).append('\n');
            cnt++;
        }
        return result.toString();
    }
    public String joinGame(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 2) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        try {
            if (mappedID.get(parseInt(params[0])) == null) {
                throw new IOException("Game Does Not Exist");
            }
            GameData game = server.joinGame(params[1], mappedID.get(parseInt(params[0])), sessionAuth);
            currentPlayerColor = params[1];
            currentGameID = game.gameID();
            ws.connectToGame(sessionAuth, game.gameID());
            state = State.INGAME;
            return String.format("Joined game %s as %s team", game.gameName(), params[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Game Number Must be Integer");
        } catch (MessageException e) {
            throw new IOException("Invalid Websocket Command");
        }
    }
    public String observeGame(String... params) throws IOException {
        assertSignedIn();
        if (params.length != 1) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        try {
            if (mappedID.get(parseInt(params[0])) == null) {
                throw new IOException("Game Does Not Exist");
            }
            if (mappedID.containsKey(parseInt(params[0]))) {
                currentGameID = parseInt(params[0]);
                currentPlayerColor = "OBSERVER";
                ws.connectToGame(sessionAuth, parseInt(params[0]));
                state = State.OBSERVE;
            }
            return String.format("Observing game with ID %s", params[0]);
        } catch (NumberFormatException e) {
            throw new IOException("Game Number Must be Integer");
        } catch (MessageException e) {
            throw new IOException("Unable to Connect to Game");
        }
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

    /// INGAME State Methods
    private String highlight(String... params) throws IOException {
        assertInGame();
        if (params.length != 2) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        String colStr = String.valueOf(params[1].charAt(0)).toLowerCase();
        int col = colStrToInt(colStr);
        int row = parseInt(String.valueOf(params[1].charAt(1)));
        bdPrint.printBoard(currentPlayerColor, currentGame, new ChessPosition(row, col));
        return "Highlighting Valid Moves";
    }

    private String makeMove(String... params) throws IOException, MessageException {
        assertInGame();
        ChessPiece.PieceType promPiece = null;
        if (params[0].equalsIgnoreCase("pawn") &&
            (parseInt(String.valueOf(params[2].charAt(1))) == 8 || parseInt(String.valueOf(params[2].charAt(1))) == 1)) {
            if (params.length != 4) {
                throw new IOException("Invalid Parameters, Promote Ready, Check help()");
            }
            if (params[3].equalsIgnoreCase("QUEEN")) {
                promPiece = ChessPiece.PieceType.QUEEN;
            } else if (params[3].equalsIgnoreCase("ROOK")) {
                promPiece = ChessPiece.PieceType.ROOK;
            } else if (params[3].equalsIgnoreCase("BISHOP")) {
                promPiece = ChessPiece.PieceType.BISHOP;
            } else if (params[3].equalsIgnoreCase("KNIGHT")) {
                promPiece = ChessPiece.PieceType.KNIGHT;
            } else {
                throw new IOException("Invalid Promotion Piece, Check help()");
            }
        } else {
            if (params.length != 3) {
                throw new IOException("Invalid Parameters, Check help()");
            }
        }
        String stColStr = String.valueOf(params[1].charAt(0)).toLowerCase();
        int stCol = colStrToInt(stColStr);
        int stRow = parseInt(String.valueOf(params[1].charAt(1)));
        ChessPosition startPos = new ChessPosition(stRow, stCol);
        String ndColStr = String.valueOf(params[2].charAt(0)).toLowerCase();
        int ndCol = colStrToInt(ndColStr);
        int ndRow = parseInt(String.valueOf(params[2].charAt(1)));
        ChessPosition endPos = new ChessPosition(ndRow, ndCol);
        ws.makeGameMove(sessionAuth, currentGameID, new ChessMove(startPos, endPos, promPiece));
        return "";
    }

    private String redrawBoard(String... params) throws IOException {
        assertObserveInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        bdPrint.printBoard(currentPlayerColor, currentGame, null);
        return "Redrawing Board";
    }

    private String resignGame(String... params) throws IOException, MessageException {
        assertInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        ws.resignGame(sessionAuth, currentGameID);
        return "";
        // sort of works but breaks
    }

    private String leaveGame(String... params) throws IOException, MessageException {
        assertObserveInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        ws.leaveGame(sessionAuth, currentGameID);
        currentGameID = 0;
        currentGame = null;
        currentPlayerColor = null;
        state = State.SIGNEDIN;
        return "Leaving Game";
    }

    private int colStrToInt(String colStr) throws IOException {
        int col;
        switch (colStr) {
            case "a" -> col = 1;
            case "b" -> col = 2;
            case "c" -> col = 3;
            case "d" -> col = 4;
            case "e" -> col = 5;
            case "f" -> col = 6;
            case "g" -> col = 7;
            case "h" -> col = 8;
            default -> throw new IOException("Invalid Column Letter");
        }
        return col;
    }

    private void assertInGame() throws IOException {
        if (state != State.INGAME) {
            throw new IOException("You must join a game first!");
        }
    }

    private void assertObserveInGame() throws IOException {
        if (state == State.SIGNEDIN || state == State.SIGNEDOUT) {
            throw new IOException("You must Join or Observe a game first!");
        }
    }
}