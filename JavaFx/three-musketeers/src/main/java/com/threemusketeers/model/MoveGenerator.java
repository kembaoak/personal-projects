package com.threemusketeers.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Musketeers and Enemies (Guards) move differently: a Musketeer's only legal
 * move is capturing an orthogonally-adjacent Enemy (stepping onto its
 * square); an Enemy's only legal move is sliding onto an orthogonally-adjacent
 * empty square. Enemies can never capture a Musketeer.
 */
public final class MoveGenerator {

    private MoveGenerator() {
    }

    public static List<Position> legalDestinations(Board board, Position from) {
        PieceType mover = board.at(from);
        List<Position> destinations = new ArrayList<>();
        if (mover == null) {
            return destinations;
        }
        for (Position neighbor : from.orthogonalNeighbors()) {
            if (mover == PieceType.MUSKETEER) {
                if (board.at(neighbor) == PieceType.GUARD) {
                    destinations.add(neighbor);
                }
            } else {
                if (board.isEmpty(neighbor)) {
                    destinations.add(neighbor);
                }
            }
        }
        return destinations;
    }

    public static List<Move> legalMovesFor(Board board, PieceType side) {
        List<Move> moves = new ArrayList<>();
        for (Position from : board.positionsOf(side)) {
            for (Position to : legalDestinations(board, from)) {
                moves.add(new Move(from, to));
            }
        }
        return moves;
    }
}
