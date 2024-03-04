package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class View {
    ShapeRenderer debugRenderer;
    SpriteBatch batch;
    View() {
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
    }
    public void DrawDebugLine(Vector2 start, Vector2 end, int lineWidth, Color color) {
        Gdx.gl.glLineWidth(lineWidth);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(color);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public void drawField(int width, int height, int sizeOfBlock, int startX, int startY) {
        int screenHeight = Gdx.graphics.getHeight(); // TODO("Bad!@")
        for (int i = startX; i <= (startX + width); i += sizeOfBlock)
            DrawDebugLine(new Vector2(i, reformatYCoordinates(startY, screenHeight)), new Vector2(i, reformatYCoordinates(height + startY, screenHeight)), 2, Color.BLACK);

        for (int j = startY; j <= (startY + height); j += sizeOfBlock) {
            DrawDebugLine(new Vector2(startX, reformatYCoordinates(j, screenHeight)), new Vector2(width + startX, reformatYCoordinates(j, screenHeight)), 2, Color.BLACK);
        }
    }

    public int reformatYCoordinates( int yInGame, int gameFiledHeight) {
        return gameFiledHeight - yInGame;
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();
    }
}
