package com.can.bootmanager.bean;

import android.content.ComponentName;

/**
 * Created by HEKANG on 2017/3/7.
 */

public class BootReceiverComponentInfo {

    private ComponentName componentName;
    boolean ComponentEnable;  //单个组建的启动模式

    public ComponentName getComponentName() {
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public boolean isComponentEnable() {
        return ComponentEnable;
    }

    public void setComponentEnable(boolean componentEnable) {
        ComponentEnable = componentEnable;
    }
}
