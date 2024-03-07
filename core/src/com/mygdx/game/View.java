package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Logic.Cell;
import com.mygdx.game.Logic.CellType;

public class View {
    private final ShapeRenderer debugRenderer;
    private final SpriteBatch batch;
    private final Texture img;
    private final Texture grass;
    private final Texture wall;
    private static final float sizeOfBlock = 64;

    View() {
        img = new Texture("badlogic.jpg");
        grass = new Texture("grass.png");
        wall = new Texture("wall.jpg");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
    }

    public void view(final Logic model) {
        // start - offset from (0, 0)
        Vector2 start = new Vector2(0, 0);
        int fieldWidth = model.getFieldWidth();
        int fieldHeight = model.getFieldHeight();

        ScreenUtils.clear(1, 1, 0, 1);
        drawField(fieldWidth, fieldHeight, start, model.getField(), fieldHeight);

        final Sprite player = new Sprite(img, 64, 64);
        Vector2 playerPos = logicToScreen(
                model.getPlayerX(),
                model.getPlayerY(),
                fieldHeight,
                start
        );
        player.setPosition(playerPos.x, playerPos.y - sizeOfBlock);
        player.setSize(64, 64);

        batch.begin();
        player.draw(batch);
        batch.end();
    }

    private void DrawDebugLine(Vector2 start, Vector2 end) {
        Gdx.gl.glLineWidth(2);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.BLACK);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawTexture(CellType type, int posX, int posY) {
        Texture toDraw;
        switch (type) {
            case FLOR: {
                toDraw = grass;
                break;
            }
            case WALL: {
                toDraw = wall;
                break;
            }
            default: {
                toDraw = img;
                break;
            }

        }
        batch.begin();
        batch.draw(toDraw, posX, posY);
        batch.end();
    }

    private void drawField(int width, int height, Vector2 start, Logic.Cell[][] field, int fieldHeight) {
        // Vert lines
        for (int i = 0; i <= width; i++) {
            DrawDebugLine(
                    logicToScreen(i, 0, height, start),
                    logicToScreen(i, height, height, start)
            );
        }

        // Horiz lines
        for (int i = 0; i <= height; i++) {
            DrawDebugLine(
                    logicToScreen(0, i, height, start),
                    logicToScreen(width, i, height, start)
            );
        }
        for (int i = 0; i < field.length; i++)
            for (int j = 0; j < field[i].length; j++) {
                Vector2 currentCellPos = logicToScreen(
                        j,
                        i + 1,
                        fieldHeight,
                        start
                );
                drawTexture(field[i][j].type, (int)currentCellPos.x, (int)currentCellPos.y);
            }
    }


    // LibGDX goes from bottom left to top right
    public Vector2 logicToScreen(
            int xLogic,
            int yLogic,
            int fieldHeight,
            Vector2 start
    ) {
        yLogic = fieldHeight - yLogic;
        return new Vector2((float)xLogic, (float)yLogic).scl(sizeOfBlock).add(start);
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();
    }
}
