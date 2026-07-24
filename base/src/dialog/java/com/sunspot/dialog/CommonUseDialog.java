package com.sunspot.dialog;

import static android.graphics.Typeface.DEFAULT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.sunspot.base.R;
import com.sunspot.libext.CommonUtils;
import com.sunspot.libext.DeviceUtil;
import com.sunspot.libext.PadPixelUtil;
import com.sunspot.libext.PixelUtilKt;
import com.sunspot.libext.ResourceUtil;


/**
 * 通用屏幕中间展示弹窗
 */
public class CommonUseDialog extends Dialog implements LifecycleEventObserver {
    protected View rootView;
    /**
     * 弹窗背景
     */
    private View rlBg;
    /**
     * 标题
     */
    private TextView tvTitle;
    /**
     * 中间展示文案
     */
    private TextView tvMsg;
    /**
     * 确认样式按钮
     */
    private TextView tvConfirm;
    /**
     * 取消样式按钮
     */
    private TextView tvCancel;
    /**
     * 取消样式按钮
     */
    private LinearLayout llButtonGroup;
    /**
     * 中间展示文案的ViewGroup（用于替换自定义布局）
     */
    private LinearLayout llCenterGroup;
    /**
     * 关闭按钮
     */
    private ImageView ivDialogClose;

    private OnConfirmClickListener mConfirmClickListener;
    private OnCancelClickListener mCancelClickListener;
    private OnCloseBtnClickListener mCloseBtnClickListener;

    /**
     * 默认点击确认后 diaolog会消失
     */
    private boolean mClickConfirmDismiss = true;

    /**
     * 手机上自定义dialog宽度，pad上不生效使用统一值
     */
    private int mCustomDialogWidth;

    /**
     * 弹窗背景资源
     */
    public static int BG_RES = R.drawable.cu_bg;

    /**
     * 确认按钮背景资源
     */
    public static int CONFIRM_BG_RES = R.drawable.cu_confirm_btn_selector;

    /**
     * 取消按钮背景资源
     */
    public static int CANCEL_BG_RES = R.drawable.cu_cancel_btn_selector;

    /**
     * 取消按钮字体颜色资源
     */
    public static int CANCEL_TEXT_COLOR_RES = R.color.cu_text_color;

    /**
     * 确认按钮字体颜色
     */
    public static int CONFIRM_TEXT_COLOR_RES = R.color.cu_confirm_text_color;

    public TextView getConfirmBtn() {
        return tvConfirm;
    }

    public interface OnConfirmClickListener {
        void onConfirmClick(View v);
    }

    public interface OnCancelClickListener {
        void onCancelClickListener(View v);
    }

    public interface OnCloseBtnClickListener {
        void onCloseBtnClickListener(View v);
    }

    public CommonUseDialog(Context context) {
        this(context, LinearLayout.HORIZONTAL, R.style.CommonUseDialog);
    }

    public CommonUseDialog(Context context, int btnOrientation) {
        this(context, btnOrientation, R.style.CommonUseDialog);
    }

    public CommonUseDialog(Context context, int btnOrientation, int themeResId) {
        super(context, themeResId);
        initLifecycle(context);
        if (btnOrientation == LinearLayout.VERTICAL) {
            rootView = LayoutInflater.from(context).inflate(getVerticalLayoutRes(), null);
        } else {
            rootView = LayoutInflater.from(context).inflate(getHorizontalLayoutRes(), null);
        }
        this.setCancelable(false);
        initView(rootView);
        addListener();
    }

    /**
     * 初始化 Context 生命周期监听
     */
    private void initLifecycle(Context context) {
        Lifecycle lifecycle = getContextLifecycle(context);
        if (lifecycle == null) {
            return;
        }
        lifecycle.addObserver(this);
    }

    private Lifecycle getContextLifecycle(Context context) {
        return context instanceof LifecycleOwner ? ((LifecycleOwner) context).getLifecycle() : null;
    }

