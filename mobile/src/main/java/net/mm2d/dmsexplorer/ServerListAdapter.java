/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.upnp.Icon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * MediaServerをRecyclerViewへ表示するためのAdapter。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListAdapter
        extends RecyclerView.Adapter<ServerListAdapter.ViewHolder> {
    private static final String TAG = "ServerListAdapter";
    private final int mSelectedZ;
    private final int mAccentRadius;

    public interface OnItemClickListener {
        void onItemClick(@NonNull View v, @NonNull View accent, int position, @NonNull MediaServer server);
    }

    private final LayoutInflater mInflater;
    private final List<MediaServer> mList;
    private OnItemClickListener mListener;
    private int mSelection = -1;

    public ServerListAdapter(Context context, Collection<? extends MediaServer> servers) {
        mInflater = LayoutInflater.from(context);
        if (servers == null) {
            mList = new ArrayList<>();
        } else {
            mList = new ArrayList<>(servers);
        }
        final Resources res = context.getResources();
        mSelectedZ = res.getDimensionPixelSize(R.dimen.raise_focus);
        mAccentRadius = res.getDimensionPixelSize(R.dimen.accent_radius);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.li_server, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.applyItem(position, mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public void clear() {
        mList.clear();
    }

    public void addAll(Collection<? extends MediaServer> servers) {
        mList.addAll(servers);
    }

    public int add(MediaServer server) {
        mList.add(server);
        return mList.size() - 1;
    }

    public int remove(MediaServer server) {
        final int position = mList.indexOf(server);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public void setSelection(int position) {
        mSelection = position;
        notifyDataSetChanged();
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            final MediaServer server = holder.getItem();
            final int position = holder.getListPosition();
            final View accent = holder.getAccent();
            if (mListener != null) {
                mListener.onItemClick(v, accent, position, server);
            }
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final View mMark;
        private final View mAccent;
        private final ImageView mImageAccent;
        private final TextView mTextAccent;
        private final TextView mText1;
        private final TextView mText2;
        private int mPosition;
        private MediaServer mServer;
        private final GradientDrawable mAccentBackground;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mMark = mView.findViewById(R.id.mark);
            mAccent = mView.findViewById(R.id.accent);
            mImageAccent = (ImageView) mView.findViewById(R.id.imageAccent);
            mTextAccent = (TextView) mView.findViewById(R.id.textAccent);
            mText1 = (TextView) mView.findViewById(R.id.text1);
            mText2 = (TextView) mView.findViewById(R.id.text2);
            mView.setOnClickListener(mItemClickListener);
            mView.setTag(this);
            mAccentBackground = new GradientDrawable();
            mAccentBackground.setCornerRadius(mAccentRadius);
            mTextAccent.setBackground(mAccentBackground);
        }

        void applyItem(final int position, final @NonNull MediaServer server) {
            mPosition = position;
            mServer = server;
            setSelectionMark(position);
            setIconAndTitle(server.getIcon(), server.getFriendlyName());
            final StringBuilder sb = new StringBuilder();
            sb.append("IP: ");
            sb.append(server.getIpAddress());
            final String serial = server.getSerialNumber();
            if (serial != null && !serial.isEmpty()) {
                sb.append("  ");
                sb.append("Serial: ");
                sb.append(serial);
            }
            mText2.setText(sb.toString());
        }

        private void setSelectionMark(final int position) {
            if (position == mSelection) {
                mMark.setVisibility(View.VISIBLE);
                mView.setBackgroundResource(R.drawable.bg_list_item_selected);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mView.setTranslationZ(mSelectedZ);
                }
            } else {
                mMark.setVisibility(View.INVISIBLE);
                mView.setBackgroundResource(R.drawable.bg_list_item_normal);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mView.setTranslationZ(0);
                }
            }
        }

        private void setIconAndTitle(final @Nullable Icon icon, final @NonNull String title) {
            if (icon != null) {
                mTextAccent.setText(null);
                mTextAccent.setVisibility(View.GONE);
                final byte[] binary = icon.getBinary();
                mImageAccent.setImageBitmap(BitmapFactory.decodeByteArray(binary, 0, binary.length));
                mImageAccent.setVisibility(View.VISIBLE);
            } else {
                mImageAccent.setImageBitmap(null);
                mImageAccent.setVisibility(View.GONE);
                mTextAccent.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(title)) {
                    final String c = title.substring(0, 1);
                    mTextAccent.setText(c);
                } else {
                    mTextAccent.setText(null);
                }
                mAccentBackground.setColor(ThemeUtils.getAccentColor(title));
            }
            mText1.setText(title);
        }

        View getAccent() {
            return mAccent;
        }

        MediaServer getItem() {
            return mServer;
        }

        int getListPosition() {
            return mPosition;
        }
    }
}
