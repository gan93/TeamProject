package edu.android.teamproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ComItemDetailActivity extends AppCompatActivity implements CommentDown.DataCallback {

    private static final String ITEM_ID = "comItemId";
    private static final int ITEM_ERROR = -1;

    public static Intent newIntent(Context context, int comId){
        Intent intent = new Intent(context, ComItemDetailActivity.class);
        intent.putExtra(ITEM_ID, comId);

        return  intent;
    }

    private ComItemDao dao;

    private Button btncommentAdd;
    private int commentCount;
    private EditText commentEdText;
    private TextView textTitle, textUserId, textDate, textCommentCount, textTag, textViewCount;
    private TextView textView;
    private ImageView imageView1, imageView2, imageView3;
    private ComItem comItem;

    private CommentDown commentDown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_item);

        textTitle = findViewById(R.id.comItemTitle);
        textUserId = findViewById(R.id.comItemUserId);
        textDate = findViewById(R.id.comItemDate);
        textView = findViewById(R.id.comItemText);
        textTag = findViewById(R.id.comItemTag);
        textCommentCount = findViewById(R.id.itemCommentCount);
        textViewCount = findViewById(R.id.comItemViewCount);
        btncommentAdd = findViewById(R.id.btnCommentWrite);

        int position = getIntent().getIntExtra(ITEM_ID, ITEM_ERROR);
        if(position != ITEM_ERROR){
            dao = ComItemDao.getComItemInstance(this);
            comItem = dao.update().get(position);
            commentDown = new CommentDown(comItem.getItemId(), this);
        }

        textTitle.setText(comItem.getTitle());
        textUserId.setText("아이디 : " + comItem.getUserEmail());
        textDate.setText("등록일 : " + comItem.getDate());
        textView.setText(comItem.getText());
        textViewCount.setText("조회수 : " + String.valueOf(comItem.getViewCount()));
        textCommentCount.setText("댓글 : " + String.valueOf(comItem.getCommentCount()));
        StringBuilder builder = new StringBuilder();
        builder.append("Tag : ");
        for(String s : comItem.getTag()) {
            if(s != null) {
                builder.append(s);
                textTag.setText(builder);
            }
            builder.append(", ");
        }

        int temp = 0;

        for(String fileName : comItem.getImages()){

            final ImageView imageView = new ImageView(this);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.
                    getReferenceFromUrl("gs://timproject-14aaa.appspot.com").child(ComItemDao.community + fileName);

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    GlideApp.with(ComItemDetailActivity.this)
                            .load(uri)
                            .into(imageView);
                }
            });
            LinearLayout imageLayout = findViewById(R.id.comImageLayout);
            imageLayout.addView(imageView);
            imageView.setPadding(8, 8, 8, 8);
        }

        final CommentAddMenu commentAddMenu = new CommentAddMenu(this);
        LinearLayout comLayout = findViewById(R.id.commentMenuLayout);
        comLayout.addView(commentAddMenu);

        commentEdText = commentAddMenu.findViewById(R.id.commentEdText);
        Button btnCommentUpdate = commentAddMenu.findViewById(R.id.btnCommentUpdateM);
        btnCommentUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentUpdate();
            }
        });

        btncommentAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentEdText.post(new Runnable() {
                    @Override
                    public void run() {
                        commentEdText.setFocusableInTouchMode(true);
                        commentEdText.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(commentEdText,0);
                    }
                });
            }
        });

    }

    private void commentUpdate() {
        CommentItem commentItem = new CommentItem();
        commentItem.setCommentText(commentEdText.getText().toString());
        commentDown.insert(commentItem);
    }

    @Override
    public void CommentCallback() {
        List<CommentItem> commentItemList = commentDown.update();
        commentCount = commentItemList.size();

        for(int i = 0; i < commentCount; i++){
            if(commentItemList.get(i).getCommentUserId().equals(commentDown.userEmail())){
                CommentMasterItem commentMasterItem = new CommentMasterItem(this);
                LinearLayout comLayout = findViewById(R.id.commentItemLayout);
                TextView textUserEmail = commentMasterItem.findViewById(R.id.commentUserIdM);
                TextView textDate = commentMasterItem.findViewById(R.id.commentDateM);
                TextView textView = commentMasterItem.findViewById(R.id.commentTextM);
                textUserEmail.setText(commentItemList.get(i).getCommentUserId());
                textDate.setText(commentItemList.get(i).getCommentDate());
                textView.setText(commentItemList.get(i).getCommentText());
                comLayout.addView(commentMasterItem);
            }else {
                CommentItemLayout commentItem = new CommentItemLayout(this);
                LinearLayout comLayout = findViewById(R.id.comItemLayout);
                TextView textUserEmail = commentItem.findViewById(R.id.commentItemLayout);
                TextView textDate = commentItem.findViewById(R.id.commentDate);
                TextView textView = commentItem.findViewById(R.id.commentText);
                textUserEmail.setText(commentItemList.get(i).getCommentUserId());
                textDate.setText(commentItemList.get(i).getCommentDate());
                textView.setText(commentItemList.get(i).getCommentText());
                comLayout.addView(commentItem);
            }
        }

    }

    class CommentItemLayout extends CardView {

        public CommentItemLayout(Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            inflater.inflate(R.layout.comment_item, this, true);
        }
    }

    class CommentMasterItem extends CardView {

        public CommentMasterItem(Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            inflater.inflate(R.layout.comment_master_item, this, true);
        }
    }

    class CommentAddMenu extends LinearLayout {

        public CommentAddMenu(Context context) {
            super(context);
            LayoutInflater inflater = getLayoutInflater();
            inflater.inflate(R.layout.comment_menu, this, true);
        }
    }

    @Override
    protected void onDestroy() {
        dao.viewCountUpdate(comItem);
        super.onDestroy();
    }
}
