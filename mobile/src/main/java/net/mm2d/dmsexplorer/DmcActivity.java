/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MediaRenderer.ActionCallback;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.ChapterInfo;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.adapter.CdsPropertyAdapter;
import net.mm2d.dmsexplorer.adapter.PropertyAdapter;
import net.mm2d.dmsexplorer.view.ChapterMark;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class DmcActivity extends AppCompatActivity {

    /**
     * このActivityを起動するためのIntentを作成する。
     *
     * <p>Extraの設定と読み出しをこのクラス内で完結させる。
     *
     * @param context     コンテキスト
     * @param serverUdn   MediaServerのUDN
     * @param object      再生するCdsObject
     * @param uri         再生するURI
     * @param rendererUdn MediaRendererのUDN
     * @return このActivityを起動するためのIntent
     */
    public static Intent makeIntent(final @NonNull Context context,
                                    final @NonNull String serverUdn,
                                    final @NonNull CdsObject object,
                                    final @NonNull String uri,
                                    final @NonNull String rendererUdn) {
        final Intent intent = new Intent(context, DmcActivity.class);
        intent.putExtra(Const.EXTRA_SERVER_UDN, serverUdn);
        intent.putExtra(Const.EXTRA_OBJECT, object);
        intent.putExtra(Const.EXTRA_URI, uri);
        intent.putExtra(Const.EXTRA_RENDERER_UDN, rendererUdn);
        return intent;
    }

    private static final int CHAPTER_MARGIN = (int) TimeUnit.SECONDS.toMillis(5);
    private Handler mHandler;
    @NonNull
    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            mMediaRenderer.getPositionInfo((success, result) -> onGetPositionInfo(result));
            mMediaRenderer.getTransportInfo((success, result) -> onGetTransportInfo(result));
        }
    };
    @NonNull
    private final ActionCallback mShowToastOnError = (success, result) -> {
        if (!success) {
            mHandler.post(() -> Toast.makeText(this, R.string.toast_command_error_occurred, Toast.LENGTH_LONG).show());
        }
    };

    private static final char EN_SPACE = 0x2002; // &ensp;
    private MediaRenderer mMediaRenderer;
    private ImageView mPlay;
    private View mNext;
    private View mPrevious;
    private SeekBar mSeekBar;
    private ChapterMark mChapterMark;
    private TextView mProgressText;
    private TextView mDurationText;
    private int mProgress;
    private int mDuration;
    private boolean mTracking;
    private boolean mPlaying;
    private List<Integer> mChapterInfo;
    private PropertyAdapter mPropertyAdapter;

    private void onGetPositionInfo(Map<String, String> result) {
        if (result == null) {
            mHandler.postDelayed(mGetPositionTask, 1000);
            return;
        }
        final int duration = MediaRenderer.getDuration(result);
        final int progress = MediaRenderer.getProgress(result);
        if (duration < 0 || progress < 0) {
            mHandler.postDelayed(mGetPositionTask, 1000);
            return;
        }
        mDuration = duration;
        mProgress = progress;
        final long interval = 1000 - mProgress % 1000;
        mHandler.postDelayed(mGetPositionTask, interval);
        mHandler.post(this::updatePosition);
    }

    private void onGetTransportInfo(Map<String, String> result) {
        final boolean playing = "PLAYING".equals(MediaRenderer.getCurrentTransportState(result));
        if (mPlaying == playing) {
            return;
        }
        mPlaying = playing;
        mHandler.post(() -> mPlay.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play));
    }

    private void updatePosition() {
        mDurationText.setText(makeTimeText(mDuration));
        if (mTracking) {
            return;
        }
        mProgressText.setText(makeTimeText(mProgress));
        mSeekBar.setMax(mDuration);
        mSeekBar.setProgress(mProgress);
        mChapterMark.setDuration(mDuration);
    }

    private static String makeTimeText(int millisecond) {
        final long second = (millisecond / 1000) % 60;
        final long minute = (millisecond / 60000) % 60;
        final long hour = millisecond / 3600000;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dmc_activity);
        mHandler = new Handler();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        final String serverUdn = intent.getStringExtra(Const.EXTRA_SERVER_UDN);
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final String uri = intent.getStringExtra(Const.EXTRA_URI);
        final String rendererUdn = intent.getStringExtra(Const.EXTRA_RENDERER_UDN);
        final MediaServer server = DataHolder.getInstance().getMsControlPoint().getDevice(serverUdn);
        mMediaRenderer = DataHolder.getInstance().getMrControlPoint().getDevice(rendererUdn);
        if (mMediaRenderer == null || server == null) {
            finish();
            return;
        }

        final String title = AribUtils.toDisplayableString(object.getTitle());
        actionBar.setTitle(title);
        actionBar.setSubtitle(mMediaRenderer.getFriendlyName() + "  ←  " + server.getFriendlyName());

        final ImageView image = (ImageView) findViewById(R.id.image);
        image.setImageResource(getImageResource(object));

        mPropertyAdapter = new CdsPropertyAdapter(this, object);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail);
        recyclerView.setAdapter(mPropertyAdapter);

        mPlay = (ImageView) findViewById(R.id.play);
        mNext = findViewById(R.id.next);
        mPrevious = findViewById(R.id.previous);
        mChapterMark = (ChapterMark) findViewById(R.id.chapterMark);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mProgressText = (TextView) findViewById(R.id.textProgress);
        mDurationText = (TextView) findViewById(R.id.textDuration);
        mMediaRenderer.subscribe();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSeekBar.setProgressDrawable(getDrawable(R.drawable.seekbar_track));
        }

        if (object.getType() == CdsObject.TYPE_IMAGE) {
            findViewById(R.id.seekPanel).setVisibility(View.INVISIBLE);
            start(object, uri);
            return;
        }
        setUpPlay(mPlay);
        mNext.setOnClickListener(v -> goNext());
        mPrevious.setOnClickListener(v -> goPrevious());
        setUpSeekBar(mSeekBar);

        start(object, uri);
        mHandler.postDelayed(mGetPositionTask, 1000);
        getChapterInfo(object);
    }

    private int getImageResource(CdsObject object) {
        switch (object.getType()) {
            case CdsObject.TYPE_VIDEO:
                return R.drawable.ic_movie;
            case CdsObject.TYPE_AUDIO:
                return R.drawable.ic_music;
            case CdsObject.TYPE_IMAGE:
                return R.drawable.ic_image;
        }
        return 0;
    }

    private void setUpPlay(ImageView play) {
        if (!mMediaRenderer.isSupportPause()) {
            return;
        }
        play.setVisibility(View.VISIBLE);
        play.setOnClickListener(v -> {
            if (mPlaying) {
                mMediaRenderer.pause(mShowToastOnError);
            } else {
                mMediaRenderer.play(mShowToastOnError);
            }
        });
    }

    private void start(CdsObject object, String uri) {
        mMediaRenderer.setAVTransportURI(object, uri, (success, result) -> {
            if (success) {
                mMediaRenderer.play(mShowToastOnError);
            } else {
                mHandler.post(() -> {
                    Toast.makeText(this, R.string.toast_command_error_occurred, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private final Runnable mTrackingCancel = new Runnable() {
        @Override
        public void run() {
            mTracking = false;
        }
    };

    private void setUpSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mProgressText.setText(makeTimeText(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mTrackingCancel);
                mTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaRenderer.seek(seekBar.getProgress(), mShowToastOnError);
                mHandler.postDelayed(mTrackingCancel, 1000);
            }
        });
    }

    private void getChapterInfo(@NonNull CdsObject object) {
        ChapterInfo.get(object, result -> mHandler.post(() -> setChapterInfo(result)));
    }

    private void setChapterInfo(@Nullable List<Integer> chapterInfo) {
        if (chapterInfo == null) {
            return;
        }
        mChapterInfo = chapterInfo;
        mNext.setVisibility(View.VISIBLE);
        mPrevious.setVisibility(View.VISIBLE);
        mChapterMark.setChapterInfo(mChapterInfo);
        final int count = mPropertyAdapter.getItemCount();
        mPropertyAdapter.addEntry(getString(R.string.prop_chapter_info),
                makeChapterString(chapterInfo));
        mPropertyAdapter.notifyItemInserted(count);
    }

    private String makeChapterString(List<Integer> chapterInfo) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chapterInfo.size(); i++) {
            if (sb.length() != 0) {
                sb.append("\n");
            }
            if (i < 9) {
                sb.append(EN_SPACE);
            }
            sb.append(String.valueOf(i + 1));
            sb.append(" : ");
            final int chapter = chapterInfo.get(i);
            sb.append(makeTimeText(chapter));
        }
        return sb.toString();
    }

    private void goNext() {
        final int chapter = getCurrentChapter() + 1;
        if (chapter < mChapterInfo.size()) {
            mMediaRenderer.seek(mChapterInfo.get(chapter), mShowToastOnError);
        }
    }

    private void goPrevious() {
        int chapter = getCurrentChapter();
        if (chapter > 0 && mProgress - mChapterInfo.get(chapter) < CHAPTER_MARGIN) {
            chapter--;
        }
        if (chapter >= 0) {
            mMediaRenderer.seek(mChapterInfo.get(chapter), mShowToastOnError);
        }
    }

    private int getCurrentChapter() {
        final int progress = mProgress;
        for (int i = 0; i < mChapterInfo.size(); i++) {
            if (progress < mChapterInfo.get(i)) {
                return i - 1;
            }
        }
        return mChapterInfo.size() - 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mGetPositionTask);
        if (mMediaRenderer != null) {
            mMediaRenderer.stop(null);
            mMediaRenderer.clearAVTransportURI(null);
            mMediaRenderer.unsubscribe();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
