package ai.carol.deeplinking.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import ai.carol.deeplinking.model.ClockInObject;

public final class ConverterHelper {

    private ConverterHelper() { }

    //region - Public

    public static List<ClockInObject> getClockInsFromString(final String str) {
        if (str == null) {
            return null;
        }

        final Type type = new TypeToken<List<ClockInObject>>(){}.getType();
        return new Gson().fromJson(str, type);
    }

    public static String getStringFromClockIns(final List<ClockInObject> clockIns) {
        return new Gson().toJson(clockIns, getClockInsType());
    }

    //endregion

    //region - Private

    private static Type getClockInsType() {
        TypeToken type = new TypeToken<List<ClockInObject>>(){}.getType();
        return new TypeToken<List<ClockInObject>>(){}.getType();
    }

    //endregion

}
