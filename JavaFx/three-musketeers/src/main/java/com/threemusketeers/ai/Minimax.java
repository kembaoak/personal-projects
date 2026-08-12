package com.threemusketeers.ai;

import com.threemusketeers.model.GameState;
import com.threemusketeers.model.GameStatus;
import com.threemusketeers.model.Move;
import com.threemusketeers.model.MoveGenerator;
import com.threemusketeers.model.PieceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Alpha-beta search that always maximizes {@link Evaluator#evaluate} for a
 * single fixed side ({@code aiSide}), whichever ply is currently on move.
 */
public final class Minimax {

    private static final double EPSILON = 1e-9;
    private static final double WIN_SCORE = 100_000;

    private final Evaluator evaluator;
    private final PieceType aiSide;
    private final int maxDepth;
    private final Random random = new Random();

    public Minimax(Evaluator evaluator, PieceType aiSide, int maxDepth) {
        this.evaluator = evaluator;
        this.aiSide = aiSide;
        this.maxDepth = maxDepth;
    }

    public Move findBestMove(GameState state) {
        List<Move> moves = MoveGenerator.legalMovesFor(state.board(), state.sideToMove());
        if (moves.isEmpty()) {
            throw new IllegalStateException("No legal moves available for " + state.sideToMove());
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        List<Move> bestMoves = new ArrayList<>();
        for (Move move : moves) {
            GameState next = state.copy();
            next.applyMove(move);
            double score = search(next, maxDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (score > bestScore + EPSILON) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (Math.abs(score - bestScore) <= EPSILON) {
                bestMoves.add(move);
            }
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private double search(GameState state, int depthRemaining, double alpha, double beta) {
        if (state.status() != GameStatus.IN_PROGRESS) {
            return terminalScore(state.status(), depthRemaining);
        }
        if (depthRemaining == 0) {
            return evaluator.evaluate(state);
        }

        List<Move> moves = MoveGenerator.legalMovesFor(state.board(), state.sideToMove());
        boolean maximizing = state.sideToMove() == aiSide;

        double best = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (Move move : moves) {
            GameState next = state.copy();
            next.applyMove(move);
            double score = search(next, depthRemaining - 1, alpha, beta);
            if (maximizing) {
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, score);
                beta = Math.min(beta, best);
            }
            if (alpha >= beta) {
                break;
            }
        }
        return best;
    }

    private double terminalScore(GameStatus status, int depthRemaining) {
        boolean aiWins = (status == GameStatus.MUSKETEERS_WIN && aiSide == PieceType.MUSKETEER)
                || (status == GameStatus.GUARDS_WIN && aiSide == PieceType.GUARD);
        double magnitude = WIN_SCORE + depthRemaining;
        return aiWins ? magnitude : -magnitude;
    }
}
