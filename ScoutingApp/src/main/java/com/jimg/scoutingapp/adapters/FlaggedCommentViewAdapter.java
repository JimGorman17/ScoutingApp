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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.fragments.FlaggedCommentsFragment;
import com.jimg.scoutingapp.helpers.ErrorHelpers;
import com.jimg.scoutingapp.pojos.FlaggedCommentPojo;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Jim on 4/13/2014.
 */
public class FlaggedCommentViewAdapter extends BaseAdapter {

    static class ViewHolderItem {
        ListView parentListView;
        Integer commentId;

        @InjectView(R.id.columnFormattedCommentString) TextView formattedCommentTextView;
        @InjectView(R.id.columnEditCommentString) EditText commentEditText;
        @InjectView(R.id.columnNumberOfFlags) TextView flagsTextView;
        @InjectView(R.id.comment_edit_button) ImageButton editButton;
        @InjectView(R.id.comment_delete_button) ImageButton deleteButton;
        @InjectView(R.id.comment_ignore_button) ImageButton ignoreButton;
        @InjectView(R.id.comment_cancel_edit_Button) ImageButton cancelEditButton;
        @InjectView(R.id.comment_submit_button) ImageButton submitButton;
        @InjectView(R.id.comment_action_buttons_linear_layout) LinearLayout actionButtonsLayout;
        @InjectView(R.id.comment_edit_action_buttons_linear_layout) LinearLayout editActionButtonsLayout;

        private ViewHolderItem(ListView parentListView, View view) {
            this.parentListView = parentListView;
            ButterKnife.inject(this, view);
        }
    }

    private OnListSizeModifiedListener listener;

    public interface OnListSizeModifiedListener {
        public void onListSizeModified(Integer newListSize);
    }

    private FlaggedCommentsFragment mFlaggedCommentsFragment;
    private MainActivity mMainActivity;
    private ArrayList<FlaggedCommentPojo> mFlaggedCommentPojoList;
    private static LayoutInflater mInflater = null;

    public FlaggedCommentViewAdapter(FlaggedCommentsFragment flaggedCommentsFragment, MainActivity mainActivity, ArrayList<FlaggedCommentPojo> flaggedCommentPojoList) {
        mFlaggedCommentsFragment = flaggedCommentsFragment;
        this.mMainActivity = mainActivity;
        this.mFlaggedCommentPojoList = flaggedCommentPojoList;
        mInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (flaggedCommentsFragment instanceof OnListSizeModifiedListener) {
            listener = flaggedCommentsFragment;
        }
        else {
            throw new ClassCastException(flaggedCommentsFragment.toString() + " must implement FlaggedCommentViewAdapter.OnListSizeModifiedListener.");
        }
    }

    @Override
    public int getCount() {
        return mFlaggedCommentPojoList.size();
    }

