package com.jw.iii.pocketjw.Activity.Problems;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetFileCallback;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jw.iii.pocketjw.Helper.Comment.CommentItem;
import com.jw.iii.pocketjw.Helper.Comment.CommentItemAdapter;
import com.jw.iii.pocketjw.Helper.Problem.ProblemItem;
import com.jw.iii.pocketjw.Helper.Problem.ProblemItemAdapter;
import com.jw.iii.pocketjw.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProblemItemActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_item);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent problemItemIntent = getIntent();
        Bundle bundle = problemItemIntent.getExtras();
        problemID = bundle.get("problemID").toString();
        problemTitle = bundle.get("problemTitle").toString();
        problemDesc =  bundle.get("problemDesc").toString();
        problemView = bundle.get("problemView").toString();
        problemComment = bundle.get("problemComment").toString();
        problemPublisher = (AVUser)bundle.get("problemPublisher");
        problemImages = (ArrayList<String>)bundle.get("problemImages");

        initView();

        increaseViewCount();
    }

    private void initView() {
        setTitle(problemTitle);

        problemPublisherImageView = (ImageView)findViewById(R.id.problemPublisherImageView);
        problemPublisherTextView = (TextView)findViewById(R.id.problemPublisherTextView);

        problemTitleTextView = (TextView)findViewById(R.id.problemTitleTextView);
        problemDescTextView = (TextView)findViewById(R.id.problemDescTextView);
        problemImagesLinearLayout = (LinearLayout)findViewById(R.id.problemImagesLinearLayout);
        problemViewTextView = (TextView)findViewById(R.id.problemViewTextView);
        problemCommentTextView = (TextView)findViewById(R.id.problemCommentTextView);
        addCommentTextView = (TextView)findViewById(R.id.addCommentTextView);
        commentsListView = (PullToRefreshListView)findViewById(R.id.commentsListView);

        problemTitleTextView.setText(problemTitle);
        problemDescTextView.setText(problemDesc);
        problemViewTextView.setText(problemView);
        problemCommentTextView.setText(problemComment);

        loadPublisher();

        loadProblemImages();

        commentsListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        commentsListView.setOnRefreshListener(commentsListener);
        commentsListView.setOnItemClickListener(commentsItemListener);

        commentsItems = new ArrayList<>();
        commentsItemsTmp = new ArrayList<>();
        commentsItemAdapter = new CommentItemAdapter(commentsItems, this, ImageLoader.getInstance());

        commentsListView.setAdapter(commentsItemAdapter);

        addCommentTextView.setOnClickListener(addCommentListener);
    }

    private void loadPublisher() {

        AVQuery<AVObject> query = new AVQuery<>("_User");
        query.getInBackground(problemPublisher.getObjectId(), new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    AVUser user = (AVUser)avObject;
                    AVFile file = user.getAVFile("gravatar");
                    problemPublisherTextView.setText(user.get("name").toString());
                    if (file != null) {
                        ImageLoader.getInstance().displayImage(file.getUrl(), problemPublisherImageView);
                    } else {
                        problemPublisherImageView.setImageResource(R.drawable.ic_launcher);
                    }
                }
            }
        });
    }

    private void loadProblemImages() {

        images = new ArrayList<>();

        for (final String imageName : problemImages) {
            AVQuery<AVObject> query = new AVQuery<>("_File");
            query.whereEqualTo("name", imageName);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null && !avObjects.isEmpty()) {
                        final AVObject object = avObjects.get(0);
                        final String url = object.get("url").toString();
                        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String s, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String s, View view, FailReason failReason) {

                            }

                            @Override
                            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                if (bitmap != null) {
                                    saveImage(bitmap, SDCARD_PATH_PROBLEM + "/" + imageName + ".jpeg");
                                    images.add(SDCARD_PATH_PROBLEM + "/" + imageName + ".jpeg");
                                    ImageView imageView = new ImageView(getApplicationContext());
                                    imageView.setOnClickListener(imageViewListener);
                                    imageView.setContentDescription(String.valueOf(images.size()));
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(240, 240);
                                    layoutParams.setMargins(8, 8, 8, 8);
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    problemImagesLinearLayout.addView(imageView);
                                }
                            }

                            @Override
                            public void onLoadingCancelled(String s, View view) {

                            }
                        });
                    }
                }
            });
        }
    }

    private void increaseViewCount() {
        AVQuery<AVObject> query = new AVQuery<>("Problem");
        query.getInBackground(problemID, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    avObject.put("views", (int)avObject.get("views") + 1);
                    avObject.saveInBackground();
                }
            }
        });
    }

    private void saveImage(Bitmap bitmap, String filePath) {
        BufferedOutputStream outputStream = null;
        try {
            File file = new File(filePath);
            File dirFile = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            dirFile.mkdirs();
            file.createNewFile();
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class GetFooterDataTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            getData();
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {
            parseData();
            commentsListView.onRefreshComplete();
            commentsListView.getRefreshableView().setSelectionFromTop(prevLocation, TRIM_MEMORY_BACKGROUND);
            super.onPostExecute(strings);
        }
    }

    private void getData() {
        final AVQuery<AVObject> query = new AVQuery<>("Comment").setSkip(page++ * PAGE_COUNT).setLimit(PAGE_COUNT).orderByDescending("approval").whereEqualTo("problem", problemID);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    if (avObjects.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "没有更多答案", Toast.LENGTH_SHORT).show();
                    } else {
                        for (AVObject object : avObjects) {
                            CommentItem commentItem = new CommentItem(object.get("comment").toString(), object.getObjectId(),
                                    object.get("approval").toString(), object.getAVUser("publisher"), object.get("images").toString());
                            commentsItemsTmp.add(commentItem);
                        }
                    }
                }
            }
        });
    }

    private void parseData() {
        for (CommentItem commentItem : commentsItemsTmp) {
            commentsItems.add(commentItem);
            commentsItemAdapter.notifyDataSetChanged();
        }
        commentsItemsTmp.clear();
    }

    PullToRefreshBase.OnRefreshListener<ListView> commentsListener = new PullToRefreshBase.OnRefreshListener<ListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            if (refreshView.isFooterShown()) {
                prevLocation = commentsItems.size();
                new GetFooterDataTask().execute();
            }
        }
    };

    // TODO: Item click listener
    AdapterView.OnItemClickListener commentsItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent commentItemIntent = new Intent(ProblemItemActivity.this, CommentItemActivity.class);
            commentItemIntent.putExtra("problemTitle", problemTitle);
            commentItemIntent.putExtra("comment", commentsItems.get(position - 1).getComment());
            commentItemIntent.putExtra("commentID", commentsItems.get(position - 1).getCommentID());
            commentItemIntent.putExtra("commentPublisherUrl", commentsItems.get(position - 1).getCommentPublisherUrl());
            commentItemIntent.putExtra("commentPublisherName", commentsItems.get(position - 1).getCommentPublisherName());
            commentItemIntent.putExtra("commentApproval", commentsItems.get(position - 1).getCommentApproval());
            commentItemIntent.putExtra("commentImages", commentsItems.get(position - 1).getCommentImages());
            startActivity(commentItemIntent);
        }
    };

    View.OnClickListener imageViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = Integer.valueOf(v.getContentDescription().toString());
            Intent imageDisplayIntent = new Intent(ProblemItemActivity.this, ImageDisplayActivity.class);
            imageDisplayIntent.putExtra("index", index);
            imageDisplayIntent.putExtra("images", images);
            startActivity(imageDisplayIntent);
        }
    };

    View.OnClickListener addCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent addCommentIntent = new Intent(ProblemItemActivity.this, AddCommentActivity.class);
            addCommentIntent.putExtra("problemID", problemID);
            startActivity(addCommentIntent);
        }
    };

    final private int PAGE_COUNT = 10;
    private int page = 0;
    private int prevLocation = 0;

    private String problemID, problemTitle, problemDesc, problemView, problemComment;
    private AVUser problemPublisher;
    private ArrayList<String> problemImages, images;

    private static final String SDCARD_PATH_PROBLEM = "/sdcard/PocketJW/Problem";

    private ImageView problemPublisherImageView;
    private TextView problemPublisherTextView;
    private TextView problemTitleTextView, problemDescTextView;
    private LinearLayout problemImagesLinearLayout;
    private TextView problemViewTextView, problemCommentTextView;
    private TextView addCommentTextView;

    private ArrayList<CommentItem> commentsItems, commentsItemsTmp;
    private CommentItemAdapter commentsItemAdapter;
    private PullToRefreshListView commentsListView;
}
