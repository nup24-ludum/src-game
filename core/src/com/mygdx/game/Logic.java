package com.mygdx.game;

import com.ai.astar.AStar;
import com.ai.astar.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Logic {
    // TODO things probably should be made into classes

    // TODO make it generic!!!!!
    public class Pair {
        public Pos pos;
        public MoveDirection dir;
        Pair(Pos pos, MoveDirection dir) {
            this.pos = pos;
            this.dir = dir;
        }
    }
    public enum Team {
        PLAYER,
        ENEMY,
    }

    public enum ThingType {
        PLAYER,
        BOX,
        WATCHER,
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
        TREASURE,
        BED_BOT,
        BED_TOP_G,
        BED_TOP_B,
        BED_TOP_E,
        BED_TOP_G2,
        BATH_FLOOR,
        TOILET,
        TOILET_L,
        TOILET_M,
        TOILET_R,
        SHOWER_L,
        SHOWER_R,
        SINK_L,
        SINK_R,
        BL,
        BR,
        UL,
        UR,
        UR_CANDIES,
        TUMB,
        TUMB_COOKIE,
        BATHROOM_FLOOR,
        TOILET_FLOOR,
    }
    public static class Cell {
        public CellType type;
        public boolean hasShadow;

        Cell (CellType type) {
            this.type = type;
        }

        public String toShortString() {
            if (hasShadow) {
                return "X";
            }

            return switch (type) {
                case FLOOR -> " ";
                case WALL -> "W";
                case ENTRANCE -> "E";
                case TREASURE -> "$";
                default -> "?";
            };

        }
    }

    public static class Pos {
        public int x;
        public int y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Pos applyDir(final MoveDirection dir) {
            return switch (dir) {
                case LEFT -> new Pos(x - 1, y);
                case RIGHT -> new Pos(x + 1, y);
                case UP -> new Pos(x, y - 1);
                case DOWN -> new Pos(x, y + 1);
            };
        }

        public MoveDirection getDir(final Pos to) {
            if (to.x == this.x) {
                if (to.y - 1 == this.y) {
                    return MoveDirection.DOWN;
                }
                if (to.y + 1 == this.y) {
                    return MoveDirection.UP;
                }
            } else if (to.y == this.y) {
                if (to.x - 1 == this.x) {
                    return MoveDirection.RIGHT;
                }
                if (to.x + 1 == this.x) {
                    return MoveDirection.LEFT;
                }
            }

            return null;
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
        if (x < 0) { return null; }
        if (y < 0) { return null; }
        if (x >= fieldWidth) { return null; }
        if (y >= fieldHeight) { return null; }
        return field[y][x];
    }

    private final static int MOVES_PER_TURN = 1;
    private final static int GNOME_TIMER = 5;

    private int moveCounter;
    private Team currTeam;
    private final Map<CellType, Boolean> isWalkable;
    private final Map<Pos, ThingType> thingTypeMap;
    private final Cell[][] field;
    private Pos playerPos;
    private final int fieldWidth;
    private final int fieldHeight;
    private final List<Pair> history; // update this when loading new level
    private List<Pos> path;
    private boolean gnomeExists = false;
    private Pos gnomePos;
    private int gnomeCountdown;
    private boolean canDeployGnome;
    private boolean isPlayerSleeping;
    private boolean isTreasureStolen;

    public Logic(
            final CellType[][] field,
            final Map<Pos, ThingType> thingTypeMap,
            final Map<CellType, Boolean> isWalkable
            ) {
        history = new ArrayList<>();
        path = new ArrayList<>();
        playerPos = findPlayerPos(field);
        fieldHeight = field.length;
        fieldWidth = field[0].length;
        currTeam = Team.PLAYER;
        moveCounter = MOVES_PER_TURN;
        gnomeCountdown = GNOME_TIMER;
        canDeployGnome = true;

        this.isWalkable = isWalkable;
        this.thingTypeMap = new HashMap<>(thingTypeMap
                .entrySet().stream()
                .filter(x -> x.getValue() != ThingType.PLAYER)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        this.thingTypeMap.put(playerPos, ThingType.PLAYER);

        this.field = new Cell[fieldHeight][fieldWidth];
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                this.field[y][x] = new Cell(field[y][x]);
            }
        }
    }

    public List<Pos> getPath() {
        return path;
    }

    public boolean isWalkable(int x, int y) {
        return isWalkable.get(getCell(x, y).type);
    }

    public boolean isPlayerAlive() {
        return thingTypeMap.values().stream().anyMatch(x -> x == ThingType.PLAYER);
    }

    public Pos getPlayerPos() {
        return thingTypeMap.entrySet()
                .stream()
                .filter(x -> x.getValue() == ThingType.PLAYER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    public Pos getWatcherPos() {
        return thingTypeMap.entrySet()
                .stream()
                .filter(x -> x.getValue() == ThingType.WATCHER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    public void tickGnome() {
        if (gnomeExists && gnomeCountdown > 0) {
            gnomeCountdown--;
        }
    }

    public List<Pos> buildPath(Pos initPos, Pos target) {
        final Node initialNode = new Node(initPos.y, initPos.x);
        final Node finalNode = new Node(target.y, target.x);
        AStar aStar = new AStar(fieldHeight, fieldWidth, initialNode, finalNode, 10, 10000000);
        int[][] blocksArray = IntStream.range(0, fieldWidth)
                .mapToObj(x -> IntStream.range(0, fieldHeight).mapToObj(y -> new Pos(x, y)))
                .flatMap(x -> x)
                .filter(p -> !isWalkable(p.x, p.y))
                .map(p -> new int[]{ p.y, p.x })
                .toArray(int[][]::new);
        aStar.setBlocks(blocksArray);

        return aStar.findPath().stream()
                .map(n -> new Pos(n.getCol(), n.getRow()))
                .collect(Collectors.toList());
    }

    private Pos findPlayerPos(CellType[][] field) {
        Pos player = new Pos(0, 0);
        for (int i = 0; i < field.length; i++){
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] == CellType.ENTRANCE) {
                    player = new Pos(j, i);
                }
            }
        }
        return player;
    }

    public List<Pair> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public boolean getIsTreasureStolen() { return isTreasureStolen; }

    public void movePlayer(final MoveDirection dir) {
        if (currTeam != Team.PLAYER || moveCounter == 0) {
            return;
        }

        if (moveThing(playerPos, dir)) {
            playerPos = playerPos.applyDir(dir);
            history.add(new Pair(playerPos, dir));
            moveCounter--;
        }

        System.out.println("Player MoveCounter: " + (moveCounter+1) + " -> " + moveCounter);
    }

    public void stealTreasure() {
        final Logic.Pos pos = getPlayerPos();

        if (pos.x > 6 && pos.x < 12 && pos.y > 8) {
            isTreasureStolen = true;
        }
    }

    public Team getCurrTeam() {
        return currTeam;
    }

    public void finishTurn() {
        moveCounter = 0;
    }

    public boolean isTurnOver() {
        return moveCounter == 0;
    }

    public void switchTeams() {
        currTeam = switch (currTeam) {
            case PLAYER -> Team.ENEMY;
            case ENEMY -> Team.PLAYER;
        };

        moveCounter = MOVES_PER_TURN;
    }

    public void killGnome() {
        gnomeExists = false;
    }

    public void playerToggleSleep() {
        if (isPlayerSleeping) {
            isPlayerSleeping = false;
            finishTurn();
            return;
        }

        final Logic.Pos bedPos = new Logic.Pos(4, 9);
        if (!bedPos.equals(getPlayerPos())) {
            return;
        }

        isPlayerSleeping = true;
    }

    public boolean isPlayerSleeping() {
        return isPlayerSleeping;
    }

    public void deployGnome(Logic.Pos pos) {
        if (!canDeployGnome || isPlayerSleeping) {
            return;
        }

        gnomePos = pos;
        gnomeExists = true;
        canDeployGnome = false;
    }

    public Pos getGnomePos() {
        return gnomePos;
    }

    public boolean gnomeExists() {
        return gnomeExists;
    }

    public boolean isGnomeActive() {
        return gnomeExists && gnomeCountdown == 0;
    }

    private Pos findMostLeftPos(int historyIndexOfStartLower, Pos startCyclePos) {
        Pos currentLeftMin = startCyclePos;
        for (int i = historyIndexOfStartLower; history.get(i).pos != startCyclePos; i++) {
            Pos currentPos = history.get(i).pos;
            if (currentLeftMin.x > currentPos.x) {
                currentLeftMin = currentPos;
            }
        }
        return currentLeftMin;
    }

    private Pos findMostTopPos(int historyIndexOfStartLower, Pos startCyclePos) {
        Pos currentTopMin = startCyclePos;
        for (int i = historyIndexOfStartLower; history.get(i).pos != startCyclePos; i++) {
            Pos currentPos = history.get(i).pos;
            if (currentTopMin.y > currentPos.y) {
                currentTopMin = currentPos;
            }
        }
        return currentTopMin;
    }

    private int findRightMost(int historyIndexOfStartLower, Pos startCyclePos) {
        int currentRightMax = startCyclePos.x;
        for (int i = historyIndexOfStartLower; history.get(i).pos != startCyclePos; i++) {
            Pos currentPos = history.get(i).pos;
            if (currentRightMax < currentPos.x) {
                currentRightMax = currentPos.x;
            }
        }
        return currentRightMax;
    }

    public Stream<Map.Entry<Pos, ThingType>> allThings() {
        return thingTypeMap.entrySet().stream();
    }

    public boolean moveThing(final Pos thingPos, final MoveDirection dir) {
        final Pos newThingPos = thingPos.applyDir(dir);

        if (!thingTypeMap.containsKey(thingPos)) {
            return true; // Might want to return false
        }

        if (!posValid(newThingPos, thingTypeMap.get(thingPos))) {
            return false;
        }

        if (!thingTypeMap.containsKey(newThingPos)) {
            thingTypeMap.put(newThingPos, thingTypeMap.remove(thingPos));
            return true;
        }

        // Delete player
        if (thingTypeMap.get(newThingPos) == ThingType.PLAYER && thingTypeMap.get(thingPos) == ThingType.WATCHER) {
            thingTypeMap.put(newThingPos, thingTypeMap.remove(thingPos));
            return true;
        }

        return false;
    }

    /* Function checks that you can go or slide a box on this position. */
    private boolean posValid(final Pos pos, final ThingType ty) {
        if (pos.x < 0 || pos.y < 0) {
            return false;
        }

        if (pos.x >= fieldWidth || pos.y >= fieldHeight) {
            return false;
        }

        return isWalkable(pos.x, pos.y);
    }
}
