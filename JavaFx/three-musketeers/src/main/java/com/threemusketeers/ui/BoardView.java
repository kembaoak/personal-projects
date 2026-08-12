package com.threemusketeers.ui;

import com.threemusketeers.model.Board;
import com.threemusketeers.model.Position;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class BoardView extends GridPane {

    private final CellView[][] cells = new CellView[Position.SIZE][Position.SIZE];
    private Consumer<Position> clickHandler = position -> { };

    public BoardView() {
        getStyleClass().add("board");
        for (int row = 0; row < Position.SIZE; row++) {
            for (int col = 0; col < Position.SIZE; col++) {
                Position position = new Position(row, col);
                CellView cell = new CellView(position);
                cell.setOnMouseClicked(event -> clickHandler.accept(cell.position()));
                cells[row][col] = cell;
                add(cell, col, row);
            }
        }
    }

    public void setOnCellClicked(Consumer<Position> handler) {
        this.clickHandler = handler;
    }

    public void render(Board board) {
        for (CellView[] row : cells) {
            for (CellView cell : row) {
                cell.setPieceType(board.at(cell.position()));
            }
        }
    }

    public void setSelected(Position selected) {
        for (CellView[] row : cells) {
            for (CellView cell : row) {
                cell.setSelected(cell.position().equals(selected));
            }
        }
    }

    public void setLegalDestinations(Set<Position> destinations) {
        Set<Position> safe = destinations == null ? Set.of() : new HashSet<>(destinations);
        for (CellView[] row : cells) {
            for (CellView cell : row) {
                cell.setLegalDestination(safe.contains(cell.position()));
            }
        }
    }
}
