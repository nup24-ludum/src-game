package com.mygdx.game;

import java.util.List;

public class WatcherAi {
    public enum State {
        WALKING_TO_ROOM,
        /*
        Patrols the corridors and checks rooms.

        If they hear the player in the corridor --> STALK
        */
        CHECKING,
        /*
        Stare at the player in the corridor
        */
        STALK,
        /*
        Rush to the gnome location.
        */
        RUSHING_TO_GNOME,
        /*
        Rush to the player.
        */
        RUSHING_TO_PLAYER,
    }

    private int nextRoomToCheck = 0;

    private State state;

    public WatcherAi() {
        state = State.WALKING_TO_ROOM;
    }

    public void think(final Logic logic) {
        System.out.println("AI state: " + state);

        if (state != State.RUSHING_TO_PLAYER && state != State.RUSHING_TO_GNOME && isPlayerOutside(logic)) {
            state = State.STALK;
            System.out.println("State overridden to stalking");
        }

        switch (state) {
            case WALKING_TO_ROOM -> {
                final Logic.Pos target = roomPos(nextRoomToCheck);
                if (walkToTarget(2, logic, target)) {
                    state = State.CHECKING;
                }
            }
            case CHECKING -> {
                if (nextRoomToCheck == 2) {
                    nextRoomToCheck = 0;
                } else {
                    nextRoomToCheck++;
                }
                state = State.WALKING_TO_ROOM;
            }
            case STALK -> {
                final Logic.Pos target = new Logic.Pos(7 ,5);

                if (isPlayerWrong(logic)) {
                    state = State.RUSHING_TO_PLAYER;
                    return;
                }

                if (!isPlayerOutside(logic)) {
                    state = State.WALKING_TO_ROOM;
                    return;
                }

                if (isMeInCorridor(logic)) {
                    return;
                }

                walkToTarget(2, logic, target);
            }
            case RUSHING_TO_GNOME -> {
            }
            case RUSHING_TO_PLAYER -> {
                final Logic.Pos target = logic.getPlayerPos();
                walkToTarget(3, logic, target);
            }
        }
    }

    private boolean isMeInCorridor(final Logic logic) {
        final Logic.Pos mPos = logic.getWatcherPos();

        return mPos.y < 7 && mPos.y > 4;
    }

    private boolean walkToTarget(
            final int fuel,
            final Logic logic,
            final Logic.Pos target
    ) {
        for (int i = 0; i < fuel; ++i) {
            final Logic.Pos mPos = logic.getWatcherPos();

            if (target.equals(mPos)) {
                return true;
            }

            final Logic.MoveDirection dir = nextMove(logic, target);

            if (dir == null) {
                System.out.println("Cannot pathfind to room " + nextRoomToCheck);
                return false;
            }

            if (!logic.moveThing(mPos, dir)) {
                System.out.println("Can't move");
            }
        }

        return false;
    }

    private boolean isPlayerWrong(final Logic logic) {
        final Logic.Pos playerPos = logic.getPlayerPos();

        // Other rooms
        if (playerPos.y >= 7 && playerPos.x >= 7) {
            return true;
        }

        // Showers
        if (playerPos.y <= 4 && playerPos.x < 6) {
            return true;
        }

        // Watcher room
        if (playerPos.y <= 4 && playerPos.x > 10) {
            return true;
        }

        return false;
    }

    private boolean isPlayerOutside(final Logic logic) {
        final Logic.Pos playerPos = logic.getPlayerPos();

        return playerPos.y < 7;
    }

    private Logic.MoveDirection nextMove(
            final Logic logic,
            final Logic.Pos target
    ) {
        final Logic.Pos mPos = logic.getWatcherPos();
        final List<Logic.Pos> path = logic.buildPath(mPos, target);

        if (path.isEmpty()) {
            return null;
        }

        final Logic.Pos nextPos = path.stream()
                .skip(1).findFirst().orElseThrow();

        return mPos.getDir(nextPos);
    }

    private Logic.Pos roomPos(int idx) {
        return switch (idx) {
            case 0 -> new Logic.Pos(15, 9);
            case 1 -> new Logic.Pos(9, 9);
            case 2 -> new Logic.Pos(3, 9);
            default -> throw new IllegalArgumentException();
        };
    }
}
