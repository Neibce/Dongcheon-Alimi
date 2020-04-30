package me.tyoj.dcalimi;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.pm10.library.CircleIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainFragment extends Fragment {
    private int dateOffset;
    private String mSelYear;
    private String mSelMonth;
    private String mSelDate;
    private String mSelDay;

    private TouchDelegateComposite mTouchDelegateComposite;
    private ViewPager mViewPager;
    private BusInfo mBusInfo;

    private FragmentManager mFragmentManager;
    private Context mContext;

    private void setDateValues(Calendar calendar){
        mSelYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.getTime());
        mSelMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(calendar.getTime());
        mSelDate = new SimpleDateFormat("dd", Locale.getDefault()).format(calendar.getTime());
        mSelDay = new SimpleDateFormat("E", Locale.getDefault()).format(calendar.getTime());
    }

    private void refreshViewPager(){
        mViewPager.setCurrentItem(0);
        SchoolMealViewPagerAdapter schoolMealViewPagerAdapter = new SchoolMealViewPagerAdapter(getChildFragmentManager(), mSelYear, mSelMonth, mSelDate);
        mViewPager.setAdapter(schoolMealViewPagerAdapter);
        schoolMealViewPagerAdapter.notifyDataSetChanged();
    }

    private void increaseImageButtonArea(final ImageButton button) {
        View parent = (View) button.getParent();

        parent.post(new Runnable() {
            public void run() {
                Rect rect = new Rect();
                button.getHitRect(rect);
                rect.top -= 25;
                rect.left -= 25;
                rect.bottom += 25;
                rect.right += 25;
                mTouchDelegateComposite.addDelegate(new TouchDelegate(rect, button));
            }
        });
        parent.setTouchDelegate(mTouchDelegateComposite);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mFragmentManager = getParentFragmentManager();
        mContext = getContext();

        MyDate myDate = new MyDate();
        TextView tvDDayTitle = view.findViewById(R.id.tvDDayTitle);
        TextView tvDDayLeft = view.findViewById(R.id.tvDDayLeft);

        Pair<String, Long> pairDDay= new SchoolExam(mFragmentManager, mContext).getDDay(myDate.getYear(), myDate.getMonth(), myDate.getDate());
        if(pairDDay != null && pairDDay.second != null) {
            tvDDayTitle.setText(String.format(getString(R.string.d_day_title), pairDDay.first));
            if(pairDDay.second == 0)
                tvDDayLeft.setText(getString(R.string.d_day));
            else
                tvDDayLeft.setText(String.format(getString(R.string.d_day_left), pairDDay.second));
        }

        ImageButton btnBusInfoRefresh = view.findViewById(R.id.btnBusInfoRefresh);
        mBusInfo = new BusInfo(getParentFragmentManager(), getContext(), view);
        mBusInfo.get();
        btnBusInfoRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mBusInfo.get();
            }
        });

        mViewPager = view.findViewById(R.id.viewPagerMeal);

        final Calendar calendarMealSelected = Calendar.getInstance();
        dateOffset = 0;
        if (calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            dateOffset = 2;
        else if (calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            dateOffset = 1;
        calendarMealSelected.add(Calendar.DATE, dateOffset);
        setDateValues(calendarMealSelected);
        refreshViewPager();

        CircleIndicator circleIndicator = view.findViewById(R.id.indicatorMeal);
        circleIndicator.setupWithViewPager(mViewPager);

        final TextView tvMealDate = view.findViewById(R.id.tvMealDate);
        tvMealDate.setText(String.format(Locale.getDefault(),"%s월 %s일 (%s)", mSelMonth, mSelDate, mSelDay));

        final ImageButton btnMealDateAfter = view.findViewById(R.id.btnMealDateAfter);
        final ImageButton btnMealDateBefore = view.findViewById(R.id.btnMealDateBefore);

        btnMealDateBefore.setEnabled(false);
        btnMealDateBefore.setVisibility(View.INVISIBLE);

        btnMealDateBefore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btnMealDateAfter.setEnabled(true);
                btnMealDateAfter.setVisibility(View.VISIBLE);

                int amount = -1;
                if (calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
                    amount = -3;

                calendarMealSelected.add(Calendar.DATE, amount);
                dateOffset += amount;

                Log.d("FM", "" + dateOffset);

                if(calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && dateOffset <= 2 || dateOffset <= 0){
                    btnMealDateBefore.setEnabled(false);
                    btnMealDateBefore.setVisibility(View.INVISIBLE);
                }

                setDateValues(calendarMealSelected);
                refreshViewPager();

                tvMealDate.setText(String.format(Locale.getDefault(),"%s월 %s일 (%s)", mSelMonth, mSelDate, mSelDay));
            }
        });

        btnMealDateAfter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btnMealDateBefore.setEnabled(true);
                btnMealDateBefore.setVisibility(View.VISIBLE);

                int amount = 1;
                if (calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
                    amount = 3;

                calendarMealSelected.add(Calendar.DATE, amount);
                dateOffset += amount;

                Log.d("FM", "" + dateOffset);

                if((calendarMealSelected.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && dateOffset >= 12) || dateOffset >= 14){
                    btnMealDateAfter.setEnabled(false);
                    btnMealDateAfter.setVisibility(View.INVISIBLE);
                }

                setDateValues(calendarMealSelected);
                refreshViewPager();

                tvMealDate.setText(String.format(Locale.getDefault(),"%s월 %s일 (%s)", mSelMonth, mSelDate, mSelDay));
            }
        });

        mTouchDelegateComposite = new TouchDelegateComposite((View)btnMealDateAfter.getParent());
        increaseImageButtonArea(btnMealDateBefore);
        increaseImageButtonArea(btnMealDateAfter);

        return view;
    }
}
