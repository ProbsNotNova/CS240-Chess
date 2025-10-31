package chess.calculators;

import chess.*;

import java.util.Collection;

public class KnightCalc implements MoveCalc {

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = new int[][]{{-2,-1}, {-1, -2}, {-2, 1}, {-1, 2}, {2, -1}, {1, -2}, {2, 1}, {1, 2}};
        // for each part of directions in move, check for position move validity
        // 0 is the first [] of directions and 1 is the second [] of directions
        return kniKiValMoves(directions, myPosition, board);
    }
}
