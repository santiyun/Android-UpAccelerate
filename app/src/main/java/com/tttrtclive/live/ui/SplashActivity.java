package com.tttrtclive.live.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.JniObjs;
import com.tttrtclive.live.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.live.utils.MyLog;
import com.tttrtclive.live.utils.SharedPreferencesUtil;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;
import com.yanzhenjie.permission.AndPermission;

import java.util.Random;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;

public class SplashActivity extends BaseActivity {

    private static final int ACTIVITY_MAIN = 100;
    private static final int ACTIVITY_SETTING = 101;
    private ProgressDialog mDialog;
    private boolean mIsLoging;
    private EditText mRoomIDET;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private String mRoomName;
    private long mUserId;

    /*-------------------------------配置参数---------------------------------*/
    private int mLocalVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    private int mPushVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    public String mLocalIP, mLocalPushUrl;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate, mLocalPort;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    private int mEncodeType = 0;//0:H.264  1:H.265
    private int mAudioSRate = 0;// 0:48000 1:44100
    public int mChannels = 1;
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        // 权限申请
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .start();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (mTTTEngine == null) {
            mTTTEngine = TTTRtcEngine.getInstance();
        }

        initView();
        // 读取保存的数据
        String roomID = (String) SharedPreferencesUtil.getParam(this, "RoomID", "");
        mRoomIDET.setText(roomID);
        mRoomIDET.setSelection(mRoomIDET.length());
        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.ttt_hint_loading_channel));
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
    }

    private void initView() {
        mRoomIDET = findViewById(R.id.room_id);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String version = TTTRtcEngine.getInstance().getSdkVersion();
        String result = String.format(string, version);
        mVersion.setText(result);
    }

    public void onClickEnterButton(View v) {
        mRoomName = mRoomIDET.getText().toString().trim();
        if (TextUtils.isEmpty(mRoomName)) {
            Toast.makeText(this, getString(R.string.ttt_error_enterchannel_check_channel_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsLoging) return;
        mIsLoging = true;

        Random mRandom = new Random();
        mUserId = mRandom.nextInt(999999);

        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mRoomName);
        // 设置推流格式H264/H265的推流地址
        if (!LocalConfig.mIsCusPushUrl) {
            mLocalPushUrl = "rtmp://push.3ttech.cn/sdk/" + mRoomName;
        }

        if (TextUtils.isEmpty(mLocalIP)) {
            mLocalIP = "39.106.33.180";
        }

        if (mLocalPort == 0) {
            mLocalPort = 25000;
        }

        if (mEncodeType != 0) {
            mLocalPushUrl += "?trans=1";
        }
        initSDK();
        // 加入直播房间
        MyLog.d("set push url : " + mLocalPushUrl);
        MyLog.d("set ip : " + mLocalIP + " | port : " + mLocalPort);
        mTTTEngine.joinChannel("", mRoomName, mUserId);
        mDialog.show();
    }

    private void initSDK() {
        // 设置直播模式
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        // 启用视频功能
        mTTTEngine.enableVideo();
        // 设置主播角色
        mTTTEngine.setClientRole(CLIENT_ROLE_ANCHOR);
        // 设置视频编码等级
        if (mLocalVideoProfile != 0) {
            TTTRtcEngine.getInstance().setVideoProfile(mLocalVideoProfile, false);
        } else {
            // 自定义宽,高,码率等参数
            TTTRtcEngine.getInstance().setVideoProfile(mLocalWidth, mLocalHeight, mLocalBitRate, mLocalFrameRate);
        }
        // 设置推流地址
        PublisherConfiguration mPublisherConfiguration = new PublisherConfiguration();
        if (LocalConfig.mIsCusPushUrl) {
            mPublisherConfiguration.setPushUrl(mLocalPushUrl);
        } else {
            mPublisherConfiguration.setPushUrl("rtmp://push.3ttech.cn/sdk/" + mRoomName);
        }
        mTTTEngine.configPublisher(mPublisherConfiguration);
        // 设置服务器地址
        if (!TextUtils.isEmpty(mLocalIP)) {
            mTTTEngine.setServerIp(String.valueOf(mLocalIP), mLocalPort);
        }
        // 启用高音质选项
        mTTTEngine.enableUplinkAccelerate(true);
        mTTTEngine.setHighQualityAudioParameters(true);
        // 设置视频服务器混流相关参数
        TTTRtcEngine.getInstance().setVideoMixerParams(mPushBitRate, mPushFrameRate, mPushWidth, mPushHeight);
        // 设置音频服务器混流相关参数
        TTTRtcEngine.getInstance().setAudioMixerParams(mAudioSRate, mAudioSRate == 0 ? 48000 : 44100, mChannels);
    }

    public void onSetButtonClick(View v) {
        mRoomName = mRoomIDET.getText().toString().trim();
        LocalConfig.mRoomID = mRoomName;
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra("LVP", mLocalVideoProfile);
        intent.putExtra("PVP", mPushVideoProfile);
        intent.putExtra("LWIDTH", mLocalWidth);
        intent.putExtra("LHEIGHT", mLocalHeight);
        intent.putExtra("LBRATE", mLocalBitRate);
        intent.putExtra("LFRATE", mLocalFrameRate);
        intent.putExtra("LIP", mLocalIP);
        intent.putExtra("LPUSHURL", mLocalPushUrl);
        intent.putExtra("LPORT", mLocalPort);
        intent.putExtra("PWIDTH", mPushWidth);
        intent.putExtra("PHEIGHT", mPushHeight);
        intent.putExtra("PBRATE", mPushBitRate);
        intent.putExtra("PFRATE", mPushFrameRate);
        intent.putExtra("EDT", mEncodeType);
        intent.putExtra("ASR", mAudioSRate);
        intent.putExtra("CHN", mChannels);
        startActivityForResult(intent, ACTIVITY_SETTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case ACTIVITY_MAIN:
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                break;
            case ACTIVITY_SETTING:
                mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
                mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
                mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
                mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
                mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
                mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
                mLocalIP = intent.getStringExtra("LIP");
                mLocalPushUrl = intent.getStringExtra("LPUSHURL");
                mLocalPort = intent.getIntExtra("LPORT", mLocalPort);
                mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
                mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
                mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
                mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
                mEncodeType = intent.getIntExtra("EDT", mEncodeType);
                mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);
                mChannels = intent.getIntExtra("CHN", mChannels);
                break;
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        //界面跳转
                        Intent activityIntent = new Intent();
                        activityIntent.putExtra("ROOM_ID", Long.parseLong(mRoomName));
                        activityIntent.putExtra("USER_ID", mUserId);
                        activityIntent.putExtra("ROLE", CLIENT_ROLE_ANCHOR);
                        activityIntent.setClass(mContext, MainActivity.class);
                        startActivityForResult(activityIntent, ACTIVITY_MAIN);
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mDialog.dismiss();
                        mIsLoging = false;
                        int errorType = mJniObjs.mErrorType;
                        if (errorType == Constants.ERROR_ENTER_ROOM_INVALIDCHANNELNAME) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_format), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_TIMEOUT) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_timeout), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_token_invaild), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_BAD_VERSION) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_version), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_CONNECT_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_unconnect), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_NOEXIST) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_room_no_exist), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_SERVER_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_verification_failed), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_unknow), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

}
