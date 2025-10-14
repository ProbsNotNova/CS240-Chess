package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnCalc {
//    static void moveCheck(ChessBoard board, ChessPosition posCheck, ChessGame.TeamColor ally) {
//        if (board.getPiece(posCheck).getTeamColor() != ally || board.getPiece(posCheck).getTeamColor() == ally) {
//
//        }
//    }
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
        ChessPosition move = new ChessPosition(row+rowDir, col);  // changing the - to a + passed middle of board test
                                                                            // may have backwards implementations
        if(board.getPiece(move) == null) {
            validMoves.add(new ChessMove(myPosition, move, null));

            // If pawn hasn't moved from starting row for color, validate double move space
            ChessPosition doubleMove = new ChessPosition(row+(rowDir*2), col);
            if((row == 7 && rowDir<0) || (row == 2 && rowDir>0)) { //code sees bottom left as {1,1}
                if(board.getPiece(doubleMove) == null) {
                    validMoves.add(new ChessMove(myPosition, doubleMove, null));
                }
            }
        }
        // if diagonal in front of pawn contains enemy, it's a valid move
        if ()

        // spot enemy (front diagonals)



//            ChessPosition startMove1 = new ChessPosition(row-1, col);
//        if(board.getPiece(startMove1) == null) {
//            validMoves.add(new ChessMove(myPosition, startMove1, null));
//
//            ChessPosition startMove2 = new ChessPosition(row - 2, col);
//            if (/*firstMove &&*/ board.getPiece(startMove2) == null) {
//                validMoves.add(new ChessMove(myPosition, startMove2, null));
//                moveCheck(board, startMove1, ally);
//            }
//            moveCheck(board, startMove1, ally);
//        }

        return validMoves;
    }
}
