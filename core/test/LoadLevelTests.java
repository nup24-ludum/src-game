import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.mygdx.game.Logic;
import com.mygdx.game.MyGame;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@RunWith(GdxTestRunner.class)
public class LoadLevelTests {
    @Test
    public void testIsCellType() {
        final MyGame game = new MyGame();

        game.loadField("map1.tmx");

    }
}
