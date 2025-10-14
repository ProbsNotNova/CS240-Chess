package chess.calculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnCalc {
    static void moveCheck(ChessBoard board, ChessPosition posCheck, ChessGame.TeamColor ally) {
        if (board.getPiece(posCheck).getTeamColor() != ally || board.getPiece(posCheck).getTeamColor() == ally) {

        }
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        Collection<ChessMove> validMoves = new ArrayList();
        ChessPiece startPiece = board.getPiece(myPosition);
        ChessGame.TeamColor ally = startPiece.getTeamColor();
        //boolean firstMove = true;

        // First move double move if valid


        //single moves after first/double
        ChessPosition move = new ChessPosition(row-1, col);
        if(board.getPiece(move) == null) {
            validMoves.add(new ChessMove(myPosition, move, null));
            if(row == 6 && board.getPiece(myPosition).getTeamColor() == ally) {

            } else if (row == 1 && board.getPiece(myPosition).getTeamColor() != ally){

            }
        }

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

//        default public boolean checkPos(ChessPosition myPosition, Collection<ChessMove> validMoves, ChessBoard board,
//                                    ChessGame.TeamColor ally, ChessPosition posCheck) {
//        if (board.getPiece(posCheck) == null) {
//            validMoves.add(new ChessMove(myPosition, posCheck, null) ); // promote for Pawn
//        } else if (board.getPiece(posCheck).getTeamColor() != ally) {
//            validMoves.add(new ChessMove(myPosition, posCheck, null) );
//            return false;
//        } else if (board.getPiece(posCheck).getTeamColor() == ally) {
//            return false;
//        }
//        return true;
//    }

//        int[][] directions = new int[][]{{-2,-1}, {-1, -2}, {-2, 1}, {-1, 2}, {2, -1}, {1, -2}, {2, 1}, {1, 2}};
//        // for each part of directions in move, check for position move validity
//        // 0 is the first [] of directions and 1 is the second [] of directions
//        for (int[] move : directions) {
//            if(0 < row-move[0] && row-move[0] < 9 && 0 < col-move[1] && col-move[1] < 9) {
//                ChessPosition posCheck = new ChessPosition(row-move[0], col-move[1]);
//                checkPos(myPosition, validMoves, board, ally, posCheck);
//            }
//        }
        return validMoves;
    }
}
