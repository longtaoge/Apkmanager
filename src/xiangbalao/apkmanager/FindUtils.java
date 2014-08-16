package xiangbalao.apkmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class FindUtils {

	File path;
	List<String> results;

	public List<ApkInfo> findapks(Context context, String pathname) {
		results = new ArrayList<String>();
		find(pathname, ".apk");

		List<ApkInfo> apks = new ArrayList<ApkInfo>();
		for (String apk : results) {
			ApkInfo apkinfo = new ApkInfo();
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageArchiveInfo(apk,
					PackageManager.GET_ACTIVITIES);
			if (pi == null)
				continue;
			ApplicationInfo ai = pi.applicationInfo;
			ai.sourceDir = apk;
			ai.publicSourceDir = apk;

			String apksize = pi.versionName;
			String apkname = ai.loadLabel(pm).toString();
			Drawable apkicon = ai.loadIcon(pm);

			apkinfo.setAppname(apkname);
			apkinfo.setIcon(apkicon);
			apkinfo.setSize(apksize);
			apkinfo.setPath(apk);

			apks.add(apkinfo);

		}
		
		Collections.sort(apks,comparator);
		return apks;
	}
	
	Comparator<ApkInfo> comparator = new Comparator<ApkInfo>() {
		
		@Override
		public int compare(ApkInfo apk1, ApkInfo apk2) {
			// TODO Auto-generated method stub
			if (!apk1.getAppname().equals(apk2.getAppname()))
				return apk1.getAppname().compareTo(apk2.getAppname());
			else if (!apk1.getSize().equals(apk2.getSize()))
				return apk1.getSize().compareTo(apk2.getSize());
			return 0;
		}
	};

	private void find(String findpath, String fileext) {
		path = new File(findpath);
		if (!path.isDirectory())
			return;

		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				if (files[i].getAbsolutePath().toLowerCase().endsWith(fileext)
						&& !files[i].getName().startsWith("."))
					results.add(files[i].getPath());
			} else {
				if (!files[i].getName().startsWith("."))
					find(files[i].getPath(), fileext);
			}
		}
	}

}
