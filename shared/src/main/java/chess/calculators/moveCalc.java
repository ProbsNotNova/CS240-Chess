package chess.calculators;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface moveCalc {
    default public boolean checkPos(ChessPosition myPosition, Collection<ChessMove> validMoves, ChessBoard board,
                                    ChessGame.TeamColor ally, ChessPosition posCheck) {
        if (board.getPiece(posCheck) == null) {
            validMoves.add(new ChessMove(myPosition, posCheck, null) ); // promote for Pawn
        } else if (board.getPiece(posCheck).getTeamColor() != ally) {
            validMoves.add(new ChessMove(myPosition, posCheck, null) );
            return false;
        } else if (board.getPiece(posCheck).getTeamColor() == ally) {
            return false;
        }
        return true;
    }





}
