package com.threemusketeers.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class BoardTest {

    @Test
    void initialLayoutHasCorrectPieceCounts() {
        Board board = Board.initial();
        assertEquals(3, board.positionsOf(PieceType.MUSKETEER).size());
        assertEquals(22, board.positionsOf(PieceType.GUARD).size());
        assertEquals(0, emptyCellCount(board));
    }

    @Test
    void musketeerCountIsInvariantSinceEnemiesCanNeverCapture() {
        Board board = Board.initial();
        Random random = new Random(7);
        PieceType side = PieceType.MUSKETEER;

        for (int i = 0; i < 200; i++) {
            assertEquals(3, board.positionsOf(PieceType.MUSKETEER).size(), "musketeer count changed after move " + i);
            List<Move> moves = MoveGenerator.legalMovesFor(board, side);
            if (moves.isEmpty()) {
                break;
            }
            Move move = moves.get(random.nextInt(moves.size()));
            board.move(move);
            side = side.opposite();
        }
        assertEquals(3, board.positionsOf(PieceType.MUSKETEER).size());
    }

    @Test
    void copyIsADeepCopyNotAffectedByMutatingOriginal() {
        Board board = Board.initial();
        Board copy = board.copy();
        assertNotSame(board, copy);

        Move move = MoveGenerator.legalMovesFor(board, PieceType.MUSKETEER).get(0);
        board.move(move);

        assertEquals(PieceType.MUSKETEER, copy.at(move.from()));
        assertEquals(PieceType.MUSKETEER, board.at(move.to()));
        assertNull(board.at(move.from()));
    }

    private static int emptyCellCount(Board board) {
        int count = 0;
        for (int row = 0; row < Position.SIZE; row++) {
            for (int col = 0; col < Position.SIZE; col++) {
                if (board.isEmpty(new Position(row, col))) {
                    count++;
                }
            }
        }
        return count;
    }
}
