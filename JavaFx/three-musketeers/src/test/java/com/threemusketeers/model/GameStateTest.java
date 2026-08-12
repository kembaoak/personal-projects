package com.threemusketeers.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameStateTest {

    private static final PieceType M = PieceType.MUSKETEER;
    private static final PieceType G = PieceType.GUARD;

    @Test
    void newGameStartsInProgressWithMusketeersToMove() {
        GameState state = GameState.newGame();
        assertEquals(GameStatus.IN_PROGRESS, state.status());
        assertEquals(PieceType.MUSKETEER, state.sideToMove());
    }

    @Test
    void applyMoveAppliesACaptureAndFlipsTurn() {
        GameState state = GameState.newGame();
        Move move = MoveGenerator.legalMovesFor(state.board(), M).get(0);

        state.applyMove(move);

        assertEquals(PieceType.GUARD, state.sideToMove());
        assertEquals(M, state.board().at(move.to()));
        assertNull(state.board().at(move.from()));
    }

    @Test
    void applyMoveRejectsMovingTheOpponentsPiece() {
        GameState state = GameState.newGame();
        Position guardPos = state.board().positionsOf(G).get(0);
        Position target = guardPos.orthogonalNeighbors().get(0);

        assertThrows(IllegalArgumentException.class, () -> state.applyMove(new Move(guardPos, target)));
    }

    @Test
    void applyMoveRejectsADiagonalDestination() {
        GameState state = GameState.newGame();
        Position musketeerPos = state.board().positionsOf(M).get(0);
        Position diagonal = new Position(musketeerPos.row() - 1, musketeerPos.col() - 1);

        assertThrows(IllegalArgumentException.class, () -> state.applyMove(new Move(musketeerPos, diagonal)));
    }

    @Test
    void applyMoveThrowsOnceGameHasEnded() {
        PieceType[][] layout = {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {M, null, M, null, M},
                {null, null, null, null, null},
                {null, null, null, null, null},
        };
        Board board = Board.custom(layout);
        GameState state = new GameState(board, PieceType.MUSKETEER);

        assertEquals(GameStatus.GUARDS_WIN, state.status());
        assertThrows(IllegalStateException.class,
                () -> state.applyMove(new Move(new Position(2, 0), new Position(2, 1))));
    }

    @Test
    void copyIsIndependentOfOriginal() {
        GameState state = GameState.newGame();
        GameState copy = state.copy();
        List<Move> moves = MoveGenerator.legalMovesFor(state.board(), M);

        copy.applyMove(moves.get(0));

        assertEquals(PieceType.MUSKETEER, state.sideToMove());
        assertEquals(PieceType.GUARD, copy.sideToMove());
    }
}
