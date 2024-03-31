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
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.Random;
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
    private final Texture shadowHand;
    private final Texture chest;

    /* Resources for the don't starve look */
    private final ModelBatch modelBatch;
    private final Model shadowModel;
    private final ModelInstance shadowInstance;
    private final PerspectiveCamera cam;
    private final DecalBatch decalBatch;

    private static final float sizeOfBlock = 1 / 10f * 2;
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
        shadow = new Texture("shadow4.png");
        shadowHand = new Texture("shadowhand2.png");
        chest = new Texture("chest-2.png");
        badLogic64 = new Texture("badlogic64.jpg");

        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(30, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        decalBatch = new DecalBatch(10, new CameraGroupStrategy(cam));

        shadowModel = buildShadow(List.of(
                new Logic.Pos(0, 1),
                new Logic.Pos(0, 2),
                new Logic.Pos(0, 3),
                new Logic.Pos(1, 3),
                new Logic.Pos(2, 3),
                new Logic.Pos(2, 2),
                new Logic.Pos(2, 1),
                new Logic.Pos(3, 1),
                new Logic.Pos(4, 1),
                new Logic.Pos(5, 1),
                new Logic.Pos(5, 2),
                new Logic.Pos(5, 3),
                new Logic.Pos(6, 3),
                new Logic.Pos(6, 4)
        ), shadow, shadowHand);
        shadowInstance = new ModelInstance(shadowModel);
    }

    public void view(final Logic model) {
        cam.position.set(cameraPos(model));
        cam.lookAt(fieldLookAtPoint(model));
        cam.far = 300f;
        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        drawField(model);
        drawWalls(model);
        decalBatch.flush();
	
	for (final Logic.Pair pair : model.getHistory()) {
            // pair contains an old pos.
            final Logic.Pos t = pair.pos.applyDir(pair.dir);
            final Logic.Pos oldPos = new Logic.Pos(
                    pair.pos.x - (t.x - pair.pos.x),
                    pair.pos.y - (t.y - pair.pos.y)
            );

            //DrawDebugLine(beg, end);
        }

        modelBatch.begin(cam);
        modelBatch.render(shadowInstance);
        modelBatch.end();
        
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
    
    private static Vector3 shadowWiggle(final Random rand) {
        return new Vector3(
                0.10f * sizeOfBlock / 2 * rand.nextFloat(-1, 1),
                0,
                0.10f * sizeOfBlock / 2 * rand.nextFloat(-1, 1)
        );
    }

    private static Model buildShadow(
            final List<Logic.Pos> points,
            final Texture shadow,
            final Texture shadowHand) {
        final ModelBuilder builder = new ModelBuilder();
        builder.begin();
        final Random rand = new Random();
        Vector3 prev = logicToDisplay(new Logic.Pos(0, 0))
                .add(sizeOfBlock / 2, -0.99f, sizeOfBlock / 2);

        final MeshPartBuilder bodyBuilder = builder.part(
                "arm",
                GL20.GL_TRIANGLES,
                Usage.Position | Usage.TextureCoordinates,
                new Material(
                        new BlendingAttribute(true, 1),
//                        ColorAttribute.createDiffuse(Color.BLACK)
                        TextureAttribute.createDiffuse(new TextureRegion(shadow))
                )
        );

        for (int i = 0; i < points.size(); i++) {
            final Logic.Pos pos = points.get(i);
            final Vector3 end = logicToDisplay(pos)
                    .add(sizeOfBlock / 2, -0.99f, sizeOfBlock / 2);
            final Vector3 dir = end.cpy().sub(prev).nor();
            final Vector3 perpNo = new Vector3(dir.z, 0, -dir.x);
            final Vector3 perp = new Vector3(dir.z, 0, -dir.x)
                    .scl(sizeOfBlock / 2);
            final Vector3 ranoff = dir.cpy().scl(0.3f * sizeOfBlock / 2 * rand.nextFloat(0.2f, 1))
                    .add(perpNo.scl( sizeOfBlock / 2 * (
                            (0.3f * rand.nextFloat(0, 1) + 0.2f) *
                                    (rand.nextBoolean() ? -1 : 1)
                    )));
            final Vector3 off = dir.cpy().scl(sizeOfBlock / 2 * 0.2f);
            final Vector3 visend;
            final float endWidth;

            if (i == points.size() - 1) {
                endWidth = 0.2f;
                visend = end.cpy().add(dir.cpy().scl(-sizeOfBlock / 2 * 0.4f));
            } else {
                endWidth = 0.45f;
                visend = end.cpy().add(off).add(ranoff);
            }

            bodyBuilder.rect(
                    prev.cpy().add(dir.cpy().scl(-sizeOfBlock / 2 * 0.1f)).add(perp.cpy().scl(0.35f)).add(shadowWiggle(rand)),
                    prev.cpy().add(dir.cpy().scl(-sizeOfBlock / 2 * 0.1f)).sub(perp.cpy().scl(0.35f)).add(shadowWiggle(rand)),
                    visend.cpy().sub(perp.cpy().scl(endWidth)).add(shadowWiggle(rand)),
                    visend.cpy().add(perp.cpy().scl(endWidth)).add(shadowWiggle(rand)),
//                    prev.cpy().add(perp.cpy().scl(0.55f)).add(shadowWiggle(rand)),
//                    prev.cpy().sub(perp.cpy().scl(0.55f)).add(shadowWiggle(rand)),
//                    visend.cpy().add(off).sub(perp.cpy().scl(0.65f)).add(shadowWiggle(rand)),
//                    visend.cpy().add(off).add(perp.cpy().scl(0.65f)).add(shadowWiggle(rand)),
                    Vector3.Y
            );

            prev = visend;
        }

        final MeshPartBuilder handBuilder = builder.part(
                "hand",
                GL20.GL_TRIANGLES,
                Usage.Position | Usage.TextureCoordinates,
                new Material(
                        new BlendingAttribute(true, 1),
                        TextureAttribute.createDiffuse(new TextureRegion(shadowHand))
                )
        );

        final Logic.Pos pos = points.get(points.size() - 1);
        final Vector3 end = logicToDisplay(pos)
                .add(sizeOfBlock / 2, -0.99f, sizeOfBlock / 2);
        final Vector3 dir = end.cpy().sub(prev).nor();
        final Vector3 perpNo = new Vector3(dir.z, 0, -dir.x);
        final Vector3 perp = new Vector3(dir.z, 0, -dir.x)
                .scl(sizeOfBlock / 2);
        final Vector3 ranoff = dir.cpy().scl(0.3f * sizeOfBlock / 2 * rand.nextFloat(0.2f, 1))
                .add(perpNo.scl( sizeOfBlock / 2 * (
                        (0.3f * rand.nextFloat(0, 1) + 0.2f) *
                                (rand.nextBoolean() ? -1 : 1)
                )));
        final Vector3 off = dir.cpy().scl(sizeOfBlock / 2 * 0.2f);
        final Vector3 visend = end.cpy().add(
                dir.cpy().scl(sizeOfBlock / 2 * 1.2f)
        );
        final Vector3 prevOff = dir.cpy().scl(-sizeOfBlock / 2 * 0.4f);

        bodyBuilder.rect(
                prev.cpy().add(prevOff).add(perp.cpy().scl(0.9f)),
                prev.cpy().add(prevOff).sub(perp.cpy().scl(0.9f)),
                visend.cpy().add(off).sub(perp.cpy().scl(1.2f)),
                visend.cpy().add(off).add(perp.cpy().scl(1.2f)),
                Vector3.Y
        );

        return builder.end();
    }

    private void drawWalls(final Logic logic) {
        final int width = logic.getFieldWidth();
        final int height = logic.getFieldHeight();
        final Vector3 center = fieldCenter(logic);

        final Decal leftWall = Decal.newDecal(
                (float)height * sizeOfBlock,
                wallHeight,
                new TextureRegion(wall) // TODO improve
        );
        leftWall.rotateY(90);
        leftWall.setPosition(
                logicToDisplay(new Logic.Pos(0, 0)).x,
                wallHeight/2f - 1f,
                center.z
        );

        final Decal rightWall = Decal.newDecal(
                (float)height * sizeOfBlock,
                wallHeight,
                new TextureRegion(wall) // TODO improve
        );
        rightWall.rotateY(-90);
        rightWall.setPosition(
                logicToDisplay(new Logic.Pos(width, 0)).x,
                wallHeight/2f - 1f,
                center.z
        );

        final Decal backWall = Decal.newDecal(
                (float)width * sizeOfBlock,
                wallHeight,
                new TextureRegion(wall) // TODO improve
        );
        backWall.setPosition(
                center.x,
                wallHeight/2f - 1f,
                logicToDisplay(new Logic.Pos(0, 0)).z
        );

        decalBatch.add(leftWall);
        decalBatch.add(rightWall);
        decalBatch.add(backWall);
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

    private Vector3 cameraPos(final Logic logic) {
        final Vector3 camPos = fieldLookAtPoint(logic);

        camPos.y = 1.5f + (1 - (float)logic.getFieldHeight()/10f) * 1.2f;
        camPos.z = 3.0f - (1 - (float)logic.getFieldHeight()/10f) * 3.0f;

        return camPos;
    }

    private Vector3 fieldLookAtPoint(final Logic logic) {
        final Vector3 center = fieldCenter(logic);

        /* Looking at a point slightly above -1f improves the overall feel */
        center.y = -0.8f;

        return center;
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
            center.z += sizeOfBlock / 2f;
        }

        /* The actual y of tiles */
        center.y = -1f;

        return center;
    }

    public static Vector3 logicToDisplay(
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
        shadowModel.dispose();
    }
}
