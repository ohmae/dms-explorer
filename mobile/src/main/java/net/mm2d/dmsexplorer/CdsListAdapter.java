/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.mm2d.cds.CdsObject;
import net.mm2d.util.Arib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class CdsListAdapter
        extends RecyclerView.Adapter<CdsListAdapter.ViewHolder> {
    private static final String TAG = "CdsListAdapter";

    public interface OnItemClickListener {
        void onItemClick(View v, View accent, int position, CdsObject object);
    }

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<CdsObject> mList;
    private OnItemClickListener mListener;
    private int mSelection = -1;

    public CdsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.li_cds, parent, false);
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

    public void addAll(Collection<? extends CdsObject> objects) {
        mList.addAll(objects);
    }

    public int add(CdsObject obj) {
        mList.add(obj);
        return mList.size() - 1;
    }

    public int remove(CdsObject obj) {
        final int position = mList.indexOf(obj);
        if (position >= 0) {
            mList.remove(position);
        }
        return position;
    }

    public void setSelection(int position) {
        mSelection = position;
        notifyDataSetChanged();
    }

    public int getSelection() {
        return mSelection;
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            final CdsObject obj = holder.getItem();
            final int position = holder.getListPosition();
            final View accent = holder.getAccent();
            if (mListener != null) {
                mListener.onItemClick(v, accent, position, obj);
            }
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final View mMark;
        private final TextView mAccent;
        private final TextView mText1;
        private final TextView mText2;
        private final ImageView mImage;
        private int mPosition;
        private CdsObject mObject;
        private final GradientDrawable mAccentBackground;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mMark = mView.findViewById(R.id.mark);
            mAccent = (TextView) mView.findViewById(R.id.textAccent);
            mText1 = (TextView) mView.findViewById(R.id.text1);
            mText2 = (TextView) mView.findViewById(R.id.text2);
            mImage = (ImageView) mView.findViewById(R.id.image);
            mView.setOnClickListener(mItemClickListener);
            mView.setTag(this);
            final int radius = mView.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.accent_radius);
            mAccentBackground = new GradientDrawable();
            mAccentBackground.setCornerRadius(radius);
            mAccent.setBackground(mAccentBackground);
        }

        public void applyItem(int position, CdsObject obj) {
            mPosition = position;
            if (position == mSelection) {
                mMark.setVisibility(View.VISIBLE);
                mView.setBackgroundResource(R.drawable.bg_list_item_selected);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final int z = mView.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.raise_focus);
                    mView.setTranslationZ(z);
                }
            } else {
                mMark.setVisibility(View.INVISIBLE);
                mView.setBackgroundResource(R.drawable.bg_list_item);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mView.setTranslationZ(0);
                }
            }
            mObject = obj;
            final String name = obj.getTitle();
            if (!name.isEmpty()) {
                final String c = name.substring(0, 1);
                mAccent.setText(Arib.toDisplayableString(c));
            } else {
                mAccent.setText("");
            }
            mAccentBackground.setColor(Utils.getAccentColor(name));
            mText1.setText(Arib.toDisplayableString(name));
            mText2.setText(obj.getUpnpClass());
            switch (obj.getType()) {
                case CdsObject.TYPE_CONTAINER:
                    mImage.setImageResource(R.drawable.ic_folder);
                    break;
                case CdsObject.TYPE_AUDIO:
                    mImage.setImageResource(R.drawable.ic_audio);
                    break;
                case CdsObject.TYPE_IMAGE:
                    mImage.setImageResource(R.drawable.ic_image);
                    break;
                case CdsObject.TYPE_VIDEO:
                    mImage.setImageResource(R.drawable.ic_movie);
                    break;
                case CdsObject.TYPE_UNKNOWN:
                default:
                    mImage.setImageResource(0);
                    break;
            }
        }

        public View getAccent() {
            return mAccent;
        }

        public CdsObject getItem() {
            return mObject;
        }

        public int getListPosition() {
            return mPosition;
        }
    }
}
