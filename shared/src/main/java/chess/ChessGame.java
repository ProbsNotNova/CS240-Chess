package chess;

import java.util.ArrayList;
import java.util.Collection;

//import static chess.ChessPiece.PieceType.KING;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
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
     *     HELPER FOR VALIDMOVES MOVES A SINGLE PIECE SOMEWHERE
     */
    public void movePiece (ChessPiece piece, ChessPosition startPosition, ChessPosition endPosition) {
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);
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
        ChessPiece piece = board.getPiece(startPosition);

        if (piece.getPieceType() != null) {
            Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);
            Collection<ChessMove> validMoves = new ArrayList();
            for (ChessMove move : pieceMoves) {
                movePiece(piece, startPosition, move.getEndPosition());
                if (!isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }
                movePiece(piece, move.getEndPosition(), startPosition);
            }
            return validMoves; // Might want to figure out how to avoid
        } else {return null;}  // The two returns here or change idk
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        // above is likely wrong but follows an essential line of thinking
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (!validMoves(move.getStartPosition()).isEmpty() && piece.getTeamColor() == getTeamTurn()) {
            movePiece(piece, move.getStartPosition(), move.getEndPosition());
        }
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
        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                ChessPiece piece = board.getPiece(new ChessPosition(x, y));
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> enemyMoves = piece.pieceMoves(board, new ChessPosition(x, y));
                    for (ChessMove move : enemyMoves) {
                        ChessPiece gamePiece = board.getPiece(move.getEndPosition());
                        if (gamePiece != null && gamePiece.getPieceType() == ChessPiece.PieceType.KING) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     *     HELPER FOR CHECKMATE CREATE COPY OF BOARD
     */
    public ChessBoard boardCopy (ChessBoard board) {
        ChessBoard copyOfBoard = new ChessBoard();
        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                ChessPiece piece = board.getPiece(new ChessPosition(x, y));
                ChessPiece pieceCopy = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                copyOfBoard.addPiece(new ChessPosition(x, y), pieceCopy);
            }
        }
        return copyOfBoard;
    }

    /**
     *     HELPER FOR MOVE SIMULATION TO CHECK VALIDITY FOR CHECKMATE
     */
    public boolean isSimMoveInCheck (ChessMove move, TeamColor teamColor) {
        ChessBoard ogBoard = board;
        ChessBoard copyBoard = boardCopy(board);
        this.board = copyBoard;
        ChessPiece piece = board.getPiece(move.getStartPosition());
        movePiece(piece, move.getStartPosition(), move.getEndPosition());
        boolean check = isInCheck(teamColor);
        this.board = ogBoard;
        return check;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // no piece moves and king in check then checkmate
        if (isInCheck(teamColor)) {
            for (int x = 1; x < 9; x++) {
                for (int y = 1; y < 9; y++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(x, y));
                    if (piece != null ) {
                        if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                            Collection<ChessMove> kingMoves = piece.pieceMoves(board, new ChessPosition(x, y));
                            for (ChessMove move : kingMoves) {
                                ChessPiece gamePiece = board.getPiece(move.getEndPosition());
                                if (gamePiece != null && gamePiece.getTeamColor() != teamColor) {
                                    return true;
                                }
                            }
                        }
                        if (piece.getTeamColor() == teamColor) {
                            Collection<ChessMove> MOVES = piece.pieceMoves(board, new ChessPosition(x, y));
                            for (ChessMove move : MOVES) {
                                if (isSimMoveInCheck(move, teamColor)) {
                                    return true;
                                }
                            }
                        }


                    }
                }
            }
        }
        return false;
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
        boolean stalemate = false;
//        if (teamColor in stalemate) {
//            stalemate = true;
//        } else {
//            stalemate = false;
//        }
        return stalemate;
        }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
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
