/*
 * Copyright (c) 2010 - 2015 Ushahidi Inc
 * All rights reserved
 * Contact: team@ushahidi.com
 * Website: http://www.ushahidi.com
 * GNU Lesser General Public License Usage
 * This file may be used under the terms of the GNU Lesser
 * General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.LGPL included in the
 * packaging of this file. Please review the following information to
 * ensure the GNU Lesser General Public License version 3 requirements
 * will be met: http://www.gnu.org/licenses/lgpl.html.
 *
 * If you have questions regarding the use of this file, please contact
 * Ushahidi developers at team@ushahidi.com.
 */

package org.addhen.smssync.presentation.view.ui.adapter;

import com.addhen.android.raiburari.presentation.ui.adapter.BaseRecyclerViewAdapter;

import org.addhen.smssync.R;
import org.addhen.smssync.presentation.model.MessageModel;
import org.addhen.smssync.presentation.util.Utility;
import org.addhen.smssync.presentation.view.ui.widget.TextDrawable;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class MessageAdapter extends BaseRecyclerViewAdapter<MessageModel> {

    private View mEmptyView;

    private SparseBooleanArray mSelectedItems;

    private OnCheckedListener mOnCheckedListener;

    private OnMoreActionListener mOnMoreActionListener;

    private TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
            .round();

    private Animation flipIn;

    private Animation flipOut;

    public MessageAdapter(Context context, final View emptyView) {
        mEmptyView = emptyView;
        onDataSetChanged();
        mSelectedItems = new SparseBooleanArray();
        flipIn = AnimationUtils.loadAnimation(context, R.anim.flip_front);
        flipOut = AnimationUtils.loadAnimation(context, R.anim.flip_back);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return new Widgets(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.list_message_item, viewGroup, false));
    }

    @Override
    public int getAdapterItemCount() {
        return getItems().size();
    }

    @Override
    public void setItems(List<MessageModel> items) {
        super.setItems(items);
        onDataSetChanged();
    }

    /**
     * Sets an empty view when the adapter's data item gets to zero
     */
    private void onDataSetChanged() {
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Toggles an item in the adapter as selected or de-selected
     *
     * @param position The index of the item to be toggled
     */
    public void toggleSelection(int position) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
    }

    private boolean isChecked(int position) {
        if (mSelectedItems.get(position, false)) {
            return true;
        }
        return false;
    }

    /**
     * Count of the selected item
     *
     * @return The selected item size
     */
    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    /**
     * Clear all selections
     */
    public void clearSelections() {
        mSelectedItems.clear();
        onDataSetChanged();
    }

    /**
     * Gets all selected items
     *
     * @return The list of selected items
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public void setOnCheckedListener(OnCheckedListener onCheckedListener) {
        mOnCheckedListener = onCheckedListener;
    }

    public void setOnMoreActionListener(OnMoreActionListener onMoreActionListener) {
        mOnMoreActionListener = onMoreActionListener;
    }

    private void updateCheckedState(Widgets holder, int position) {
        if (isChecked(position)) {
            holder.imageView.setImageDrawable(
                    mDrawableBuilder.build(holder.itemView.getContext().getResources()
                            .getDrawable(R.drawable.ic_done_white_18dp), 0xff616161));
        } else {
            TextDrawable drawable = mDrawableBuilder
                    .build(holder.itemView.getContext().getResources()
                                    .getDrawable(R.drawable.ic_call_white_18dp),
                            holder.itemView.getContext().getResources().getColor(
                                    R.color.orange_light));
            holder.imageView.setImageDrawable(drawable);
        }
    }

    private void setFlipAnimation(Widgets widgets, int position) {
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (animation == flipOut) {
                    updateCheckedState(widgets, position);
                }
                widgets.imageView.clearAnimation();
                widgets.imageView.setAnimation(flipIn);
                widgets.imageView.startAnimation(flipIn);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isChecked(position)) {
                    widgets.checkIcon.setVisibility(View.VISIBLE);
                } else {
                    widgets.checkIcon.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        flipIn.setAnimationListener(animationListener);
        flipOut.setAnimationListener(animationListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MessageModel messageModel = getItem(position);
        // Initialize view with content
        Widgets widgets = ((Widgets) holder);
        widgets.messageFrom.setText(messageModel.messageFrom);
        if (messageModel.messageDate != null) {
            widgets.messageDate.setText(Utility.formatDate(messageModel.messageDate));
        }
        widgets.message.setText(messageModel.messageBody);
        // Pending messages
        if (messageModel.messageType == MessageModel.Type.PENDING) {
            widgets.messageType.setText(widgets.itemView.getContext().getString(
                    R.string.sms).toUpperCase(Locale.getDefault()));
        } else if (messageModel.messageType == MessageModel.Type.TASK) {
            // Task messages
            widgets.messageType
                    .setText(widgets.itemView.getContext().getString(R.string.task).toUpperCase(
                            Locale.getDefault()));
        }
        widgets.messageType
                .setTextColor(widgets.itemView.getContext().getResources().getColor(R.color.red));

        widgets.imageView.setOnClickListener(v -> {

            if (mOnCheckedListener != null) {
                mOnCheckedListener.onChecked(position);
            }
            widgets.imageView.clearAnimation();
            widgets.imageView.setAnimation(flipOut);
            widgets.imageView.startAnimation(flipOut);
            setFlipAnimation(widgets, position);
        });

        updateCheckedState(widgets, position);
        widgets.statusIndicator.setOnClickListener(v -> {
            if (mOnMoreActionListener != null) {
                mOnMoreActionListener.onMoreActionTap(position);
            }
        });
    }

    public class Widgets extends RecyclerView.ViewHolder {

        @Bind(R.id.status_indicator)
        ImageView statusIndicator;

        @Bind(R.id.message_from)
        AppCompatTextView messageFrom;

        @Bind(R.id.message_date)
        AppCompatTextView messageDate;

        @Bind(R.id.message)
        AppCompatTextView message;

        @Bind(R.id.sent_message_type)
        AppCompatTextView messageType;

        @Bind(R.id.message_icons)
        ImageView imageView;

        @Bind(R.id.check_icon)
        ImageView checkIcon;

        public Widgets(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnCheckedListener {

        void onChecked(int position);
    }

    public interface OnMoreActionListener {

        void onMoreActionTap(int position);
    }
}