package com.inducesmile.citizenreportingtool;

public class IncidentReport {
    private String mIncidentDescription;
    private String mImagePath;
    private Integer mEmergencyLevel;

    public IncidentReport(String mIncidentDescription, String mImagePath, Integer mEmergencyLevel) {
        this.mIncidentDescription = mIncidentDescription;
        this.mImagePath = mImagePath;
        this.mEmergencyLevel = mEmergencyLevel;
    }

    public String getmIncidentDescription() {
        return mIncidentDescription;
    }

    public void setmIncidentDescription(String mIncidentDescription) {
        this.mIncidentDescription = mIncidentDescription;
    }

    public String getmImagePath() {
        return mImagePath;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public Integer getmEmergencyLevel() {
        return mEmergencyLevel;
    }

    public void setmEmergencyLevel(Integer mEmergencyLevel) {
        this.mEmergencyLevel = mEmergencyLevel;
    }
}
