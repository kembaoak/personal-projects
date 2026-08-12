package com.threemusketeers.ui;

import com.threemusketeers.ai.GamePlayer;
import com.threemusketeers.model.GameState;
import com.threemusketeers.model.GameStatus;
import com.threemusketeers.model.Move;
import com.threemusketeers.model.MoveGenerator;
import com.threemusketeers.model.PieceType;
import com.threemusketeers.model.Position;
import javafx.concurrent.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mediates between the board UI and the rules engine: handles click-to-select
 * / click-to-move interaction and kicks off background AI turns.
 */
final class GameController {

    private final BoardView boardView;
    private final Consumer<GameState> onStateChanged;

    private Map<PieceType, GamePlayer> aiPlayers = Map.of();
    private GameState gameState;
    private Position selected;
    private boolean aiThinking;

    GameController(BoardView boardView, Consumer<GameState> onStateChanged) {
        this.boardView = boardView;
        this.onStateChanged = onStateChanged;
        boardView.setOnCellClicked(this::onCellClicked);
    }

    void startNewGame(Map<PieceType, GamePlayer> aiPlayers) {
        this.aiPlayers = aiPlayers;
        this.gameState = GameState.newGame();
        this.selected = null;
        this.aiThinking = false;
        refreshView();
        maybeTriggerAiMove();
    }

    boolean isAiTurn() {
        return gameState != null
                && gameState.status() == GameStatus.IN_PROGRESS
                && aiPlayers.containsKey(gameState.sideToMove());
    }

    private void onCellClicked(Position position) {
        if (gameState == null || aiThinking || gameState.status() != GameStatus.IN_PROGRESS) {
            return;
        }
        if (aiPlayers.containsKey(gameState.sideToMove())) {
            return;
        }

        PieceType clicked = gameState.board().at(position);
        if (selected == null) {
            if (clicked == gameState.sideToMove()) {
                selected = position;
                refreshView();
            }
            return;
        }
        if (position.equals(selected)) {
            selected = null;
            refreshView();
            return;
        }
        if (clicked == gameState.sideToMove()) {
            selected = position;
            refreshView();
            return;
        }

        List<Position> destinations = MoveGenerator.legalDestinations(gameState.board(), selected);
        if (destinations.contains(position)) {
            applyMove(new Move(selected, position));
        }
    }

    private void applyMove(Move move) {
        gameState.applyMove(move);
        selected = null;
        refreshView();
        maybeTriggerAiMove();
    }

    private void maybeTriggerAiMove() {
        if (gameState.status() != GameStatus.IN_PROGRESS) {
            return;
        }
        GamePlayer aiPlayer = aiPlayers.get(gameState.sideToMove());
        if (aiPlayer == null) {
            return;
        }

        aiThinking = true;
        onStateChanged.accept(gameState);

        GameState snapshot = gameState.copy();
        Task<Move> task = new Task<>() {
            @Override
            protected Move call() {
                return aiPlayer.chooseMove(snapshot);
            }
        };
        task.setOnSucceeded(event -> {
            aiThinking = false;
            applyMove(task.getValue());
        });
        task.setOnFailed(event -> {
            aiThinking = false;
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });
        Thread thread = new Thread(task, "ai-search");
        thread.setDaemon(true);
        thread.start();
    }

    private void refreshView() {
        boardView.render(gameState.board());
        boardView.setSelected(selected);
        Set<Position> destinations = selected == null
                ? Set.of()
                : new HashSet<>(MoveGenerator.legalDestinations(gameState.board(), selected));
        boardView.setLegalDestinations(destinations);
        onStateChanged.accept(gameState);
    }
}
