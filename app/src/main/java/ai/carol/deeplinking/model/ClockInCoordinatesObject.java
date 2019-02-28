package ai.carol.deeplinking.model;

import com.google.gson.annotations.SerializedName;

public final class ClockInCoordinatesObject {

    @SuppressWarnings("unused")
    @SerializedName("lat")
    private double latitude;

    @SuppressWarnings("unused")
    @SerializedName("long")
    private double longitude;

    @SuppressWarnings("unused")
    @SerializedName("formatted")
    private String formatted;


    private ClockInCoordinatesObject() { }


    @SuppressWarnings("unused")
    public double getLatitude() {
        return latitude;
    }

    @SuppressWarnings("unused")
    public double getLongitude() {
        return longitude;
    }

    @SuppressWarnings("unused")
    public String getFormatted() {
        return formatted;
    }

}
