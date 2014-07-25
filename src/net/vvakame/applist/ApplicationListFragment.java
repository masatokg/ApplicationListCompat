package net.vvakame.applist;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ApplicationListFragment extends ListFragment {

	public static interface ApplicationEventCallbackPicker {
		public ApplicationEventCallback getInstance();
	}

	public static interface ApplicationEventCallback {
		public void onApplicationClicked(AppData data);
	}

	enum Mode {
		SHARE, LAUNCH;

		public String getName() {
			String name = name().charAt(0) + name().substring(1).toLowerCase();
			return name;
		}
	}

	public static class AppData {
		final public String appName;
		final public Drawable icon;
		final public String packageName;
		final public String className;

		public AppData(String appName, Drawable icon, String packageName,
				String className) {
			this.appName = appName;
			this.icon = icon;
			this.packageName = packageName;
			this.className = className;
		}
	}

	Context mContext;

	Mode mCurrent = Mode.SHARE;

	ApplicationEventCallback mCallback;

	ApplicationListAdapter mAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		ApplicationEventCallbackPicker picker;
		if (activity instanceof ApplicationEventCallbackPicker) {
			picker = (ApplicationEventCallbackPicker) activity;
		} else {
			throw new IllegalArgumentException();
		}

		ApplicationEventCallback callback = picker.getInstance();
		if (callback == null) {
			throw new IllegalArgumentException();
		}

		mCallback = callback;
		mContext = activity.getApplicationContext();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyText("アプリケーションがありません");
		setHasOptionsMenu(true);
		mAdapter = new ApplicationListAdapter(mContext);
		setListAdapter(mAdapter);

		setListShown(false);
		getLoaderManager().initLoader(0, null, new DataLoaderCallbacks());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AppData data = (AppData) getListAdapter().getItem(position);
		mCallback.onApplicationClicked(data);

		if (mCurrent == Mode.SHARE) {
			Intent intent = createShareIntent(data);
			if (canReceiveShare(intent)) {
				startActivity(intent);
			} else {
				String msg = data.appName + "では共有できません";
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		} else if (mCurrent == Mode.LAUNCH) {
			Intent intent = createLaunchIntent(data);
			startActivity(intent);
		}
	}

	Intent createShareIntent(AppData data) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setPackage(data.packageName);
		intent.setType("text/plain");
		String msg = "+1 " + data.appName + " ";
		msg += "https://market.android.com/details?id=" + data.packageName;
		intent.putExtra(Intent.EXTRA_TEXT, msg);

		return intent;
	}

	boolean canReceiveShare(Intent intent) {
		PackageManager manager = mContext.getPackageManager();
		List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);

		return list.size() != 0;
	}

	Intent createLaunchIntent(AppData data) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(data.packageName, data.className));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	static class ApplicationListAdapter extends ArrayAdapter<AppData> {
		final LayoutInflater mInflater;

		public ApplicationListAdapter(Context context) {
			super(context, 0);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if (convertView == null) {
				view = mInflater.inflate(R.layout.application_list_row, parent,
						false);
			} else {
				view = convertView;
			}

			AppData data = getItem(position);
			ImageView iconImage = (ImageView) view.findViewById(R.id.icon);
			iconImage.setImageDrawable(data.icon);
			TextView appText = (TextView) view.findViewById(R.id.app_name);
			appText.setText(data.appName);

			return view;
		}
	}

	class DataLoaderCallbacks implements
			LoaderManager.LoaderCallbacks<List<AppData>> {
		@Override
		public Loader<List<AppData>> onCreateLoader(int id, Bundle args) {
			return new ApplicationListLoader(mContext);
		}

		@Override
		public void onLoadFinished(Loader<List<AppData>> loader,
				List<AppData> data) {

			for (AppData appData : data) {
				mAdapter.add(appData);
			}
			mAdapter.notifyDataSetChanged();
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<List<AppData>> loader) {
			mAdapter.clear();
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		SubMenu subMenu = menu.addSubMenu("Action");
		subMenu.setIcon(android.R.drawable.ic_menu_send);
		onPrepareSubMenu(subMenu);
	}

	public void onPrepareSubMenu(SubMenu subMenu) {
		subMenu.clear();
		for (Mode mode : Mode.values()) {
			int i = mode.ordinal();
			String name = mode.getName();
			subMenu.add(0, i, i, name);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast toast;
		if (item.getTitle().equals("Action")) {
			toast = Toast.makeText(mContext, mCurrent.getName(),
					Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(mContext, item.getTitle(),
					Toast.LENGTH_SHORT);
		}
		toast.show();

		for (Mode mode : Mode.values()) {
			if (mode.ordinal() == item.getOrder()) {
				mCurrent = mode;
			}
		}

		return true;
	}
}
