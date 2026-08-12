package com.threemusketeers.ai;

import com.threemusketeers.model.GameState;

/**
 * Scores how good a position is for the side this evaluator represents.
 * Higher is always better for that side, regardless of whose turn it is.
 */
public interface Evaluator {
    double evaluate(GameState state);
}
