package com.threemusketeers.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveGeneratorTest {

    private static final PieceType M = PieceType.MUSKETEER;
    private static final PieceType G = PieceType.GUARD;

    @Test
    void musketeerCanOnlyCaptureOrthogonallyAdjacentEnemies() {
        PieceType[][] layout = {
                {null, G, null, null, null},
                {G, M, G, null, null},
                {null, G, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
        };
        Board board = Board.custom(layout);
        List<Position> destinations = MoveGenerator.legalDestinations(board, new Position(1, 1));
        assertEquals(4, destinations.size());
        assertTrue(destinations.containsAll(List.of(
                new Position(0, 1), new Position(2, 1), new Position(1, 0), new Position(1, 2))));
    }

    @Test
    void enemyCanOnlySlideToEmptyAdjacentSquaresAndCannotCaptureMusketeer() {
        PieceType[][] layout = {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, G, M, null},
                {null, null, G, null, null},
                {null, null, null, null, null},
        };
        Board board = Board.custom(layout);
        List<Position> destinations = MoveGenerator.legalDestinations(board, new Position(2, 2));
        assertEquals(2, destinations.size());
        assertTrue(destinations.containsAll(List.of(new Position(1, 2), new Position(2, 1))));
    }

    @Test
    void cornerPieceOnlyConsidersItsTwoOnBoardOrthogonalNeighbors() {
        PieceType[][] layout = {
                {M, G, null, null, null},
                {G, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
        };
        Board board = Board.custom(layout);
        List<Position> destinations = MoveGenerator.legalDestinations(board, new Position(0, 0));
        assertEquals(2, destinations.size());
        assertTrue(destinations.containsAll(List.of(new Position(0, 1), new Position(1, 0))));
    }

    @Test
    void atGameStartOnlyMusketeersHaveLegalMovesSinceTheBoardIsFull() {
        Board board = Board.initial();

        List<Move> musketeerMoves = MoveGenerator.legalMovesFor(board, M);
        assertEquals(12, musketeerMoves.size());
        for (Move move : musketeerMoves) {
            assertEquals(M, board.at(move.from()));
            assertEquals(G, board.at(move.to()));
        }

        List<Move> guardMoves = MoveGenerator.legalMovesFor(board, G);
        assertTrue(guardMoves.isEmpty());
    }
}
