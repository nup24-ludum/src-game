package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.stream.IntStream;

public class View {
    private final ShapeRenderer debugRenderer;
    private final SpriteBatch batch;
    private final Texture playerImg;
    private final Texture badLogic64;
    private final Texture boxImg;
    private final Texture[] grass;
    private final Texture wall;
    private final Texture shadow;
    private final Texture chest;

    /* Resources for the don't starve look */
    private final ModelBatch modelBatch;
    private final Model leftWall;
    private final Model rightWall;
    private final Model backWall;
    private final ModelInstance leftWallInstance;
    private final ModelInstance rightWallInstance;
    private final ModelInstance backWallInstance;
    private final PerspectiveCamera cam;
    private final DecalBatch decalBatch;

    private static float sizeOfBlock = 1 / 10f * 2;
    private static final float wallHeight = 0.8f;

    View() {
        playerImg = new Texture("char.png");
        boxImg = new Texture("box.png");
//        grass = new Texture[] { new Texture("boxy.png") };
        grass = IntStream.range(1, 7)
                .mapToObj(x -> new Texture("grass" + x + ".png"))
                .toArray(Texture[]::new);
        wall = new Texture("wall.png");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
        shadow = new Texture("shadow-2.png");
        chest = new Texture("chest-2.png");
        badLogic64 = new Texture("badlogic64.jpg");

        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(30, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 1.5f, 3.0f);
        cam.lookAt(0, -0.8f, 0);
        cam.far = 300f;
        cam.update();
        final ModelBuilder modelBuilder = new ModelBuilder();

        // LB, RB, RT, LT
        leftWall = modelBuilder.createRect(
                -1, -1, 1,
                -1, -1, -1,
                -1, -1 + wallHeight, -1,
                -1, -1 + wallHeight, 1,
                0, 0, -1,
                new Material(
                        TextureAttribute.createDiffuse(wall),
                        ColorAttribute.createDiffuse(new Color(1, 1, 1, 1))
                ),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates
        );
        rightWall = modelBuilder.createRect(
                1, -1, -1,
                1, -1, 1,
                1, -1 + wallHeight, 1,
                1, -1 + wallHeight, -1,
                0, 0, -1,
                new Material(
                        TextureAttribute.createDiffuse(wall),
                        ColorAttribute.createDiffuse(new Color(1, 1, 1, 1))
                ),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates
        );
        backWall = modelBuilder.createRect(
                -1, -1, -1,
                1, -1, -1,
                1, -1 + wallHeight, -1,
                -1, -1 + wallHeight, -1,
                0, 0, -1,
                new Material(
                        TextureAttribute.createDiffuse(wall),
//                        ColorAttribute.createDiffuse(new Color(0.6f, 0.6f, 0.6f, 1))
                        ColorAttribute.createDiffuse(new Color(1f, 1f, 1f, 1))

                ),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates
        );

        leftWallInstance = new ModelInstance(leftWall);
        rightWallInstance = new ModelInstance(rightWall);
        backWallInstance = new ModelInstance(backWall);

        decalBatch = new DecalBatch(10, new CameraGroupStrategy(cam));
    }

    public void view(final Logic model) {
        // start - offset from (0, 0)

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(leftWallInstance);
        modelBatch.render(rightWallInstance);
        modelBatch.render(backWallInstance);
        modelBatch.end();

        drawField(model);
        
	for (final Logic.Pair pair : model.getHistory()) {
            // pair contains an old pos.
            final Logic.Pos t = pair.pos.applyDir(pair.dir);
            final Logic.Pos oldPos = new Logic.Pos(
                    pair.pos.x - (t.x - pair.pos.x),
                    pair.pos.y - (t.y - pair.pos.y)
            );

            final Vector2 beg = logicToScreen(oldPos, fieldHeight, start)
                    .add(sizeOfBlock / 2, -sizeOfBlock / 2);
            final Vector2 end = logicToScreen(pair.pos, fieldHeight, start)
                    .add(sizeOfBlock / 2, -sizeOfBlock / 2);

            DrawDebugLine(beg, end);
        }

        model.allThings().forEach(entry -> {
            final Logic.Pos lPos = entry.getKey();
            Vector3 pos = logicToDisplay(lPos).add(sizeOfBlock / 2f, -1f + sizeOfBlock / 2f, sizeOfBlock / 2f);
            final Logic.ThingType ty = entry.getValue();
            final Texture img;
            switch (ty) {
                case PLAYER:
                    img = playerImg;
                    break;
                case BOX:
                    pos = pos.add(0, 0, sizeOfBlock / 2f);
                    img = boxImg;
                    break;
                default:
                    img = null;
            }
            final Decal dec = Decal.newDecal(sizeOfBlock, sizeOfBlock, new TextureRegion(img));
            if (ty == Logic.ThingType.PLAYER) {
                dec.setScale(2f);
                pos = pos.add(0, sizeOfBlock / 2f, sizeOfBlock / 2f - 0.08f);
            } else {
                dec.setScaleY(1.5f);
                pos = pos.add(0, sizeOfBlock / 4f, 0);
            }
            dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            dec.setPosition(pos);
//            dec.lookAt(cam.position, cam.up);

            decalBatch.add(dec);
        });
        decalBatch.flush();
    }

    private void DrawDebugLine(Vector2 start, Vector2 end) {
        Gdx.gl.glLineWidth(2);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.RED);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawField(final Logic logic) {
        for (int y = 0; y < logic.getFieldHeight(); y++) {
            for (int x = 0; x < logic.getFieldWidth(); x++) {
                Vector3 currentCellPos = logicToDisplay(
                        new Logic.Pos(x, y)
                ).add(sizeOfBlock / 2, -1, sizeOfBlock / 2);

                int idx = ((x << 16) ^ y) % grass.length;
                final Decal dec = Decal.newDecal(sizeOfBlock, sizeOfBlock, new TextureRegion(grass[idx]));
                dec.rotateX(90);
                dec.setPosition(currentCellPos);
                decalBatch.add(dec);
            }
        }
    }

    public Vector3 logicToDisplay(
            final Logic.Pos lPos
    ) {
        return new Vector3(
                (float)lPos.x,
                0f,
                (float)lPos.y
        ).scl(sizeOfBlock).add(-1, 0, -1);
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();

        modelBatch.dispose();
        leftWall.dispose();
        rightWall.dispose();
        backWall.dispose();
    }
}
