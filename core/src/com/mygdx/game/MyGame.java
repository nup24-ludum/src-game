package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    Sprite player;

    private InputProcessor inputProcessor;
    private float playerX;
    private float playerY;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        debugRenderer = new ShapeRenderer();
        player = new Sprite(img, 64, 64);
        player.setPosition(0, Gdx.graphics.getHeight() - 64);
        inputProcessor = new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
               switch (keycode) {
                   case Input.Keys.A:{
                       player.setX(player.getX() - 64);
                       System.out.println("A");
                       break;
                   }
                   case Input.Keys.D:{
                       player.setX(player.getX() + 64);
                       System.out.println("D");
                       break;
                   }
                   case Input.Keys.W:{
                       player.setY(player.getY() + 64);
                       System.out.println("W");
                       break;
                   }
                   case Input.Keys.S:{
                       player.setY(player.getY() - 64);
                       System.out.println("S");
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

            private void logger(int screenX, int screenY, int pointer, int button) {
                System.out.print("X: ");
                System.out.print(screenX);
                System.out.print(" Y: ");
                System.out.println(screenY);
                System.out.print(pointer);
                System.out.print(" --- ");
                System.out.println(button);
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                logger(screenX, screenY, pointer, button);
                if (button == Input.Buttons.LEFT) {
                    float windowY = Gdx.graphics.getHeight();
                    player.setCenter(screenX, windowY - screenY);
                }
                return true;
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

    private void updatePlayerPosition() {
        playerX = player.getX();
        playerY = player.getY();
    }

    ShapeRenderer debugRenderer;

    public void DrawDebugLine(Vector2 start, Vector2 end, int lineWidth, Color color) {
        Gdx.gl.glLineWidth(lineWidth);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(color);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawGreedField() {
        for (int i = 0; i < 640; i += 64)
            DrawDebugLine(new Vector2(i, 0), new Vector2(i, 1000), 2, Color.BLACK);
        int screenHeight = Gdx.graphics.getHeight();
        for (int j = 0; j < 640; j += 64) {
            DrawDebugLine(new Vector2(0, screenHeight - j), new Vector2(1000, screenHeight - j), 2, Color.BLACK);
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(1, 1, 0, 1);
        drawGreedField();

        batch.begin();
        player.draw(batch);
        batch.end();
        // rotateSprite();
        updatePlayerPosition();
    }

    private void rotateSprite() {
        float rotation = player.getRotation();
        rotation += 10;
        if (rotation > 360)
            rotation -= 360;
        player.setRotation(rotation);
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
