package com.threemusketeers.model;

import java.util.ArrayList;
import java.util.List;

public record Position(int row, int col) {

    public static final int SIZE = 5;

    public boolean isOnBoard() {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public List<Position> orthogonalNeighbors() {
        List<Position> result = new ArrayList<>(4);
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] delta : deltas) {
            Position candidate = new Position(row + delta[0], col + delta[1]);
            if (candidate.isOnBoard()) {
                result.add(candidate);
            }
        }
        return result;
    }
}
