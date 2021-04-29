package wraith.redutils;

import net.minecraft.util.Identifier;

public class Utils {

    public static Identifier ID(String path) {
        return new Identifier(RedUtils.MOD_ID, path);
    }

}