    /**
     * 页面生命周期回调
     * @param lifecycleOwner lifecycleOwner
     * @param event 生命周期状态
     */
    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner,
                               @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (isShowing()) {
                dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Lifecycle lifecycle = getContextLifecycle(getContext());
        if (lifecycle != null) {
            lifecycle.removeObserver(this);
        }
    }

    /**
     * 安全显示dialog
     * 内部封装判断页面销毁或存在
     */
    public void showSafely() {
        if (getContext() instanceof Activity) {
            Activity act = (Activity) getContext();
            if (act.isFinishing() || act.isDestroyed()) {
                return;
            }
        }

        show();
    }

    protected int getVerticalLayoutRes() {
        return R.layout.cu_common_dialog_vertical;
    }

    protected int getHorizontalLayoutRes() {
        return R.layout.cu_common_dialog_horizontal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(rootView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int width;
        if (DeviceUtil.isPad(getContext())) {
            //pad上
            width = PadPixelUtil.getPadDialogWidth(getContext());
        } else {
            //手机上
            width = mCustomDialogWidth;
        }
        if (width <= 0) {
            //没有单独设置dialog宽高，使用布局里的默认大小（撑满）
            return;
        }
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = width;
            window.setAttributes(params);
        }
    }

    /**
     * 设置点击监听，默认为关闭弹窗
     */
    protected void addListener() {
        if (tvConfirm != null) {
            tvConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConfirmClickListener != null) {
                        mConfirmClickListener.onConfirmClick(v);
                    }
                    if (mClickConfirmDismiss) {
                        dismiss();
                    }
                }
            });
        }
        if (tvCancel != null) {
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCancelClickListener != null) {
                        mCancelClickListener.onCancelClickListener(v);
                    }
                    dismiss();
                }
            });
        }

        if (ivDialogClose != null) {
            ivDialogClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCloseBtnClickListener != null) {
                        mCloseBtnClickListener.onCloseBtnClickListener(v);
                    }
                    dismiss();
                }
            });
        }
    }

    private void initView(View rootView) {
        rlBg = rootView.findViewById(R.id.rcrl_root_layout);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_dialog_title);
        tvMsg = (TextView) rootView.findViewById(R.id.tv_dialog_message);
        tvConfirm = (TextView) rootView.findViewById(R.id.tv_dialog_confirm);
        tvCancel = (TextView) rootView.findViewById(R.id.tv_dialog_cancel);
        llCenterGroup = (LinearLayout) rootView.findViewById(R.id.ll_dialog_center_group);
        ivDialogClose = (ImageView) rootView.findViewById(R.id.iv_dialog_close);
        llButtonGroup = (LinearLayout) rootView.findViewById(R.id.ll_dialog_button);

        // 设置弹窗背景图
        if (rlBg != null) {
            rlBg.setBackgroundResource(BG_RES);
        }

        if (tvConfirm != null) {
            tvConfirm.setBackground(ResourceUtil.getDrawable(CONFIRM_BG_RES));
            tvConfirm.setTextColor(ResourceUtil.getColor(CONFIRM_TEXT_COLOR_RES));
        }
        if (tvCancel != null) {
            tvCancel.setBackground(ResourceUtil.getDrawable(CANCEL_BG_RES));
            tvCancel.setTextColor(ResourceUtil.getColor(CANCEL_TEXT_COLOR_RES));
        }
    }

    /**
     * 设置确认按钮点击监听
     */
    public CommonUseDialog setConfirmClickListener(OnConfirmClickListener confirmClickListener) {
        this.mConfirmClickListener = confirmClickListener;
        return this;
    }

    /**
     * 设置取消按钮点击监听
     */
    public CommonUseDialog setCancelClickListener(OnCancelClickListener mCancelClickListener) {
        this.mCancelClickListener = mCancelClickListener;
        return this;
    }

    /**
     * 设置右上角关闭按钮点击监听
     */
    public CommonUseDialog setCloseBtnClickListener(OnCloseBtnClickListener mCloseBtnClickListener) {
        this.mCloseBtnClickListener = mCloseBtnClickListener;
        return this;
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public CommonUseDialog setTitle(String title) {
        if (tvTitle == null) {
            return this;
        }
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
        return this;
    }

    /**
     * 设置字体样式
     *
     * @param type
     * @return
     */
    public CommonUseDialog setTitleStyle(int type) {
        if (tvTitle == null) {
            return this;
        }
        tvTitle.setTypeface(DEFAULT, type);
        return this;
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
    }

    public CommonUseDialog setTitleTextBold(boolean setBold) {
        if (tvTitle == null) {
            return this;
        }
        if (setBold) {
            TextPaint paint = tvTitle.getPaint();
            paint.setFakeBoldText(true);
        }
        return this;
    }

    public CommonUseDialog setTitleIntRes(int title) {
        if (tvTitle == null) {
            return this;
        }
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
        return this;
    }

    /**
     * 设置中间展示文案
     *
     * @param message
     */
    public CommonUseDialog setMessge(String message) {
        if (tvMsg == null) {
            return this;
        }
        tvMsg.setText(message);
        return this;
    }

    public CommonUseDialog setMessge(int message) {
        if (tvMsg == null) {
            return this;
        }
        tvMsg.setText(message);
        return this;
    }

    public CommonUseDialog setMessageColor(int color) {
        if (tvMsg == null) {
            return this;
        }
        tvMsg.setTextColor(color);
        return this;
    }

    public TextView getMsgTv() {
        return tvMsg;
    }

    /**
     * 设置中间文案位置
     *
     * @param gravity
     */
    public CommonUseDialog setMessageGravity(int gravity) {
        if (tvMsg == null) {
            return this;
        }
        tvMsg.setGravity(gravity);
        return this;
    }

    /**
     * 设置中间文案字数限制
     *
     * @param size
     */
    public CommonUseDialog setMessageSize(Float size) {
        if (tvMsg == null) {
            return this;
        }
        tvMsg.setTextSize(size);
        return this;
    }

    /**
     * 设置右边按钮文案
     *
     * @param btnName
     */
    public CommonUseDialog setConfirmBtnText(String btnName) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setText(btnName);
        return this;
    }

    public CommonUseDialog setConfirmBtnText(int btnName) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setText(btnName);
        return this;
    }

    /**
     * 设置点击确认按钮后 dialo需不需要消失
     *
     * @param dismiss
     * @return
     */
    public CommonUseDialog setClickConfirmDismiss(boolean dismiss) {
        mClickConfirmDismiss = dismiss;
        return this;
    }

    /**
     * 设置右边按钮颜色
     *
     * @param color
     */
    public CommonUseDialog setConfirmBtnColor(int color) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setTextColor(color);
        return this;
    }

    /**
     * 设置右边按钮背景颜色
     * int
     *
     * @param drawable
     */
    public CommonUseDialog setConfirmBtnBgDrawable(int drawable) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setBackgroundResource(drawable);
        return this;
    }

    /**
     * 设置左边按钮文案
     *
     * @param btnName
     */
    public CommonUseDialog setCancelBtnText(String btnName) {
        if (tvCancel == null) {
            return this;
        }
        tvCancel.setText(btnName);
        return this;
    }

    public CommonUseDialog setCancelBtnText(int btnName) {
        if (tvCancel == null) {
            return this;
        }
        tvCancel.setText(btnName);
        return this;
    }

    /**
     * 设置左边按钮颜色
     *
     * @param color
     */
    public CommonUseDialog setCancelBtnColor(int color) {
        if (tvCancel == null) {
            return this;
        }
        tvCancel.setTextColor(color);
        return this;
    }

    /**
     * 隐藏标题
     */
    public CommonUseDialog hideTitleView() {
        if (tvTitle == null) {
            return this;
        }
        tvTitle.setVisibility(View.GONE);
        return this;
    }

    /**
     * 隐藏中间文案
     */
    public CommonUseDialog hideMessageView() {
        if (llCenterGroup == null) {
            return this;
        }
        llCenterGroup.setVisibility(View.GONE);
        return this;
    }

    /**
     * 隐藏底部按钮
     */
    public CommonUseDialog hideButtonView() {
        if (llButtonGroup == null) {
            return this;
        }
        llButtonGroup.setVisibility(View.GONE);
        return this;
    }

    /**
     * 只展示确定样式按钮
     */
    public CommonUseDialog justShowConfirmBtn() {
        if (tvConfirm != null) {
            tvConfirm.setVisibility(View.VISIBLE);
        }
        if (tvCancel != null) {
            tvCancel.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * 只展示取消样式按钮
     */
    public CommonUseDialog justShowCancelBtn() {
        if (tvCancel != null) {
            tvCancel.setVisibility(View.VISIBLE);
        }
        if (tvConfirm != null) {
            tvConfirm.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * 设置中间为自定义View
     *
     * @param view
     */
    public CommonUseDialog setCustomCenterView(View view) {
        if (llCenterGroup == null) {
            return this;
        }
        llCenterGroup.removeAllViews();
        if (tvMsg != null) {
            tvMsg.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        llCenterGroup.addView(view, layoutParams);
        return this;
    }

    public CommonUseDialog setCustomCenterView(View view, int width, int height) {
        if (llCenterGroup == null) {
            return this;
        }
        llCenterGroup.removeAllViews();
        if (tvMsg != null) {
            tvMsg.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width,
                height);
        layoutParams.gravity = Gravity.CENTER;
        llCenterGroup.addView(view, layoutParams);
        return this;
    }

    /**
     * 设置关闭按钮展示/隐藏
     *
     * @param visibility
     */
    public CommonUseDialog setCloseBtnVisibility(int visibility) {
        if (ivDialogClose == null) {
            return this;
        }
        ivDialogClose.setVisibility(visibility);
        return this;
    }

    /**
     * 设置第一个按钮的背景
     *
     * @param resId 资源id
     */
    public CommonUseDialog setCancelBtnBg(int resId) {
        if (tvCancel == null) {
            return this;
        }
        tvCancel.setBackgroundResource(resId);
        return this;
    }

    /**
     * 设置第二个按钮的背景
     *
     * @param resId 资源id
     */
    public CommonUseDialog setConfirmBtnBg(int resId) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setBackgroundResource(resId);
        return this;
    }

    /**
     * 设置第二个按钮margin
     *
     * @param leftMargin   左
     * @param topMargin    上
     * @param rightMargin  右
     * @param bottomMargin 下
     */
    public CommonUseDialog setConfirmBtnMargin(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        if (tvConfirm == null) {
            return this;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvConfirm.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = rightMargin;
        layoutParams.bottomMargin = bottomMargin;
        tvConfirm.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * 设置弹窗内容边距
     *
     * @param leftMargin   左
     * @param topMargin    上
     * @param rightMargin  右
     * @param bottomMargin 下
     */
    public CommonUseDialog setMsgContentMargin(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        if (llCenterGroup == null) {
            return this;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) llCenterGroup.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = rightMargin;
        layoutParams.bottomMargin = bottomMargin;
        llCenterGroup.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * 设置按钮内容边距
     *
     * @param leftMargin   左
     * @param topMargin    上
     * @param rightMargin  右
     * @param bottomMargin 下
     */
    public CommonUseDialog setBtnContentMargin(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        if (llButtonGroup == null) {
            return this;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) llButtonGroup.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = rightMargin;
        layoutParams.bottomMargin = bottomMargin;
        llButtonGroup.setLayoutParams(layoutParams);
        return this;
    }

    public CommonUseDialog setCancelTextSize(float sp) {
        if (tvCancel == null) {
            return this;
        }
        tvCancel.setTextSize(sp);
        return this;
    }

    public CommonUseDialog setConfirmTextSize(float sp) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setTextSize(sp);
        return this;
    }

    public CommonUseDialog openCanScrollContent() {
        if (tvMsg != null) {
            tvMsg.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (rootView != null) {
            rootView.setPadding(0, PixelUtilKt.dp2px(120), 0, PixelUtilKt.dp2px(120));
        }
        return this;
    }

    public void setMessageMargins(int left, int top, int right, int bottom) {
        if (tvMsg == null) {
            return;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //4个参数按顺序分别是左上右下
        layoutParams.setMargins(left, top, right, bottom);
        tvMsg.setLayoutParams(layoutParams);
    }


    public static CommonUseDialog showCommonDialog(Context context, int title, int message, int confirmText,
                                                   int cancelText) {
        return showCommonDialog(context, title, message, confirmText, cancelText, null);
    }

    public static CommonUseDialog showCommonDialog(Context context, int title, int message, int confirmText,
                                                   int cancelText, OnConfirmClickListener confirmClickListener) {
        return showCommonDialog(context, title, message, confirmText, cancelText,
                confirmClickListener, null);
    }

    public static CommonUseDialog showCommonDialog(Context context, int title, int message, int confirmText,
                                                   int cancelText, OnConfirmClickListener confirmClickListener,
                                                   OnCancelClickListener cancelClickListener) {
        return showCommonDialog(context, title, message, confirmText, cancelText, View.GONE,
                confirmClickListener, cancelClickListener, null);

    }

    public static CommonUseDialog showCommonDialog(Context context, int title, int message, int confirmText,
                                                   int cancelText, int closeBtnVisibility, OnConfirmClickListener
                                                           confirmClickListener, OnCancelClickListener
                                                           cancelClickListener, OnCloseBtnClickListener closeBtnClickListener) {
        CommonUseDialog dialog = new CommonUseDialog(context);
        if (title > 0) {
            dialog.setTitleIntRes(title);
        }
        if (message > 0) {
            dialog.setMessge(message);
        }
        if (confirmText > 0) {
            dialog.setConfirmBtnText(confirmText);
        }
        if (cancelText > 0) {
            dialog.setCancelBtnText(cancelText);
        }
        if (confirmClickListener != null) {
            dialog.setConfirmClickListener(confirmClickListener);
        }
        if (cancelClickListener != null) {
            dialog.setCancelClickListener(cancelClickListener);
        }
        if (closeBtnClickListener != null) {
            dialog.setCloseBtnClickListener(closeBtnClickListener);
        }
        dialog.setCloseBtnVisibility(closeBtnVisibility);
        return dialog;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    /**
     * 配置项 在show之前调用；手机上自定义dialog宽度，pad上不生效会默认使用统一值；
     *
     * @param width px值
     */
    public CommonUseDialog setDialogWidth(int width) {
        mCustomDialogWidth = width;
        return this;
    }

    /**
     * 修改中间自定义view 距离顶部title的距离
     *
     * @param topMargin
     */
    public CommonUseDialog setCenterViewMarginTop(int topMargin) {
        if (llCenterGroup == null) {
            return this;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) llCenterGroup.getLayoutParams();
        layoutParams.topMargin = CommonUtils.dip2px(getContext(), topMargin);
        llCenterGroup.setLayoutParams(layoutParams);
        return this;
    }

    public CommonUseDialog setConfirmBtnTextWithBold(String btnName) {
        if (tvConfirm == null) {
            return this;
        }
        tvConfirm.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvConfirm.setText(btnName);
        return this;
    }

    /**
     * 设置dialog距离屏幕上下左右距离
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public CommonUseDialog setDialogPadding(int left, int top, int right, int bottom) {
        if (rootView == null) {
            return this;
        }
        rootView.findViewById(R.id.rcrl_root_layout).setPadding(left, top, right, bottom);
        return this;
    }

}
