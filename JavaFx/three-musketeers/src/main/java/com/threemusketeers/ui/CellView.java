package com.threemusketeers.ui;

import com.threemusketeers.model.PieceType;
import com.threemusketeers.model.Position;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

final class CellView extends StackPane {

    private static final double SIZE = 64;
    private static final String MUSKETEER_ICON = "🤺"; // 🤺 fencer
    private static final String ENEMY_ICON = "🪖"; // 🪖 military helmet

    private final Position position;
    private final Rectangle background;
    private final Circle token;
    private final Text icon;

    CellView(Position position) {
        this.position = position;

        background = new Rectangle(SIZE, SIZE);
        background.getStyleClass().add("cell-background");
        background.getStyleClass().add((position.row() + position.col()) % 2 == 0 ? "cell-light" : "cell-dark");

        token = new Circle(SIZE / 2.0 - 6);
        token.getStyleClass().add("piece-token");
        token.setMouseTransparent(true);
        token.setVisible(false);

        icon = new Text();
        icon.getStyleClass().add("piece-icon");
        icon.setMouseTransparent(true);
        icon.setVisible(false);

        getChildren().addAll(background, token, icon);
        getStyleClass().add("cell");

        setOnMouseEntered(event -> setStyleClassPresence("hover", true));
        setOnMouseExited(event -> setStyleClassPresence("hover", false));
    }

    Position position() {
        return position;
    }

    void setPieceType(PieceType type) {
        if (type == null) {
            token.setVisible(false);
            icon.setVisible(false);
            return;
        }
        token.setVisible(true);
        icon.setVisible(true);
        token.getStyleClass().removeAll("musketeer-token", "enemy-token");
        icon.getStyleClass().removeAll("musketeer-icon", "enemy-icon");
        if (type == PieceType.MUSKETEER) {
            token.getStyleClass().add("musketeer-token");
            icon.getStyleClass().add("musketeer-icon");
            icon.setText(MUSKETEER_ICON);
        } else {
            token.getStyleClass().add("enemy-token");
            icon.getStyleClass().add("enemy-icon");
            icon.setText(ENEMY_ICON);
        }
    }

    void setSelected(boolean selected) {
        setStyleClassPresence("selected", selected);
    }

    void setLegalDestination(boolean legal) {
        setStyleClassPresence("legal-destination", legal);
    }

    private void setStyleClassPresence(String styleClass, boolean present) {
        if (present) {
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        } else {
            getStyleClass().remove(styleClass);
        }
    }
}
