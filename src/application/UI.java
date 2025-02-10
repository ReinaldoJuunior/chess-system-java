package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UI {

    // https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";


    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static ChessPosition readChessPosition(Scanner sc) {
        try {
            String s = sc.nextLine();
            char column = s.charAt(0);
            int rw = Integer.parseInt(s.substring(1));
            return new ChessPosition(column, rw);
        } catch (RuntimeException e) {
            throw new InputMismatchException("Error reading ChessPosition. Valid values are from a1 to h8.");
        }
    }

    public static void printBoard(ChessPiece[][] pieces) {
        printBoardRows(pieces, new boolean[pieces.length][pieces.length]);
        printColumnLabels();
    }

    public static void printBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        printBoardRows(pieces, possibleMoves);
        printColumnLabels();
    }

    private static void printBoardRows(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        IntStream.range(0, pieces.length)
                .forEach(row -> {
                    printRowNumber(row);
                    printRowPieces(pieces[row], possibleMoves[row]);
                    System.out.println();
                });
    }

    private static void printRowNumber(int row) {
        System.out.print((8 - row) + " ");
    }

    private static void printRowPieces(ChessPiece[] rowPieces, boolean[] rowMoves) {
        IntStream.range(0, rowPieces.length)
                .forEach(col -> printPiece(rowPieces[col], rowMoves[col]));
    }

    private static void printColumnLabels() {
        System.out.println("  a b c d e f g h");
    }

    public static void printMatch(ChessMatch chessMatch, List<ChessPiece> captured) {
        printGameBoard(chessMatch);
        printCapturedPiecesSection(captured);
        printGameStatus(chessMatch);
    }

    private static void printGameBoard(ChessMatch chessMatch) {
        printBoard(chessMatch.getPieces());
        System.out.println();
    }

    private static void printCapturedPiecesSection(List<ChessPiece> captured) {
        printCapturedPieces(captured);
        System.out.println();
    }

    private static void printGameStatus(ChessMatch chessMatch) {
        System.out.println("Turn: " + chessMatch.getTurn());
        
        if (chessMatch.getCheckMate()) {
            printCheckMateStatus(chessMatch);
        } else {
            printOngoingGameStatus(chessMatch);
        }
    }

    private static void printCheckMateStatus(ChessMatch chessMatch) {
        System.out.println("CHECKMATE!");
        System.out.println("Winner: " + chessMatch.getCurrentPlayer());
    }

    private static void printOngoingGameStatus(ChessMatch chessMatch) {
        System.out.println("Waiting player: " + chessMatch.getCurrentPlayer());
        if (chessMatch.getCheck()) {
            System.out.println("CHECK!");
        }
    }

    private static void printPiece(ChessPiece piece, boolean background) {
        applyBackgroundIfNeeded(background);
        printPieceSymbol(piece);
        System.out.print(" ");
    }

    private static void applyBackgroundIfNeeded(boolean background) {
        if (background) {
            System.out.print(ANSI_BLUE_BACKGROUND);
        }
    }

    private static void printPieceSymbol(ChessPiece piece) {
        if (piece == null) {
            printEmptySquare();
        } else {
            printColoredPiece(piece);
        }
    }

    private static void printEmptySquare() {
        System.out.print("-" + ANSI_RESET);
    }

    private static void printColoredPiece(ChessPiece piece) {
        String pieceColor = (piece.getColor() == Color.WHITE) ? ANSI_WHITE : ANSI_YELLOW;
        System.out.print(pieceColor + piece + ANSI_RESET);
    }

    private static void printCapturedPieces(List<ChessPiece> captured){
        List<ChessPiece> white = captured.stream().filter(x -> x.getColor() == Color.WHITE).collect(Collectors.toList());
        List<ChessPiece> black = captured.stream().filter(x -> x.getColor() == Color.BLACK).collect(Collectors.toList());
        System.out.println("Captured pieces: ");
        System.out.print("White: ");
        System.out.print(ANSI_WHITE);
        System.out.println(Arrays.toString(white.toArray()));
        System.out.print(ANSI_RESET);
        System.out.print("Black: ");
        System.out.print(ANSI_YELLOW);
        System.out.println(Arrays.toString(black.toArray()));
        System.out.print(ANSI_RESET);

    }
}
