package com.tttrtclive.live.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import so.library.SoSpinner;

@SuppressLint("ValidFragment")
public class LocalFragment extends Fragment implements SoSpinner.OnItemSelectedListener {

    private VideoProfileManager mVideoProfileManager = new VideoProfileManager();
    private EditText mPixView, mBiteView, mFrameView, mFrameIP, mFramePort, mFramePushUrl;
    private VideoProfileManager.VideoProfile mVideoProfile;
    private SetActivity mSetActivity;
    public String mPushUrlPrefix = "rtmp://push.3ttech.cn/sdk/";
    private static LocalFragment sf;

    public static LocalFragment getInstance() {
        if (sf == null)
            sf = new LocalFragment();
        return sf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSetActivity = (SetActivity) getActivity();
        View v = inflater.inflate(R.layout.local_set, null);

        mPixView = v.findViewById(R.id.local_pix_view);
        mBiteView = v.findViewById(R.id.local_bite_rate);
        mFrameView = v.findViewById(R.id.local_frame_rate);
        mFrameIP = v.findViewById(R.id.local_frame_ip);
        mFramePort = v.findViewById(R.id.local_frame_port);
        mFramePushUrl = v.findViewById(R.id.local_frame_push_url);

        SoSpinner localPixSpinner = v.findViewById(R.id.local_pix_spinner);
        localPixSpinner.setOnItemSelectedListener(this);

        if (mSetActivity.mLocalVideoProfile != 0) {
            mVideoProfile = mVideoProfileManager.getVideoProfile(mSetActivity.mLocalVideoProfile);
            localPixSpinner.setSelectedIndex(mVideoProfileManager.mVideoProfiles.indexOf(mVideoProfile));
        } else {
            mPixView.setText(mSetActivity.mLocalWidth + "x" + mSetActivity.mLocalHeight);
            mBiteView.setText(mSetActivity.mLocalBitRate + "");
            mFrameView.setText(mSetActivity.mLocalFrameRate + "");

            localPixSpinner.setSelectedIndex(5);
        }

        if (TextUtils.isEmpty(mSetActivity.mLocalIP)) {
            mFrameIP.setText("39.106.33.180");
        } else {
            mFrameIP.setText(mSetActivity.mLocalIP);
        }

        if (mSetActivity.mLocalPort != 0) {
            mFramePort.setText(String.valueOf(mSetActivity.mLocalPort));
        } else {
            mFramePort.setText(String.valueOf(25000));
        }

        if (LocalConfig.mIsCusPushUrl) {
            mFramePushUrl.setText(mSetActivity.mLocalPushUrl);
        } else {
            if (TextUtils.isEmpty(LocalConfig.mRoomID)) {
                mFramePushUrl.setText(mPushUrlPrefix);
            } else {
                mFramePushUrl.setText(mPushUrlPrefix + LocalConfig.mRoomID);
            }
        }
        return v;
    }

    @Override
    public void onItemSelected(View parent, int position) {
        if (position != 5) {
            mVideoProfile = mVideoProfileManager.mVideoProfiles.get(position);
            mSetActivity.mLocalVideoProfile = mVideoProfile.videoProfile;
            mPixView.setText(mVideoProfile.width + "x" + mVideoProfile.height);
            mBiteView.setText(mVideoProfile.bRate + "");
            mFrameView.setText(mVideoProfile.fRate + "");

            mPixView.setEnabled(false);
            mBiteView.setEnabled(false);
            mFrameView.setEnabled(false);
        } else {
            mSetActivity.mLocalVideoProfile = 0;

            mPixView.setEnabled(true);
            mBiteView.setEnabled(true);
            mFrameView.setEnabled(true);
        }
    }

    public boolean getParams() {
        if (mPixView.getText() == null || TextUtils.isEmpty(mPixView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频分辨率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] wh = mPixView.getText().toString().trim().split("x");
        if (wh.length != 2) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            mSetActivity.mLocalWidth = Integer.parseInt(wh[0]);
            if (mSetActivity.mLocalWidth <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率宽必须大于0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            mSetActivity.mLocalHeight = Integer.parseInt(wh[1]);
            if (mSetActivity.mLocalHeight <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率高必须大于0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mBiteView.getText() == null || TextUtils.isEmpty(mBiteView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频码率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        mSetActivity.mLocalBitRate = Integer.parseInt(mBiteView.getText().toString().trim());
        if (mSetActivity.mLocalBitRate <= 0) {
            Toast.makeText(getContext(), "自定义视频码率必须大于0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mFrameView.getText() == null || TextUtils.isEmpty(mFrameView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频帧率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        mSetActivity.mLocalFrameRate = Integer.parseInt(mFrameView.getText().toString().trim());
        if (mSetActivity.mLocalFrameRate <= 0) {
            Toast.makeText(getContext(), "自定义视频帧率必须大于0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mFrameIP.getText() != null && !TextUtils.isEmpty(mFrameIP.getText().toString())) {
            String str = mFrameIP.getText().toString();
            String pattern = "(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})(\\.(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})){3}";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(str);
            if (!m.find()) {
                Toast.makeText(getContext(), "IP设置的格式有问题", Toast.LENGTH_SHORT).show();
                return false;
            }
            mSetActivity.mLocalIP = mFrameIP.getText().toString().trim();
        }

        String defPushUrl = mPushUrlPrefix + LocalConfig.mRoomID;
        String pushUrl = mFramePushUrl.getText().toString().trim();
        if (defPushUrl.equals(pushUrl)) {
            LocalConfig.mIsCusPushUrl = false;
        } else {
            LocalConfig.mIsCusPushUrl = true;
        }
        mSetActivity.mLocalPushUrl = pushUrl;

        if (!TextUtils.isEmpty(mFramePort.getText())) {
            mSetActivity.mLocalPort = Integer.parseInt(mFramePort.getText().toString().trim());
        } else {
            mSetActivity.mLocalPort = 0;
        }
        return true;
    }

}