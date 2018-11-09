package com.hfs.library;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * @author HuangFusheng
 * @date 2018/11/8
 * @description SearchHistoryAdapter
 */
public class SearchHistoryAdapter extends BaseQuickAdapter<String,BaseViewHolder> {

    public SearchHistoryAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_item_search_history, item);
    }
}
