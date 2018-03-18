package com.camnter.content.provider.plugin.host;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * @author CaMnter
 */

@SuppressWarnings("DanglingJavadoc")
public class ContentProviderPluginActivity extends BaseAppCompatActivity
    implements View.OnClickListener {

    private int count = 0;
    private Uri pluginUri;
    private ContentResolver resolver;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.findView(R.id.query_plugin_content_provider).setOnClickListener(this);
        this.findView(R.id.insert_plugin_content_provider).setOnClickListener(this);
        this.findView(R.id.delete_plugin_content_provider).setOnClickListener(this);

        /**
         * 插件 uri
         * content://com.camnter.content.provider.plugin.plugin.PluginContentProvider
         *
         * 实际 uri 得写成
         * content://com.camnter.content.provider.plugin.host.StubContentProvider/com.camnter.content.provider.plugin.plugin.PluginContentProvider
         */
        this.pluginUri = Uri.parse(StubContentProvider.STUB_URI.toString() + '/' +
            "com.camnter.content.provider.plugin.plugin.PluginContentProvider");
        this.resolver = this.getContentResolver();
    }


    @Override
    protected void initListeners() {

    }


    @Override
    protected void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_plugin_content_provider:
                final Cursor result = this.resolver.query(this.pluginUri, null, null, null, null);
                if (result == null) return;
                final StringBuilder stringBuilder = new StringBuilder();
                for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                    final int id = result.getInt(result.getColumnIndex("_id"));
                    final String content = result.getString(result.getColumnIndex("content"));
                    stringBuilder.append("_id = ")
                        .append(id)
                        .append(", content = ")
                        .append(content)
                        .append(";");
                }
                result.close();
                if (stringBuilder.length() > 0) {
                    final String info = stringBuilder.substring(0, stringBuilder.length() - 1);
                    ToastUtils.show(this, info, Toast.LENGTH_LONG);
                }
                break;
            case R.id.insert_plugin_content_provider:
                final ContentValues values = new ContentValues();
                values.put("content", "Save you from anything - " + ++this.count);
                this.resolver.insert(this.pluginUri, values);
                ToastUtils.show(this, "insert successfully", Toast.LENGTH_LONG);
                break;
            case R.id.delete_plugin_content_provider:
                this.resolver.delete(this.pluginUri, null, null);
                ToastUtils.show(this, "delete successfully", Toast.LENGTH_LONG);
                break;
        }
    }

}
