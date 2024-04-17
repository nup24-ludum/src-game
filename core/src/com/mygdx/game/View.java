package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.*;

public class View {
    private final ShapeRenderer debugRenderer;
    private final SpriteBatch batch;
    private final Texture playerImg;
    private final Texture gnomeImg;
    private final Texture boyImg;
    private final Texture boyImgSus;

    private final Texture help;
    private final Texture badLogic64;
    private final Texture boxImg;
    private final Texture watcherImg;
    private final Map<Logic.CellType, TextureRegion> tileTexes;

    /* Resources for the don't starve look */
    private final ModelBatch modelBatch;
    private final Camera cam;
    private final DecalBatch decalBatch;

    private static final float sizeOfBlock = 1 / 25f * 2;

    View(Map<Logic.CellType, TextureRegion> tileTexes) {
        this.tileTexes = tileTexes;
        playerImg = new Texture("char.png");
        boyImg = new Texture("boy.png");
        boyImgSus = new Texture("boy_q.png");
        watcherImg = new Texture("demon.png");
        gnomeImg = new Texture("gnome.png");
        boxImg = new Texture("box.png");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
        badLogic64 = new Texture("badlogic64.jpg");
        help = new Texture("help_window.png");

        modelBatch = new ModelBatch();
//        cam = new PerspectiveCamera(
//                30,
//                Gdx.graphics.getWidth(),
//                Gdx.graphics.getHeight()
//        );
        cam = new OrthographicCamera(
                (float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight(),
                1f
        );

        decalBatch = new DecalBatch(10, new CameraGroupStrategy(cam));
    }

    public void view(final Logic model, final WatcherAi ai) {
        cam.position.set(cameraPos(model));
        cam.lookAt(fieldLookAtPoint(model));
        cam.far = 300f;
//        cam.up.set(Vector3.Z.cpy().scl(-1));
        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        drawField(model);
        drawPlayerTrace(model);
        decalBatch.flush();

        modelBatch.begin(cam);
        modelBatch.end();

	    model.allThings().forEach(entry -> {
            final Logic.Pos lPos = entry.getKey();
            Vector3 pos = logicToDisplay(lPos).add(sizeOfBlock / 2f, sizeOfBlock / 2f, 0.01f);
            final Logic.ThingType ty = entry.getValue();
            final Texture img;
            switch (ty) {
                case PLAYER:
                    img = playerImg;
                    break;
                case WATCHER:
                    switch (ai.getState()) {
                        case RUSHING_TO_PLAYER:
                            img = watcherImg;
                            break;
                        case STALK:
                            img = boyImgSus;
                            break;
                        default:
                            img = boyImg;
                            break;
                    }
                    break;
                case BOX:
                    img = boxImg;
                    break;
                default:
                    img = null;
                    break;
            };
            final Decal dec = Decal.newDecal(sizeOfBlock, sizeOfBlock, new TextureRegion(img));
            dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            dec.setPosition(pos);

            if (ty == Logic.ThingType.PLAYER && model.isPlayerSleeping()) {
                return;
            }
//            dec.lookAt(cam.position, cam.up);

            decalBatch.add(dec);
        });
        if (model.gnomeExists()) {
            final Logic.Pos lPos = model.getGnomePos();
            Vector3 pos = logicToDisplay(lPos).add(sizeOfBlock / 2f, sizeOfBlock / 2f, 0.01f);
            final Decal dec = Decal.newDecal(sizeOfBlock, sizeOfBlock, new TextureRegion(gnomeImg));
            dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            dec.setPosition(pos);
            decalBatch.add(dec);
        }

        final Decal dec = Decal.newDecal(sizeOfBlock * 4, sizeOfBlock * 3, new TextureRegion(help));
        dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Vector3 pos = logicToDisplay(new Logic.Pos(18, 2)).add(sizeOfBlock / 2f, sizeOfBlock / 2f, 0.01f);
        dec.setPosition(pos);
        decalBatch.add(dec);

        final Decal dec2 = Decal.newDecal(2, 2, new TextureRegion(boxImg));
        final float x = model.getFadePercent();
        final float qnt = 5;
        dec2.setColor(1, 1, 1, (float)(Math.ceil(x * qnt)) / qnt);
        dec2.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Vector3 pos2 = logicToDisplay(new Logic.Pos(9, 2)).add(sizeOfBlock / 2f, sizeOfBlock / 2f, 0.02f);
        dec2.setPosition(pos2);
        decalBatch.add(dec2);

        decalBatch.flush();
    }

    private void drawPlayerTrace(final Logic logic) {
        if (logic.getPath().isEmpty()) {
            return;
        }

        for (Logic.Pos pos : logic.getPath()) {
            final Vector3 beg = logicToDisplay(pos)
                    .add(sizeOfBlock / 2, sizeOfBlock / 2, 0.2f);

            final Decal dec = Decal.newDecal(
                    sizeOfBlock, sizeOfBlock,
                    new TextureRegion(boxImg)
            );
            dec.setColor(1, 1, 1, 0.6f);
            dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            dec.setPosition(beg);
            decalBatch.add(dec);
        }
    }

    private void drawField(final Logic logic) {
        for (int y = 0; y < logic.getFieldHeight(); y++) {
            for (int x = 0; x < logic.getFieldWidth(); x++) {
                final Logic.Pos lpos = new Logic.Pos(x, y);
                Vector3 currentCellPos = logicToDisplay(
                        lpos
                ).add(sizeOfBlock / 2, sizeOfBlock / 2, 0);
                final Logic.Cell cell = logic.getCell(x, y);
                final int ridx = (x << 16) ^ y;
                final Logic.Pos cellLogPos = new Logic.Pos(x, y);

                TextureRegion tileTexture;

                switch (cell.type) {
                    case ENTRANCE:
                        tileTexture = tileTexes.get(Logic.CellType.FLOOR);
                        break;
                    default:
                        tileTexture = tileTexes.get(cell.type);
                        break;
                };

                if (logic.isPlayerSleeping() && lpos.equals(new Logic.Pos(5, 8))) {
                    tileTexture = tileTexes.get(Logic.CellType.BED_TOP_G2);
                }

                if (logic.getIsTreasureStolen() && cell.type == Logic.CellType.TUMB_COOKIE) {
                    tileTexture = tileTexes.get(Logic.CellType.TUMB);
                }

                if (tileTexture == null) {
                    continue;
                }

                final Decal dec = Decal.newDecal(
                        sizeOfBlock, sizeOfBlock,
                        new TextureRegion(tileTexture)
                );
                dec.setPosition(currentCellPos);
                decalBatch.add(dec);
            }
        }
    }

    private Vector3 cameraPos(final Logic logic) {
        return fieldCenter(logic).add(0, 0, 4f);
    }

    private Vector3 fieldLookAtPoint(final Logic logic) {
        return fieldCenter(logic);
    }

    private Vector3 fieldCenter(final Logic logic) {
        final int width = logic.getFieldWidth();
        final int height = logic.getFieldHeight();
        final Vector3 center = logicToDisplay(new Logic.Pos(width / 2, height / 2));

        /* For evenly sized maps it's not the center of any tile */
        if (width % 2 == 1) {
            center.x += sizeOfBlock / 2f;
        }

        if (height % 2 == 1) {
            center.y += sizeOfBlock / 2f;
        }

        return center;
    }

    public static Vector3 logicToDisplay(
            final Logic.Pos lPos
    ) {
        Vector3 prePos = new Vector3(
                (float)lPos.x,
                (float)lPos.y,
                0f
        ).scl(sizeOfBlock);

        return new Vector3(prePos.x, 2 - prePos.y, prePos.z);
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();

        modelBatch.dispose();
    }
}
