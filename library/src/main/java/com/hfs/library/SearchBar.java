package com.hfs.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

/**
 * @author HuangFusheng
 * @date 2018/11/8
 * @description 搜索控件
 */
public class SearchBar extends LinearLayout implements View.OnClickListener, BaseQuickAdapter.OnItemClickListener {

    private static final String TAG = "SearchBar";

    private Context mContext;

    private ImageView mIvBack;
    private TextView mTvCancel;
    private RecyclerView mRvSearchBar;
    private TextView mTvSearchHistory;
    private ClearEditText mClearEditText;
    private View mFootView;
    private TextView mTvClearSearchContent;

    private BackCallBackListener mBackCallBackListener;
    private SearchCallBackListener mSearchCallBackListener;

    private SearchHistoryAdapter mSearchHistoryAdapter;
    private List<String> mSearchTitles = new ArrayList<>();

    private RecordSQLiteOpenHelper mRecordSQLiteOpenHelper;
    private SQLiteDatabase mSQLiteDatabase;

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        init();
    }

    private void init() {
        initView();

        mRecordSQLiteOpenHelper = new RecordSQLiteOpenHelper(mContext);

        //查询所有的历史搜索记录
        queryData("");


        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        mClearEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    String content = mClearEditText.getText().toString().trim();

                    if (mSearchCallBackListener != null) {
                        mSearchCallBackListener.onSearchAction(content);
                    }
                    // 点击搜索键后，对该搜索字段在数据库是否存在进行检查（查询
                    boolean hasData = hasData(content);
                    // 3. 若存在，则不保存；若不存在，则将该搜索字段保存（插入）到数据库，并作为历史搜索记录
                    if (!hasData) {
                        insertData(content);
                        queryData("");
                    }
                }
                return false;
            }
        });


        /**
         * 搜索框的文本变化实时监听
         */
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                // 每次输入后，模糊查询数据库 & 显示
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
                String tempName = mClearEditText.getText().toString().trim();
                queryData(tempName);

            }
        });

    }

    /**
     * 插入数据
     *
     * @param content
     */
    private void insertData(String content) {
        mSQLiteDatabase = mRecordSQLiteOpenHelper.getWritableDatabase();
        mSQLiteDatabase.execSQL("insert into records(name) values('" + content + "')");
        mSQLiteDatabase.close();
    }

    /**
     * 查询数据
     *
     * @param content
     */
    private void queryData(String content) {

        Cursor cursor = mRecordSQLiteOpenHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name like '%" + content + "%' order by id desc ", null);

        mSearchTitles.clear();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            mSearchTitles.add(name);
        }

        mSearchHistoryAdapter.notifyDataSetChanged();

        // 当输入框为空 & 数据库中有搜索记录时，显示 "删除搜索记录"按钮
        if (mSearchTitles.size() > 0) {
            mTvSearchHistory.setVisibility(VISIBLE);
            mSearchHistoryAdapter.removeFooterView(mFootView);
            mSearchHistoryAdapter.addFooterView(mFootView);
        } else {
            mSearchHistoryAdapter.removeFooterView(mFootView);
            mTvSearchHistory.setVisibility(GONE);
        }
    }

    /**
     * 删除数据
     */
    private void deleteData() {
        mSQLiteDatabase = mRecordSQLiteOpenHelper.getWritableDatabase();
        mSQLiteDatabase.execSQL("delete from records");
        mSQLiteDatabase.close();

        mSearchHistoryAdapter.removeFooterView(mFootView);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_search_bar, this);

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(this);
        mRvSearchBar = findViewById(R.id.rv_search_bar);
        mTvSearchHistory = findViewById(R.id.tv_search_history);
        mClearEditText = findViewById(R.id.et_clear_content);

        mSearchHistoryAdapter = new SearchHistoryAdapter(R.layout.item_search_history, mSearchTitles);
        mRvSearchBar.setLayoutManager(new LinearLayoutManager(mContext));
        mRvSearchBar.setAdapter(mSearchHistoryAdapter);
        mSearchHistoryAdapter.setOnItemClickListener(this);

        mFootView = LayoutInflater.from(mContext).inflate(R.layout.item_search_foot, null);
        mTvClearSearchContent = mFootView.findViewById(R.id.tv_clear_search_content);
        mTvClearSearchContent.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            if (mBackCallBackListener != null) {
                mBackCallBackListener.onBackAction();
            }
        } else if (v.getId() == R.id.tv_cancel) {
            if (mBackCallBackListener != null) {
                mBackCallBackListener.onBackAction();
            }
        } else if (v.getId() == R.id.tv_clear_search_content) {
            // 清空数据库
            deleteData();
            // 模糊搜索空字符 = 显示所有的搜索历史（此时是没有搜索记录的）
            queryData("");
        }
    }


    public void setBackCallBackListener(BackCallBackListener backCallBackListener) {
        mBackCallBackListener = backCallBackListener;
    }

    public void setSearchCallBackListener(SearchCallBackListener searchCallBackListener) {
        mSearchCallBackListener = searchCallBackListener;
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (mSearchCallBackListener != null) {
            mSearchCallBackListener.onSearchAction(mSearchTitles.get(position));
        }

        if (mBackCallBackListener != null) {
            mBackCallBackListener.onBackAction();
        }
    }

    /**
     * 判断数据库中是否有数据
     *
     * @param content
     * @return
     */
    public boolean hasData(String content) {
        // 从数据库中Record表里找到name=tempName的id
        Cursor cursor = mRecordSQLiteOpenHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name =?", new String[]{content});
        //  判断是否有下一个
        return cursor.moveToNext();
    }
}
