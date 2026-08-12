package com.threemusketeers.ai;

import com.threemusketeers.model.Board;
import com.threemusketeers.model.GameState;
import com.threemusketeers.model.PieceType;
import com.threemusketeers.model.Position;

import java.util.List;

/**
 * Enemies win by forcing all 3 Musketeers into the same row or column, and
 * lose their only lever (adjacency) if too many of them get captured.
 */
public final class GuardEvaluator implements Evaluator {

    private static final double ALIGNMENT_WEIGHT = 10.0;
    private static final double SURVIVAL_WEIGHT = 0.5;

    @Override
    public double evaluate(GameState state) {
        Board board = state.board();
        List<Position> musketeers = board.positionsOf(PieceType.MUSKETEER);

        int alignmentProgress = Alignment.progress(musketeers);
        int enemyCount = board.positionsOf(PieceType.GUARD).size();

        return ALIGNMENT_WEIGHT * alignmentProgress + SURVIVAL_WEIGHT * enemyCount;
    }
}
