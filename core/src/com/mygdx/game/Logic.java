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

    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public Cell[][] field;

    private int playerX;
    private int playerY;
    private final int fieldWidth;
    private final int fieldHeight;

    public Logic(int fieldWidth, int fieldHeight) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        // test load field
        this.field = new Cell[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++)
            for (int j = 0; j < field[i].length; j++){
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

        System.out.println("New game field os size (" + fieldWidth + ", " + fieldHeight + ")");
        System.out.println("Player is at (" + playerX + ", " + playerY + ")");
    }

    public void movePlayer(final MoveDirection dir) {
        System.out.println("Moving player at (" + playerX + ", " + playerY + ") in dir " + dir);
        switch (dir) {
            case LEFT:
                if (playerX <= 0) { break; }
                if (field[playerY][playerX - 1].type == CellType.WALL) {break;}
                playerX -= 1;
                break;
            case RIGHT:
                if (playerX + 1 >= fieldWidth) { break; }
                if (field[playerY][playerX + 1].type == CellType.WALL) {break;}
                playerX += 1;
                break;
            case UP:
                if (playerY <= 0) { break; }
                if (field[playerY - 1][playerX].type == CellType.WALL) {break;}
                playerY -= 1;
                break;
            case DOWN:
                if (playerY + 1 >= fieldHeight) { break; }
                if (field[playerY + 1][playerX].type == CellType.WALL) {break;}
                playerY += 1;
                break;
        }
        System.out.println("Player is now at (" + playerX + ", " + playerY + ")");
    }
}
