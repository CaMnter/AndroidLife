package com.camnter.newlife.ui.activity.smartrouter;

import android.os.Bundle;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartrouter.SmartRouters;
import com.camnter.smartrouter.annotation.RouterField;
import com.camnter.smartrouter.annotation.RouterHost;

/**
 * @author CaMnter
 */

@RouterHost("router-0x01")
public class SmartRouterSimpleActivity extends BaseAppCompatActivity {

    @RouterField("1")
    char exampleChar;
    @RouterField("2")
    byte exampleByte;
    @RouterField("3")
    short exampleShort;
    @RouterField("2233")
    int exampleInt;
    @RouterField("22.331")
    float exampleFloat;
    @RouterField("22.332")
    double exampleDouble;
    @RouterField("2333")
    long exampleLong;
    @RouterField("true")
    boolean exampleBoolean;

    @RouterField("4")
    Character exampleBoxedCharacter;
    @RouterField("5")
    Byte exampleBoxedByte;
    @RouterField("6")
    Short exampleBoxedShort;
    @RouterField("12233")
    Integer exampleBoxedInteger;
    @RouterField("122.331")
    Float exampleBoxedFloat;
    @RouterField("122.332")
    Double exampleBoxedDouble;
    @RouterField("12333")
    Long exampleBoxedLong;
    @RouterField("true")
    Boolean exampleBoxedBoolean;
    @RouterField("CaMnter")
    String exampleBoxedString;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_router_simple;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        SmartRouters.running(this);
        ((TextView) this.findViewById(R.id.smart_router_simple_example_char_text)).setText(
            String.valueOf(this.exampleChar));
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {

    }

}
