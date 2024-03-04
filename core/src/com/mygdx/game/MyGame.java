package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGame extends ApplicationAdapter {
    Texture img;
    Sprite player;
    View viewUtils;
    private InputProcessor inputProcessor;

    @Override
    public void create() {
        viewUtils = new View();
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

    ShapeRenderer debugRenderer;


    @Override
    public void render() {
        ScreenUtils.clear(1, 1, 0, 1);
        viewUtils.drawField(320, 320, 64, 100, -50);
    }

    @Override
    public void dispose() {

    }
}
