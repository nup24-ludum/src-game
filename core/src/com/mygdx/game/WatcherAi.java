package com.mygdx.game;

public class WatcherAi {
    public enum State {
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

    private State state;

    public WatcherAi() {
        state = State.CHECKING;
    }

    public void think(final Logic logic) {
        System.out.println("AI state: " + state);
    }
}
