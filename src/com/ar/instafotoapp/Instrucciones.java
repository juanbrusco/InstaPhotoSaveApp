package com.ar.instafotoapp;

import com.ar.instafotoapp.R;

import android.support.v7.app.ActionBarActivity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Instrucciones extends ActionBarActivity {

	TextView i1;
	TextView i2;
	TextView i3;
	TextView i4;
	TextView i5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instrucciones);

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


		i1 = (TextView) findViewById(R.id.instruccionesTexto1);
		i1.setTypeface(tf);
		i2 = (TextView) findViewById(R.id.instruccionesTexto2);
		i2.setTypeface(tf);
		i3 = (TextView) findViewById(R.id.instruccionesTexto3);
		i3.setTypeface(tf);
		i4 = (TextView) findViewById(R.id.instruccionesTexto4);
		i4.setTypeface(tf);
		i5 = (TextView) findViewById(R.id.instruccionesTexto5);
		i5.setTypeface(tf);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.instrucciones, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
