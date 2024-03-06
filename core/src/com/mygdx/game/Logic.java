package com.mygdx.game;

public class Logic {
    public enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN,
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

    private int playerX;
    private int playerY;
    private final int fieldWidth;
    private final int fieldHeight;

    public Logic(int fieldWidth, int fieldHeight) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;

        System.out.println("New game field os size (" + fieldWidth + ", " + fieldHeight + ")");
        System.out.println("Player is at (" + playerX + ", " + playerY + ")");
    }

    public void movePlayer(final MoveDirection dir) {
        System.out.println("Moving player at (" + playerX + ", " + playerY + ") in dir " + dir);
        switch (dir) {
            case LEFT:
                if (playerX <= 0) { break; }
                playerX -= 1;
                break;
            case RIGHT:
                if (playerX + 1 >= fieldWidth) { break; }
                playerX += 1;
                break;
            case UP:
                if (playerY <= 0) { break; }
                playerY -= 1;
                break;
            case DOWN:
                if (playerY + 1 >= fieldHeight) { break; }
                playerY += 1;
                break;
        }
        System.out.println("Player is now at (" + playerX + ", " + playerY + ")");
    }
}
