package net.vvakame.applist;

import java.util.ArrayList;
import java.util.List;

import net.vvakame.applist.ApplicationListFragment.AppData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.content.AsyncTaskLoader;

class ApplicationListLoader extends AsyncTaskLoader<List<AppData>> {

	PackageManager mPm;
	List<AppData> mAppList;

	public ApplicationListLoader(Context context) {
		super(context);

		mPm = context.getPackageManager();
	}

	@Override
	public List<AppData> loadInBackground() {

		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list = mPm.queryIntentActivities(intent, 0);

		mAppList = new ArrayList<AppData>();
		for (ResolveInfo resolveInfo : list) {
			String appName = resolveInfo.loadLabel(mPm).toString();
			Drawable icon = resolveInfo.loadIcon(mPm);
			String packageName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;

			AppData data = new AppData(appName, icon, packageName, className);
			mAppList.add(data);
		}

		return mAppList;
	}

	@Override
	public void deliverResult(List<AppData> apps) {
		mAppList = apps;

		if (isStarted()) {
			super.deliverResult(apps);
		}
	}

	@Override
	protected void onStartLoading() {
		if (mAppList != null) {
			deliverResult(mAppList);
		}

		if (mAppList == null) {
			forceLoad();
		}
	}
}