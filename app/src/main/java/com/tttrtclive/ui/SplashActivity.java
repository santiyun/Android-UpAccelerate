package com.tttrtclive.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.R;
import com.tttrtclive.bean.MyPermissionBean;
import com.tttrtclive.helper.MyPermissionManager;
import com.tttrtclive.utils.SharedPreferencesUtil;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class SplashActivity extends BaseActivity {

    public static final int ACTIVITY_MAIN = 100;

    private ProgressDialog mDialog;
    private MyPermissionManager mMyPermissionManager;

    public boolean mIsLoging;
    private EditText mRoomIDET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (action != null && mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        ArrayList<MyPermissionBean> mPermissionList = new ArrayList<>();
        mPermissionList.add(new MyPermissionBean(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.permission_record_audio)));
        mPermissionList.add(new MyPermissionBean(Manifest.permission.CAMERA, getResources().getString(R.string.permission_camera)));
        mPermissionList.add(new MyPermissionBean(Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_read_phone_state)));
        mMyPermissionManager = new MyPermissionManager(this, new MyPermissionManager.PermissionUtilsInter() {
            @Override
            public List<MyPermissionBean> getApplyPermissions() {
                return mPermissionList;
            }

            @Override
            public AlertDialog.Builder getTipAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipDialog() {
                return null;
            }

            @Override
            public AlertDialog.Builder getTipAppSettingAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipAppSettingDialog() {
                return null;
            }
        });
        boolean isOk = mMyPermissionManager.checkPermission();
        if (isOk) {
            init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyPermissionManager != null) {
            mMyPermissionManager.clearResource();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mMyPermissionManager != null) {
            boolean isOk = mMyPermissionManager.onRequestPermissionsResults(this, requestCode, permissions, grantResults);
            if (isOk) {
                init();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MyPermissionManager.REQUEST_SETTING_CODE:
                if (mMyPermissionManager != null) {
                    boolean isOk = mMyPermissionManager.onActivityResults(requestCode);
                    if (isOk) {
                        init();
                    }
                }
                break;
            case ACTIVITY_MAIN:
                mIsLoging = false;
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                break;
        }
    }

    private void initView() {
        mRoomIDET = findViewById(R.id.room_id);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getSdkVersion());
        mVersion.setText(result);

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setTitle("");
        mDialog.setMessage("正在跳转界面中...");
    }

    private void init() {
        // 初始化组件
        initView();
        // 读取保存的数据
        String roomID = (String) SharedPreferencesUtil.getParam(this, "RoomID", "");
        if (roomID != null) {
            mRoomIDET.setText(roomID);
            mRoomIDET.setSelection(roomID.length());
        }
    }

    public void onClickEnterButton(View v) {
        String mRoomName = mRoomIDET.getText().toString().trim();
        if (TextUtils.isEmpty(mRoomName)) {
            Toast.makeText(this, getResources().getString(R.string.ttt_error_enterchannel_check_channel_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.getTrimmedLength(mRoomName) >= 19) {
            Toast.makeText(this, R.string.hint_channel_name_limit, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long roomId = Long.parseLong(mRoomName);
            if (roomId <= 0) {
                Toast.makeText(this, "房间号必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "房间号只支持整型字符串", Toast.LENGTH_SHORT).show();
        }

        if (mIsLoging) return;
        mIsLoging = true;
        mDialog.show();

        Random mRandom = new Random();
        long mUserId = mRandom.nextInt(999999);
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mRoomName);
        //界面跳转
        Intent activityIntent = new Intent();
        activityIntent.putExtra("roomId", mRoomName);
        activityIntent.putExtra("uid", mUserId);
        activityIntent.setClass(mContext, MainActivity.class);
        startActivityForResult(activityIntent, ACTIVITY_MAIN);
    }
}
