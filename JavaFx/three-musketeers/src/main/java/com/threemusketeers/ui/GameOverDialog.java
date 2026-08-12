package com.threemusketeers.ui;

import com.threemusketeers.model.GameStatus;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

final class GameOverDialog {

    private GameOverDialog() {
    }

    static void show(Window owner, GameStatus status, Runnable onNewGame) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle("Game Over");

        boolean musketeersWin = status == GameStatus.MUSKETEERS_WIN;
        alert.setHeaderText((musketeersWin ? "Musketeers" : "Enemies") + " win!");
        alert.setContentText(musketeersWin
                ? "The Musketeers cleared their surroundings without ever lining up in a row or column."
                : "The Enemies forced all three Musketeers into the same row or column.");

        ButtonType newGameButton = new ButtonType("New Game");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(newGameButton, closeButton);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == newGameButton) {
            onNewGame.run();
        }
    }
}
