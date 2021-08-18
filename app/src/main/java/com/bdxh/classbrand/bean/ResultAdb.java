package com.bdxh.classbrand.bean;

public class ResultAdb {

    /**
     * data : {"operation":"ScreenCapture"}
     */

    public DataBean data;


    public static class DataBean {

        /**
         * 下发指令
         */
        private String operation;

        private String timeOn;

        private String timeOff;

        private String versionId;

        private String time;

        private String mac;

        //wifi ip
        private String ip;

        private String ipV6;

        //网线ip
        private String netIp;

        public String getIpV6() {
            return ipV6;
        }

        public void setIpV6(String ipV6) {
            this.ipV6 = ipV6;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getNetIp() {
            return netIp;
        }

        public void setNetIp(String netIp) {
            this.netIp = netIp;
        }

        public String getOperation(){
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getTimeOn() {
            return timeOn;
        }

        public String getTimeOff() {
            return timeOff;
        }

        public String getVersionId() {
            return versionId;
        }

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "operation='" + operation + '\'' +
                    ", timeOn='" + timeOn + '\'' +
                    ", timeOff='" + timeOff + '\'' +
                    ", versionId='" + versionId + '\'' +
                    ", time ='" + time + '\'' +
                    ", mac='" + mac + '\'' +
                    ", ip ='" + ip + '\'' +
                    ", netIp='" + netIp + '\'' +
                    ", ipV6='" + ipV6 + '\'' +
                    '}';
        }
    }
}
