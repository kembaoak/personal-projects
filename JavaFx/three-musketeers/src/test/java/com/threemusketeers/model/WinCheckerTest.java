package com.threemusketeers.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinCheckerTest {

    private static final PieceType M = PieceType.MUSKETEER;

    @Test
    void alignedInTheSameRowCountsAsAligned() {
        PieceType[][] layout = {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {M, null, M, null, M},
                {null, null, null, null, null},
                {null, null, null, null, null},
        };
        assertTrue(WinChecker.isMusketeersAligned(Board.custom(layout)));
    }

    @Test
    void alignedInTheSameColumnCountsAsAligned() {
        PieceType[][] layout = {
                {null, null, M, null, null},
                {null, null, null, null, null},
                {null, null, M, null, null},
                {null, null, null, null, null},
                {null, null, M, null, null},
        };
        assertTrue(WinChecker.isMusketeersAligned(Board.custom(layout)));
    }

    @Test
    void scatteredMusketeersAreNotAligned() {
        PieceType[][] layout = {
                {M, null, null, null, null},
                {null, null, null, M, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, M, null, null},
        };
        assertFalse(WinChecker.isMusketeersAligned(Board.custom(layout)));
    }

    @Test
    void evaluateReturnsEnemiesWinWheneverAlignedRegardlessOfWhoseTurn() {
        PieceType[][] layout = {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {M, null, M, null, M},
                {null, null, null, null, null},
                {null, null, null, null, null},
        };
        Board board = Board.custom(layout);
        assertEquals(GameStatus.GUARDS_WIN, WinChecker.evaluate(board, PieceType.MUSKETEER));
        assertEquals(GameStatus.GUARDS_WIN, WinChecker.evaluate(board, PieceType.GUARD));
    }

    @Test
    void evaluateReturnsMusketeersWinOnlyWhenItIsTheirTurnWithNoAdjacentEnemyAndNotAligned() {
        PieceType[][] layout = {
                {M, null, null, null, null},
                {null, null, null, null, null},
                {null, null, M, null, null},
                {null, null, null, null, null},
                {null, null, null, null, M},
        };
        Board board = Board.custom(layout);
        assertFalse(WinChecker.isMusketeersAligned(board));
        assertTrue(MoveGenerator.legalMovesFor(board, M).isEmpty());

        assertEquals(GameStatus.MUSKETEERS_WIN, WinChecker.evaluate(board, PieceType.MUSKETEER));
        // Not their turn yet, so the same stuck position isn't a loss until it actually is.
        assertEquals(GameStatus.IN_PROGRESS, WinChecker.evaluate(board, PieceType.GUARD));
    }

    @Test
    void evaluateIsInProgressWhenMusketeersHaveCapturesAndAreNotAligned() {
        Board board = Board.initial();
        assertEquals(GameStatus.IN_PROGRESS, WinChecker.evaluate(board, PieceType.MUSKETEER));
    }
}
