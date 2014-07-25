package net.vvakame.applist;

import net.vvakame.applist.ApplicationListFragment.AppData;
import net.vvakame.applist.ApplicationListFragment.ApplicationEventCallback;
import net.vvakame.applist.ApplicationListFragment.ApplicationEventCallbackPicker;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ApplicationEventCallbackPicker, ApplicationEventCallback {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ApplicationListFragment fragment = new ApplicationListFragment();
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.container, fragment);
		transaction.commit();
	}

	@Override
	public ApplicationEventCallback getInstance() {
		return this;
	}

	@Override
	public void onApplicationClicked(AppData data) {
		ImageView iconImage = new ImageView(this);
		iconImage.setImageDrawable(data.icon);

		Toast toast = new Toast(this);
		toast.setView(iconImage);
		toast.show();
	}
}