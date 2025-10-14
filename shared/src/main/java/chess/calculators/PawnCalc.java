package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnCalc {

    static void addToValidMoves (int rowDir, int row, Collection<ChessMove> vMoves,ChessPosition myPos, ChessPosition move) {


        // if pawn makes it to last row it can move to, promote piece move. Otherwise, regular move
        if ((rowDir < 0 && row == 2) || (rowDir > 0 && row == 7)) {
            vMoves.add(new ChessMove(myPos, move, ChessPiece.PieceType.QUEEN));
            vMoves.add(new ChessMove(myPos, move, ChessPiece.PieceType.ROOK));
            vMoves.add(new ChessMove(myPos, move, ChessPiece.PieceType.BISHOP));
            vMoves.add(new ChessMove(myPos, move, ChessPiece.PieceType.KNIGHT));
        } else {
            vMoves.add(new ChessMove(myPos, move, null));
        }
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();

        //row direction move set based on pawn color
        int rowDir = 0;
        if (ally == ChessGame.TeamColor.WHITE) {
            rowDir = 1;
        } else if (ally == ChessGame.TeamColor.BLACK) {
            rowDir = -1;
        } //something broken with colors if rowDir stays 0


        //move 1 space forward for colored piece (based on rowDir) if space empty
        ChessPosition move = new ChessPosition(row + rowDir, col);  // changing the - to a + passed middle of board test
        // may have backwards implementations
        if (board.getPiece(move) == null) {
            addToValidMoves(rowDir, row, validMoves, myPosition, move);
//            validMoves.add(new ChessMove(myPosition, move, null));

            // If pawn hasn't moved from starting row for color, validate double move space
            ChessPosition doubleMove = new ChessPosition(row + (rowDir * 2), col);
            if ((row == 7 && rowDir < 0) || (row == 2 && rowDir > 0)) { //code sees bottom left as {1,1}
                if (board.getPiece(doubleMove) == null) {
                    addToValidMoves(rowDir, row, validMoves, myPosition, doubleMove);
//                    validMoves.add(new ChessMove(myPosition, doubleMove, null));
                }
            }
        }
        // if diagonal in front of pawn contains enemy, it's a valid move MAKE SOME INTO HELPER METHOD?
        ChessPosition captureLeft = new ChessPosition(row + (rowDir), col - 1);
        ChessPosition captureRight = new ChessPosition(row + (rowDir), col + 1);
        if (0 < col-1 && (board.getPiece(captureLeft) != null) && (board.getPiece(captureLeft).getTeamColor() != ally)) {
            addToValidMoves(rowDir, row, validMoves, myPosition, captureLeft);
//            validMoves.add(new ChessMove(myPosition, captureLeft, null));
        } else if (col+1 < 8 && (board.getPiece(captureRight) != null) && (board.getPiece(captureRight).getTeamColor() != ally)) {
            addToValidMoves(rowDir, row, validMoves, myPosition, captureRight);
//            validMoves.add(new ChessMove(myPosition, captureRight, null));
        }



        return validMoves;
    }
}
