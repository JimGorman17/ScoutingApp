package com.jimg.scoutingapp.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.pojos.TopUserPojo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Jim on 4/15/2014.
 */
public class TopUserViewAdapter extends BaseAdapter {

    static class ViewHolderItem {

        @InjectView(R.id.columnUserPicture) ImageView userPictureImageView;
        @InjectView(R.id.columnUserInfo) TextView userInfoTextView;
        @InjectView(R.id.columnCommentCount) TextView commentCountTextView;

        private ViewHolderItem(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private MainActivity mMainActivity;
    private ArrayList<TopUserPojo> mTopUserPojoList;
    private static LayoutInflater mInflater = null;
    private ImageLoader mImageLoader;

    public TopUserViewAdapter(MainActivity mainActivity, ArrayList<TopUserPojo> topUserPojoList) {
        this.mMainActivity = mainActivity;
        this.mTopUserPojoList = topUserPojoList;
        mInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return mTopUserPojoList.size();
    }

    @Override
    public TopUserPojo getItem(int position) {
        return mTopUserPojoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolderItem viewHolder;

        if (vi == null) {
            vi = mInflater.inflate(R.layout.top_users_list_row, null);
            viewHolder = new ViewHolderItem(vi);
            vi.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) vi.getTag();
        }

        TopUserPojo item = getItem(position);
        mImageLoader.displayImage(item.pictureUrl, viewHolder.userPictureImageView);
        viewHolder.userInfoTextView.setText(Html.fromHtml(item.userInfo));
        viewHolder.commentCountTextView.setText(item.commentCount.toString());

        return vi;
    }
}
