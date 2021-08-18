package com.bdxh.classbrand.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class UniqueIDUtils {

    public static String getLocalMacIdFromIp(){
        String strMacAddr = "";
        try {
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }

                String str = Integer.toHexString(b[i]&0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMacAddr;
    }

    /**
     * 获取设备本地IP
     */
    protected static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //列举
            Enumeration en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = (InetAddress) en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

}
