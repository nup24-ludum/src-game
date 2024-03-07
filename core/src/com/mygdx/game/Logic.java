package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Logic {
    // TODO things probably should be made into classes
    public enum ThingType {
        PLAYER,
        BOX,
    }
    public enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN,
    }
    public enum CellType {
        FLOOR,
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
                case FLOOR: return " ";
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

        @Override
        public int hashCode () {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals (Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Pos other = (Pos)obj;
            if (x != other.x) return false;
            if (y != other.y) return false;
            return true;
        }
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public Cell getCell(final int x, final int y) {
        return field[y][x];
    }

    private final Map<Pos, ThingType> thingTypeMap;
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
        this.thingTypeMap = new HashMap<>();
        this.field = new Cell[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (i == 0 && j == 0) {
                    field[i][j] = new Cell(CellType.FLOOR, false);
                    continue;
                }
                if (i == 1 && j == 0) {
                    field[i][j] = new Cell(CellType.FLOOR, false);
                    continue;
                }
                if (i == 0 || i == field.length - 1 || j == 0) {
                    field[i][j] = new Cell(CellType.WALL, false);
                    continue;
                }
                field[i][j] = new Cell(CellType.FLOOR, false);
            }
        }

        System.out.println("Field dump:");
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                System.out.print(field[y][x].toShortString());
            }
            System.out.print("\n");
        }

        thingTypeMap.put(playerPos, ThingType.PLAYER);
        thingTypeMap.put(new Pos(1, 1), ThingType.BOX); // TODO "spawn_thing" method?
        thingTypeMap.put(new Pos(3, 3), ThingType.BOX);

        System.out.println("New game field os size (" + fieldWidth + ", " + fieldHeight + ")");
        System.out.println("Player is at " + playerPos);
    }

    public void movePlayer(final MoveDirection dir) {
        if (moveThing(playerPos, dir)) {
            playerPos = playerPos.applyDir(dir);
        }
    }

    public Stream<Map.Entry<Pos, ThingType>> allThings() {
        return thingTypeMap.entrySet().stream();
    }

    private boolean moveThing(final Pos thingPos, final MoveDirection dir) {
        final Pos newThingPos = thingPos.applyDir(dir);

        if (!thingTypeMap.containsKey(thingPos)) {
            return true; // Might want to return false
        }

        if (!posValid(newThingPos)) {
            return false;
        }

        if (!thingTypeMap.containsKey(newThingPos)) {
            thingTypeMap.put(newThingPos, thingTypeMap.remove(thingPos));
            return true;
        }

        /* Obstacle at newPos. Let's try to push it away */
        final Pos obstacleNewPos = newThingPos.applyDir(dir);
        if (!posValid(obstacleNewPos)) {
            return false;
        }
        if (thingTypeMap.containsKey(obstacleNewPos)) {
            return false;
        }

        thingTypeMap.put(obstacleNewPos, thingTypeMap.remove(newThingPos));
        thingTypeMap.put(newThingPos, thingTypeMap.remove(thingPos));

        return true;
    }

    private boolean posValid(final Pos pos) {
        if (pos.x < 0 || pos.y < 0) {
            return false;
        }

        if (pos.x >= fieldWidth || pos.y >= fieldHeight) {
            return false;
        }

        final Cell cell = getCell(pos.x, pos.y);

        if (cell.type == CellType.WALL) {
            return false;
        }

        return !cell.hasShadow;
    }
}
