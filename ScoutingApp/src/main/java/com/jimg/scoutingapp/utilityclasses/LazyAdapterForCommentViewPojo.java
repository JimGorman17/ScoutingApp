package com.jimg.scoutingapp.utilityclasses;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.helpers.DisplayToast;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


/**
 * Created by Jim on 3/16/14.
 */
public class LazyAdapterForCommentViewPojo extends BaseAdapter {

    private static class ViewHolderItem {
        ImageView userPictureImageView;
        TextView userNameTextView;
        TextView commentStringTextView;
        ImageButton commentEditButton;
        ImageButton commentDeleteButton;
    }

    private Activity mActivity;
    private ArrayList<CommentViewPojo> mCommentViewPojoList;
    private static LayoutInflater mInflater = null;
    private ImageLoader mImageLoader;

    public LazyAdapterForCommentViewPojo(Activity activity, ArrayList<CommentViewPojo> commentViewPojoList) {
        mActivity = activity;
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
        ViewHolderItem viewHolder;

        if (vi == null) {
            vi = mInflater.inflate(R.layout.comment_list_row, null);

            viewHolder = new ViewHolderItem();
            viewHolder.userPictureImageView = (ImageView) vi.findViewById(R.id.columnUserPictureForComment);
            viewHolder.userNameTextView = (TextView) vi.findViewById(R.id.columnUserDisplayNameForComment);
            viewHolder.commentStringTextView = (TextView) vi.findViewById(R.id.columnCommentStringForPlayerPage);
            viewHolder.commentEditButton = (ImageButton) vi.findViewById(R.id.comment_edit_button);
            viewHolder.commentDeleteButton = (ImageButton) vi.findViewById(R.id.comment_delete_button);

            viewHolder.commentEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DisplayToast(mActivity, "Edit button clicked.", Toast.LENGTH_LONG).run();
                }
            });

            viewHolder.commentDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DisplayToast(mActivity, "Delete button clicked.", Toast.LENGTH_LONG).run();
                }
            });

            vi.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolderItem) vi.getTag();
        }

        CommentViewPojo item = getItem(position);

        mImageLoader.displayImage(item.PictureUrl, viewHolder.userPictureImageView);
        viewHolder.userNameTextView.setText(item.DisplayName);
        viewHolder.commentStringTextView.setText(Html.fromHtml(item.FormattedComment));

        return vi;
    }
}
