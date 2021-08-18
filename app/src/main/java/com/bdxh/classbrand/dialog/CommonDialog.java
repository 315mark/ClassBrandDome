package com.bdxh.classbrand.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bdxh.classbrand.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 自定义Dialog类---CommonDialog
 */
public class CommonDialog extends Dialog {

    @BindView(R.id.dialog_tv_title)
    TextView mTvTitle;

    @BindView(R.id.dialog_tv_msg)
    TextView mTvMessage;

    @BindView(R.id.dialog_fl_content)
    FrameLayout mFlContent;

    @BindView(R.id.dialog_btn_positive)
    Button mBtnPositive;

    @BindView(R.id.cancel_btn)
    Button mBtnNegative;

    @BindView(R.id.dialog_ll_root_view)
    LinearLayout mContentView;

    private CommonDialog(Context context)
    {
        super(context);
    }

    private CommonDialog(Context context, int themeResId)
    {
        super(context, themeResId);
    }

    private CommonDialog(Context context, boolean cancelable,
                         OnCancelListener cancelListener)
    {
        super(context, cancelable, cancelListener);
    }

    public void setTitle(int titleId)
    {
        mTvTitle.setText(titleId);
    }

    public void setTitle(String title)
    {
        mTvTitle.setText(title);
    }

    public void setMessage(int msgId)
    {
        mTvMessage.setText(msgId);
    }

    public void setMessage(String message)
    {
        mTvMessage.setText(message);
    }

    public void setPositiveButton(int textId, OnClickListener listener)
    {
        setPositiveButton(getContext().getString(textId), listener);
    }

    public void setPositiveButton(String text, final OnClickListener listener)
    {
        mBtnPositive.setText(text);
        mBtnPositive.setOnClickListener(v -> {
            if (listener != null)
            {
                listener.onClick(CommonDialog.this, 1);
            }
            else
            {
                dismiss();
            }
        });
    }

    public void setNegativeButton(int textId, OnClickListener listener) {
        setNegativeButton(getContext().getString(textId), listener);
    }

    public void setNegativeButton(String text, final OnClickListener listener) {
        mBtnNegative.setText(text);
        mBtnNegative.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(CommonDialog.this, 2);
            }
            else {
                dismiss();
            }
        });
    }

    public void setPositiveCancel() {
        mBtnPositive.setVisibility(View.GONE);
    }

    public void setPositiveshow() {
        mBtnPositive.setVisibility(View.VISIBLE);
    }

    public void setNegativeCancel() {
        mBtnNegative.setVisibility(View.GONE);
    }

    public void setNegativeshow() {
        mBtnNegative.setVisibility(View.VISIBLE);
    }

    public void setView(int layoutId){
        mFlContent.removeAllViews();
        View.inflate(getContext(), layoutId, mFlContent);
    }

    public void setView(View view){
        mFlContent.removeAllViews();
        mFlContent.addView(view);
    }

    public TextView getTvMessage()
    {
        return mTvMessage;
    }

    //--------------------------------

    public LinearLayout getContentView() {
        return mContentView;
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private OnClickListener positiveButtonListener;
        private OnClickListener negativeButtonListener;
        private View view;
        private boolean cancelable = true;

        /**
         * 建造器的构造方法：
         * @param context
         */
        public Builder(Context context){
            this.context = context;
        }


        /**
         * 利用资源id设置title
         * @param titleId
         * @return Builder
         */
        public Builder setTitle(int titleId){
            this.title = (String) context.getText(titleId);
            return this;
        }

        /**
         * 利用字符串设置title
         * @param title
         * @return Builder
         */
        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public Builder setMessage(int msgId){
            this.message = (String) context.getText(msgId);
            return this;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(int textId, OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(textId);
            this.positiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(String text, OnClickListener listener)
        {
            this.positiveButtonText = text;
            this.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, OnClickListener listener)
        {
            this.negativeButtonText = (String) context.getText(textId);
            this.negativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(String text, OnClickListener listener)
        {
            this.negativeButtonText = text;
            this.negativeButtonListener = listener;
            return this;
        }

        public Builder setView(int layoutId)
        {
            this.view = View.inflate(context, layoutId, null);
            return this;
        }

        public Builder setView(View view)
        {
            this.view = view;
            return this;
        }

        public Builder setCancelable(boolean cancelable)
        {
            this.cancelable = cancelable;
            return this;
        }

        public CommonDialog create(){
            final CommonDialog dialog = new CommonDialog(context, R.style.BaseDialog);
            dialog.setContentView(R.layout.dialog_layout);
            ButterKnife.bind(dialog);

            // 设置标题
            if (!TextUtils.isEmpty(title)) {
                dialog.setTitle(title);
            }
            // 设置提示信息
            if (!TextUtils.isEmpty(message)) {
                dialog.setMessage(message);
            }
            // 设置内容布局
            if (view != null) {
                dialog.setView(view);
            }

            if (!TextUtils.isEmpty(positiveButtonText)) {
                dialog.mBtnPositive.setVisibility(View.VISIBLE);
                dialog.setPositiveButton(positiveButtonText, positiveButtonListener);
            }else {
                dialog.setPositiveCancel();
            }

            if (!TextUtils.isEmpty(negativeButtonText)) {
                dialog.mBtnNegative.setVisibility(View.VISIBLE);
                dialog.setNegativeButton(negativeButtonText, negativeButtonListener);
            }else {
                dialog.setNegativeCancel();
            }

            dialog.setCancelable(cancelable);
            return dialog;
        }

        public CommonDialog show() {
            final CommonDialog dialog = create();
            dialog.show();
            return dialog;
        }

    }
}
