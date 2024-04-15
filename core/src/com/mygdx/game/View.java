package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class View {
    private final ShapeRenderer debugRenderer;
    private final SpriteBatch batch;
    private final Texture playerImg;
    private final Texture badLogic64;
    private final Texture boxImg;
    private final Texture floor;
    private final Texture shadow;
    private final Texture shadowHand;
    private final Texture chest;

    /* Resources for the don't starve look */
    private final ModelBatch modelBatch;
    private List<Model> shadowModels;
    private final Camera cam;
    private final DecalBatch decalBatch;

    private static final float sizeOfBlock = 1 / 10f * 2;
    private final Texture[] traceTexture;

    View() {
        playerImg = new Texture("char.png");
        boxImg = new Texture("box.png");
//        grass = new Texture[] { new Texture("boxy.png") };
        floor = new Texture("floor.png");
        debugRenderer =  new ShapeRenderer();
        batch = new SpriteBatch();
        shadow = new Texture("shadow4.png");
        shadowHand = new Texture("shadowhand2.png");
        chest = new Texture("chest-2.png");
        badLogic64 = new Texture("badlogic64.jpg");
        traceTexture = IntStream.range(1, 4)
                .mapToObj(x -> new Texture("trace" + x + ".png"))
                .toArray(Texture[]::new);

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
        shadowModels = Collections.emptyList();
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
        // FIXME this is some lame shit right here
        if (shadowModels.isEmpty() && model.getIsTreasureStolen()) {
            shadowModels = buildShadowSegments(model)
                    .stream()
                    .map(x -> buildShadow(x, shadow, shadowHand))
                    .collect(Collectors.toList());
        }
        if (!shadowModels.isEmpty() && model.getIsTreasureStolen()) {
            shadowModels.stream()
                    .map(ModelInstance::new)
                    .forEach(modelBatch::render);
        } else if (!shadowModels.isEmpty()) {
            shadowModels.forEach(Model::dispose);
            shadowModels.clear();
        }
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

    private static Model buildShadow(
            final List<Logic.Pos> points,
            final Texture shadow,
            final Texture shadowHand) {
        final ModelBuilder builder = new ModelBuilder();
        builder.begin();
        final Random rand = new Random();
        Vector3 prev = logicToDisplay(points.get(0))
                .add(sizeOfBlock / 2, -0.99f, sizeOfBlock / 2);

        // FIXME we probably might need a unique visual for 1-cell shadow
        if (points.size() == 1) {
            final MeshPartBuilder handBuilder = builder.part(
                    "hand",
                    GL20.GL_TRIANGLES,
                    Usage.Position | Usage.TextureCoordinates,
                    new Material(
                            new BlendingAttribute(true, 1),
                            TextureAttribute.createDiffuse(new TextureRegion(shadowHand))
                    )
            );

            prev.add(-sizeOfBlock / 2, 0, 0);
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

            handBuilder.rect(
                    prev.cpy().add(prevOff).add(perp.cpy().scl(0.9f)),
                    prev.cpy().add(prevOff).sub(perp.cpy().scl(0.9f)),
                    visend.cpy().add(off).sub(perp.cpy().scl(1.2f)),
                    visend.cpy().add(off).add(perp.cpy().scl(1.2f)),
                    Vector3.Y
            );

            return builder.end();
        }

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

        for (int i = 1; i < points.size(); i++) {
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

        handBuilder.rect(
                prev.cpy().add(prevOff).add(perp.cpy().scl(0.9f)),
                prev.cpy().add(prevOff).sub(perp.cpy().scl(0.9f)),
                visend.cpy().add(off).sub(perp.cpy().scl(1.2f)),
                visend.cpy().add(off).add(perp.cpy().scl(1.2f)),
                Vector3.Y
        );

        return builder.end();
    }

    private void drawPlayerTrace(final Logic logic) {
        if (logic.getIsTreasureStolen()) {
            return;
        }
        
        for (final Logic.Pair pair : logic.getHistory()) {
            // pair contains an old pos.
            final Logic.Pos t = pair.pos.applyDir(pair.dir);
            final Logic.Pos oldPos = new Logic.Pos(
                    pair.pos.x - (t.x - pair.pos.x),
                    pair.pos.y - (t.y - pair.pos.y)
            );

            final Vector3 beg = logicToDisplay(oldPos)
                    .add(sizeOfBlock / 2, 0, sizeOfBlock / 2);
            final Vector3 end = logicToDisplay(pair.pos)
                    .add(sizeOfBlock / 2, 0, sizeOfBlock / 2);
            final Vector3 tracePos = beg.cpy().scl(0.5f)
                    .add(end.cpy().scl(0.5f));
            tracePos.y = -0.99f;

            //DrawDebugLine(beg, end);
            int x = pair.pos.x;
            int y = pair.pos.y;
            final Decal dec = Decal.newDecal(
                    sizeOfBlock, sizeOfBlock * 0.45f,
                    new TextureRegion(traceTexture[((x << 16) ^ y) % traceTexture.length])
            );
            dec.setColor(1, 1, 1, 0.6f);
            dec.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            dec.rotateX(-90);
            dec.setPosition(tracePos);

            switch (pair.dir) {
                case DOWN -> dec.rotateZ(-90);
                case LEFT -> dec.rotateZ(180);
                case UP -> dec.rotateZ(90);
            }

            decalBatch.add(dec);
        }
    }

    private void drawField(final Logic logic) {
        for (int y = 0; y < logic.getFieldHeight(); y++) {
            for (int x = 0; x < logic.getFieldWidth(); x++) {
                Vector3 currentCellPos = logicToDisplay(
                        new Logic.Pos(x, y)
                ).add(sizeOfBlock / 2, sizeOfBlock / 2, 0);
                final Logic.Cell cell = logic.getCell(x, y);
                final int ridx = (x << 16) ^ y;
                final Logic.Pos cellLogPos = new Logic.Pos(x, y);

                final Texture tileTexture = switch (cell.type) {
                    case FLOOR -> floor;
                    case WALL -> null;
                    case ENTRANCE -> badLogic64;
                    case TREASURE -> logic.getIsTreasureStolen() ? floor : chest;
                };

                if (tileTexture == null) {
//                    for (final Logic.MoveDirection dir : dirs) {
//                        drawTileEdge(ridx, dir, logic, cellLogPos, currentCellPos);
//                    }
                    continue;
                }

                final Decal dec = Decal.newDecal(
                        sizeOfBlock, sizeOfBlock,
                        new TextureRegion(tileTexture)
                );
//                dec.rotateX(-90);
                dec.setPosition(currentCellPos);
                decalBatch.add(dec);
            }
        }
    }

    private Vector3 cameraPos(final Logic logic) {
//        final Vector3 camPos = fieldLookAtPoint(logic);
//
//        camPos.y = 1.5f + (1 - (float)logic.getFieldHeight()/10f) * 1.2f;
//        camPos.z = 3.0f - (1 - (float)logic.getFieldHeight()/10f) * 3.0f;

//        return new Vector3(0, 0, 2);
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
        //.add(-1, -1, );
    }

    public void dispose() {
        debugRenderer.dispose();
        batch.dispose();

        modelBatch.dispose();
        shadowModels.forEach(Model::dispose);
    }
}
