package com.threemusketeers.model;

import java.util.ArrayList;
import java.util.List;

public final class Board {

    private static final List<Position> MUSKETEER_START = List.of(
            new Position(1, 2), new Position(3, 1), new Position(3, 3));

    private final PieceType[][] grid;

    private Board(PieceType[][] grid) {
        this.grid = grid;
    }

    /**
     * The traditional 5x5 starting position: 3 Musketeers, 22 Enemies, and no
     * empty squares. Musketeers move first and can only move by capturing an
     * adjacent Enemy, so every square being occupied at the start is
     * intentional.
     */
    public static Board initial() {
        PieceType[][] grid = new PieceType[Position.SIZE][Position.SIZE];
        for (int row = 0; row < Position.SIZE; row++) {
            for (int col = 0; col < Position.SIZE; col++) {
                grid[row][col] = PieceType.GUARD;
            }
        }
        for (Position p : MUSKETEER_START) {
            grid[p.row()][p.col()] = PieceType.MUSKETEER;
        }
        return new Board(grid);
    }

    /**
     * Builds a board from an arbitrary layout. Intended for tests that need
     * to construct specific positions rather than playing out {@link #initial()}.
     */
    public static Board custom(PieceType[][] layout) {
        if (layout.length != Position.SIZE) {
            throw new IllegalArgumentException("Layout must have " + Position.SIZE + " rows");
        }
        PieceType[][] copiedGrid = new PieceType[Position.SIZE][Position.SIZE];
        for (int row = 0; row < Position.SIZE; row++) {
            if (layout[row].length != Position.SIZE) {
                throw new IllegalArgumentException("Layout row " + row + " must have " + Position.SIZE + " columns");
            }
            System.arraycopy(layout[row], 0, copiedGrid[row], 0, Position.SIZE);
        }
        return new Board(copiedGrid);
    }

    public PieceType at(Position p) {
        return grid[p.row()][p.col()];
    }

    public boolean isEmpty(Position p) {
        return at(p) == null;
    }

    /**
     * Moves the piece at {@code move.from()} to {@code move.to()}. If an
     * Enemy occupied the destination, it is captured (simply overwritten).
     */
    public void move(Move move) {
        PieceType piece = at(move.from());
        grid[move.from().row()][move.from().col()] = null;
        grid[move.to().row()][move.to().col()] = piece;
    }

    public List<Position> positionsOf(PieceType type) {
        List<Position> result = new ArrayList<>();
        for (int row = 0; row < Position.SIZE; row++) {
            for (int col = 0; col < Position.SIZE; col++) {
                if (grid[row][col] == type) {
                    result.add(new Position(row, col));
                }
            }
        }
        return result;
    }

    public Board copy() {
        PieceType[][] copiedGrid = new PieceType[Position.SIZE][Position.SIZE];
        for (int row = 0; row < Position.SIZE; row++) {
            System.arraycopy(grid[row], 0, copiedGrid[row], 0, Position.SIZE);
        }
        return new Board(copiedGrid);
    }
}
