package com.bdxh.classbrand.bean;


public class MsgInfo  {


    /**
     * result : 1
     * message : OK
     * jsonData : {"mDeviceVersionInfo":{"id":1,"versionId":"1","versionType":1,"deviceType":1,"versionCode":"1.0.1","packageUrl":"https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk","versionDesc":"1","usableStatus":1,"reviserAdminId":"1","creatorAdminId":"1","status":1,"modifyTime":0,"createTime":11111}}
     */

    public String result;
    private String message;
    private JsonDataBean jsonData;

    public JsonDataBean getJsonData() {
        return jsonData;
    }


    public static class JsonDataBean {

        /**
         * mDeviceVersionInfo : {"id":1,"versionId":"1","versionType":1,"deviceType":1,"versionCode":"1.0.1","packageUrl":"https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk","versionDesc":"1","usableStatus":1,"reviserAdminId":"1","creatorAdminId":"1","status":1,"modifyTime":0,"createTime":11111}
         */
        private DeviceVersionInfo mDeviceVersionInfo;
        private String primaryFileUrl;

        public String getPrimaryFileUrl() {
            return primaryFileUrl;
        }

        public DeviceVersionInfo getDeviceVersionInfo() {
            return mDeviceVersionInfo;
        }

        @Override
        public String toString() {
            return "JsonDataBean{" +
                    "mDeviceVersionInfo=" + mDeviceVersionInfo +
                    ", primaryFileUrl='" + primaryFileUrl + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MsgInfo{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                ", jsonData=" + jsonData +
                '}';
    }
}
