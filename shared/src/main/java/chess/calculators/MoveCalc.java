package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public interface MoveCalc {
    default boolean checkPos(ChessPosition myPosition, Collection<ChessMove> validMoves, ChessBoard board,
                                    ChessGame.TeamColor ally, ChessPosition posCheck) {
        if (board.getPiece(posCheck) == null) {
            validMoves.add(new ChessMove(myPosition, posCheck, null) );
        } else if (board.getPiece(posCheck).getTeamColor() != ally) {
            validMoves.add(new ChessMove(myPosition, posCheck, null) );
            return false;
        } else {
            return board.getPiece(posCheck).getTeamColor() != ally;
        }
        return true;
    }
    default Collection<ChessMove> kniKiValMoves(int[][] directions, ChessPosition myPosition, ChessBoard board) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();

        for (int[] move : directions) {
            if(0 < row-move[0] && row-move[0] < 9 && 0 < col-move[1] && col-move[1] < 9) {
                ChessPosition posCheck = new ChessPosition(row-move[0], col-move[1]);
                checkPos(myPosition, validMoves, board, ally, posCheck);
            }
        }
        return validMoves;
    }





}
