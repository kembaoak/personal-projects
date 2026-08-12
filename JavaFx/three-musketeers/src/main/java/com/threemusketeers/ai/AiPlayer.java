package com.threemusketeers.ai;

import com.threemusketeers.model.GameState;
import com.threemusketeers.model.Move;
import com.threemusketeers.model.PieceType;

public final class AiPlayer implements GamePlayer {

    public static final int DEFAULT_DEPTH = 3;

    private final Minimax minimax;

    public AiPlayer(PieceType side) {
        this(side, DEFAULT_DEPTH);
    }

    public AiPlayer(PieceType side, int depth) {
        Evaluator evaluator = side == PieceType.GUARD ? new GuardEvaluator() : new MusketeerEvaluator();
        this.minimax = new Minimax(evaluator, side, depth);
    }

    @Override
    public Move chooseMove(GameState state) {
        return minimax.findBestMove(state);
    }
}
