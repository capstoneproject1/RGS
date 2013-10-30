package com.example.tts;

import java.util.Locale;

import org.w3c.dom.Text;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;

public class MainActivity extends Activity implements OnInitListener {

	private TextToSpeech tts;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, 0);
		tts = new TextToSpeech(this,this);
	}


	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		String speech1 = "Hello Inseok how are you thanks you";
		String speech2 = "Salta how about taking a break now";
		String speech3 = "Hello Salta how are you";
		tts.setLanguage(Locale.US);
		tts.speak(speech1, TextToSpeech.QUEUE_ADD,null);
		tts.speak(speech3, TextToSpeech.QUEUE_ADD, null);
		tts.speak(speech2, TextToSpeech.QUEUE_ADD, null);
	}

}
