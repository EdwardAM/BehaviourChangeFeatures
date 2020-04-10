package com.example.ikoala.ui.social;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.ikoala.R;
import com.example.ikoala.adapters.LocalOpportunitiesPageAdapter;
import com.example.ikoala.database.DatabaseManager;
import com.example.ikoala.database.PhysicalActivity;
import com.example.ikoala.ui.ParentActivity;
import com.example.ikoala.utils.ColorUtils;
import com.google.android.material.tabs.TabLayout;


public class LocalOpportunitiesActivity extends ParentActivity
{

    private static final String TAG = LocalOpportunitiesActivity.class.getSimpleName();

    //Standard view variables
    private PhysicalActivity mActivity;
    private ActionBar mActionBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    LocalOpportunitiesTabSearchFragment searchTab;
    LocalOpportunitiesTabMapFragment mapTab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_opportunities);

        mActivity = getIntent().getParcelableExtra("activity");

        setupViewUI();

        viewPager = findViewById(R.id.tab_container);
        LocalOpportunitiesPageAdapter adapter = new LocalOpportunitiesPageAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        Bundle bundle = new Bundle();
        searchTab = new LocalOpportunitiesTabSearchFragment();
        mapTab = new LocalOpportunitiesTabMapFragment();

        bundle.putString("customSearchQuery", mActivity.getCustomSearchQuery());
        bundle.putString("mapSearchTerm", mActivity.getMapSearchTerm());

        //Getting the user postcode
        DatabaseManager.getUser(mFirebaseAuth.getUid(),
                user ->
                {
                    String postcode = user.getLocation();
                    bundle.putString("postcode", postcode);

                    //Setting up tabs with data
                    searchTab.setArguments(bundle);
                    mapTab.setArguments(bundle);
                    adapter.addFragment(searchTab, "Links");
                    adapter.addFragment(mapTab, "Map");
                    viewPager.setAdapter(adapter);

                    tabLayout = findViewById(R.id.local_opportunity_tabs);
                    tabLayout.setupWithViewPager(viewPager);
                }
        );
    }

    @Override
    protected void setupViewUI()
    {
        mActionBar = getSupportActionBar();
        if (mActionBar != null)
        {
            mActionBar.show();
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
            String color = ColorUtils.getColorHex(this, R.attr.themeColorHighlight);
            mActionBar.setTitle(HtmlCompat.fromHtml("<font color=\"" + color + "\">" + mActivity.getName() + " opportunities</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            mActionBar.setBackgroundDrawable(new ColorDrawable(ColorUtils.getColor(this, R.attr.themeColorSecondary)));
        }
    }
    @Override
    public void onBackPressed()
    {
        super.onDefaultBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}
