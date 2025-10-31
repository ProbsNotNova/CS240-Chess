package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class RookCalc implements MoveCalc {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();
        boolean moveUp = true;
        boolean moveDown = true;
        boolean moveLeft = true;
        boolean moveRight = true;
        for (int n=1; n<9; n++) {
            //row - n
            if (moveUp && (0 < row-n)) {
                ChessPosition topLeftPos = new ChessPosition(row-n, col);
                moveUp = checkPos(myPosition, validMoves, board, ally, topLeftPos);
            }
            //row + n
            if (moveDown && (row+n < 9)) {
                ChessPosition botRightPos = new ChessPosition(row+n, col);
                moveDown = checkPos(myPosition, validMoves, board, ally, botRightPos);
            }
            // col - n
            if (moveLeft && (0 < col-n)) {
                ChessPosition topRightPos = new ChessPosition(row, col-n);
                moveLeft = checkPos(myPosition, validMoves, board, ally, topRightPos);
            }
            // col + n
            if (moveRight && (col+n < 9)) {
                ChessPosition botLeftPos = new ChessPosition(row, col+n);
                moveRight = checkPos(myPosition, validMoves, board, ally, botLeftPos);
            }
        }
        return validMoves;
    }
}
