package com.threemusketeers.ai;

import com.threemusketeers.model.GameState;
import com.threemusketeers.model.Move;

public interface GamePlayer {
    Move chooseMove(GameState state);
}
