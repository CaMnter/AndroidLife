package com.camnter.newlife.ui.activity.smartrouter;

import android.os.Bundle;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartrouter.SmartRouters;
import com.camnter.smartrouter.annotation.RouterField;
import com.camnter.smartrouter.annotation.RouterPath;

/**
 * @author CaMnter
 */

@RouterPath("router-0x01")
public class SmartRouterSampleActivity extends BaseAppCompatActivity {

    @RouterField("char")
    char exampleChar;
    @RouterField("byte")
    byte exampleByte;
    @RouterField("short")
    short exampleShort;
    @RouterField("int")
    int exampleInt;
    @RouterField("float")
    float exampleFloat;
    @RouterField("double")
    double exampleDouble;
    @RouterField("long")
    long exampleLong;
    @RouterField("boolean")
    boolean exampleBoolean;

    @RouterField("boxedCharacter")
    Character exampleBoxedCharacter;
    @RouterField("boxedByte")
    Byte exampleBoxedByte;
    @RouterField("boxedShort")
    Short exampleBoxedShort;
    @RouterField("boxedInteger")
    Integer exampleBoxedInteger;
    @RouterField("boxedFloat")
    Float exampleBoxedFloat;
    @RouterField("boxedDouble")
    Double exampleBoxedDouble;
    @RouterField("boxedLong")
    Long exampleBoxedLong;
    @RouterField("boxedBoolean")
    Boolean exampleBoxedBoolean;
    @RouterField("boxedString")
    String exampleBoxedString;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_router_sample;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        SmartRouters.setFieldValue(this);
        String stringBuilder = "exampleChar = " +
            String.valueOf(this.exampleChar) +
            "\n" +
            "exampleByte = " +
            String.valueOf(this.exampleByte) +
            "\n" +
            "exampleShort = " +
            String.valueOf(this.exampleShort) +
            "\n" +
            "exampleInt = " +
            String.valueOf(this.exampleInt) +
            "\n" +
            "exampleFloat = " +
            String.valueOf(this.exampleFloat) +
            "\n" +
            "exampleDouble = " +
            String.valueOf(this.exampleDouble) +
            "\n" +
            "exampleLong = " +
            String.valueOf(this.exampleLong) +
            "\n" +
            "exampleBoolean = " +
            String.valueOf(this.exampleBoolean) +
            "\n" +
            "exampleBoxedCharacter = " +
            String.valueOf(this.exampleBoxedCharacter) +
            "\n" +
            "exampleBoxedByte = " +
            String.valueOf(this.exampleBoxedByte) +
            "\n" +
            "exampleBoxedShort = " +
            String.valueOf(this.exampleBoxedShort) +
            "\n" +
            "exampleBoxedInteger = " +
            String.valueOf(this.exampleBoxedInteger) +
            "\n" +
            "exampleBoxedFloat = " +
            String.valueOf(this.exampleBoxedFloat) +
            "\n" +
            "exampleBoxedDouble = " +
            String.valueOf(this.exampleBoxedDouble) +
            "\n" +
            "exampleBoxedLong = " +
            String.valueOf(this.exampleBoxedLong) +
            "\n" +
            "exampleBoxedBoolean = " +
            String.valueOf(this.exampleBoxedBoolean) +
            "\n" +
            "exampleBoxedString = " +
            String.valueOf(this.exampleBoxedString);
        ((TextView) this.findViewById(R.id.smart_router_sample_example_text))
            .setText(stringBuilder);
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
