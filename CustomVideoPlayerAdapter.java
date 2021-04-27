package com.sanxin.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.sanxin.R;
import com.sanxin.config.Utils;

import java.util.ArrayList;
import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;

public class CustomVideoPlayerAdapter extends RecyclerView.Adapter<CustomVideoPlayerAdapter.CustomVideoPlayerViewHolder> {
    Activity activity;

    List<String> videoUrlList = new ArrayList<>();
    List<StyledPlayerView> styledPlayerViewList = new ArrayList<>();

    PromptDialog promptDialog;

    Utils.Callback<Boolean> onIsPlayingChangedCallback;
    Utils.Callback<Integer> onLongClickListener;

    public CustomVideoPlayerAdapter(Activity activity) {
        this.activity = activity;
        promptDialog = new PromptDialog(activity);
    }

    public CustomVideoPlayerAdapter setOnLongClickListener(Utils.Callback<Integer> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    public CustomVideoPlayerAdapter setOnIsPlayingChangedCallback(Utils.Callback<Boolean> onIsPlayingChangedCallback) {
        this.onIsPlayingChangedCallback = onIsPlayingChangedCallback;
        return this;
    }

    public CustomVideoPlayerAdapter setVideoUrlList(List<String> videoUrlList) {
        this.videoUrlList = videoUrlList;
        return this;
    }

    public List<StyledPlayerView> getStyledPlayerViewList() {
        return styledPlayerViewList;
    }

    @NonNull
    @Override
    public CustomVideoPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVideoPlayerViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_video_player, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVideoPlayerViewHolder holder, int position) {
        if (holder.isBind) return;
        holder.isBind = true;

        holder.video.getPlayer().setMediaItem(MediaItem.fromUri(videoUrlList.get(position)));
        holder.video.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    holder.play.setVisibility(View.GONE);
                    promptDialog.dismissImmediately();
                } else {
                    holder.play.setVisibility(View.VISIBLE);
                }
                onIsPlayingChangedCallback.onSuccess(isPlaying);
            }

            @SuppressLint("SwitchIntDef")
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        promptDialog.showLoading("缓冲中,,", false);
                        break;
                    case Player.STATE_READY:
                        activity.runOnUiThread(() -> {
                            if (holder.video.getPlayer().isPlaying()) {
                                promptDialog.dismissImmediately();
                            }
                        });
                        break;
                }
            }
        });

        holder.mask.setOnClickListener(v -> {
            if (holder.video.getPlayer().isPlaying()) {
                holder.video.getPlayer().pause();
            } else {
                // 播放完了就重放
                if (holder.video.getPlayer().getCurrentPosition() >= holder.video.getPlayer().getDuration()) {
                    holder.video.getPlayer().seekTo(0);
                }
                holder.video.getPlayer().play();
            }

        });
        holder.mask.setOnLongClickListener(v -> {
            onLongClickListener.onSuccess(position);
            return true;
        });

        styledPlayerViewList.add(position, holder.video);

    }

    @Override
    public int getItemCount() {
        return videoUrlList.size();
    }

    static class CustomVideoPlayerViewHolder extends RecyclerView.ViewHolder {
        Boolean isBind = false;
        StyledPlayerView video;
        RelativeLayout mask;
        ImageView play;

        public CustomVideoPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            video = itemView.findViewById(R.id.video);
            mask = itemView.findViewById(R.id.mask);
            play = itemView.findViewById(R.id.play);

            // 播放器配置
            video.setPlayer(new SimpleExoPlayer.Builder(itemView.getContext()).build());
        }
    }
}
