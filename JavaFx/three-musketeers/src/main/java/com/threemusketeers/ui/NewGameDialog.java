package com.threemusketeers.ui;

import com.threemusketeers.model.PieceType;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class NewGameDialog {

    enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_AI
    }

    record NewGameConfig(GameMode mode, PieceType humanSide) {
    }

    private NewGameDialog() {
    }

    static Optional<NewGameConfig> showAndWait(Window owner) {
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("New Game");
        stage.setResizable(false);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton humanVsHuman = new RadioButton("Human vs Human");
        humanVsHuman.setToggleGroup(modeGroup);
        humanVsHuman.setSelected(true);
        RadioButton humanVsAi = new RadioButton("Human vs AI");
        humanVsAi.setToggleGroup(modeGroup);

        ToggleGroup sideGroup = new ToggleGroup();
        RadioButton playMusketeers = new RadioButton("Play as Musketeers");
        playMusketeers.setToggleGroup(sideGroup);
        playMusketeers.setSelected(true);
        RadioButton playGuards = new RadioButton("Play as Enemies");
        playGuards.setToggleGroup(sideGroup);

        VBox sideBox = new VBox(6, playMusketeers, playGuards);
        sideBox.setPadding(new Insets(0, 0, 0, 20));
        sideBox.disableProperty().bind(humanVsAi.selectedProperty().not());

        Button startButton = new Button("Start");
        AtomicReference<NewGameConfig> result = new AtomicReference<>();
        startButton.setOnAction(event -> {
            GameMode mode = humanVsAi.isSelected() ? GameMode.HUMAN_VS_AI : GameMode.HUMAN_VS_HUMAN;
            PieceType humanSide = playGuards.isSelected() ? PieceType.GUARD : PieceType.MUSKETEER;
            result.set(new NewGameConfig(mode, humanSide));
            stage.close();
        });

        VBox root = new VBox(12, humanVsHuman, humanVsAi, sideBox, startButton);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("new-game-dialog");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(NewGameDialog.class.getResource("/com/threemusketeers/ui/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();

        return Optional.ofNullable(result.get());
    }
}
