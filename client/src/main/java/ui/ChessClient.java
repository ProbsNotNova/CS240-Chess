package ui;

import backend.ServerFacade;
import chess.*;
import model.GameData;
import websocket.MessageException;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;
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
    private int currentGame = 0;
    private final Map<Integer, Integer> mappedID = new HashMap<>();

    private ServerFacade server = new ServerFacade(8080);
    private final BoardPrinter bdPrint = new BoardPrinter();
    private final WebSocketFacade ws;
    private ChessBoard currentBoard;

    /// ////////
    public ChessClient(int serverPort, String serverUrl) throws IOException, MessageException {
        server = new ServerFacade(serverPort);
        ws = new WebSocketFacade(serverUrl, this);

    }

    public void notify(ServerMessage serverMessage, boolean error) {
        if (error) { // maybe this needs to actually be in my console since it has print prompt
            System.out.println(SET_TEXT_COLOR_RED + serverMessage.getMessage());
            printPrompt();
        } else {
            System.out.println(SET_TEXT_COLOR_BLUE + serverMessage.getMessage());
            printPrompt();
        }
    }

    public void loadGame(ChessGame inputGame) {
        bdPrint.printBoard(currentPlayerColor, inputGame.getBoard(), null);
        currentBoard = inputGame.getBoard();
    }
    /// ////////

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
                case "list" -> listGames(params); // ASK TA for help on game joins staying despite logout or quitting.
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "help" -> help();
                default -> help();
            };
        }
        return switch (cmd) {
            case "highlight" -> highlight(params); // ASK TA for help on game joins staying despite logout or quitting.
            case "move" -> makeMove(params);
            case "redraw" -> redrawBoard();
            case "resign" -> resignGame();
            case "leave" -> leaveGame();
            case "help" -> help();
            default -> help();
        };
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
                    - highlight <piece> <row> <col> - a piece's valid moves
                    - move <piece> <start row> <start col> <end row> <end col> {<Promote pawn to QUEEN, ROOK, BISHOP, or KNIGHT>} - a piece
                    - redraw - the board
                    - resign - the game
                    - leave - with possible commands
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
            currentGame = game.gameID();
            ws.connectToGame(sessionAuth, game.gameID());
//            bdPrint.printBoard(params[1], null);
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
                ws.connectToGame(sessionAuth, parseInt(params[0]));
                bdPrint.printBoard("WHITE", null, null);
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
        if (params.length != 3) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        bdPrint.printBoard(currentPlayerColor, currentBoard, new ChessPosition(parseInt(params[1]), parseInt(params[2])));
        return "Highlighting Valid Moves";
    }

    private String makeMove(String... params) throws IOException, MessageException {
        assertInGame();
        ChessPosition startPos = new ChessPosition(parseInt(params[1]), parseInt(params[2]));
        ChessPosition endPos = new ChessPosition(parseInt(params[3]), parseInt(params[4]));
        if (!params[0].equalsIgnoreCase("pawn")) {

            if (params.length != 5) {
            throw new IOException("Invalid Parameters, Check help()");
            }

            ws.makeGameMove(sessionAuth, currentGame, new ChessMove(startPos, endPos, null));
        } else {
            if (params.length != 6) {
                throw new IOException("Invalid Parameters, Promote Ready, Check help()");
            }
            ChessPiece.PieceType promPiece;
            if (params[5].equalsIgnoreCase("QUEEN")) {
                promPiece = ChessPiece.PieceType.QUEEN;
            } else if (params[5].equalsIgnoreCase("ROOK")) {
                promPiece = ChessPiece.PieceType.QUEEN;
            } else if (params[5].equalsIgnoreCase("BISHOP")) {
                promPiece = ChessPiece.PieceType.QUEEN;
            } else if (params[5].equalsIgnoreCase("KNIGHT")) {
                promPiece = ChessPiece.PieceType.QUEEN;
            } else {
                throw new IOException("Invalid Promotion Piece, Check help()");
            }
            ws.makeGameMove(sessionAuth, currentGame, new ChessMove(startPos, endPos, promPiece));
        }
        return "Moved Piece";
    }

    private String redrawBoard(String... params) throws IOException {
        assertInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        bdPrint.printBoard(currentPlayerColor, currentBoard, null);
        return "Redrawing Board";
    }

    private String resignGame(String... params) throws IOException, MessageException {
        assertInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        ws.resignGame(sessionAuth, currentGame);
        currentGame = 0;
        return "You Resigned the Game";
    }

    private String leaveGame(String... params) throws IOException, MessageException {
        assertInGame();
        if (params.length != 0) {
            throw new IOException("Invalid Parameters, Check help()");
        }
        ws.leaveGame(sessionAuth, currentGame);
        currentGame = 0;
        state = State.SIGNEDIN;
        return "Leaving Game";
    }

    private void assertInGame() throws IOException {
        if (state != State.INGAME) {
            throw new IOException("You must join a game first!");
        }
    }
}