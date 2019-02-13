package ai.carol.deeplinking.model;

import com.google.gson.annotations.SerializedName;

public final class ClockInObject {

    @SuppressWarnings("unused")
    @SerializedName("clockinCoordinates")
    private String clockinCoordinates;

    @SuppressWarnings("unused")
    @SerializedName("clockinDatetime")
    private String clockinDatetime;

    @SuppressWarnings("unused")
    @SerializedName("clockinDatetimeStr")
    private String clockinDatetimeStr;

    @SuppressWarnings("unused")
    @SerializedName("clockinMode")
    private int clockinMode;

    @SuppressWarnings("unused")
    @SerializedName("clockinNSRNumber")
    private int clockinNSRNumber;

    @SuppressWarnings("unused")
    @SerializedName("deviceCode")
    private String deviceCode;

    @SuppressWarnings("unused")
    @SerializedName("employeePersonId")
    private String employeePersonId;

    @SuppressWarnings("unused")
    @SerializedName("clockinImage")
    private String clockinImage;

    @SuppressWarnings("unused")
    @SerializedName("locationCode")
    private String locationCode;

    @SuppressWarnings("unused")
    @SerializedName("smsSentOnDateTime")
    private String smsSentOnDateTime;

    @SuppressWarnings("unused")
    @SerializedName("supervisorPersonId")
    private String supervisorPersonId;

    private ClockInObject() { }

    @SuppressWarnings("unused")
    public String getClockinCoordinates() {
        return clockinCoordinates;
    }

    @SuppressWarnings("unused")
    public String getClockinDatetime() {
        return clockinDatetime;
    }

    @SuppressWarnings("unused")
    public String getClockinDatetimeStr() {
        return clockinDatetimeStr;
    }

    @SuppressWarnings("unused")
    public int getClockinMode() {
        return clockinMode;
    }

    @SuppressWarnings("unused")
    public int getClockinNSRNumber() {
        return clockinNSRNumber;
    }

    @SuppressWarnings("unused")
    public String getDeviceCode() {
        return deviceCode;
    }

    @SuppressWarnings("unused")
    public String getEmployeePersonId() {
        return employeePersonId;
    }

    @SuppressWarnings("unused")
    public String getClockinImage() {
        return clockinImage;
    }

    @SuppressWarnings("unused")
    public String getLocationCode() {
        return locationCode;
    }

    @SuppressWarnings("unused")
    public String getSmsSentOnDateTime() {
        return smsSentOnDateTime;
    }

    @SuppressWarnings("unused")
    public String getSupervisorPersonId() {
        return supervisorPersonId;
    }

}
