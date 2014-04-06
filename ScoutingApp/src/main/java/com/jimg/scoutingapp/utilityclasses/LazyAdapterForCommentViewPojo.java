package com.jimg.scoutingapp.utilityclasses;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.pojos.CommentViewPojo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


/**
 * Created by Jim on 3/16/14.
 */
public class LazyAdapterForCommentViewPojo extends BaseAdapter {

    private ArrayList<CommentViewPojo> mCommentViewPojoList;
    private static LayoutInflater mInflater = null;
    private ImageLoader mImageLoader;

    public LazyAdapterForCommentViewPojo(Activity activity, ArrayList<CommentViewPojo> commentViewPojoList) {
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
        if (convertView == null) {
            vi = mInflater.inflate(R.layout.comment_list_row, null);
        }

        CommentViewPojo item = getItem(position);

        ImageView userPictureImageView = (ImageView) vi.findViewById(R.id.columnUserPictureForComment);
        TextView userNameTextView = (TextView) vi.findViewById(R.id.columnUserDisplayNameForComment);
        TextView commentStringTextView = (TextView) vi.findViewById(R.id.columnCommentStringForPlayerPage);

        mImageLoader.displayImage(item.PictureUrl, userPictureImageView);
        userNameTextView.setText(item.DisplayName);
        commentStringTextView.setText(Html.fromHtml(item.FormattedComment));

        return vi;
    }
}
