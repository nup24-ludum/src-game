package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.*;

import java.util.*;

public class MyGame extends ApplicationAdapter {
    private View view;
    private Logic logic;
    private WatcherAi watcherAi;

    @Override
    public void create() {
        setupLevel();

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
                    case Input.Keys.R: {
                        setupLevel();
                        break;
                    }
                    case Input.Keys.Z: {
                        logic.deployGnome(logic.getPlayerPos());
                        break;
                    }
                    case Input.Keys.X: {
                        logic.finishTurn();
                        break;
                    }
                    case Input.Keys.Q: {
                        logic.playerToggleSleep();
                        break;
                    }
                    case Input.Keys.E: {
                        logic.stealTreasure();
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

    private void setupLevel() {
        watcherAi = new WatcherAi();

        final TiledMap map = new TmxMapLoader().load("loc.tmx");
        final Iterator<TiledMapTileSet> tsetIt = map.getTileSets().iterator();

        assert(tsetIt.hasNext());
        final TiledMapTileSet tset = tsetIt.next();
        assert(!tsetIt.hasNext());

        final Map<Logic.CellType, Boolean> isWalkable = new HashMap<>();
        final Map<Integer, Logic.CellType> cellIdToTy = new HashMap<>();
        final Map<Logic.CellType, TextureRegion> tileTexes = new HashMap<>();
        System.out.println("Loaded tiles:");
        for (final TiledMapTile tile : tset) {
            final MapProperties tileProps = tile.getProperties();
            final String tyStr = tileProps.get("java_ty", String.class);
            final Logic.CellType ty = tyStr == null ? Logic.CellType.FLOOR : Logic.CellType.valueOf(tyStr);
            final boolean walkable = tileProps.get("is_walkable", false, Boolean.class);

            isWalkable.put(ty, walkable);
            cellIdToTy.put(tile.getId(), ty);
            tileTexes.put(ty, tile.getTextureRegion());
        }
        System.out.println(isWalkable);
        System.out.println(cellIdToTy);

        final Iterator<MapLayer> layerIterator = map.getLayers().iterator();
        assert(layerIterator.hasNext());
        final TiledMapTileLayer layer = (TiledMapTileLayer)layerIterator.next();
        assert(!layerIterator.hasNext());

        Logic.CellType[][] field = new Logic.CellType[layer.getHeight()][layer.getWidth()];

        for (int x = 0; x < layer.getWidth(); ++x) {
            for (int y = 0; y < layer.getHeight(); ++y) {
                final TiledMapTileLayer.Cell cell = layer.getCell(x, layer.getHeight() - y - 1);
                if (cell == null) {
                    field[y][x] = Logic.CellType.FLOOR;
                    continue;
                }
                field[y][x] = cellIdToTy.get(cell.getTile().getId());
            }
        }

        HashMap<Logic.Pos, Logic.ThingType> objectsOnField = new LinkedHashMap<>();
        // Place player
        field[9][4] = Logic.CellType.ENTRANCE;
        // Patch walkable info
        isWalkable.put(Logic.CellType.ENTRANCE, true);
        // Spawn the enemy
        objectsOnField.put(new Logic.Pos(14, 2), Logic.ThingType.WATCHER);

        logic = new Logic(field, objectsOnField, isWalkable);
        view = new View(tileTexes);
    }

    private void tick() {
        if (!logic.isPlayerAlive()) {
            return;
        }

        if (logic.isTurnOver()) {
            logic.switchTeams();
            if (logic.getCurrTeam() == Logic.Team.ENEMY) {
                watcherAi.think(logic);
                logic.finishTurn();
            } else {
                logic.tickGnome();
            }
        }
    }

    @Override
    public void render() {
        tick();

        view.view(logic, watcherAi);
    }

    @Override
    public void dispose() {
        view.dispose();
    }
}
