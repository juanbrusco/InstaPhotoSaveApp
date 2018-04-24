package com.ar.instafotoapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ar.utils.FloatingActionButton;

public class MainActivity extends ActionBarActivity {

	Editable url = null;
	String fotoObtenida = null;
	String obtenerFoto = null;
	static String appName = "";
	ImageView img;
	ProgressDialog pDialog1;
	ProgressDialog pDialog2;
	ProgressDialog pDialog3;
	Bitmap bitmap;
	String fotoUrl = null;
	EditText txtUrl;
	FloatingActionButton fabButton;
	FloatingActionButton fabButtonGuardar;
	static File folderTemp;
	Animation animScale;
	String resultProcesar = "noOk";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// animaciones
		animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);

		// Setear la fuente a utilizar en el mainActivity
		String fontPath = "fonts/Roboto-Light.ttf";
		Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);

		// Cambiar la fuente del actionbar
		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) getWindow().findViewById(
				actionBarTitle);
		Typeface forte = Typeface.createFromAsset(getAssets(), fontPath);
		if (actionBarTitleView != null) {
			actionBarTitleView.setTypeface(forte);
			actionBarTitleView.setTextSize(14);
		}

		// nombre app
		appName = getApplicationName(getApplicationContext());

		// txtUrl
		txtUrl = (EditText) findViewById(R.id.url);
		txtUrl.setTypeface(tf);
		txtUrl.getBackground().setColorFilter(Color.WHITE,
				PorterDuff.Mode.SRC_ATOP);

		// img view
		img = (ImageView) findViewById(R.id.fotoView);

		// Crear Botón flotante Material Design
		fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_share))
				.withButtonColor(Color.parseColor("#F58076"))
				.withGravity(Gravity.BOTTOM | Gravity.END)
				.withMargins(0, 0, 16, 16).create();

		fabButton.hideFloatingActionButton();

		fabButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				share();
			}
		});

		// Crear Botón flotante Material Design
		fabButtonGuardar = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.ic_inbox))
				.withButtonColor(Color.parseColor("#F58076"))
				.withGravity(Gravity.BOTTOM | Gravity.START)
				.withMargins(16, 0, 0, 16).create();

		fabButtonGuardar.hideFloatingActionButton();

		fabButtonGuardar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				guardarFotoAction();
			}
		});

		// btnPegarUrl
		Button btnPegarUrl = (Button) findViewById(R.id.pegar);
		btnPegarUrl.setTypeface(tf);
		btnPegarUrl.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				url = txtUrl.getText();
				String check = txtUrl.getText().toString();
				if (!check.matches("")) {
					new procesarFoto().execute();
				} else {
					Toast.makeText(MainActivity.this, "Error de url",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});

	}

	public static String getApplicationName(Context context) {
		String name = context.getResources().getString(R.string.app_name);
		return name;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.instrucciones) {
			this.closeOptionsMenu();
			Intent i = new Intent(this, Instrucciones.class);
			this.startActivity(i);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
			return true;
		}

		if (id == R.id.compartir) {
			try {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT,
						"Descarga InstaPhotoSave desde https://play.google.com/store/apps/details?id=com.ar.instafotoapp");
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent,
						"¡Comparte la App!"));
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, "Error al compartir la App",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		if (id == R.id.acerca) {
			Toast.makeText(MainActivity.this,
					"App desarrollada por Juan Brusco -v1.0", Toast.LENGTH_LONG)
					.show();

		}

		return super.onOptionsItemSelected(item);
	}

	private class ObtenerFoto extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog2 = new ProgressDialog(MainActivity.this);
			pDialog2.setMessage("Obteniendo foto ...");
			pDialog2.setCanceledOnTouchOutside(false);
			pDialog2.show();

		}

		@Override
		protected String doInBackground(String... params) {
			try {
				String strUrl = getMetaFromSource(params[0]);
				String obtenerProperty = strUrl.substring(
						strUrl.lastIndexOf("og:image") + 1,
						strUrl.indexOf("pg"));
				String obtenerContent = obtenerProperty.substring(
						obtenerProperty.indexOf("https://") + 1,
						obtenerProperty.indexOf("j"));
				obtenerFoto = "h" + obtenerContent + "jpg";
			} catch (Exception e) {
				e.printStackTrace();
			}

			return obtenerFoto;
		}
