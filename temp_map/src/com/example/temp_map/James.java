package com.example.temp_map;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class James extends Activity implements OnInitListener {
	String talk;
	private TextToSpeech tts;
	
	public James(String talk){
		this.talk = talk;
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, 0);
		tts = new TextToSpeech(this,this);
	}
	
	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		tts.setLanguage(Locale.US);
		tts.speak(talk, TextToSpeech.QUEUE_ADD,null);
	}
	
}
