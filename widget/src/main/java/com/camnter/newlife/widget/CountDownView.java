package com.camnter.newlife.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Description：CountDownView
 * Created by：CaMnter
 */

public class CountDownView extends LinearLayout {

    private static final int DEFAULT_TIME_COLOR = 0xffFF4A3E;
    private static final int DEFAULT_COLON_COLOR = 0xffFF4A3E;
    private static final int DEFAULT_BORDER_COLOR = 0xffE5E5E5;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xffFFFFFF;

    private static final String ZERO_ZERO = "00";

    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final String TAG = CountDownView.class.getSimpleName();

    private int timeColor;
    private int colonColor;
    private int borderColor;
    private int backgroundColor;

    @BindView(R2.id.count_down_hour_text)
    TextView hourText;
    @BindView(R2.id.count_down_first_colon_text)
    TextView firstColonText;
    @BindView(R2.id.count_down_minute_text)
    TextView minuteText;
    @BindView(R2.id.exclusive_count_down_second_colon_text)
    TextView secondColonText;
    @BindView(R2.id.count_down_second_text)
    TextView secondText;

    private static final boolean LOG = false;

    private CountDownListener countDownListener;

    private static final int MSG = 0x26;

    private static class CountDownHandler extends Handler {

        private final WeakReference<CountDownView> countDownViewReference;

        public CountDownHandler(@NonNull final CountDownView countDownView) {
            this.countDownViewReference = new WeakReference<>(countDownView);
        }

        @Override
        public void handleMessage(Message msg) {
            final CountDownView exclusiveCountDownView = this.countDownViewReference.get();
            if (exclusiveCountDownView == null || exclusiveCountDownView.mCancelled) {
                return;
            }

            final long millisLeft = exclusiveCountDownView.mStopTimeInFuture - SystemClock.elapsedRealtime();

            if (millisLeft <= 0) {
                exclusiveCountDownView.log(TAG, "\t\t\t[CountDownHandler]\t\t\t[handleMessage]\t\t\t[millisLeft <= 0 :millisLeft = " + millisLeft + "]");
                // Nothing to do
            } else if (millisLeft < exclusiveCountDownView.mCountdownInterval) {
                exclusiveCountDownView.log(TAG, "\t\t\t[CountDownHandler]\t\t\t[handleMessage]\t\t\t[millisLeft < 1000 :millisLeft = " + millisLeft + "]");
                // no tick, just delay until done
                exclusiveCountDownView.onFinish();
                sendMessageDelayed(obtainMessage(MSG), millisLeft);
            } else {
                exclusiveCountDownView.log(TAG, "\t\t\t[CountDownHandler]\t\t\t[handleMessage]\t\t\t[millisLeft >= 1000 :millisLeft = " + millisLeft + "]");
                long lastTickStart = SystemClock.elapsedRealtime();
                exclusiveCountDownView.onTick(millisLeft);

                // take into account user's onTick taking time to execute
                long delay = lastTickStart + exclusiveCountDownView.mCountdownInterval - SystemClock.elapsedRealtime();

                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) delay += exclusiveCountDownView.mCountdownInterval;

                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }

    }

    private void log(String tag, String msg) {
        if (!LOG) return;
        Log.i(tag, msg);
    }

    private final Handler mHandler;
    private final Locale locale = Locale.getDefault();
    private static final String LESS_THAN_TEN_FORMAT = "%02d";
    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00"));


    public CountDownView(Context context) {
        super(context);
        this.mHandler = new CountDownHandler(this);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = new CountDownHandler(this);
        this.initAttributes(context, attrs);
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHandler = new CountDownHandler(this);
        this.initAttributes(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHandler = new CountDownHandler(this);
        this.initAttributes(context, attrs);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_count_down, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountDownView);
        this.timeColor = typedArray.getColor(R.styleable.CountDownView_countDownTimeColor, DEFAULT_TIME_COLOR);
        this.colonColor = typedArray.getColor(R.styleable.CountDownView_countDownColonColor, DEFAULT_COLON_COLOR);
        this.borderColor = typedArray.getColor(R.styleable.CountDownView_countDownBorderColor, DEFAULT_BORDER_COLOR);
        this.backgroundColor = typedArray.getColor(R.styleable.CountDownView_countDownBackgroundColor, DEFAULT_BACKGROUND_COLOR);
        typedArray.recycle();
    }

