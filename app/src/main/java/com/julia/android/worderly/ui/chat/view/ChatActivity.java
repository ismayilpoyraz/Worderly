package com.julia.android.worderly.ui.chat.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.julia.android.worderly.R;
import com.julia.android.worderly.adapter.ChatFirebaseAdapter;
import com.julia.android.worderly.adapter.MessageViewHolder;
import com.julia.android.worderly.model.Message;
import com.julia.android.worderly.model.User;
import com.julia.android.worderly.ui.chat.presenter.ChatPresenter;
import com.julia.android.worderly.ui.chat.presenter.ChatPresenterImpl;
import com.julia.android.worderly.utils.Constants;
import com.julia.android.worderly.utils.FirebaseConstants;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.julia.android.worderly.utils.Constants.DEFAULT_MSG_LENGTH_LIMIT;
import static com.julia.android.worderly.utils.Constants.PREF_NAME;
import static com.julia.android.worderly.utils.Constants.PREF_USER;

public class ChatActivity extends AppCompatActivity implements ChatView {

    private static final String TAG = ChatActivity.class.getSimpleName();
    @BindView(R.id.toolbar_main_activity)
    Toolbar mToolbar;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.messageRecyclerView)
    RecyclerView mMessageRecyclerView;
    @BindView(R.id.messageEditText)
    EditText mMessageEditText;
    @BindView(R.id.button_send)
    Button mSendButton;

    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private ChatPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        mPresenter = new ChatPresenterImpl(this);
        getSharedPrefs();
        getBundleExtras();
        setUpActionBar();

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mMessageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        setUpFirebaseAdapter();

        // Add separation lines between message items in RecyclerView
        mMessageRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void getBundleExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String opponentId = extras.getString(Constants.EXTRA_OPPONENT_ID);
            mPresenter.setChatRoomChild(opponentId);
        }
    }

    private void getSharedPrefs() {
        SharedPreferences mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(PREF_USER, Constants.PREF_USER_DEFAULT_VALUE);
        if (!Objects.equals(json, Constants.PREF_USER_DEFAULT_VALUE)) {
            User user = gson.fromJson(json, User.class);
            mPresenter.setUserFromJson(user);
        }
    }

    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }
    }

    private void setUpFirebaseAdapter() {
        mFirebaseAdapter = new ChatFirebaseAdapter(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                FirebaseDatabase.getInstance().getReference()
                        .child(FirebaseConstants.FIREBASE_GAMES_CHILD)
                        .child(mPresenter.getChatRoomChild())
                        .child(FirebaseConstants.FIREBASE_MESSAGES_CHILD));

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        // Hide ProgressBar when empty chat log
        if (mFirebaseAdapter.getItemCount() == 0) {
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    @OnTextChanged(value = R.id.messageEditText)
    void onMessageInput(Editable editable) {
        if (editable.toString().trim().length() > 0) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    @OnClick(R.id.button_send)
    public void onClick() {
        mPresenter.onSendButtonClick(mMessageEditText.getText().toString());
        mMessageEditText.setText("");
    }
}