package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ui.EscapeSequences.*;

public class BoardPrinter {
    private static ChessBoard board = new ChessBoard();
    private static final String[] HEADERS = { " A  B  C  D  E  F  G  H ",
                                              " 8 ", " 7 ", " 6 ", " 5 ",
                                              " 4 ", " 3 ", " 2 ", " 1 ",
                                              " H  G  F  E  D  C  B  A "};

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 9;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;

    public void printBoard(String currentPlayerColor, ChessBoard inputBoard, ChessPosition startPos) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean highlight = false;
        Collection<ChessMove> validMoves = new ArrayList<>();

        if (inputBoard != null) {
            board = inputBoard;
        }
        if (startPos != null) {
            highlight = true;
            assert inputBoard != null;
            validMoves = inputBoard.getPiece(startPos).pieceMoves(board, startPos);
        }
        out.print(ERASE_SCREEN);
        board.resetBoard(); // may need to get rid of this for reprints
        if (currentPlayerColor.equals("WHITE")) {
            drawAlphaHeader(out, HEADERS[0]);
            out.println();
            drawWhiteSideBoard(out, highlight, validMoves);
            drawAlphaHeader(out, HEADERS[0]);
        } else {
            drawAlphaHeader(out, HEADERS[9]);
            out.println();
            drawBlackSideBoard(out, highlight, validMoves);
            drawAlphaHeader(out, HEADERS[9]);
        }

        out.print(SET_BG_COLOR_BLACK);
//        out.print(SET_TEXT_COLOR_WHITE);
        out.println();
        out.print(RESET_BG_COLOR);
    }

    private static void drawNumHeader(PrintStream out, String headerText) {
            setLightGrey(out);
            printHeaderText(out, headerText);
            setLightGrey(out);
    }

    private static void drawAlphaHeader(PrintStream out, String headerText) {
        setLightGrey(out);
        int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
        int suffixLength = SQUARE_SIZE_IN_PADDED_CHARS - prefixLength - 1;

        out.print(EMPTY.repeat(prefixLength));
        printHeaderText(out, headerText);
        setLightGrey(out);
        out.print(EMPTY.repeat(suffixLength));
        setBlack(out);
    }

    private static void printHeaderText(PrintStream out, String headerText) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_BOLD);
        out.print(SET_TEXT_COLOR_BLACK);

        out.print(headerText);

        setBlack(out);
    }

    private static void drawWhiteSideBoard(PrintStream out, boolean highlight, Collection<ChessMove> valMoves) {
        int numHeadCnt = 1;
        for (int boardRow = 8; boardRow >= 1; boardRow--) {
            drawNumHeader(out, HEADERS[numHeadCnt]);
            for (int boardCol = 1; boardCol < BOARD_SIZE_IN_SQUARES; boardCol++) {
                drawSquares(out, boardRow, boardCol, highlight, valMoves);
            }
            drawNumHeader(out, HEADERS[numHeadCnt]);
            numHeadCnt++;
            setBlack(out);
            out.println();
        }

    }
    private static void drawBlackSideBoard(PrintStream out, boolean highlight, Collection<ChessMove> valMoves) {
            int numHeadCnt = 8;
        for (int boardRow = 1; boardRow < BOARD_SIZE_IN_SQUARES; boardRow++) {
            drawNumHeader(out, HEADERS[numHeadCnt]);
            for (int boardCol = 8; boardCol >= 1; boardCol--) {
                drawSquares(out, boardRow, boardCol, highlight, valMoves);
            }
            drawNumHeader(out, HEADERS[numHeadCnt]);
            numHeadCnt--;
            setBlack(out);
            out.println();

        }

    }
    private static void drawSquares(PrintStream out, int boardRow, int boardCol, boolean highlight, Collection<ChessMove> valMoves) {
        if (boardRow % 2 == 0) {
            if (boardCol % 2 == 0) {
                setBlack(out);
                if (highlight && validEndPos(valMoves, boardRow, boardCol)) {
                    setDarkGreen(out);
                }
                printPiece(out, board.getPiece(new ChessPosition(boardRow, boardCol)));

            } else {
                setWhite(out);
                if (highlight && validEndPos(valMoves, boardRow, boardCol)) {
                    setGreen(out);
                }
                printPiece(out, board.getPiece(new ChessPosition(boardRow, boardCol)));
            }
        } else {
            if (boardCol % 2 == 0) {
                setWhite(out);
                if (highlight && validEndPos(valMoves, boardRow, boardCol)) {
                    setGreen(out);
                }
                printPiece(out, board.getPiece(new ChessPosition(boardRow, boardCol)));

            } else {
                setBlack(out);
                if (highlight && validEndPos(valMoves, boardRow, boardCol)) {
                    setDarkGreen(out);
                }
                printPiece(out, board.getPiece(new ChessPosition(boardRow, boardCol)));
            }
        }

    }

    private static boolean validEndPos(Collection<ChessMove> valMoves, int boardRow, int boardCol) {
        for (ChessMove move : valMoves) {
            if (Objects.equals(move.getEndPosition(), new ChessPosition(boardRow, boardCol))) {
                return true;
            }
        }
        return false;
    }

    private static void printPiece(PrintStream out, ChessPiece piece) {
        if (piece == null) {
            out.print(EMPTY.repeat(1));
            return;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            setWhitePieceBlue(out);
        } else {
            setBlackPieceRed(out);
        }
        switch (piece.getPieceType()) {
            case KING:
                out.print(KING);
                break;
            case QUEEN:
                out.print(QUEEN);
                break;
            case BISHOP:
                out.print(BISHOP);
                break;
            case ROOK:
                out.print(ROOK);
                break;
            case KNIGHT:
                out.print(KNIGHT);
                break;
            case PAWN:
                out.print(PAWN);
                break;
        }

    }

    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }
    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }
    private static void setLightGrey(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
    }
    private static void setDarkGreen(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
    }
    private static void setGreen(PrintStream out) {
        out.print(SET_BG_COLOR_GREEN);
    }

    private static void setBlackPieceRed(PrintStream out) {
        out.print(SET_TEXT_COLOR_RED);
    }
    private static void setWhitePieceBlue(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLUE);
    }
}


