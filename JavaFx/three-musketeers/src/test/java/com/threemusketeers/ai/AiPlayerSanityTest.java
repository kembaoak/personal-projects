package com.threemusketeers.ai;

import com.threemusketeers.model.Board;
import com.threemusketeers.model.GameState;
import com.threemusketeers.model.GameStatus;
import com.threemusketeers.model.Move;
import com.threemusketeers.model.MoveGenerator;
import com.threemusketeers.model.PieceType;
import com.threemusketeers.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPlayerSanityTest {

    private static final PieceType M = PieceType.MUSKETEER;
    private static final PieceType G = PieceType.GUARD;

    @Test
    void aiAlwaysReturnsALegalMove() {
        GameState musketeerTurn = GameState.newGame();
        AiPlayer musketeerAi = new AiPlayer(M, 2);
        Move musketeerMove = musketeerAi.chooseMove(musketeerTurn);
        assertTrue(MoveGenerator.legalMovesFor(musketeerTurn.board(), M).contains(musketeerMove));

        GameState guardTurn = musketeerTurn.copy();
        guardTurn.applyMove(musketeerMove);
        AiPlayer guardAi = new AiPlayer(G, 2);
        Move guardMove = guardAi.chooseMove(guardTurn);
        assertTrue(MoveGenerator.legalMovesFor(guardTurn.board(), G).contains(guardMove));
    }

    @Test
    void musketeerAiAvoidsASelfInflictedAlignmentLossWhenASafeAlternativeExists() {
        // M3 has two capture options: one lands it in row 0, aligning all
        // three Musketeers (an immediate, self-inflicted loss); the other
        // keeps it clear. A sane Musketeer AI must pick the safe one.
        PieceType[][] layout = {
                {M, null, G, null, M},
                {null, null, M, null, null},
                {G, G, G, G, G},
                {G, G, G, G, G},
                {G, G, G, G, G},
        };
        Board board = Board.custom(layout);
        GameState state = new GameState(board, M);
        assertEquals(GameStatus.IN_PROGRESS, state.status());
        assertEquals(2, MoveGenerator.legalMovesFor(board, M).size());

        AiPlayer musketeerAi = new AiPlayer(M, 2);
        Move chosen = musketeerAi.chooseMove(state);

        GameState result = state.copy();
        result.applyMove(chosen);

        assertEquals(GameStatus.IN_PROGRESS, result.status());
        assertEquals(new Position(2, 2), chosen.to());
    }

    @Test
    void depthThreeSearchCompletesQuickly() {
        GameState state = GameState.newGame();
        AiPlayer musketeerAi = new AiPlayer(M, AiPlayer.DEFAULT_DEPTH);

        long start = System.nanoTime();
        Move move = musketeerAi.chooseMove(state);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertNotNull(move);
        assertTrue(elapsedMs < 5000, "depth-3 search took too long: " + elapsedMs + "ms");
    }
}
