package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class BishopCalc implements MoveCalc {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();
        boolean topLeft = true;
        boolean bottomRight = true;
        boolean topRight = true;
        boolean bottomLeft = true;
        for (int n=1; n<8; n++) {
            //row - 1n col - 1n
            if (topLeft && (0 < row-n) && (0 < col-n)) {
                ChessPosition topLeftPos = new ChessPosition(row-n, col-n);
                topLeft = checkPos(myPosition, validMoves, board, ally, topLeftPos);
            }
            //row + 1n col + 1n
            if (bottomRight && (row+n < 9) && (col+n < 9)) {
                ChessPosition botRightPos = new ChessPosition(row+n, col+n);
                bottomRight = checkPos(myPosition, validMoves, board, ally, botRightPos);
            }
            // row - 1n col + 1n
            if (topRight && (0 < row-n) && (col+n < 9)) {
                ChessPosition topRightPos = new ChessPosition(row-n, col+n);
                topRight = checkPos(myPosition, validMoves, board, ally, topRightPos);
            }
            // row + 1n col - 1n
            if (bottomLeft && (row+n < 9) && (0 < col-n)) {
                ChessPosition botLeftPos = new ChessPosition(row+n, col-n);
                bottomLeft = checkPos(myPosition, validMoves, board, ally, botLeftPos);
            }
        }
        return validMoves;
    }
}
