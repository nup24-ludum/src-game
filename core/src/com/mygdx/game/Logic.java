package com.mygdx.game;

public class Logic {
    public enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN,
    }

    public enum CellType {
        FLOR,
        WALL,
        ENTRANCE,
        TREASURE
    }
    public static class Cell {
        CellType type;
        boolean hasShadow;

        Cell (CellType type, boolean hasShadow) {
            this.type = type;
            this.hasShadow = hasShadow;
        }

        public String toShortString() {
            if (hasShadow) {
                return "X";
            }

            switch (type) {
                case FLOR: return " ";
                case WALL: return "W";
                case ENTRANCE: return "E";
                case TREASURE: return "$";
            }

            return "?";
        }
    }

    public static class Pos {
        public int x;
        public int y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Pos applyDir(final MoveDirection dir) {
            switch (dir) {
                case LEFT:  return new Pos(x - 1, y);
                case RIGHT: return new Pos(x + 1, y);
                case UP:    return new Pos(x, y - 1);
                case DOWN:  return new Pos(x, y + 1);
            }

            return this;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public Pos getPlayerPos() {
        return playerPos;
    }

    public Cell[][] getField() {
        return field;
    }

    private final Cell[][] field;
    private Pos playerPos;
    private final int fieldWidth;
    private final int fieldHeight;

    public Logic(int fieldWidth, int fieldHeight) {
        // TODO make this constructor argument
        this.playerPos = new Pos(0, 0);
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        // test load field
        this.field = new Cell[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (i == 0 && j == 0) {
                    field[i][j] = new Cell(CellType.FLOR, false);
                    continue;
                }
                if (i == 1 && j == 0) {
                    field[i][j] = new Cell(CellType.FLOR, false);
                    continue;
                }
                if (i == 0 || i == field.length - 1 || j == 0) {
                    field[i][j] = new Cell(CellType.WALL, false);
                    continue;
                }
                field[i][j] = new Cell(CellType.FLOR, false);
            }
        }

        System.out.println("Field dump:");
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                System.out.print(field[y][x].toShortString());
            }
            System.out.print("\n");
        }

        System.out.println("New game field os size (" + fieldWidth + ", " + fieldHeight + ")");
        System.out.println("Player is at " + playerPos);
    }

    public void movePlayer(final MoveDirection dir) {
        System.out.println("Moving player at " + playerPos + " in dir " + dir);
        final Pos newPos = playerPos.applyDir(dir);
        if (posValid(newPos)) {
            playerPos = newPos;
        } else {
            System.out.println("Player move rejected");
        }
        System.out.println("Player is now at " + playerPos);
    }

    private boolean posValid(final Pos pos) {
        if (pos.x < 0 || pos.y < 0) {
            return false;
        }

        if (pos.x >= fieldWidth || pos.y >= fieldHeight) {
            return false;
        }

        if (field[pos.x][pos.y].type == CellType.WALL) {
            return false;
        }

        return !field[pos.x][pos.y].hasShadow;
    }
}
