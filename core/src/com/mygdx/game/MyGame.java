package com.mygdx.game;

import com.badlogic.gdx.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyGame extends ApplicationAdapter {
    private View view;
    private Logic logic;

    private Map<Logic.Pos, Logic.ThingType> thingTypeMap;

    private final String[][] level_1 = new String[][]{
            {"x","x", "x", "x", "x", "x", "x"},
            {"e","o", "o", "o", "o", "o", "t"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x","x", "x", "x", "x", "x", "x"}
    };
    private final String[][] level_2 = new String[][]{
            {"x","x", "x", "x", "x", "x", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "e", "o", "t", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x", "o", "o", "o", "o", "o", "x"},
            {"x","x", "x", "x", "x", "x", "x"}
    };

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
                    case Input.Keys.NUM_1: {
                        logic = loadHardcodedLevelAndGenerateLogic(1);
                        break;
                    }
                    case Input.Keys.NUM_2: {
                        logic = loadHardcodedLevelAndGenerateLogic(2);
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
    public Logic.CellType[][] loadField() {
//        Logic.Pos playerPos = new Logic.Pos(0, 0);
        int fieldWidth = 10;
        int fieldHeight = 10;
        Logic.CellType[][] field = new Logic.CellType[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
//                if (i == 2 && j == 4) {
//                    field[i][j] = Logic.CellType.ENTRANCE;
//                    continue;
//                }
//                if (i == 7 && j == 6) {
//                    field[i][j] = Logic.CellType.TREASURE;
//                    continue;
//                }
//
//                if (i == 0 && j == 0) {
//                    field[i][j] = Logic.CellType.FLOOR;
//                    continue;
//                }
//                if (i == 1 && j == 0) {
//                    field[i][j] = Logic.CellType.FLOOR;
//                    continue;
//                }
//                if (i == 0 || i == field.length - 1 || j == 0) {
//                    field[i][j] = Logic.CellType.WALL;
//                    continue;
//                }
                field[i][j] = Logic.CellType.FLOOR;
            }
        }

        field[0][0] = Logic.CellType.ENTRANCE;
//        System.out.println("Field dump:");
//        for (int y = 0; y < fieldHeight; y++) {
//            for (int x = 0; x < fieldWidth; x++) {
//                System.out.print(field[y][x].toShortString());
//            }
//            System.out.prin   t("\n");
//        }
//        System.out.println("New game field os size (" + fieldWidth + ", " + fieldHeight + ")");
//        System.out.println("Player is at " + playerPos);
//        for (int i = 0; i < field.length; i++) {
//            for (int j = 0; j < field[i].length; j++) {
//                if (field[i][j] == Logic.CellType.ENTRANCE) {
//                    playerPos = new Logic.Pos(j, i);
//                }
//            }
//        }

        spawnThings();
        return field;
    }
    //TODO all constants are hardcoded - fix!!!
    // TODO make this method depending from loaded field!
    private void spawnThings() {
        this.thingTypeMap = new HashMap<>();
//        thingTypeMap.put(playerPos, Logic.ThingType.PLAYER);
        thingTypeMap.put(new Logic.Pos(1, 1), Logic.ThingType.BOX);
        thingTypeMap.put(new Logic.Pos(3, 3), Logic.ThingType.BOX);
    }
    // for now this function is god killer
    Logic loadHardcodedLevelAndGenerateLogic(int n) {
        String[][] levelTemplate;
        Logic.CellType[][] field;
        HashMap<Logic.Pos, Logic.ThingType> objectsOnField = new LinkedHashMap<>();

        if (n == 1) {
            levelTemplate = level_1;
        } else {
            levelTemplate = level_2;
        }
        field = new Logic.CellType[levelTemplate.length][];

        for (int i = 0; i < levelTemplate.length; i++) {
            field[i] = new Logic.CellType[levelTemplate[i].length];
            for (int j = 0; j < levelTemplate[i].length; j++) {
                switch (levelTemplate[i][j]) {
                    case "o": {
                        field[i][j] = Logic.CellType.FLOOR;
                        break;
                    }
                    case "x": {
                        field[i][j] = Logic.CellType.WALL;
                        break;
                    }
                    case "t": {
                        field[i][j] = Logic.CellType.TREASURE;
                        break;
                    }
                    case "e": {
                        field[i][j] = Logic.CellType.ENTRANCE;
                        objectsOnField.put(new Logic.Pos(j, i), Logic.ThingType.PLAYER);
                        break;
                    }
                    case "b": {
                        field[i][j] = Logic.CellType.FLOOR;
                        objectsOnField.put(new Logic.Pos(j, i), Logic.ThingType.BOX);
                        break;
                    }
                }
            }
        }
        return new Logic(field, objectsOnField);
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
