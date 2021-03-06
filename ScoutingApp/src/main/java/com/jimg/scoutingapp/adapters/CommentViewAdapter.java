package com.jimg.scoutingapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.fragments.PlayerFragment;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by Jim on 3/16/14.
 */
public class CommentViewAdapter extends BaseAdapter {

    static class ViewHolderItem {
        ListView parentListView;
        EditText commentEditText;

        Integer commentId;
        String rawComment;

        @InjectView(R.id.columnUserPictureForComment) ImageView userPictureImageView;
        @InjectView(R.id.columnUserDisplayNameForComment) TextView userNameTextView;
        @InjectView(R.id.columnCommentStringForPlayerPage) TextView commentStringTextView;
        @InjectView(R.id.comment_action_buttons_linear_layout) LinearLayout actionButtonsLinearLayout;
        @InjectView(R.id.comment_edit_button) ImageButton commentEditButton;
        @InjectView(R.id.comment_delete_button) ImageButton commentDeleteButton;
        @InjectView(R.id.comment_flag_button) ImageButton commentFlagButton;

        private ViewHolderItem(ListView parentListView, View view) {
            this.parentListView = parentListView;
            this.commentEditText = ButterKnife.findById((View)parentListView.getParent(), R.id.playerPageEditText);
            ButterKnife.inject(this, view);
        }
    }

    private PlayerFragment mPlayerFragment;
    private MainActivity mMainActivity;
    private ArrayList<CommentViewPojo> mCommentViewPojoList;
    private static LayoutInflater mInflater = null;
    private ImageLoader mImageLoader;

    public CommentViewAdapter(PlayerFragment playerFragment, MainActivity activity, ArrayList<CommentViewPojo> commentViewPojoList) {
        mPlayerFragment = playerFragment;
        mMainActivity = activity;
        mCommentViewPojoList = commentViewPojoList;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = ImageLoader.getInstance();
    }

    public int getCount() {
        return mCommentViewPojoList.size();
    }

    public CommentViewPojo getItem(int position) {
        return mCommentViewPojoList.get(position);
    }

    public long getItemId(int position) {
        return mCommentViewPojoList.get(position).CommentId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolderItem viewHolder;

        if (vi == null) {
            vi = mInflater.inflate(R.layout.comment_list_row, null);

            viewHolder = new ViewHolderItem((ListView)parent, vi);
            viewHolder.commentEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent().getParent()).getTag();
                    mPlayerFragment.mCurrentlySelectedCommentId = selectedItemViewHolder.commentId;

                    ((BaseAdapter) selectedItemViewHolder.parentListView.getAdapter()).notifyDataSetChanged();
                    EditText commentEditText = selectedItemViewHolder.commentEditText;
                    if (commentEditText == null) {
                        commentEditText = ButterKnife.findById((View) selectedItemViewHolder.parentListView.getParent().getParent(), R.id.playerPageEditText);
                    }

                    commentEditText.setText(selectedItemViewHolder.rawComment);
                }
            });

            viewHolder.commentDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(mMainActivity);
                    adb.setTitle("Delete?");
                    adb.setMessage("Are you sure you want to delete this comment?");

                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent().getParent()).getTag();
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("OK", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            JsonObject json = new JsonObject();
                            json.addProperty(Constants.commentIdExtra, selectedItemViewHolder.commentId);
                            json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);
                            json.addProperty(Constants.deleteExtra, true);

                            mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", mMainActivity.getString(R.string.please_wait_deleting_comment), false);
                            Ion.with(mMainActivity, Constants.restServiceUrlBase + "Comment/Save?" + Constants.getJson)
                                    .progressDialog(mMainActivity.mProgressDialog)
                                    .setJsonObjectBody(json)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            if (e != null) {
                                                ErrorHelpers.handleError(mMainActivity.getString(R.string.failure_to_delete_comment), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                                            }

                                            removeItemFromAdapter(selectedItemViewHolder);
                                            mMainActivity.dismissProgressDialog();
                                        }
                                    });
                        }
                    });
                    adb.show();
                }
            });

            viewHolder.commentFlagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(mMainActivity);
                    adb.setTitle("Flag?");
                    adb.setMessage("Are you sure you want to flag this comment for moderator review?");

                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("OK", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            JsonObject json = new JsonObject();
                            json.addProperty(Constants.commentIdExtra, selectedItemViewHolder.commentId);
                            json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);
                            json.addProperty(Constants.flaggedExtra, true);

                            mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", mMainActivity.getString(R.string.please_wait_flagging_comment), false);
                            Ion.with(mMainActivity, Constants.restServiceUrlBase + "Comment/Save?" + Constants.getJson)
                                    .progressDialog(mMainActivity.mProgressDialog)
                                    .setJsonObjectBody(json)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            if (e != null) {
                                                ErrorHelpers.handleError(mMainActivity.getString(R.string.failure_to_delete_comment), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                                            }
                                            viewHolder.commentFlagButton.setVisibility(View.GONE);
                                            mMainActivity.dismissProgressDialog();
                                        }
                                    });
                        }
                    });
                    adb.show();
                }
            });

            vi.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolderItem) vi.getTag();
        }

        CommentViewPojo item = getItem(position);

        viewHolder.commentId = item.CommentId;
        mImageLoader.displayImage(item.PictureUrl, viewHolder.userPictureImageView);
        viewHolder.userNameTextView.setText(item.DisplayName);
        viewHolder.rawComment = item.CommentString;
        viewHolder.commentStringTextView.setText(Html.fromHtml(item.FormattedComment));
        viewHolder.actionButtonsLinearLayout.setVisibility(item.CanEditOrDelete ? View.VISIBLE : View.GONE);
        viewHolder.commentFlagButton.setVisibility(item.CanFlag ? View.VISIBLE : View.GONE);

        if (viewHolder.commentId.equals(mPlayerFragment.mCurrentlySelectedCommentId)) {
            vi.setBackgroundColor(mMainActivity.getResources().getColor(R.color.LightYellow));
        }
        else {
            vi.setBackgroundColor(mMainActivity.getResources().getColor(android.R.color.transparent));
        }

        return vi;
    }

    private void removeItemFromAdapter(ViewHolderItem selectedItemViewHolder) {
        for (int i = 0; i < mCommentViewPojoList.size(); i++) {
            CommentViewPojo comment = mCommentViewPojoList.get(i);
            if (comment.CommentId == selectedItemViewHolder.commentId) {
                mCommentViewPojoList.remove(comment);
                break;
            }
        }
        ((BaseAdapter) selectedItemViewHolder.parentListView.getAdapter()).notifyDataSetChanged();
    }

    public void cancelEdit() {
        mPlayerFragment.mCurrentlySelectedCommentId = 0;
        notifyDataSetChanged();
    }
}
