package com.jimg.scoutingapp.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.pojos.FlaggedCommentPojo;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Jim on 4/13/2014.
 */
public class FlaggedCommentViewAdapter extends BaseAdapter {

    static class ViewHolderItem {
        Integer commentId;
        String rawComment;

        @InjectView(R.id.columnFormattedCommentString) TextView formattedCommentTextView;
        @InjectView(R.id.columnNumberOfFlags) TextView flagsTextView;

        private ViewHolderItem(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private Context mContext;
    private ArrayList<FlaggedCommentPojo> mFlaggedCommentPojoList;
    private static LayoutInflater mInflater = null;
    public Integer mCurrentlySelectedCommentId = 0;

    public FlaggedCommentViewAdapter(Context context, ArrayList<FlaggedCommentPojo> flaggedCommentPojoList) {
        this.mContext = context;
        this.mFlaggedCommentPojoList = flaggedCommentPojoList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

            viewHolder = new ViewHolderItem(vi);
            vi.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) vi.getTag();
        }

        FlaggedCommentPojo item = getItem(position);
        viewHolder.formattedCommentTextView.setText(Html.fromHtml(item.formattedComment));
        viewHolder.flagsTextView.setText(item.numberOfFlags.toString());

        return vi;
    }
}
