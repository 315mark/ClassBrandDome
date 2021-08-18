package com.bdxh.classbrand;

import com.bdxh.classbrand.bean.ResultAdb;

public interface IGetMessageCallBack {
    public void setMessage(String message);

    //指令
    public void setCommand(ResultAdb.DataBean message);
}
