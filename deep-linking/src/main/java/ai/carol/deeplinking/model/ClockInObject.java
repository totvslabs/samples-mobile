package ai.carol.deeplinking.model;

import com.google.gson.annotations.SerializedName;

public final class ClockInObject {

    @SuppressWarnings("unused")
    @SerializedName("name")
    private String name;

    @SuppressWarnings("unused")
    @SerializedName("data")
    private ClockInDataObject data;


    private ClockInObject() { }


    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public ClockInDataObject getData() {
        return data;
    }

}
