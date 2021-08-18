package com.bdxh.classbrand.bean;


public class ResultInfo {

    /**
     * data : {"versionId":"1","deviceType":"1"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private String versionId;
        private String deviceType;
        private String deviceCode;
        private String mediaFileBase64;

        public String getVersionId() {
            return versionId;
        }

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getDeviceCode() {
            return deviceCode;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }

        public String getMediaFileBase64() {
            return mediaFileBase64;
        }

        public void setMediaFileBase64(String mediaFileBase64) {
            this.mediaFileBase64 = mediaFileBase64;
        }
    }


}