    @Override
    public FlaggedCommentPojo getItem(int position) {
        return mFlaggedCommentPojoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFlaggedCommentPojoList.get(position).commentId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolderItem viewHolder;

        if (vi == null) {
            vi = mInflater.inflate(R.layout.flagged_comment_list_row, null);
            viewHolder = new ViewHolderItem((ListView) parent, vi);

            viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();
                    mFlaggedCommentsFragment.mCurrentlySelectedCommentId = selectedItemViewHolder.commentId;
                    ((BaseAdapter) selectedItemViewHolder.parentListView.getAdapter()).notifyDataSetChanged();
                }
            });

            viewHolder.cancelEditButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();
                    mFlaggedCommentsFragment.mCurrentlySelectedCommentId = 0;
                    ((BaseAdapter) selectedItemViewHolder.parentListView.getAdapter()).notifyDataSetChanged();
                }
            });

            viewHolder.submitButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();

                    JsonObject json = new JsonObject();
                    json.addProperty(Constants.commentIdExtra, selectedItemViewHolder.commentId);
                    json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);
                    json.addProperty(Constants.commentExtra, selectedItemViewHolder.commentEditText.getText().toString());
                    json.addProperty(Constants.handleFlagsExtra, true);

                    mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", mMainActivity.getString(R.string.please_wait_posting_comment), false);
                    Ion.with(mMainActivity, Constants.restServiceUrlBase + "Comment/Save?" + Constants.getJson)
                            .progressDialog(mMainActivity.mProgressDialog)
                            .setJsonObjectBody(json)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (e != null) {
                                        ErrorHelpers.handleError(mMainActivity.getString(R.string.failure_to_post_comment), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
                                    } else {
                                        removeItemFromAdapter(selectedItemViewHolder);
                                    }
                                    mMainActivity.dismissProgressDialog();
                                }
                            });

                }
            });

            viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(mMainActivity);
                    adb.setTitle("Delete?");
                    adb.setMessage("Are you sure you want to delete this comment?");

                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();
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

            viewHolder.ignoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(mMainActivity);
                    adb.setTitle("Ignore?");
                    adb.setMessage("Are you sure you want to ignore this comment?");

                    final ViewHolderItem selectedItemViewHolder = (ViewHolderItem) ((View) v.getParent().getParent().getParent()).getTag();
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("OK", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            JsonObject json = new JsonObject();
                            json.addProperty(Constants.commentIdExtra, selectedItemViewHolder.commentId);
                            json.addProperty(Constants.authTokenExtra, mMainActivity.mAuthToken);

                            mMainActivity.mProgressDialog = ProgressDialog.show(mMainActivity, "", mMainActivity.getString(R.string.please_wait_ignoring_flags), false);
                            Ion.with(mMainActivity, Constants.restServiceUrlBase + "FlaggedComment/IgnoreFlags?" + Constants.getJson)
                                    .progressDialog(mMainActivity.mProgressDialog)
                                    .setJsonObjectBody(json)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            if (e != null) {
                                                ErrorHelpers.handleError(mMainActivity.getString(R.string.failure_to_ignore_comments), e.getMessage(), ErrorHelpers.getStackTraceAsString(e), mMainActivity);
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

            vi.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) vi.getTag();
        }

        FlaggedCommentPojo item = getItem(position);
        viewHolder.commentId = item.commentId;
        viewHolder.formattedCommentTextView.setText(Html.fromHtml(item.formattedComment));
        viewHolder.commentEditText.setText(item.comment);
        viewHolder.flagsTextView.setText(item.numberOfFlags.toString());

        if (viewHolder.commentId.equals(mFlaggedCommentsFragment.mCurrentlySelectedCommentId)) {
            vi.setBackgroundColor(mMainActivity.getResources().getColor(R.color.LightYellow));
            viewHolder.actionButtonsLayout.setVisibility(View.GONE);
            viewHolder.formattedCommentTextView.setVisibility(View.GONE);
            viewHolder.commentEditText.setVisibility(View.VISIBLE);
            viewHolder.editActionButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            vi.setBackgroundColor(mMainActivity.getResources().getColor(android.R.color.transparent));
            viewHolder.actionButtonsLayout.setVisibility(View.VISIBLE);
            viewHolder.formattedCommentTextView.setVisibility(View.VISIBLE);
            viewHolder.commentEditText.setVisibility(View.GONE);
            viewHolder.editActionButtonsLayout.setVisibility(View.GONE);
        }

        return vi;
    }

    private void removeItemFromAdapter(ViewHolderItem viewHolderItem) {
        for (int i = 0; i < mFlaggedCommentPojoList.size(); i++) {
            FlaggedCommentPojo flaggedComment = mFlaggedCommentPojoList.get(i);
            if (flaggedComment.commentId == viewHolderItem.commentId) {
                mFlaggedCommentPojoList.remove(flaggedComment);
                break;
            }
        }
        ((BaseAdapter) viewHolderItem.parentListView.getAdapter()).notifyDataSetChanged();
        listener.onListSizeModified(getCount());
    }
}
