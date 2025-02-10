package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private final Board board;
    private boolean check;
    private boolean checkMate;

    private final List<Piece> pieceOnTheBoard = new ArrayList<>();
    private final List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] pieces = new ChessPiece[board.getRows()][board.getColumns()];
        
        IntStream.range(0, board.getRows()).forEach(i -> {
            IntStream.range(0, board.getColumns()).forEach(j -> {
                pieces[i][j] = (ChessPiece) board.piece(i, j);
            });
        });
        
        return pieces;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);

        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("Invalid move: You can't put yourself in check");
        }
        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        nextTurn();

        return (ChessPiece) capturedPiece;
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null) {
            pieceOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p =(ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            pieceOnTheBoard.add(capturedPiece);
        }
    }

    private void validateSourcePosition(Position position) {
        validatePieceExists(position);
        validatePieceOwnership(position);
        validatePieceHasMoves(position);
    }

    private void validatePieceExists(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("Invalid move: No piece found at the selected position");
        }
    }

    private void validatePieceOwnership(Position position) {
        ChessPiece piece = (ChessPiece) board.piece(position);
        if (currentPlayer != piece.getColor()) {
            throw new ChessException("Invalid move: You can only move your own pieces");
        }
    }

    private void validatePieceHasMoves(Position position) {
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("Invalid move: Selected piece has no available moves");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("Invalid move: The chosen piece can't move to the target position");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE);
    }


    private Color opponent(Color color) {
        return (color == Color.WHITE ? Color.BLACK : Color.WHITE);
    }

    private ChessPiece king(Color color) {
        List<Piece> list = pieceOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("Invalid move: No " + color + " king found on the board");
    }


    private boolean testCheck(Color color) {
        Position kingPosition = getKingPosition(color);
        List<Piece> opponentPieces = getOpponentPieces(color);
        return isKingThreatenedByAnyPiece(kingPosition, opponentPieces);
    }

    private Position getKingPosition(Color color) {
        return king(color).getChessPosition().toPosition();
    }

    private List<Piece> getOpponentPieces(Color color) {
        return pieceOnTheBoard.stream()
                .filter(piece -> ((ChessPiece) piece).getColor() == opponent(color))
                .toList();
    }

    private boolean isKingThreatenedByAnyPiece(Position kingPosition, List<Piece> opponentPieces) {
        return opponentPieces.stream()
                .anyMatch(piece -> canPieceAttackPosition(piece, kingPosition));
    }

    private boolean canPieceAttackPosition(Piece piece, Position position) {
        boolean[][] possibleMoves = piece.possibleMoves();
        return possibleMoves[position.getRow()][position.getColumn()];
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }
        
        List<Piece> allPiecesOfColor = getAllPiecesOfColor(color);
        return allPiecesOfColor.stream()
                .noneMatch(piece -> canMoveToAvoidCheck(piece, color));
    }

    private List<Piece> getAllPiecesOfColor(Color color) {
        return pieceOnTheBoard.stream()
                .filter(piece -> ((ChessPiece) piece).getColor() == color)
                .toList();
    }

    private boolean canMoveToAvoidCheck(Piece piece, Color color) {
        boolean[][] possibleMoves = piece.possibleMoves();
        Position source = ((ChessPiece) piece).getChessPosition().toPosition();
        
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                if (possibleMoves[i][j] && canMoveEscapeCheck(source, new Position(i, j), color)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveEscapeCheck(Position source, Position target, Color color) {
        Piece capturedPiece = makeMove(source, target);
        boolean isStillInCheck = testCheck(color);
        undoMove(source, target, capturedPiece);
        return !isStillInCheck;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        pieceOnTheBoard.add(piece);
    }

    private void initialSetup() {

//        placeNewPiece('c', 1, new Rook(board, Color.WHITE));
//        placeNewPiece('c', 2, new Rook(board, Color.WHITE));
//        placeNewPiece('d', 2, new Rook(board, Color.WHITE));
//        placeNewPiece('e', 2, new Rook(board, Color.WHITE));
//        placeNewPiece('e', 1, new Rook(board, Color.WHITE));
//        placeNewPiece('d', 1, new King(board, Color.WHITE));
//
//        placeNewPiece('c', 7, new Rook(board, Color.BLACK));
//        placeNewPiece('c', 8, new Rook(board, Color.BLACK));
//        placeNewPiece('d', 7, new Rook(board, Color.BLACK));
//        placeNewPiece('e', 7, new Rook(board, Color.BLACK));
//        placeNewPiece('e', 8, new Rook(board, Color.BLACK));
//        placeNewPiece('d', 8, new King(board, Color.BLACK));

        /* test check mate */
        placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));

    }

}
