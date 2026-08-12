package com.threemusketeers.ui;

import com.threemusketeers.ai.AiPlayer;
import com.threemusketeers.ai.GamePlayer;
import com.threemusketeers.model.GameState;
import com.threemusketeers.model.GameStatus;
import com.threemusketeers.model.PieceType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class ThreeMusketeersApp extends Application {

    private Stage primaryStage;
    private BoardView boardView;
    private Label statusLabel;
    private GameController controller;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        boardView = new BoardView();
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(event -> promptNewGame());

        HBox topBar = new HBox(15, statusLabel, newGameButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.getStyleClass().add("top-bar");

        StackPane boardWrapper = new StackPane(boardView);
        boardWrapper.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(boardWrapper);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/threemusketeers/ui/theme.css").toExternalForm());

        stage.setTitle("Three Musketeers");
        stage.setScene(scene);
        stage.show();

        controller = new GameController(boardView, this::onStateChanged);
        promptNewGame();
    }

    private void promptNewGame() {
        Optional<NewGameDialog.NewGameConfig> config = NewGameDialog.showAndWait(primaryStage);
        startGame(config.orElse(new NewGameDialog.NewGameConfig(
                NewGameDialog.GameMode.HUMAN_VS_HUMAN, PieceType.MUSKETEER)));
    }

    private void startGame(NewGameDialog.NewGameConfig config) {
        Map<PieceType, GamePlayer> aiPlayers = new EnumMap<>(PieceType.class);
        if (config.mode() == NewGameDialog.GameMode.HUMAN_VS_AI) {
            PieceType aiSide = config.humanSide().opposite();
            aiPlayers.put(aiSide, new AiPlayer(aiSide));
        }
        controller.startNewGame(aiPlayers);
    }

    private void onStateChanged(GameState state) {
        updateStatusLabel(state);
        if (state.status() != GameStatus.IN_PROGRESS) {
            GameOverDialog.show(primaryStage, state.status(), this::promptNewGame);
        }
    }

    private void updateStatusLabel(GameState state) {
        if (state.status() != GameStatus.IN_PROGRESS) {
            String winner = state.status() == GameStatus.MUSKETEERS_WIN ? "Musketeers" : "Enemies";
            statusLabel.setText(winner + " win!");
            return;
        }
        String sideName = state.sideToMove() == PieceType.MUSKETEER ? "Musketeers" : "Enemies";
        statusLabel.setText(controller.isAiTurn() ? sideName + " (AI) is thinking..." : sideName + "'s turn");
    }
}
