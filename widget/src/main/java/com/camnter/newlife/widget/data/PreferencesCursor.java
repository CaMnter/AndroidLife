package com.camnter.newlife.widget.data;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author CaMnter
 */

public class PreferencesCursor implements Cursor {

    private String stringValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private boolean booleanValue;


    public String getStringValue() {
        return this.stringValue;
    }


    public void setStringValue(final String stringValue) {
        this.stringValue = stringValue;
    }


    public int getIntValue() {
        return this.intValue;
    }


    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }


    public float getFloatValue() {
        return this.floatValue;
    }


    public void setFloatValue(final float floatValue) {
        this.floatValue = floatValue;
    }


    public long getLongValue() {
        return this.longValue;
    }


    public void setLongValue(final long longValue) {
        this.longValue = longValue;
    }


    public boolean isBooleanValue() {
        return this.booleanValue;
    }


    public void setBooleanValue(final boolean booleanValue) {
        this.booleanValue = booleanValue;
    }


    @Override
    public int getCount() {
        return 0;
    }


    @Override
    public int getPosition() {
        return 0;
    }


    @Override
    public boolean move(int offset) {
        return false;
    }


    @Override
    public boolean moveToPosition(int position) {
        return false;
    }


    @Override
    public boolean moveToFirst() {
        return false;
    }


    @Override
    public boolean moveToLast() {
        return false;
    }


    @Override
    public boolean moveToNext() {
        return false;
    }


    @Override
    public boolean moveToPrevious() {
        return false;
    }


    @Override
    public boolean isFirst() {
        return false;
    }


    @Override
    public boolean isLast() {
        return false;
    }


    @Override
    public boolean isBeforeFirst() {
        return false;
    }


    @Override
    public boolean isAfterLast() {
        return false;
    }


    @Override
    public int getColumnIndex(String columnName) {
        return 0;
    }


    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return 0;
    }


    @Override
    public String getColumnName(int columnIndex) {
        return null;
    }


    @Override
    public String[] getColumnNames() {
        return new String[0];
    }


    @Override
    public int getColumnCount() {
        return 0;
    }


    @Override
    public byte[] getBlob(int columnIndex) {
        return new byte[0];
    }


    @Override
    public String getString(int columnIndex) {
        return null;
    }


    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

    }


    @Override
    public short getShort(int columnIndex) {
        return 0;
    }


    @Override
    public int getInt(int columnIndex) {
        return 0;
    }


    @Override
    public long getLong(int columnIndex) {
        return 0;
    }


    @Override
    public float getFloat(int columnIndex) {
        return 0;
    }


    @Override
    public double getDouble(int columnIndex) {
        return 0;
    }


    @Override
    public int getType(int columnIndex) {
        return 0;
    }


    @Override
    public boolean isNull(int columnIndex) {
        return false;
    }


    @Override
    public void deactivate() {

    }


    @Override
    public boolean requery() {
        return false;
    }


    @Override
    public void close() {

    }


    @Override
    public boolean isClosed() {
        return false;
    }


    @Override
    public void registerContentObserver(ContentObserver observer) {

    }


    @Override
    public void unregisterContentObserver(ContentObserver observer) {

    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }


    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }


    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {

    }


    @Override
    public Uri getNotificationUri() {
        return null;
    }


    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }


    @Override
    public void setExtras(Bundle extras) {

    }


    @Override
    public Bundle getExtras() {
        return null;
    }


    @Override
    public Bundle respond(Bundle extras) {
        return null;
    }
}
