package at.vl.util;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import at.vl.Main;

public class JsonHelper {
    public static Main main;

    public static JsonValue getConfigValue() {
        return main.getReader().parse(Gdx.files.internal("data" + File.separator + "config.json"));
    }

    public static JsonValue getRoomValue() {
        return main.getReader().parse(Gdx.files.internal("data" + File.separator + "rooms.json"));
    }

    public static void setMain(Main main) {
        JsonHelper.main = main;
    }
}
