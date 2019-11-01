package ai.carol.deeplinking.model;

import com.google.gson.annotations.SerializedName;

public final class ClockInDataObject {

    @SuppressWarnings("unused")
    @SerializedName("clockinCoordinates")
    private String clockinCoordinates;

    @SuppressWarnings("unused")
    @SerializedName("clockinCoordinatesAccuracy")
    private String clockinCoordinatesAccuracy;

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
    @SerializedName("deviceCode")
    private String deviceCode;

    @SuppressWarnings("unused")
    @SerializedName("deviceSyncHistoryCode")
    private String deviceSyncHistoryCode;

    @SuppressWarnings("unused")
    @SerializedName("employeePersonId")
    private String employeePersonId;

    @SuppressWarnings("unused")
    @SerializedName("isAutoDateAndTime")
    private String isAutoDateAndTime;

    @SuppressWarnings("unused")
    @SerializedName("isSelfClockin")
    private String isSelfClockin;

    @SuppressWarnings("unused")
    @SerializedName("supervisorPersonId")
    private String supervisorPersonId;

    private ClockInDataObject() { }


    @SuppressWarnings("unused")
    public String getClockinCoordinates() {
        return clockinCoordinates;
    }

    @SuppressWarnings("unused")
    public String clockinCoordinatesAccuracy() {
        return clockinCoordinatesAccuracy;
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
    public String getDeviceCode() {
        return deviceCode;
    }

    @SuppressWarnings("unused")
    public String getDeviceSyncHistoryCode() {
        return deviceSyncHistoryCode;
    }

    @SuppressWarnings("unused")
    public String getEmployeePersonId() {
        return employeePersonId;
    }

    @SuppressWarnings("unused")
    public String getIsAutoDateAndTime() {
        return isAutoDateAndTime;
    }

    @SuppressWarnings("unused")
    public String getIsSelfClockin() {
        return isSelfClockin;
    }

    @SuppressWarnings("unused")
    public String getSupervisorPersonId() {
        return supervisorPersonId;
    }

}
