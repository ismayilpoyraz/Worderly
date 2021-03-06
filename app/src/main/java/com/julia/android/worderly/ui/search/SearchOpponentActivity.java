package com.julia.android.worderly.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.julia.android.worderly.App;
import com.julia.android.worderly.R;
import com.julia.android.worderly.StringPreference;
import com.julia.android.worderly.model.User;
import com.julia.android.worderly.network.WordCallback;
import com.julia.android.worderly.network.WordRequest;
import com.julia.android.worderly.ui.game.view.GameActivity;
import com.julia.android.worderly.utils.Constants;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class SearchOpponentActivity extends AppCompatActivity
        implements SearchOpponentPresenter.View {

    @BindView(R.id.toolbar_randomopponent_activity) Toolbar mToolbar;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.text_searching_opponent) TextView mSearchingOpponentTextView;
    @BindView(R.id.image_avatar_opponent) CircleImageView mAvatarOpponentImageView;
    @BindView(R.id.text_username_opponent) TextView mUsernameOpponentTextView;
    @Inject StringPreference mPrefs;
    private SearchOpponentPresenter mPresenter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_opponent);
        ButterKnife.bind(this);
        App.get(this).component().inject(this);
        mPresenter = new SearchOpponentPresenter(this);
        setUpActionBar();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void addOpponentFoundView(final String username, final String photoUrl) {
        mSearchingOpponentTextView.setText(R.string.msg_opponent_found);
        if(photoUrl != null) {
            Glide.with(SearchOpponentActivity.this).load(photoUrl)
                    .into(mAvatarOpponentImageView);
        }
        mUsernameOpponentTextView.setText(username);
    }


    /**
     * Opponent found -> launch the Game activity
     */
    @Override
    public void navigateToGameActivity(final User opponentUser) {
        // Fetching word from API
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        new WordRequest(requestQueue, new WordCallback() {
            @Override
            public void onSuccess(String word, String definition) {
                Timber.d("WORD: %s, DEFINITION: %s", word, definition);
                Intent i = new Intent(SearchOpponentActivity.this, GameActivity.class);
                i.putExtra(Constants.EXTRA_OPPONENT, opponentUser);
                i.putExtra("EXTRA_WORD", word.toUpperCase());
                i.putExtra("EXTRA_DEFINITION", definition);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                startActivity(i);
                finish();
            }
        });
    }


    @Override
    public User getUserFromPrefs() {
        String json = mPrefs.get();
        if (!Objects.equals(json, Constants.PREF_USER_DEFAULT_VALUE)) {
            return new Gson().fromJson(json, User.class);
        }
        return null;
    }


    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

}
