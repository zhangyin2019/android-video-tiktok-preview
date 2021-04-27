package com.sanxin.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.sanxin.R;
import com.sanxin.adapter.CustomVideoPlayerAdapter;
import com.sanxin.config.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CustomVideoPlayerActivity extends CommonActivity {
    int currentPosition = 0;
    int delayMills = 250;

    Timer custom_video_player_timer;
    Long currentProgressPosition;

    ViewPager2 custom_video_view_pager;
    SeekBar custom_video_player_progress;

    AudioManager audioManager;
    BroadcastReceiver volume_broadcastReceiver;

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.card_open_enter, R.anim.card_open_exit);
        setContentView(R.layout.activity_custom_video_player);

        // 获取传参
        currentPosition = getIntent().getIntExtra("currentProgressPosition", 0);
        List<String> videoUrlList = getIntent().getStringArrayListExtra("videoUrlList");

        // 补充状态栏高度
        LinearLayout custom_web_view_top_layout = findViewById(R.id.custom_video_player_top);
        custom_web_view_top_layout.setPadding(
                custom_web_view_top_layout.getPaddingLeft(),
                custom_web_view_top_layout.getPaddingTop() + BarUtils.getStatusBarHeight(),
                custom_web_view_top_layout.getPaddingRight(),
                custom_web_view_top_layout.getPaddingBottom()
        );

        // viewpager
        custom_video_view_pager = findViewById(R.id.custom_video_view_pager);
        custom_video_view_pager.setCurrentItem(currentPosition);
        custom_video_view_pager.setAdapter(new CustomVideoPlayerAdapter(this)
                .setVideoUrlList(videoUrlList)
                .setOnIsPlayingChangedCallback(isPlaying -> {
                    if (isPlaying) {
                        startPlayTimer();
                        if (custom_video_player_progress.getVisibility() == View.GONE) {
                            custom_video_player_progress.setVisibility(View.VISIBLE);
                        }
                    } else {
                        pausePlayTimer();
                    }
                })
                .setOnLongClickListener(position -> {
                    // 底部选项弹窗
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CustomVideoPlayerActivity.this);
                    String[] strings = new String[]{"分享", "取消"};
                    ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(CustomVideoPlayerActivity.this,
                            R.layout.item_custom_bottom_sheet_dialog, strings);
                    ListView listView = new ListView(CustomVideoPlayerActivity.this);
                    listView.setAdapter(stringArrayAdapter);
                    listView.setDivider(new ColorDrawable(ContextCompat.getColor(CustomVideoPlayerActivity.this, R.color.col_eee)));
                    listView.setDividerHeight(ConvertUtils.dp2px(1));
                    listView.setOnItemClickListener((parent, view, position1, id) -> {
                        String name = ((TextView) view).getText().toString();
                        switch (name) {
                            case "分享":
                                // TODO 接入微信分享
                                break;
                            default:
                                bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(listView);
                    bottomSheetDialog.show();
                })
        );
        custom_video_view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 重置并隐藏进度条
                currentProgressPosition = 0L;
                List<StyledPlayerView> styledPlayerViewList = ((CustomVideoPlayerAdapter) custom_video_view_pager.getAdapter()).getStyledPlayerViewList();
                styledPlayerViewList.get(currentPosition).getPlayer().seekTo(currentProgressPosition);
                custom_video_player_progress.setProgress(0);
                custom_video_player_progress.setVisibility(View.GONE);

                // 看过的暂停
                styledPlayerViewList.get(currentPosition).getPlayer().pause();

                currentPosition = position;

                // 没看过的自动播放
                styledPlayerViewList = ((CustomVideoPlayerAdapter) custom_video_view_pager.getAdapter()).getStyledPlayerViewList();
                styledPlayerViewList.get(currentPosition).getPlayer().prepare();
                styledPlayerViewList.get(currentPosition).getPlayer().play();

            }
        });

        // 返回按钮
        ImageView custom_video_player_go_back = findViewById(R.id.custom_video_player_go_back);
        custom_video_player_go_back.setOnClickListener(this::onClick);

        // 声音
        ImageView custom_video_player_audio = findViewById(R.id.custom_video_player_audio);
        custom_video_player_audio.setOnClickListener(this::onClick);

        // 旋转屏幕按钮
        ImageView custom_video_player_rotate_screen = findViewById(R.id.custom_video_player_rotate_screen);
        custom_video_player_rotate_screen.setOnClickListener(this::onClick);

        // 音量调节监听
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
        intentFilter.addAction(VOLUME_CHANGED_ACTION);
        volume_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(VOLUME_CHANGED_ACTION)) {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVolume > 0) {
                        custom_video_player_audio.setImageResource(R.drawable.audio);
                    } else {
                        custom_video_player_audio.setImageResource(R.drawable.audio_close);
                    }
                }
            }
        };
        registerReceiver(volume_broadcastReceiver, intentFilter);

        // 进度条
        custom_video_player_progress = findViewById(R.id.custom_video_player_progress);
        custom_video_player_progress.setVisibility(View.GONE);
        custom_video_player_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                List<StyledPlayerView> styledPlayerViewList = ((CustomVideoPlayerAdapter) custom_video_view_pager.getAdapter()).getStyledPlayerViewList();
                StyledPlayerView playerView = styledPlayerViewList.get(currentPosition);

                boolean isPlaying = playerView.getPlayer().isPlaying();

                // 进度调整
                float ratio = (float) seekBar.getProgress() / seekBar.getMax();
                currentProgressPosition = (long) ((float) playerView.getPlayer().getDuration() * ratio);
                playerView.getPlayer().seekTo(currentProgressPosition);

                // 保持播放状态
                if (isPlaying) {
                    playerView.getPlayer().play();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        pausePlayTimer();

        // 释放所有视频
        for (StyledPlayerView playerView : ((CustomVideoPlayerAdapter) custom_video_view_pager.getAdapter()).getStyledPlayerViewList()) {
            playerView.getPlayer().release();
        }

        // 取消注册音频广播
        unregisterReceiver(volume_broadcastReceiver);
    }

    /**
     * 暂停播放
     */
    private void pausePlayTimer() {
        if (custom_video_player_timer == null) return;
        custom_video_player_timer.cancel();
    }

    /**
     * 开始播放
     */
    private void startPlayTimer() {
        List<StyledPlayerView> styledPlayerViewList = ((CustomVideoPlayerAdapter) custom_video_view_pager.getAdapter()).getStyledPlayerViewList();
        StyledPlayerView playerView = styledPlayerViewList.get(currentPosition);
        custom_video_player_timer = new Timer();
        custom_video_player_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    currentProgressPosition = playerView.getPlayer().getCurrentPosition();
                    long duration = playerView.getPlayer().getDuration();
                    int percent = (int) ((float) currentProgressPosition / duration * 100);
                    custom_video_player_progress.setProgress(percent);
                });

            }
        }, delayMills, delayMills);
    }

    /**
     * 点击事件
     *
     * @param v-
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (Utils.isCeaselessClick()) return;

        switch (v.getId()) {
            case R.id.custom_video_player_rotate_screen:
                if (ScreenUtils.isPortrait()) {
                    // 变为横屏
                    ScreenUtils.setLandscape(this);
                } else {
                    // 变为竖屏
                    ScreenUtils.setPortrait(this);
                }
                break;
            case R.id.custom_video_player_audio:
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Snackbar.make(v, String.format("当前音量：%s%%", (int) ((float) currentVolume / maxVolume * 100)), Snackbar.LENGTH_SHORT).show();
                audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_SAME,
                        AudioManager.FLAG_SHOW_UI
                );
                break;
            case R.id.custom_video_player_go_back:
                super.finishActivity();
                break;
        }
    }
}