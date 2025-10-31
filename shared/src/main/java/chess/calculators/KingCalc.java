package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class KingCalc implements MoveCalc {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();

        int[][] directions = new int[][]{{-1,-1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        // for each part of directions in move, check for position move validity
        // 0 is the first [] of directions and 1 is the second [] of directions
        for (int[] move : directions) {
            if(0 < row-move[0] && row-move[0] < 9 && 0 < col-move[1] && col-move[1] < 9) {
                ChessPosition posCheck = new ChessPosition(row-move[0], col-move[1]);
                checkPos(myPosition, validMoves, board, ally, posCheck);
            }
        }
        return validMoves;
    }
}
