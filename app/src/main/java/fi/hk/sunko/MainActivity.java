package fi.hk.sunko;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import org.json.JSONArray;

/**
 * The main activity of the application.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity implements WeatherFragment.BGHelper {
    FragmentPagerAdapter adapterViewPager;
    ShapeDrawable background;
    JSONArray forecast;
    SharedPreferences prefs;
    final String tempFormatKey = "fi.hk.sunko.tempformat";


    /**
     * Checks wether or not permission has been given to use location services.
     *
     * @param context application context
     * @return true if permissions are given, false if not
     */
    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Override method for the onCreate lifecycle method. Sets up the defaults for the app, as well
     * as the view pager.
     *
     * @param savedInstanceState saved instance state of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getSharedPreferences("fi.hk.sunko", Context.MODE_PRIVATE);
        prefs.getString(tempFormatKey, "c");

        setStatusBarTranslucent(true);

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        // Attach the page change listener inside the activity
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            /**
             * Invoked when a new page is selected.
             * @param position the newly selected page
             */
            @Override
            public void onPageSelected(int position) {
                ImageView wbg1 = (ImageView) findViewById(R.id.primaryBG);
                ImageView wbg2 = (ImageView) findViewById(R.id.secondaryBG);
                ImageView fbg = (ImageView) findViewById(R.id.forecastBG);

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
                }
            }

            /**
             * Invoked when the current page is scrolled.
             *
             * @param position the position on the page
             * @param positionOffset position's offset
             * @param positionOffsetPixels position's offset in pixels
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ImageView wbg1 = (ImageView) findViewById(R.id.primaryBG);
                ImageView wbg2 = (ImageView) findViewById(R.id.secondaryBG);
                ImageView fbg = (ImageView) findViewById(R.id.forecastBG);

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
                }
            }

            /**
             * Called when scroll state changes.
             *
             * @param state current scroll state
             */
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
    }

    /**
     * Toggles the transparency of the status bar.
     *
     * @param makeTranslucent boolean value to determine transparency
     */
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Override method for the background helper interface, sets the background.
     *
     * @param drawable background drawable sent by the weather fragment
     */
    @Override
    public void setBG(ShapeDrawable drawable) {
        background = drawable;
    }

    /**
     * Override method for the background helper interface, returns the background drawable.
     *
     * @return the background drawable
     */
    @Override
    public ShapeDrawable getBG() {
        return background;
    }

    @Override
    public void setForecast(JSONArray forecast) {
        this.forecast = forecast;
        FragmentManager fm = getSupportFragmentManager();
        ForecastFragment ff = (ForecastFragment)fm.findFragmentById(R.id.forecastFragment);
        if (ff != null) {
            ff.setForecast(forecast);
            ff.adapter.notifyDataSetChanged();

        }
    }

    /**
     * Override method for the background helper interface, returns the forecast data.
     *
     * @return the forecast data
     */
    @Override
    public JSONArray getForecast() {
        return forecast;
    }

    /**
     * View pager adapter, enables swiping between fragments.
     */
    private static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        private MyPagerAdapter(FragmentManager fragmentManager) {
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
                    return WeatherFragment.newInstance(0, "Page # 1");
                case 1:
                    return ForecastFragment.newInstance(1, "Page # 2");
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
