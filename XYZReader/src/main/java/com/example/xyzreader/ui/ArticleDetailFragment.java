package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.util.List;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "ArticleDetailFragment";
    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    private static final int CHOOSER_CONSTANT = 100;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;
    //private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private int mTopInset;
    private CoordinatorLayout mCoordinatorLayout;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private int mOffset;
  //  private FloatingActionButton mFab;
    private AppBarLayout.OnOffsetChangedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
   //     mFab.show();
        if (mListener != null) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        /*if (mListener != null) {
            mAppBarLayout.removeOnOffsetChangedListener(mListener);
        }*/
       // mFab.hide();
        Log.d(LOG_TAG, "FRAGMENT PAUSED");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mFab.hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.BOTTOM);
            slide.addTarget(R.id.textContainer);
            slide.addTarget(R.id.meta_bar);

            slide.addTarget(R.id.article_body);
            slide.addTarget(R.id.article_title);
            slide.addTarget(R.id.article_subtitle);
            slide.addTarget(R.id.article_byline);

            slide.setInterpolator(
                    AnimationUtils.loadInterpolator(getActivityCast(),
                            android.R.interpolator.linear_out_slow_in));
            slide.setDuration(600);
            getActivityCast().getWindow().setEnterTransition(slide);
        }
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
        //mFab.show();

    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
  //      mFab.hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);
       // mFab = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        mCoordinatorLayout = (CoordinatorLayout) mRootView.findViewById(R.id.textContainer);
        mAppBarLayout = (AppBarLayout) mRootView.findViewById(R.id.meta_bar);
          mCollapsingToolbarLayout = (CollapsingToolbarLayout)
                mRootView.findViewById(R.id.collapsingToolBar);

       /* if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
                @Override
                public void onScrollChanged() {
                    mScrollY = mScrollView.getScrollY() + mAppBarLayout.getScrollY() + mCollapsingToolbarLayout.getScrollY();
                    getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                    mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    mScrollY = mScrollView.getScrollY() - mAppBarLayout.getScrollY() - mCollapsingToolbarLayout.getScrollY();
                    getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                    mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                }
            });
        }*/


        mStatusBarColorDrawable = new ColorDrawable(0);


        bindViews();
        updateStatusBar();

        return mRootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSER_CONSTANT) {
            Snackbar.make(mCoordinatorLayout, "Article Shared.", Snackbar.LENGTH_SHORT).show();
           // mFab.show();
        }

    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        final TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);


            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            Log.v(LOG_TAG, mCursor.getString(ArticleLoader.Query.BODY));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            if (mCursor != null) {
                                Log.d(LOG_TAG, mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                            }
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkVibrantColor(0xFF333333);
                                List<Palette.Swatch> swatch = p.getSwatches();


                                updateStatusBar();
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());



                                mCollapsingToolbarLayout.setContentScrimColor(mMutedColor);

                                //mFab.setRippleColor(mMutedColor);
                                //mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.MainTitleText);
                                mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ToolbarTitleExpanded);
                                mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.ToolbarTitleCollapsed);

                                mAppBarLayout.addOnOffsetChangedListener(mListener = new AppBarLayout.OnOffsetChangedListener() {
                                    boolean isShowing = false;
                                    int scrollRange = -1;
                                 //  private FloatingActionButton floatingActionButton = getActivityCast().getFab();

                                    @Override
                                    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                                        mOffset = i;
                                        if (scrollRange == -1) {
                                            scrollRange = appBarLayout.getTotalScrollRange();
                                            Log.v(LOG_TAG, "SCROLL_RANGE: " +  scrollRange);
                                        }
                                        if (scrollRange + i == 0) {
                                            mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));


                                            isShowing = true;


                                        } else if (isShowing) {
                                            mCollapsingToolbarLayout.setTitle(null);

                                            isShowing = false;
                                        }
                                        mScrollY = i;
                                        getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
                                        mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                                    Log.d(LOG_TAG, "Scroll Range: " + String.valueOf(scrollRange));
                                    Log.d(LOG_TAG, "Vertical Offset: " + String.valueOf(i));
                                      //  mFab.show();
                                    }
                                });

                               /* mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);*/

                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });

        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }
    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }
        if (mIsCard) {
            return (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY;
        } else {
            return  (mPhotoView.getHeight() - mScrollY);
        }
    }
    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            Log.v(LOG_TAG, "IN UPDATESTATUSBAR IF STATEMENT");
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);

        //  mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }


    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(LOG_TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }


}
