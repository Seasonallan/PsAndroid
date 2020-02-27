package com.season.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.book.R;
import com.season.example.popwindow.CatalogView;

import java.util.ArrayList;

public abstract class CatalogViewPagerAdapter  extends BaseViewPagerTabHostAdapter {
    public ArrayList<String> mTags;
    private Context mContext;

    public CatalogViewPagerAdapter(Context context, ArrayList<String> tags){
        mContext = context;
        mTags = tags;
        if(mTags == null){
            mTags = new ArrayList<String>();
        }
    }

    @Override
    public View getIndicator(int position) {
        View indicatorView = null;
        String tag = getTab(position);
        if(tag.equals(CatalogView.TAG_CATALOG)){
            indicatorView = newIndicator(R.string.btn_text_catalog);
            //indicatorView.setBackgroundResource(R.drawable.ic_reader_catalog_item_bg);
        }else if(tag.equals(CatalogView.TAG_DIGEST)){
            indicatorView = newIndicator(R.string.btn_text_bookdigest);
           // indicatorView.setBackgroundResource(R.drawable.ic_reader_catalog_item_bg);
        }else if(tag.equals(CatalogView.TAG_BOOKMARK)){
            indicatorView = newIndicator(R.string.btn_text_bookmark);
        }
        return indicatorView;
    }

    protected View newIndicator(int strResID) {
        TextView tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.tab_item_with_icon, null);
        tv.setId(android.R.id.title);
        tv.setText(strResID);
        return tv;
    };

    @Override
    public String getTab(int position) {
        return mTags.get(position);
    }

    @Override
    public int getCount() {
        return mTags.size();
    }

    @Override
    public View getItemView(ViewGroup container, int position) {
        final String tag = getTab(position);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.reader_catalog_tab_item_lay, null);
        final ListView mListView = contentView.findViewById(R.id.reader_catalog_lv);
        mListView.setAdapter(getAdapter(tag));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onItemClicked(tag, mListView, position);
            }
        });
        return contentView;
    }

    public abstract ListAdapter getAdapter(String tag);


    public abstract void onItemClicked(String tag, ListView mListView, int position);

}