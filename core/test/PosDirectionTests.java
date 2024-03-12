import com.mygdx.game.Logic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class PosDirectionTests {
    @Test
    public void testApplyDirection() {
        // TODO better coverage
        final Logic.Pos pos = new Logic.Pos(0, 0);
        final Logic.Pos exPos = new Logic.Pos(-1, 0);

        Assertions.assertEquals(exPos, pos.applyDir(Logic.MoveDirection.LEFT));
    }
}
