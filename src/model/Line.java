package model;

public class Line {
    Position start;
    Position end;

    public Line(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }
}
