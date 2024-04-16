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
        watcherImg = new Texture("demon.png");
        gnomeImg = new Texture("gnome.png");
        boxImg = new Texture("box.png");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
        badLogic64 = new Texture("badlogic64.jpg");

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

    public void view(final Logic model) {
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
            final Texture img = switch (ty) {
                case PLAYER -> playerImg;
                case WATCHER -> watcherImg;
                case BOX -> boxImg;
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
        decalBatch.flush();
    }

    final Logic.Pos shadowStart(final Logic logic) {
        final Logic.Pair pair = logic.getHistory().get(0);
        final Logic.Pos t = pair.pos.applyDir(pair.dir);

        return new Logic.Pos(
                pair.pos.x - (t.x - pair.pos.x),
                pair.pos.y - (t.y - pair.pos.y)
        );
    }

    private boolean isDir(final Logic.Pos l, final Logic.Pos r) {
        return  l.equals(r.applyDir(Logic.MoveDirection.LEFT)) ||
                l.equals(r.applyDir(Logic.MoveDirection.RIGHT)) ||
                l.equals(r.applyDir(Logic.MoveDirection.DOWN)) ||
                l.equals(r.applyDir(Logic.MoveDirection.UP));
    }

    private List<List<Logic.Pos>> buildShadowSegments(final Logic logic) {
        final List<List<Logic.Pos>> res = new ArrayList<>();

        Logic.Pos prev = shadowStart(logic);
        res.add(new ArrayList<>(Collections.singleton(prev)));
        for (final Logic.Pair pair : logic.getHistory()) {
            final Logic.Pos end = pair.pos;
            if (!logic.getCell(end.x, end.y).hasShadow) {
                continue;
            }

            if (!isDir(prev, end) && !res.get(res.size() - 1).isEmpty()) {
                prev = end;

                res.add(new ArrayList<>());
                res.get(res.size() - 1).add(prev);
                continue;
            }

            prev = end;
            res.get(res.size() - 1).add(end);
        }

        return res;
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

                TextureRegion tileTexture = switch (cell.type) {
                    case ENTRANCE -> new TextureRegion(badLogic64);
                    default -> tileTexes.get(cell.type);
                };

                if (logic.isPlayerSleeping() && lpos.equals(new Logic.Pos(5, 8))) {
                    tileTexture = tileTexes.get(Logic.CellType.BED_TOP_G2);
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
