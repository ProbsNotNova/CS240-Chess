package chess.calculators;

import chess.*;

import java.util.Collection;

public class KingCalc implements MoveCalc {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = new int[][]{{-1,-1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
        // for each part of directions in move, check for position move validity
        // 0 is the first [] of directions and 1 is the second [] of directions
        return kniKiValMoves(directions, myPosition, board);
    }
}
