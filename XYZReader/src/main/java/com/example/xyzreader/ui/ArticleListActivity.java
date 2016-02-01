package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private DynamicHeightNetworkImageView mNetworkImageView;
    private static final String LOG_TAG = ArticleListActivity.class.getSimpleName();
    private int finalHeight, finalWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        final View toolbarContainerView = findViewById(R.id.toolbar_container);
       // setSupportActionBar(mToolbar);
        //mToolbar.setTitle("");
        mNetworkImageView = (DynamicHeightNetworkImageView) findViewById(R.id.thumbnail);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }

    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor, mNetworkImageView);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);//
        mRecyclerView.setLayoutManager(sglm);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
       // private Activity mActivity;
        private DynamicHeightNetworkImageView mNetworkImageView;

        public Adapter(Cursor cursor,  DynamicHeightNetworkImageView networkImageView) {
            mCursor = cursor;
           // mActivity = activity;
            mNetworkImageView = networkImageView;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
           // final com.example.xyzreader.ui.DynamicHeightNetworkImageView dynamicHeightNetworkImageView = (DynamicHeightNetworkImageView) findViewById(R.id.thumbnail);
            final ViewHolder vh = new ViewHolder(view);
            final View networkImageView =  findViewById(R.id.thumbnail);
            //final ImageView imageView = (ImageView) findViewById(R.id.photo);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.setTransitionName(getResources().getString(R.string.picture));
                        Bundle bundle = ActivityOptions
                                .makeSceneTransitionAnimation(ArticleListActivity.this, view, view.getTransitionName())
                                .toBundle();
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))), bundle);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                    }
                }

            });
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);

            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.titleView.setVisibility(View.INVISIBLE);

            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
            holder.subtitleView.setVisibility(View.INVISIBLE);

            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
            if (holder.thumbnailView.getDrawable() != null) {
                Log.d(LOG_TAG, "ThumbnailView imageBitmap1: " + holder.thumbnailView.getDrawable().toString());
            }


            ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.THUMB_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                /*Palette palette = Palette.generate(bitmap);
                                Palette.Swatch swatch = palette.getLightMutedSwatch();*/
                               // holder.thumbnailView.setImageBitmap(bitmap);
//                                Log.d(LOG_TAG, "ThumbnailView imageBitmap2: " + holder.thumbnailView.getDrawable().toString());
                                holder.titleView.setVisibility(View.VISIBLE);
                                holder.subtitleView.setVisibility(View.VISIBLE);
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch generatedSwatch = palette.getLightVibrantSwatch();
                                        if (generatedSwatch != null) {
                                           // holder.titleView.setTextColor(generatedSwatch.getBodyTextColor());
                                            int color = ColorUtils.getComplimentColor(generatedSwatch.getTitleTextColor());
                                           // holder.titleView.setTextColor(color);
                                            Log.d(LOG_TAG, "TITLE TEXT COLOR: " + generatedSwatch.getBodyTextColor());
                                        }
                                        float density = getResources().getDisplayMetrics().scaledDensity;
                                        Log.d(LOG_TAG, "DEVICE DENSITY: " + density);
                                        float textHeight = holder.cardView.getHeight() * (.101f / density);
                                        holder.titleView.setTextSize(textHeight);
                                        textHeight = holder.cardView.getHeight() * (.08f / density);
                                        holder.subtitleView.setTextSize(textHeight);
                                    }
                                });

                                    //int colorInteger = swatch.getTitleTextColor();
//                                    int colorInteger = swatch.getTitleTextColor();
                                         //   holder.titleView.setTextColor(colorInteger);
                                  //  Log.d(LOG_TAG, "COLOR INT: " + colorInteger);

                                //swatch.getTitleTextColor();
                               /* int colorInteger = palette.getVibrantColor(R.color.ltgray);
                                holder.titleView.setTextColor((colorInteger));
                                Log.d(LOG_TAG, "BITMAP: " + bitmap.toString());
                                Log.d(LOG_TAG, "COLOR INT: " + colorInteger);*/
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            volleyError.printStackTrace();
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
        public CardView cardView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            cardView = (CardView) view.findViewById(R.id.cardView);
        }
    }
}
