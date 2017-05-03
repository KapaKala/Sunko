package fi.hk.sunko;

import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.rd.PageIndicatorView;
import com.rd.animation.AnimationType;

/**
 * The main view of the application.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends FragmentActivity implements WeatherFragment.BGHelper {
    FragmentPagerAdapter adapterViewPager;
    ShapeDrawable background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(1);

        PageIndicatorView pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(vpPager);
        pageIndicatorView.setAnimationType(AnimationType.SCALE);
        pageIndicatorView.setSelection(1);

        // Attach the page change listener inside the activity
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                ImageView wbg1 = (ImageView) findViewById(R.id.primaryBG);
                ImageView wbg2 = (ImageView) findViewById(R.id.secondaryBG);
                ImageView fbg = (ImageView) findViewById(R.id.forecastBG);
                ImageView sbg = (ImageView) findViewById(R.id.settingsBG);

                if (background != null) {
                    if (wbg1 != null) {
                        wbg1.setBackground(background);
                    }
                    if (wbg2 != null) {
                        wbg2.setBackground(background);
                    }
                    if (fbg != null) {
                        fbg.setBackground(background);
                    }
                    if (sbg != null) {
                        sbg.setBackground(background);
                    }
                }

            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ImageView wbg1 = (ImageView) findViewById(R.id.primaryBG);
                ImageView wbg2 = (ImageView) findViewById(R.id.secondaryBG);
                ImageView fbg = (ImageView) findViewById(R.id.forecastBG);
                ImageView sbg = (ImageView) findViewById(R.id.settingsBG);

                if (background != null) {
                    if (wbg1 != null) {
                        wbg1.setBackground(background);
                    }
                    if (wbg2 != null) {
                        wbg2.setBackground(background);
                    }
                    if (fbg != null) {
                        fbg.setBackground(background);
                    }
                    if (sbg != null) {
                        sbg.setBackground(background);
                    }
                }

            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
    }

    @Override
    public void setBG(ShapeDrawable drawable) {
        background = drawable;
    }

    @Override
    public ShapeDrawable getBG() {
        return null;
    }


    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SettingsFragment.newInstance(0, "Page # 1");
                case 1:
                    return WeatherFragment.newInstance(1, "Page # 3");
                case 2:
                    return ForecastFragment.newInstance(2, "Page # 2");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

}