    /**
     * Finalize inflating a view from XML.  This is called as the last phase
     * of inflation, after all child views have been added.
     *
     * <p>Even if the subclass overrides onFinishInflate, they should always be
     * sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        this.hourText.setTextColor(this.timeColor);
        this.minuteText.setTextColor(this.timeColor);
        this.secondText.setTextColor(this.timeColor);

        this.firstColonText.setTextColor(this.colonColor);
        this.secondColonText.setTextColor(this.colonColor);

        GradientDrawable timeBackgroundDrawable = new GradientDrawable();
        timeBackgroundDrawable.setColor(this.backgroundColor);
        timeBackgroundDrawable.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f,
            this.getResources().getDisplayMetrics()), this.borderColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.hourText.setBackground(timeBackgroundDrawable);
            this.minuteText.setBackground(timeBackgroundDrawable);
            this.secondText.setBackground(timeBackgroundDrawable);
        } else {
            this.hourText.setBackgroundDrawable(timeBackgroundDrawable);
            this.minuteText.setBackgroundDrawable(timeBackgroundDrawable);
            this.secondText.setBackgroundDrawable(timeBackgroundDrawable);
        }

        this.resetTimeAndState();
    }


    public synchronized final void setTime(final long timeMillis) {
        this.mMillisInFuture = timeMillis;
    }


    /**
     * Millis since epoch when alarm should stop.
     */
    private volatile long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private long mCountdownInterval = 1000L;

    private long mStopTimeInFuture;

    /**
     * boolean representing if the timer was cancelled
     */
    private volatile boolean mCancelled = false;

    private volatile boolean runningState = false;


    /**
     * Cancel the countdown.
     */
    public synchronized final void cancel() {
        if (!this.runningState) return;
        this.runningState = false;
        this.mCancelled = true;
        this.mHandler.removeMessages(MSG);
    }

    /**
     * Start the countdown.
     */
    public synchronized final void start() {
        if (runningState) return;
        this.runningState = true;
        this.mCancelled = false;
        if (this.mMillisInFuture <= 0) {
            onTimeError();
        }
        this.mStopTimeInFuture = SystemClock.elapsedRealtime() + this.mMillisInFuture;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG));
    }


    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    private void onTick(long millisUntilFinished) {
        this.calendar.setTimeInMillis(millisUntilFinished);
        final int timeHour = calendar.get(Calendar.HOUR_OF_DAY);
        this.hourText.setText(String.format(locale, LESS_THAN_TEN_FORMAT, this.checkCalendarHour(millisUntilFinished, timeHour)));
        this.minuteText.setText(String.format(locale, LESS_THAN_TEN_FORMAT, this.calendar.get(Calendar.MINUTE)));
        this.secondText.setText(String.format(locale, LESS_THAN_TEN_FORMAT, this.calendar.get(Calendar.SECOND)));
    }

    private int checkCalendarHour(final long millisInFuture, int calendarHour) {
        final int days = (int) (millisInFuture / ONE_DAY);
        if (days >= 1) {
            calendarHour += days * 24;
        }
        this.log(TAG, "\t\t\t[checkCalendarHour]\t\t\t[days = " + days + "]\t\t\t[calendarHour = " + calendarHour + "]");
        return calendarHour;
    }

    public void setCountDownListener(CountDownListener countDownListener) {
        this.countDownListener = countDownListener;
    }

    /**
     * Callback fired when the time is up.
     */
    private void onFinish() {
        this.resetTimeAndState();
        Log.i(TAG, "\t\t\t[onFinish]");
        if (this.countDownListener == null) return;
        this.countDownListener.onCountDownCompleted();
    }


    /**
     * Callback fired when the time is error.
     */
    private void onTimeError() {
        this.resetTimeAndState();
        if (this.countDownListener == null) return;
        this.countDownListener.onCountDownTimeError();
    }

    private void resetTimeAndState() {
        this.runningState = false;
        this.hourText.setText(ZERO_ZERO);
        this.minuteText.setText(ZERO_ZERO);
        this.secondText.setText(ZERO_ZERO);
    }


    public interface CountDownListener {

        /**
         * When count down time error
         */
        void onCountDownTimeError();

        /**
         * When count down completed
         */
        void onCountDownCompleted();

    }

}
