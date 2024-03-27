package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.util.HashMap;
import java.util.Map;

public class MyGame extends ApplicationAdapter {
    private View view;
    private Logic logic;

    private Map<Logic.Pos, Logic.ThingType> thingTypeMap;

    @Override
    public void create() {
        logic = new Logic(loadField(), thingTypeMap);
        view = new View();

        InputProcessor inputProcessor = new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.A: {
                        logic.movePlayer(Logic.MoveDirection.LEFT);
                        break;
                    }
                    case Input.Keys.D: {
                        logic.movePlayer(Logic.MoveDirection.RIGHT);
                        break;
                    }
                    case Input.Keys.W: {
                        logic.movePlayer(Logic.MoveDirection.UP);
                        break;
                    }
                    case Input.Keys.S: {
                        logic.movePlayer(Logic.MoveDirection.DOWN);
                        break;
                    }
                    case Input.Keys.K: {
                        logic.applyShadowToField();
                        break;
                    }
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return false;
            }
        };
        Gdx.input.setInputProcessor(inputProcessor);
    }
    /*
    * This is template fot loading class from libGDX interface
    * Have to implement new function loadLevelFromFile, that construct libGDX TileMap interface.
    * And then give it to this function.
    * TODO implement this method!
    */

    public Logic.CellType[][] loadField() { // backwards compatability for now

        Logic.Pos playerPos = new Logic.Pos(0, 0);
        int fieldWidth = 8;
        int fieldHeight = 8;
        Logic.CellType[][] field = new Logic.CellType[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (i == 0 && j == 0) {
                    field[i][j] = Logic.CellType.FLOOR;
                    continue;
                }
                if (i == 1 && j == 0) {
                    field[i][j] = Logic.CellType.FLOOR;
                    continue;
                }
                if (i == 0 || i == field.length - 1 || j == 0) {
                    field[i][j] = Logic.CellType.WALL;
                    continue;
                }
                field[i][j] = Logic.CellType.FLOOR;
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


        spawnThings(playerPos);
        return field;
    }
    public Logic.CellType[][] loadField(String mapPath) {
        TiledMap map = new TmxMapLoader().load(mapPath);
        System.out.println(map);

        // Convert to CellType

        int fieldWidth = 8;
        int fieldHeight = 8;
        Logic.CellType[][] field = new Logic.CellType[fieldHeight][fieldWidth];

        return field;
    }
    //TODO all constants are hardcoded - fix!!!
    // TODO make this method depending from loaded field!
    private void spawnThings(Logic.Pos playerPos) {
        this.thingTypeMap = new HashMap<>();
        thingTypeMap.put(playerPos, Logic.ThingType.PLAYER);
        thingTypeMap.put(new Logic.Pos(1, 1), Logic.ThingType.BOX);
        thingTypeMap.put(new Logic.Pos(3, 3), Logic.ThingType.BOX);
    }


    @Override
    public void render() {
        view.view(logic);
    }

    @Override
    public void dispose() {
        view.dispose();
    }
}
