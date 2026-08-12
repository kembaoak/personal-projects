package com.threemusketeers.model;

public enum PieceType {
    MUSKETEER,
    GUARD;

    public PieceType opposite() {
        return this == MUSKETEER ? GUARD : MUSKETEER;
    }
}
