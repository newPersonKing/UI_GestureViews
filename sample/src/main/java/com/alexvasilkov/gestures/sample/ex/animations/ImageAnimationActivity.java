package com.alexvasilkov.gestures.sample.ex.animations;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alexvasilkov.gestures.sample.R;
import com.alexvasilkov.gestures.sample.base.BaseSettingsActivity;
import com.alexvasilkov.gestures.sample.ex.utils.GlideHelper;
import com.alexvasilkov.gestures.sample.ex.utils.Painting;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.views.GestureImageView;

/**
 * This example demonstrates image animation from small mode into a full one.
 */
public class ImageAnimationActivity extends BaseSettingsActivity {

    private static final int PAINTING_ID = 2;

    private ImageView image;
    private GestureImageView fullImage;
    private View fullBackground;
    private ViewsTransitionAnimator<?> animator;

    private Painting painting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        image = findViewById(R.id.single_image);
        fullImage = findViewById(R.id.single_image_full);
        fullBackground = findViewById(R.id.single_image_back);

        // Loading image
        // PAINTING_ID 固定值2
        // painting 保存了 几张备用的图片id
        painting = Painting.list(getResources())[PAINTING_ID];
        // 加载原图到 image
        GlideHelper.loadThumb(image, painting.thumbId);

        // We will expand image on click
        image.setOnClickListener(view -> openFullImage());

        // Initializing image animator
        // 1 from  返回GestureTransitions 他持有一个变量  animator 类型是 ViewsTransitionAnimator
        // 2 from 和 into 分别初始化  fromListener  toListener
        // 3 因为 这俩listener 都是 RequestListener 所以又调用了 RequestListener.initAnimator(ViewsTransitionAnimator) 所以这俩listener 持有ViewsTransitionAnimator
        animator = GestureTransitions.from(image).into(fullImage);
        // 注册一个 PositionUpdateListener 保存在listeners 数组中
        animator.addPositionUpdateListener(this::applyImageAnimationState);
    }

    /**
     * Override this method if you want to provide slightly different layout.
     */
    protected void initContentView() {
        setContentView(R.layout.image_animation_screen);
        setTitle(R.string.example_image_animation);
    }

    @Override
    public void onBackPressed() {
        // We should leave full image mode instead of closing the screen
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSettingsChanged() {
        // Applying settings from toolbar menu, see BaseExampleActivity
        getSettingsController().apply(fullImage);
        // Resetting to initial image state
        fullImage.getController().resetState();
    }

    // 点击图片触发
    private void openFullImage() {
        // Setting image drawable from 'from' view to 'to' to prevent flickering
        if (fullImage.getDrawable() == null) {
            fullImage.setImageDrawable(image.getDrawable());
        }

        // Updating gesture image settings
        // 初始化fullImage 的一些属性
        getSettingsController().apply(fullImage);
        // Resetting to initial image state
        // getController 返回的是  GestureControllerForPager
        // resetState 重置状态
        fullImage.getController().resetState();

        // 1 给 enterWithAnimation 设置为true
        // 2 分别调用 fromListener  与 toListener 的onRequestView 给 animator 设置 fromView 与 toView
        // 3 并且会调用 notifyWhenReady-》isReady 只有 fromView 与 toView 都设置完之后 isReady才会返回true
        // 4 isReady 返回true 调用onViewsReady  主要实现在 ViewsTransitionAnimator
        animator.enterSingle(true);
        GlideHelper.loadFull(fullImage, painting.imageId, painting.thumbId);
    }

    private void applyImageAnimationState(float position, boolean isLeaving) {
        fullBackground.setAlpha(position);
        fullBackground.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
        fullImage.setVisibility(position == 0f && isLeaving ? View.INVISIBLE : View.VISIBLE);
    }

}
