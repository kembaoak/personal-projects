package com.threemusketeers.ai;

import com.threemusketeers.model.Board;
import com.threemusketeers.model.GameState;
import com.threemusketeers.model.MoveGenerator;
import com.threemusketeers.model.PieceType;
import com.threemusketeers.model.Position;

import java.util.List;

/**
 * Musketeers must avoid ending up all in the same row/column, while chipping
 * away at Enemies (their only legal moves are captures) toward the state
 * where no Enemy remains adjacent to any of them.
 */
public final class MusketeerEvaluator implements Evaluator {

    private static final double ALIGNMENT_WEIGHT = 10.0;
    private static final double MOBILITY_WEIGHT = 1.0;
    private static final double ENEMY_COUNT_WEIGHT = 0.3;

    @Override
    public double evaluate(GameState state) {
        Board board = state.board();
        List<Position> musketeers = board.positionsOf(PieceType.MUSKETEER);

        int alignmentProgress = Alignment.progress(musketeers);
        int captureOptions = MoveGenerator.legalMovesFor(board, PieceType.MUSKETEER).size();
        int enemyCount = board.positionsOf(PieceType.GUARD).size();

        return -ALIGNMENT_WEIGHT * alignmentProgress
                + MOBILITY_WEIGHT * captureOptions
                - ENEMY_COUNT_WEIGHT * enemyCount;
    }
}
