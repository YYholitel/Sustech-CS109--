package model;

public class Cell {
    Position pos;
    boolean isEmpty;
    int iconIndex;
    boolean isChosen;

    public Cell(Position pos, boolean isEmpty, int iconIndex) {
        this.pos = pos;
        this.isEmpty = isEmpty;
        this.iconIndex = iconIndex;
    }

    public boolean getIsChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public Position getPos() {
        return pos;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
        iconIndex = 0;
    }
}