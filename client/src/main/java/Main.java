import chess.*;
import ui.Console;
import websocket.MessageException;

import java.io.IOException;

import static java.lang.Integer.parseInt;

public class Main {
    public static void main(String[] args) throws MessageException, IOException {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        Console console = new Console(parseInt(args[0]), args[1]);
        console.run();
    }
}
