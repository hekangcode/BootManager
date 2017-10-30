package com.can.bootmanager.bean;

import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Created by HEKANG on 2017/3/1.
 */

public class AppInfo {

    private String packageName;
    private String name;
    private Drawable icon;
    private boolean receiverEnable; // 综合得出的开机广播接收者的启动模式，true 为开启状态 false 为关闭状态
    private List<BootReceiverComponentInfo> bootComponentList;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isReceiverEnable() {
        return receiverEnable;
    }

    public void setReceiverEnable(boolean receiverEnable) {
        this.receiverEnable = receiverEnable;
    }

    public List<BootReceiverComponentInfo> getBootComponentList() {
        return bootComponentList;
    }

    public void setBootComponentList(List<BootReceiverComponentInfo> bootComponentList) {
        this.bootComponentList = bootComponentList;
    }
}
