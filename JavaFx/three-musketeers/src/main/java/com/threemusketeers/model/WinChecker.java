package com.threemusketeers.model;

import java.util.List;
import java.util.stream.Collectors;

public final class WinChecker {

    private WinChecker() {
    }

    /**
     * Enemies win the moment all 3 Musketeers share the same row or the same
     * column, however that came about.
     */
    public static boolean isMusketeersAligned(Board board) {
        List<Position> musketeers = board.positionsOf(PieceType.MUSKETEER);
        long distinctRows = musketeers.stream().map(Position::row).collect(Collectors.toSet()).size();
        long distinctCols = musketeers.stream().map(Position::col).collect(Collectors.toSet()).size();
        return distinctRows == 1 || distinctCols == 1;
    }

    /**
     * Alignment (an Enemy win) is checked first, since the Musketeer win
     * condition explicitly requires the Musketeers to NOT be aligned - the
     * two conditions are mutually exclusive by construction.
     */
    public static GameStatus evaluate(Board board, PieceType sideToMove) {
        if (isMusketeersAligned(board)) {
            return GameStatus.GUARDS_WIN;
        }
        if (sideToMove == PieceType.MUSKETEER && MoveGenerator.legalMovesFor(board, PieceType.MUSKETEER).isEmpty()) {
            return GameStatus.MUSKETEERS_WIN;
        }
        return GameStatus.IN_PROGRESS;
    }
}
