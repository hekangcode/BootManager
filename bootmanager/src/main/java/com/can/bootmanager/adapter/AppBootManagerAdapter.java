package com.can.bootmanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.can.bootmanager.R;
import com.can.bootmanager.bean.AppInfo;

import java.util.List;


/**
 * Created by HEKANG on 2016/11/14.
 */

public class AppBootManagerAdapter extends RecyclerView.Adapter<AppBootManagerAdapter.MyViewHolder> {

    private Context mContext;
    private List<AppInfo> mAppList;
    private LayoutInflater mLayoutInflater;
    private View.OnFocusChangeListener mFocusListener;
    private OnItemFocusChangeListener mOnItemFocusChangeListener;
    private OnItemClickListener mOnItemClickListener;

    public void setFocusListener(View.OnFocusChangeListener focusListener) {
        this.mFocusListener = focusListener;
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChange(View msgView, int position);
    }

    public void setOnItemFocusChangeListener(OnItemFocusChangeListener itemFocusChangeListener) {
        this.mOnItemFocusChangeListener = itemFocusChangeListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    public AppBootManagerAdapter(List<AppInfo> list, Context context) {
        this.mContext = context;
        this.mAppList = list;
    }

    @Override
    public AppBootManagerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.com_can_bootmanager_item_app, parent, false);
        view.setOnFocusChangeListener(mFocusListener);
        return new AppBootManagerAdapter.MyViewHolder(view, mOnItemFocusChangeListener, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(final AppBootManagerAdapter.MyViewHolder holder, final int position) {
        AppInfo app = mAppList.get(position);
        if (mAppList.size() == 1 && position == 0) {
            holder.llItemApp.setBackground(mContext.getResources().getDrawable(R.drawable
                    .com_can_bootmanager_rect_gray));
            holder.line.setVisibility(View.INVISIBLE);
        } else if (position == 0) {
            holder.llItemApp.setBackground(mContext.getResources().getDrawable(R.drawable
                    .com_can_bootmanager_rect_gray_top));
        } else if (position == (mAppList.size() - 1)) {
            holder.llItemApp.setBackground(mContext.getResources().getDrawable(R.drawable
                    .com_can_bootmanager_rect_gray_bottom));
            holder.line.setVisibility(View.INVISIBLE);
        } else {
            holder.llItemApp.setBackgroundColor(mContext.getResources().getColor(R.color.color_1AFFFFFF));
        }
        if (app.getIcon() != null) {
            holder.ivIcon.setImageDrawable(app.getIcon());
        } else {
            holder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        }
        holder.tvName.setText(TextUtils.isEmpty(app.getName()) ? app.getPackageName() : app.getName());
        if (app.isReceiverEnable()) {
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.color_CCFFFFFF));
            holder.ivBootMode.setImageResource(R.mipmap.open);
        } else {
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.color_80FFFFFF));
            holder.ivBootMode.setImageResource(R.mipmap.close);
        }
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener, View.OnClickListener {

        private final OnItemFocusChangeListener mItemFocusChangeListener;
        private final OnItemClickListener mOnItemClickListener;
        private ImageView ivIcon, ivBootMode;
        private TextView tvName;
        private RelativeLayout llItemApp;
        private View line;

        private MyViewHolder(View view, OnItemFocusChangeListener mOnItemFocusChangeListener, OnItemClickListener
                mOnItemClickListener) {
            super(view);
            this.mItemFocusChangeListener = mOnItemFocusChangeListener;
            this.mOnItemClickListener = mOnItemClickListener;
            ivIcon = (ImageView) view.findViewById(R.id.iv_app_icon);
            tvName = (TextView) view.findViewById(R.id.tv_app_name);
            ivBootMode = (ImageView) view.findViewById(R.id.iv_boot_mode);
            ivBootMode.setOnFocusChangeListener(this);
            ivBootMode.setOnClickListener(this);
            llItemApp = (RelativeLayout) view.findViewById(R.id.ll_item_app);
            //llItemApp.setOnFocusChangeListener(this);
            //llItemApp.setOnClickListener(this);
            line = view.findViewById(R.id.line);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mFocusListener != null) {
                mFocusListener.onFocusChange(v, hasFocus);
            }
            int i = v.getId();
            if (i == R.id.iv_boot_mode) {
                if (mItemFocusChangeListener != null) {
                    mItemFocusChangeListener.onItemFocusChange(v, getLayoutPosition());
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_boot_mode) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, getLayoutPosition());
                }
            }
        }
    }
}
