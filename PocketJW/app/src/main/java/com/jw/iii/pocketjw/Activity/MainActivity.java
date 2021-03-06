package com.jw.iii.pocketjw.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jw.iii.pocketjw.Activity.More.Profile.ProfileActivity;
import com.jw.iii.pocketjw.Activity.Notice.SelectMemberActivity;
import com.jw.iii.pocketjw.Activity.Problems.AddProblemActivity;
import com.jw.iii.pocketjw.Fragment.MoreFragment;
import com.jw.iii.pocketjw.Fragment.NoticeFragment;
import com.jw.iii.pocketjw.Fragment.ProblemsFragment;
import com.jw.iii.pocketjw.Helper.Utils;
import com.jw.iii.pocketjw.R;
import com.jw.iii.pocketjw.UI.CircularImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        initView();
        initSlidingMenu();

        setChoiceItem(0);
    }

    private void initView() {
        noticeRelativeLayout = (RelativeLayout)findViewById(R.id.noticeRelativeLayout);
        noticeImageView = (ImageView)findViewById(R.id.noticeImageView);
        noticeTextView = (TextView)findViewById(R.id.noticeTextView);
        problemsRelativeLayout = (RelativeLayout)findViewById(R.id.problemsRelativeLayout);
        problemsImageView = (ImageView)findViewById(R.id.problemsImageView);
        problemsTextView = (TextView)findViewById(R.id.problemsTextView);
        moreRelativeLayout = (RelativeLayout)findViewById(R.id.moreRelativeLayout);
        moreImageView = (ImageView)findViewById(R.id.moreImageView);
        moreTextView = (TextView)findViewById(R.id.moreTextView);
        noticeRelativeLayout.setOnClickListener(this);
        problemsRelativeLayout.setOnClickListener(this);
        moreRelativeLayout.setOnClickListener(this);
    }

    private void initSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setBehindWidth(640);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setFadeEnabled(true);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.slidingmenu_main);

        loadGravatar();

        nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setText(AVUser.getCurrentUser().get("name").toString());
    }

    private void loadGravatar() {

        final CircularImage gravatar = (CircularImage)findViewById(R.id.gravatar);
        gravatar.setOnClickListener(gravatarListener);
        AVFile file = Utils.getCurrentUser().getAVFile("gravatar");
        if (file != null) {
            ImageLoader.getInstance().loadImage(Utils.getCurrentUser().getAVFile("gravatar").getUrl(), new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    gravatar.setImageResource(R.drawable.ic_launcher);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    gravatar.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    gravatar.setImageResource(R.drawable.ic_launcher);
                }
            });
        } else {
            gravatar.setImageResource(R.drawable.ic_launcher);
        }
    }

    private void setChoiceItem(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        clearChoice();
        hideFragments(transaction);
        setTitle(APP_TITLE[index]);
        currentChoice = index;

        switch (index) {
            case 0:
                noticeImageView.setImageResource(R.drawable.notice_hover);
                noticeTextView.setTextColor(getResources().getColor(R.color.primaryColor));
                if (noticeFragment == null) {
                    noticeFragment = new NoticeFragment();
                    transaction.add(R.id.contentFrameLayout, noticeFragment);
                } else {
                    transaction.show(noticeFragment);
                }
                break;
            case 1:
                problemsImageView.setImageResource(R.drawable.problem_hover);
                problemsTextView.setTextColor(getResources().getColor(R.color.primaryColor));
                if (problemsFragment == null) {
                    problemsFragment = new ProblemsFragment();
                    transaction.add(R.id.contentFrameLayout, problemsFragment);
                } else {
                    transaction.show(problemsFragment);
                }
                break;
            case 2:
                moreImageView.setImageResource(R.drawable.more_hover);
                moreTextView.setTextColor(getResources().getColor(R.color.primaryColor));
                if (moreFragment == null) {
                    moreFragment = new MoreFragment();
                    transaction.add(R.id.contentFrameLayout, moreFragment);
                } else {
                    transaction.show(moreFragment);
                }
                break;
        }
        transaction.commit();
    }

    private void clearChoice() {
        noticeImageView.setImageResource(R.drawable.notice_default);
        noticeTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        problemsImageView.setImageResource(R.drawable.problem_default);
        problemsTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        moreImageView.setImageResource(R.drawable.more_default);
        moreTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (noticeFragment != null) {
            transaction.hide(noticeFragment);
        }
        if (problemsFragment != null) {
            transaction.hide(problemsFragment);
        }
        if (moreFragment != null) {
            transaction.hide(moreFragment);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noticeRelativeLayout:
                setChoiceItem(0);
                break;
            case R.id.problemsRelativeLayout:
                setChoiceItem(1);
                break;
            case R.id.moreRelativeLayout:
                setChoiceItem(2);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_menu_add:
                if (currentChoice == 0) {
                    Intent selectMemberIntent = new Intent(MainActivity.this, SelectMemberActivity.class);
                    startActivity(selectMemberIntent);
                }
                if (currentChoice == 1) {
                    Intent addProblemIntent = new Intent(MainActivity.this, AddProblemActivity.class);
                    startActivity(addProblemIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener gravatarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        }
    };

    private int currentChoice = 0;
    private TextView nameTextView;
    private Fragment noticeFragment, problemsFragment, moreFragment;
    private RelativeLayout noticeRelativeLayout, problemsRelativeLayout, moreRelativeLayout;
    private ImageView noticeImageView, problemsImageView, moreImageView;
    private TextView noticeTextView, problemsTextView, moreTextView;
    private FragmentManager fragmentManager;

    private SlidingMenu slidingMenu;

    private final String APP_TITLE[] = { "通知发文", "问题解答", "更多信息" };
}
