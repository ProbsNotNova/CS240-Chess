package chess;

import java.util.Collection;

//import static chess.ChessPiece.PieceType.KING;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private final ChessBoard board;
    private boolean whiteTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.whiteTurn = true;
    }

    // public boolean isWhiteTurn() {
    //     return whiteTurn;
    // }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        //return whiteTurn ? TeamColor.WHITE : TeamColor.BLACK;
        TeamColor turn = TeamColor.BLACK;
        if (whiteTurn) {
            turn = TeamColor.WHITE;
        }
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        //whiteTurn = team ? TeamColor.WHITE : TeamColor.BLACK;

        // if whiteTurn true and team is black, set whiteTurn false.
        // Otherwise if whiteTurn false and team is white, set whiteTurn true
        if (whiteTurn && team == TeamColor.BLACK) {
            whiteTurn = false;
        } else if (!whiteTurn && team == TeamColor.WHITE) {
            whiteTurn = true;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //valid if our piece prevents King from being in check.
        // Piece                        moves for piece
        board.getPiece(startPosition).pieceMoves(board, startPosition);
        // validmoves = []
        // for (move in all moves) {
        //       piece
        //       if (!isInCheck()) {
        //          add to validmoves array
        //       }
        //       make move back
        //       }
    }

    /**
     *     HELPER FOR VALIDMOVES MOVES A SINGLE PIECE SOMEWHERE
     */
    public void movePiece (ChessPiece piece, ChessPosition endPosition) {

    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // need to fill move logic code in

        // turn toggle after successful move
        whiteTurn = !whiteTurn;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // sweep pieces for color opposite of teamColor to see if in check
        //call piecemoves???? to verify moving piece for is in check
        boolean check;
//        if (opposite color in check) {
//            check = true;
//        } else {
//            check = false;
//        }
//        return check;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // no piece moves and king in check then checkmate
        boolean checkmate;
//        if (teamColor in checkmate) {
//            checkmate = true;
//        } else {
//            checkmate = false;
//        }
//        return checkmate;
        }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if no pieces can move and king not in check
        boolean stalemate;
//        if (teamColor in stalemate) {
//            stalemate = true;
//        } else {
//            stalemate = false;
//        }
//        return stalemate;
        }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        board.resetBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
