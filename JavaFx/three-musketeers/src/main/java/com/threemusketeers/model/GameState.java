package com.threemusketeers.model;

public final class GameState {

    private Board board;
    private PieceType sideToMove;
    private GameStatus status;

    public GameState(Board board, PieceType sideToMove) {
        this.board = board;
        this.sideToMove = sideToMove;
        this.status = WinChecker.evaluate(board, sideToMove);
    }

    public static GameState newGame() {
        return new GameState(Board.initial(), PieceType.MUSKETEER);
    }

    public Board board() {
        return board;
    }

    public PieceType sideToMove() {
        return sideToMove;
    }

    public GameStatus status() {
        return status;
    }

    public void applyMove(Move move) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot move: game has already ended (" + status + ")");
        }
        if (board.at(move.from()) != sideToMove) {
            throw new IllegalArgumentException("It is not " + sideToMove + "'s turn, or that square is not their piece");
        }
        if (!MoveGenerator.legalDestinations(board, move.from()).contains(move.to())) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }
        board.move(move);
        sideToMove = sideToMove.opposite();
        status = WinChecker.evaluate(board, sideToMove);
    }

    public GameState copy() {
        GameState copy = new GameState(board.copy(), sideToMove);
        copy.status = this.status;
        return copy;
    }
}
