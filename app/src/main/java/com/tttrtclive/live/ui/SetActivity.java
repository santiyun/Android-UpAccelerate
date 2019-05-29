package com.tttrtclive.live.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.tttrtclive.live.R;
import com.tttrtclive.live.fragment.LocalFragment;
import com.tttrtclive.live.fragment.PushFragment;
import com.wushuangtech.library.Constants;

import java.util.ArrayList;

public class SetActivity extends BaseActivity {

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = {"本地设置", "推流设置"};
    private SegmentTabLayout mTabLayout_1;

    /*-------------------------------配置参数---------------------------------*/
    public int mLocalVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    public int mPushVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    public String mLocalIP, mLocalPushUrl;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate, mLocalPort;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    public int mEncodeType = 0;//0:H.264  1:H.265
    public int mAudioSRate = 0;// 0:48000 1:44100
    public int mChannels = 1;
    public String mRoomID;
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Intent intent = getIntent();
        mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
        mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
        mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
        mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
        mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
        mLocalIP = intent.getStringExtra("LIP");
        mLocalPushUrl = intent.getStringExtra("LPUSHURL");
        mLocalPort = intent.getIntExtra("LPORT", mLocalPort);
        mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
        mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
        mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
        mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
        mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
        mEncodeType = intent.getIntExtra("EDT", mEncodeType);
        mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);
        mChannels = intent.getIntExtra("CHN", mChannels);
        mRoomID = intent.getStringExtra("ROOMID");

        mFragments.add(LocalFragment.getInstance());
        mFragments.add(PushFragment.getInstance());

        mTabLayout_1 = findViewById(R.id.tl_1);

        mTabLayout_1.setTabData(mTitles);
        tl_1();
    }


    private void tl_1() {
        final ViewPager vp_3 = findViewById(R.id.vp_2);
        vp_3.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mTabLayout_1.setTabData(mTitles);
        mTabLayout_1.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp_3.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vp_3.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout_1.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_3.setCurrentItem(0);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    public void onExitButtonClick(View v) {
        exit();
    }

    @Override
    public void onBackPressed() {
        exit();
        super.onBackPressed();
    }

    public void onOkButtonClick(View v) {
        boolean params = LocalFragment.getInstance().getParams();
        if (!params) {
            return ;
        }
        boolean params2 = PushFragment.getInstance().getParams();
        if (!params2) {
            return ;
        }
        exit();
    }

    private void exit() {
        Intent intent = new Intent();
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
        setResult(1, intent);
        finish();
    }
}
