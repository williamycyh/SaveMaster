package com.savemaster.savefromfb;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import butterknife.ButterKnife;
import com.savemaster.savefromfb.local.history.HistoryRecordManager;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.InfoCache;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.SharedUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences defaultPreferences;
    // history
    private HistoryRecordManager recordManager;
    private CompositeDisposable disposables;
    // theme
    private String startThemeKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // history
        recordManager = new HistoryRecordManager(getActivity());
        disposables = new CompositeDisposable();

        // theme
        String themeKey = getString(R.string.theme_key);
        startThemeKey = defaultPreferences.getString(themeKey, getString(R.string.default_theme_value));
        findPreference(themeKey).setOnPreferenceChangeListener(themePreferenceChange);

        // rate app
        findPreference(getString(R.string.rate_me_now)).setOnPreferenceClickListener(preference -> {
//            SharedUtils.rateApp(getContext());
            MyCommon.googleRate(getActivity());
            return true;
        });

        // share
        findPreference(getString(R.string.tell_your_friend)).setOnPreferenceClickListener(preference -> {
            SharedUtils.shareUrl(getContext());
            return true;
        });

        // feedback
        findPreference(getString(R.string.feedback)).setOnPreferenceClickListener(preference -> {
            NavigationHelper.composeEmail(getContext(), getString(R.string.app_name) + " Android Feedback");
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gagtube_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        // remove divider
        setDivider(null);
        // versionName
        initVersion(getActivity());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        if (preference.getKey().equals(getString(R.string.metadata_cache_wipe_key))) {

            InfoCache.getInstance().clearCache();
            Toast.makeText(preference.getContext(), R.string.savemasterdown_metadata_cache_wipe_complete_notice, Toast.LENGTH_SHORT).show();
        }

        if (preference.getKey().equals(getString(R.string.clear_views_history_key))) {

            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.savemasterdown_confirm_title_dialog)
                    .setMessage(R.string.savemasterdown_delete_view_history_alert)
                    .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton(R.string.delete, ((dialog, which) -> {

                        final Disposable onDelete = recordManager.deleteWholeStreamHistory()
                                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                                        // onNext
                                        howManyDeleted -> Toast.makeText(getActivity(), R.string.savemasterdown_view_history_deleted, Toast.LENGTH_SHORT).show(),
                                        // onError
                                        throwable -> {});

                        final Disposable onClearOrphans = recordManager.removeOrphanedRecords()
                                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                                        // onNext
                                        howManyDeleted -> {
                                        },
                                        // onError
                                        throwable -> {});
                        disposables.add(onClearOrphans);
                        disposables.add(onDelete);
                    }))
                    .create()
                    .show();
        }

        if (preference.getKey().equals(getString(R.string.clear_search_history_key))) {

            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.savemasterdown_confirm_title_dialog)
                    .setMessage(R.string.savemasterdown_delete_search_history_alert)
                    .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton(R.string.delete, ((dialog, which) -> {
                        final Disposable onDelete = recordManager.removeOrphanedRecords()
                                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                                        // onNext
                                        howManyDeleted -> Toast.makeText(getActivity(), R.string.savemasterdown_search_history_deleted, Toast.LENGTH_SHORT).show(),
                                        // onError
                                        throwable -> {});
                        disposables.add(onDelete);
                    }))
                    .create()
                    .show();
        }

        return super.onPreferenceTreeClick(preference);
    }

    // theme
    private Preference.OnPreferenceChangeListener themePreferenceChange = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            defaultPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, true).apply();
            defaultPreferences.edit().putString(getString(R.string.theme_key), newValue.toString()).apply();

            if (!newValue.equals(startThemeKey) && getActivity() != null) {
                // If it's not the current theme
                getActivity().recreate();
            }
            return false;
        }
    };

    private void initVersion(Activity activity) {

        boolean debug = (0 != (activity.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        String versionName = BuildConfig.VERSION_NAME + "-" + (debug ? "DEBUG" : "PRODUCTION");
        findPreference(getString(R.string.version_name)).setSummary(versionName);
    }
}
