package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
        board.resetBoard();
        this.whiteTurn = true;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return whiteTurn ? TeamColor.WHITE : TeamColor.BLACK;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
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
        ChessPiece piece = board.getPiece(startPosition);
        if (piece.getPieceType() != null) {
            Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);
            Collection<ChessMove> validMoves = new ArrayList();
            for (ChessMove move : pieceMoves) {
                ChessPosition mvEndPos = move.getEndPosition();
                ChessPiece enemyCopy = null; // dumby initialize for IDE sake
                if (board.getPiece(mvEndPos) != null && board.getPiece(mvEndPos).getTeamColor() != piece.getTeamColor()) {
                    enemyCopy = board.getPiece(mvEndPos);
                }
                movePiece(piece, startPosition, mvEndPos);
                if (!isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }
                movePiece(piece, move.getEndPosition(), startPosition);
                board.addPiece(move.getEndPosition(), enemyCopy);
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
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece != null && validMoves(move.getStartPosition()).contains(move) && piece.getTeamColor() == getTeamTurn()) {
            movePiece(piece, move.getStartPosition(), move.getEndPosition());

            // Pawn Promotion
            ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
            if (promotionPiece != null) {
                board.addPiece(move.getEndPosition(), null);
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), promotionPiece));
            }
        } else {
            throw new InvalidMoveException();
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
                    if (piece != null && piece.getTeamColor() == teamColor && !validMoves(new ChessPosition(x, y)).isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
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
        if (!isInCheck(teamColor)) {
            for (int x = 1; x < 9; x++) {
                for (int y = 1; y < 9; y++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(x, y));
                    if (piece!=null && piece.getTeamColor()==teamColor && !validMoves(new ChessPosition(x, y)).isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whiteTurn == chessGame.whiteTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, whiteTurn);
    }
}
