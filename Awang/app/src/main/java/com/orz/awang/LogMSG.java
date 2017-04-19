package com.orz.awang;

/**
 * Created by scotg on 17/3/2
 */

public class LogMSG {
    private int type;
    private String MSG;

    public LogMSG(String message,int imgType){
        MSG = message;
        type = imgType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMSG() {
        return MSG;
    }

    public void setMSG(String MSG) {
        this.MSG = MSG;
    }

}
