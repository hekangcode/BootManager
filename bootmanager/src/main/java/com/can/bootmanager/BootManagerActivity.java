package com.can.bootmanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.can.bootmanager.adapter.AppBootManagerAdapter;
import com.can.bootmanager.bean.AppInfo;
import com.can.bootmanager.bean.BootReceiverComponentInfo;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.can.tvlib.ui.ToastUtils;
import cn.can.tvlib.ui.focus.FocusMoveUtil;
import cn.can.tvlib.ui.widgets.LoadingDialog;

public class BootManagerActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final int GET_APP_DATA_COMPLETE = 1;
    private final int FOCUS_CHANGE_FOCUS_DELAY_TIME = 100;
    private final int ITEM_FOCUS_MOVE_DELAY_TIME = 100;

    public static final String ENTRY_KEY_WHITE_APP = "whiteApp";
    private BroadcastReceiver mHomeKeyReceiver;
    private RecyclerView mRecyclerView;
    private TextView mTvNoBootApp;
    private List<AppInfo> mList;
    private AppBootManagerAdapter mAdapter;
    private Handler mHandler = new MyHandler(this);
    private Runnable mFocusMoveRunnable;
    private View mFocusedView;
    private LoadingDialog mLoadingDialog;
    private FocusMoveUtil mFocusMoveUtil;
    private ActivityManager mActivityManager;
    private String[] customPkgArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customPkgArray = getIntent().getStringArrayExtra(ENTRY_KEY_WHITE_APP);
        setContentView(R.layout.com_can_bootmanager_activity_main);
        initView();
        initData();
    }

    /**
     * 启动方法
     * 参数1：上下文
     * 参数2：流量监控要过滤应用的白名单包名数组（没有传null）
     */
    public static void actionStart(Context context, String[] pkgArray) {
        Intent intent = new Intent(context, BootManagerActivity.class);
        if (pkgArray != null && pkgArray.length > 0) {
            intent.putExtra(BootManagerActivity.ENTRY_KEY_WHITE_APP, pkgArray);
        }
        context.startActivity(intent);
    }

    private void initView() {
        mTvNoBootApp = (TextView) findViewById(R.id.tv_no_boot_app);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mFocusMoveUtil = new FocusMoveUtil(this, getWindow().getDecorView(), R.mipmap
                .com_can_bootmanager_btn_circle_focus);
    }

    private void initData() {
        showLoadingDialog();
        initHandler();
        getAllBootStartUserApp();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<BootManagerActivity> mActivity;

        private MyHandler(BootManagerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            switch (msg.what) {
                case GET_APP_DATA_COMPLETE:
                    BootManagerActivity bootManagerActivity = mActivity.get();
                    bootManagerActivity.hideLoadingDialog();
                    if (bootManagerActivity.mList != null && bootManagerActivity.mList.size() > 0) {
                        bootManagerActivity.initAdapter();
                        bootManagerActivity.initRecyclerView();
                    } else {
                        bootManagerActivity.mTvNoBootApp.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    private void initHandler() {
        mFocusMoveRunnable = new Runnable() {
            @Override
            public void run() {
                View focusedView = BootManagerActivity.this.mFocusedView;
                if (focusedView == null || !focusedView.isFocused()) {
                    return;
                }
                mFocusMoveUtil.startMoveFocus(focusedView);
            }
        };
    }

    private void initRecyclerView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getChildAt(0).requestFocus();
                mFocusMoveUtil.setFocusView(mRecyclerView.getChildAt(0));
            }
        });
        LinearLayoutManager mllManager = new LinearLayoutManager(BootManagerActivity.this);
        mRecyclerView.setLayoutManager(mllManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mHandler.postDelayed(mFocusMoveRunnable, ITEM_FOCUS_MOVE_DELAY_TIME);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mHandler.removeCallbacks(mFocusMoveRunnable);
                if (dy == 0) {
                    mHandler.post(mFocusMoveRunnable);
                }
            }
        });
    }

    private void getAllBootStartUserApp() {
        new Thread() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                List<ResolveInfo> receiversList = pm.queryBroadcastReceivers(new Intent(Intent.ACTION_BOOT_COMPLETED),
                        PackageManager.GET_DISABLED_COMPONENTS);
                Map<String, AppInfo> appMap = null;
                if (receiversList != null && receiversList.size() > 0) {
                    appMap = new HashMap<>();
                    String[] appWhiteList = AppConstants.APP_WHITE_LIST;
                    if (customPkgArray != null && customPkgArray.length > 0) {
                        appWhiteList = mergeWhiteAppArray(appWhiteList, customPkgArray);
                    }
                    for (ResolveInfo resolveinfo : receiversList) {
                        ApplicationInfo appInfo = resolveinfo.activityInfo.applicationInfo;
                        String packageName = appInfo.packageName;
                        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || Arrays.asList(appWhiteList)
                                .contains(packageName)) {
                            continue;
                        }
                        List<BootReceiverComponentInfo> componentList = new ArrayList<>();
                        BootReceiverComponentInfo componentInfo = new BootReceiverComponentInfo();
                        AppInfo app = new AppInfo();
                        ComponentName componentName = new ComponentName(packageName, resolveinfo.activityInfo.name);
                        boolean componentEnable = pm.getComponentEnabledSetting(componentName) != PackageManager
                                .COMPONENT_ENABLED_STATE_DISABLED;
                        componentInfo.setComponentName(componentName);
                        componentInfo.setComponentEnable(componentEnable);
                        if (appMap.containsKey(packageName)) {
                            appMap.get(packageName).getBootComponentList().add(componentInfo);
                        } else {
                            app.setPackageName(packageName);
                            app.setName(appInfo.loadLabel(pm).toString());
                            app.setIcon(appInfo.loadIcon(pm));
                            componentList.add(componentInfo);
                            app.setBootComponentList(componentList);
                            appMap.put(packageName, app);
                        }
                    }
                }
                if (appMap != null && appMap.size() > 0) {
                    mList = new ArrayList<>();
                    Iterator iterator = appMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        AppInfo app = (AppInfo) entry.getValue();
                        if (app != null && app.getBootComponentList() != null && app.getBootComponentList().size() >
                                0) {
                            boolean receiverEnable = true;
                            for (int i = 0; i < app.getBootComponentList().size(); i++) {
                                receiverEnable = receiverEnable && app.getBootComponentList().get(i)
                                        .isComponentEnable();
                            }
                            app.setReceiverEnable(receiverEnable);
                            mList.add(app);
                        }
                    }
                    if (mList.size() > 0) {
                        for (int i = 0; i < mList.size(); i++) {
                            AppInfo appInfo = mList.get(i);
                            if (!appInfo.isReceiverEnable()) {
                                setPackageForceStop(appInfo.getPackageName());
                            }
                        }
                    }
                }
                mHandler.sendEmptyMessage(GET_APP_DATA_COMPLETE);
            }
        }.start();
    }

    private String[] mergeWhiteAppArray(String[] ary1, String[] ary2) {
        String[] array = new String[ary1.length + ary1.length];
        System.arraycopy(ary1, 0, array, 0, ary1.length);
        System.arraycopy(ary2, 0, array, ary1.length, ary2.length);
        return array;
    }

    private void initAdapter() {
        mAdapter = new AppBootManagerAdapter(mList, this);
        mAdapter.setFocusListener(this);
        mAdapter.setOnItemFocusChangeListener(new AppBootManagerAdapter.OnItemFocusChangeListener() {
            @Override
            public void onItemFocusChange(View msgView, int position) {

            }
        });
        mAdapter.setOnItemClickListener(new AppBootManagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!isHasRootPermission()) {
                    ToastUtils.showMessage(BootManagerActivity.this, getString(R.string
                            .com_can_manager_no_root_permission));
                    return;
                }
                AppInfo appInfo = mList.get(position);
                List<BootReceiverComponentInfo> componentNamesList = appInfo.getBootComponentList();
                if (appInfo.isReceiverEnable()) {
                    for (int i = 0; i < componentNamesList.size(); i++) {
                        BootReceiverComponentInfo componentInfo = componentNamesList.get(i);
                        getPackageManager().setComponentEnabledSetting(componentInfo.getComponentName(),
                                PackageManager
                                        .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    }
                    setPackageForceStop(appInfo.getPackageName());
                    appInfo.setReceiverEnable(false);
                } else {
                    for (int i = 0; i < componentNamesList.size(); i++) {
                        BootReceiverComponentInfo componentInfo = componentNamesList.get(i);
                        if (!componentInfo.isComponentEnable()) {
                            getPackageManager().setComponentEnabledSetting(componentInfo.getComponentName(),
                                    PackageManager
                                            .COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        }
                    }
                    appInfo.setReceiverEnable(true);
                }
                mList.set(position, appInfo);
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    private void setPackageForceStop(String pkg) {
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            if (mActivityManager == null) {
                mActivityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            }
            method.invoke(mActivityManager, pkg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isHasRootPermission() {
        return getApplicationInfo().uid == 1000;
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = (LoadingDialog) cn.can.tvlib.ui.LoadingDialog.showLoadingDialog(this, getResources()
                    .getDimensionPixelSize
                            (R.dimen.px136));
        } else if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusedView = v;
            mHandler.removeCallbacks(mFocusMoveRunnable);
            mHandler.postDelayed(mFocusMoveRunnable, FOCUS_CHANGE_FOCUS_DELAY_TIME);
        }
    }

    private void startHomeKeyListener() {
        if (mHomeKeyReceiver == null) {
            mHomeKeyReceiver = new HomeKeyReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, intentFilter);
    }

    /**
     * Home键监听
     */
    protected void onHomeKeyDown() {
        finish();
    }

    class HomeKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                onHomeKeyDown();
            }
        }
    }

    @Override
    protected void onResume() {
        startHomeKeyListener();
        super.onResume();
    }

    @Override
    protected void onStop() {
        hideLoadingDialog();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHomeKeyReceiver != null) {
            unregisterReceiver(mHomeKeyReceiver);
        }
        if (mList != null) {
            mList.clear();
            mList = null;
        }
        if (mFocusMoveUtil != null) {
            mFocusMoveUtil.release();
            mFocusMoveUtil = null;
        }
        if (mAdapter != null) {
            mAdapter.setFocusListener(null);
            mAdapter.setOnItemFocusChangeListener(null);
            mAdapter = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }
}