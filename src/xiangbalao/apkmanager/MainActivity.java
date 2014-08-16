package xiangbalao.apkmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView apklist;

	private ProgressDialog pd;
	private List<ApkInfo> apks;

	private LayoutInflater mInflater;
	private ApklistAdapter adapter;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		adapter = new ApklistAdapter();

		pd = ProgressDialog.show(this, getString(R.string.app_name),
				getString(R.string.finding));
		apks = new ArrayList<ApkInfo>();
		findAPKs task = new findAPKs();
		task.execute();

		apklist = (ListView) findViewById(R.id.apklist);

		apklist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterview, View v,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(new File(apks.get(position).getPath()));
				MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
				String type = mimeTypeMap.getMimeTypeFromExtension("apk");
				intent.setDataAndType(uri, type);
				startActivity(intent);
			}
		});

		apklist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterview, View v,
					int position, long arg3) {
				// TODO Auto-generated method stub
				longpressDialog(position);
				return false;
			}
		});

	}

	private void longpressDialog(final int position) {
		AlertDialog.Builder dialog = new Builder(MainActivity.this);
		dialog.setTitle(apks.get(position).getAppname());
		dialog.setItems(new String[] { getString(R.string.delete),
				getString(R.string.share) }, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				switch (arg1) {
				case 0:
					File file = new File(apks.get(position).getPath());
					if (file.delete()) {
						apks.remove(position);
						adapter.notifyDataSetChanged();
						setTitle(getString(R.string.app_name) + "("
								+ apks.size() + ")");
					}
					break;
				case 1:
					Intent intent = new Intent(Intent.ACTION_SEND);
					Uri data = Uri.fromFile(new File(apks.get(position)
							.getPath()));
					String type = MimeTypeMap.getSingleton()
							.getMimeTypeFromExtension("apk");
					Log.d("tag", data.toString() + "\n" + type);
					intent.putExtra(Intent.EXTRA_STREAM, data);
					intent.setType(type);
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		});
		dialog.create().show();
	}

	class ApklistAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return apks.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View v, ViewGroup vg) {

			ViewHolder holder = null;

			if (v == null) {
				v = mInflater.inflate(R.layout.apklist_item, vg, false);
				holder = new ViewHolder();
				holder.apknametv = (TextView) v.findViewById(R.id.apkname);
				holder.apksizetv = (TextView) v.findViewById(R.id.apksize);
				holder.apkiconiv = (ImageView) v.findViewById(R.id.apkicon);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}

			holder.apksizetv.setText(apks.get(position).getSize());
			holder.apknametv.setText(apks.get(position).getAppname());
			holder.apkiconiv.setImageDrawable(apks.get(position).getIcon());

			return v;
		}

	}

	static class ViewHolder {
		TextView apknametv;
		TextView apksizetv;
		ImageView apkiconiv;
	}

	class findAPKs extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			FindUtils fu = new FindUtils();
			apks = fu.findapks(MainActivity.this, "/sdcard/");
			// Log.d("apk", apks.toString());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}
			apklist.setAdapter(adapter);
			setTitle(getString(R.string.app_name) + "(" + apks.size() + ")");

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_moveall:
			moveallDialog();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void moveallDialog() {
		AlertDialog.Builder dialog = new Builder(MainActivity.this);
		dialog.setTitle(getString(R.string.moveto));
		dialog.setMessage("是否要将所有安装包移动到/sdcard/Download/APK/目录下？");
		dialog.setPositiveButton(getString(R.string.ok), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				pd = ProgressDialog.show(MainActivity.this,
						getString(R.string.app_name),
						getString(R.string.moving));
				moveapkfiles("/sdcard/Download/APK/");
			}

			private void moveapkfiles(String pathto) {
				// TODO Auto-generated method stub
				for (ApkInfo apkinfo : apks) {
					File file = new File(apkinfo.getPath());
					File pathtofile = new File(pathto + "/" + file.getName());
					file.renameTo(pathtofile);
				}
				pd.dismiss();
				pd = null;
			}
		});
		dialog.setNegativeButton(getString(R.string.cancle), null);
		dialog.create().show();
	}

}