// video	 https://www.instagram.com/p/_IZJUVQftg/	
//		protected String doInBackground(String... params) {
//			try {
//				String strUrl = getMetaFromSource(params[0]);
//				String obtenerProperty = strUrl.substring(
//						strUrl.lastIndexOf("og:video:secure_url") + 1,
//						strUrl.indexOf("og:video:type"));
//				String obtenerContent = obtenerProperty.substring(
//						obtenerProperty.indexOf("https://") + 1,
//						obtenerProperty.indexOf("mp4"));
//				obtenerFoto = "h" + obtenerContent + "mp4";
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			DownloadFileVideo(obtenerFoto,"Sample.mp4");
//
//			return obtenerFoto;
//			
//		}

		protected void onPostExecute(String image) {

			if (image != null) {
				pDialog2.dismiss();
				fotoUrl = image;
				new LoadImage().execute(image);
				fabButton.showFloatingActionButton();
				fabButton.setAnimation(animScale);
				fabButtonGuardar.showFloatingActionButton();
				fabButtonGuardar.setAnimation(animScale);
			} else {
				pDialog2.dismiss();
				Toast.makeText(MainActivity.this, "Error al obtener la foto",
						Toast.LENGTH_SHORT).show();

			}
		}

	}

	
	private class LoadImage extends AsyncTask<String, String, Bitmap> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog3 = new ProgressDialog(MainActivity.this);
			pDialog3.setMessage("Cargando foto ...");
			pDialog3.setCanceledOnTouchOutside(false);
			pDialog3.show();

		}

		protected Bitmap doInBackground(String... args) {
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(
						args[0]).getContent());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		protected void onPostExecute(Bitmap image) {

			if (image != null) {
				img.setImageBitmap(image);
				pDialog3.dismiss();

			} else {

				pDialog3.dismiss();
				Toast.makeText(MainActivity.this, "Error al cargar la foto",
						Toast.LENGTH_SHORT).show();

			}
		}
	}

	public String getMetaFromSource(String urlString) {
		URLConnection feedUrl;
		try {
			feedUrl = new URL(urlString).openConnection();
			InputStream is = feedUrl.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "");
			}
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public void guardarFoto(String imageUrl, int destiny) {
		if (isDownloadManagerAvailable(getApplicationContext())) {
			String url = imageUrl;
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(url));
			request.setDescription(appName);
			request.setTitle(appName + ".jpg");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}

			if (destiny == 1) {
				request.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_DOWNLOADS, appName + '-' + ".jpg");
			} else {
				request.setDestinationInExternalPublicDir(
						Environment.getExternalStorageDirectory()
								+ "/InstaPhotoSave", "ips.jpg");
			}

			DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
		} else {
			doDownload(imageUrl);
		}

	}

	protected void doDownload(final String urlLink) {
		Thread dx = new Thread() {
			public void run() {
				@SuppressWarnings("unused")
				File root = android.os.Environment
						.getExternalStorageDirectory();
				// File dir = new File(root.getAbsolutePath() + "/Download/");
				File dir = new File(Environment.DIRECTORY_DOWNLOADS);
				if (dir.exists() == false) {
					dir.mkdirs();
				}
				// Save the path as a string value

				try {
					URL url = new URL(urlLink);
					Log.i("FILE_NAME", "File name is " + appName);
					Log.i("FILE_URLLINK", "File URL is " + url);
					URLConnection connection = url.openConnection();
					connection.connect();
					// this will be useful so that you can show a typical 0-100%
					// progress bar
					@SuppressWarnings("unused")
					int fileLength = connection.getContentLength();

					// download the file

					Toast.makeText(getApplicationContext(),
							"Descargando Imagen", Toast.LENGTH_LONG).show();
					InputStream input = new BufferedInputStream(
							url.openStream());
					OutputStream output = new FileOutputStream(dir + "/"
							+ appName);

					byte data[] = new byte[1024];
					@SuppressWarnings("unused")
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						total += count;

						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
					Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" + e);
				}
			}
		};
		dx.start();
		Toast.makeText(getApplicationContext(), "Imagen Descargada",
				Toast.LENGTH_LONG).show();

	}

	/**
	 * @param context
	 *            used to check the device version and DownloadManager
	 *            information
	 * @return true if the download manager is available
	 */
	public static boolean isDownloadManagerAvailable(Context context) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui",
					"com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager()
					.queryIntentActivities(intent,
							PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressLint("NewApi")
	public String pegarUrl() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard.hasPrimaryClip()) {
			android.content.ClipDescription description = clipboard
					.getPrimaryClipDescription();
			android.content.ClipData data = clipboard.getPrimaryClip();
			if (data != null
					&& description != null
					&& description
							.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
				return String.valueOf(data.getItemAt(0).getText());
		}
		return null;
	}

	private void share() {

		Bitmap bitmap;
		OutputStream output;

		// tomar la imagen del imageview de la pantalla y convertirla a bitmap
		img.buildDrawingCache();
		bitmap = img.getDrawingCache();

		// Find the SD Card path
		File filepath = Environment.getExternalStorageDirectory();

		// Create a new folder AndroidBegin in SD Card
		File dir = new File(filepath.getAbsolutePath() + "/InstaPhotoSave/");
		dir.mkdirs();

		// Create a name for the saved image
		File file = new File(dir, "instaPhotoSave.png");

		try {

			// Share Intent
			Intent share = new Intent(Intent.ACTION_SEND);

			// Type of file to share
			share.setType("image/jpeg");

			output = new FileOutputStream(file);

			// Compress into png format image from 0% - 100%
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.flush();
			output.close();

			// Locate the image to Share
			Uri uri = Uri.fromFile(file);

			// Pass the image into an Intnet
			share.putExtra(Intent.EXTRA_STREAM, uri);
			share.putExtra(Intent.EXTRA_TEXT,
					"Foto obtenida con InstaPhotoSave");

			// Show the social share chooser list
			startActivity(Intent.createChooser(share, "Compartir foto"));

		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "Error al compartir la foto",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}


	
	private class procesarFoto extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog1 = new ProgressDialog(MainActivity.this);
			pDialog1.setMessage("Procesando URL ...");
			pDialog1.setCanceledOnTouchOutside(false);
			pDialog1.show();

		}

		@Override
		protected String doInBackground(String... params) {
			
			resultProcesar = "ok";

			ConnectivityManager connectivityManager = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();

			if ((info == null || !info.isConnected() || !info.isAvailable())) {
				resultProcesar = "false";
			}
			return resultProcesar;
			
			
			
		}

		protected void onPostExecute(String result) {

			if (result == "ok") {
				pDialog1.dismiss();
				
					new ObtenerFoto().execute(url.toString());


			} else {
				pDialog1.dismiss();
				Toast.makeText(MainActivity.this, "Error al procesar la URL",
						Toast.LENGTH_SHORT).show();

			}
		}

	}

	private void guardarFotoAction() {
		if (fotoUrl != null) {
			guardarFoto(fotoUrl, 1);

		} else {
			Toast.makeText(MainActivity.this, "Error al guardar la foto",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void DownloadFileVideo(String fileURL, String fileName) {	        
        try {
        	String RootDir = Environment.getExternalStorageDirectory()
                    + File.separator + "Video";
              
              String dest_file_path = Environment.DIRECTORY_DOWNLOADS + "dwnloaded_file.mp4";
              File dest_file = new File(dest_file_path);
              URL u = new URL(fileURL);
              URLConnection conn = u.openConnection();
              int contentLength = conn.getContentLength();
              DataInputStream stream = new DataInputStream(u.openStream());
              byte[] buffer = new byte[contentLength];
              stream.readFully(buffer);
              stream.close();
              DataOutputStream fos = new DataOutputStream(new FileOutputStream(dest_file));
              fos.write(buffer);
              fos.flush();
              fos.close();
               
          } catch(FileNotFoundException e) {
              return; 
          } catch (IOException e) {
              return; 
          }

    }

}
