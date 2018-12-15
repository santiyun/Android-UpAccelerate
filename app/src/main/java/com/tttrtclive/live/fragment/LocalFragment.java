package com.tttrtclive.live.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;

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

        if (TextUtils.isEmpty(mSetActivity.mLocalPushUrl)) {
            if (TextUtils.isEmpty(mSetActivity.mRoomID)) {
                mFramePushUrl.setText(mPushUrlPrefix);
            } else {
                mFramePushUrl.setText(mPushUrlPrefix + mSetActivity.mRoomID);
            }
        } else {
            mFramePushUrl.setText(mSetActivity.mLocalPushUrl);
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

    public void getParams() {
        String[] wh = mPixView.getText().toString().trim().split("x");
        mSetActivity.mLocalWidth = Integer.parseInt(wh[0]);
        mSetActivity.mLocalHeight = Integer.parseInt(wh[1]);
        mSetActivity.mLocalBitRate = Integer.parseInt(mBiteView.getText().toString().trim());
        mSetActivity.mLocalFrameRate = Integer.parseInt(mFrameView.getText().toString().trim());
        mSetActivity.mLocalIP = mFrameIP.getText().toString().trim();
        mSetActivity.mLocalPushUrl = mFramePushUrl.getText().toString().trim();
        if (!TextUtils.isEmpty(mFramePort.getText())) {
            mSetActivity.mLocalPort = Integer.parseInt(mFramePort.getText().toString().trim());
        } else {
            mSetActivity.mLocalPort = 0;
        }
    }

}