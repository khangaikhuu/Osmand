package net.osmand.plus.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.osmand.aidl.OsmandAidlApi.ConnectedApp;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.PluginActivity;
import net.osmand.plus.views.SwitchFragmentPreference;

import java.util.List;

import static net.osmand.plus.settings.profiles.SettingsProfileFragment.PROFILE_STRING_KEY;

public class SettingsConfigureProfileFragment extends SettingsBaseProfileDependentFragment {

	public static final String TAG = "SettingsConfigureProfileFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		return view;
	}

	@Override
	protected int getPreferenceResId() {
		return R.xml.configure_profile;
	}

	@Override
	protected int getToolbarResId() {
		return R.layout.profile_preference_toolbar_big;
	}

	protected String getToolbarTitle() {
		return getString(R.string.configure_profile);
	}

	@Override
	protected void createUI() {
		PreferenceScreen screen = getPreferenceScreen();

		List<ConnectedApp> connectedApps = getMyApplication().getAidlApi().getConnectedApps();
		List<OsmandPlugin> plugins = OsmandPlugin.getVisiblePlugins();

		for (ConnectedApp connectedApp : connectedApps) {
			SwitchPreference preference = new SwitchPreference(getContext());

			preference.setKey(connectedApp.getPack());
			preference.setTitle(connectedApp.getName());
			preference.setIcon(connectedApp.getIcon());
			preference.setChecked(connectedApp.isEnabled());
			preference.setLayoutResource(R.layout.preference_fragment_and_switch);
			preference.setOnPreferenceChangeListener(this);

			screen.addPreference(preference);
		}
		for (OsmandPlugin plugin : plugins) {
			SwitchFragmentPreference preference = new SwitchFragmentPreference(getContext());
			preference.setKey(plugin.getId());
			preference.setPersistent(false);
			preference.setTitle(plugin.getName());
			preference.setIcon(getContentIcon(plugin.getLogoResourceId()));
			preference.setChecked(plugin.isActive());
			preference.setOnPreferenceChangeListener(this);
			preference.setLayoutResource(R.layout.preference_fragment_and_switch);

			Intent intent = new Intent(getContext(), PluginActivity.class);
			intent.putExtra(PluginActivity.EXTRA_PLUGIN_ID, plugin.getId());
			preference.setIntent(intent);

			screen.addPreference(preference);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		OsmandPlugin plugin = OsmandPlugin.getPlugin(key);
		if (plugin != null) {
			Toast.makeText(getActivity(), "Change " + plugin.getId(), Toast.LENGTH_LONG).show();
			return OsmandPlugin.enablePlugin(getActivity(), app, plugin, (Boolean) newValue);
		}
		ConnectedApp connectedApp = getMyApplication().getAidlApi().getConnectedApp(key);
		if (connectedApp != null) {
			return getMyApplication().getAidlApi().switchEnabled(connectedApp);
		}

		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Toast.makeText(getActivity(), "Click " + preference.getKey(), Toast.LENGTH_LONG).show();
		return super.onPreferenceClick(preference);
	}

	public static boolean showInstance(FragmentManager fragmentManager, ApplicationMode mode) {
		try {
			Bundle args = new Bundle();
			args.putString(PROFILE_STRING_KEY, mode.getStringKey());

			SettingsConfigureProfileFragment settingsConfigureProfileFragment = new SettingsConfigureProfileFragment();
			settingsConfigureProfileFragment.setArguments(args);

			fragmentManager.beginTransaction()
					.add(R.id.fragmentContainer, settingsConfigureProfileFragment, SettingsConfigureProfileFragment.TAG)
					.addToBackStack(SettingsConfigureProfileFragment.TAG)
					.commitAllowingStateLoss();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}