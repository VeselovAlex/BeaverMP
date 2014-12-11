package com.example.beavermp;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.TrackInfo;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicPlayer {
	public MusicPlayer(Context context, View controls)
	{
		this.context = context;
		if (controls != null)
		{
			initControls(controls);
		}
	}

	public MusicPlayer(Context context)
	{
		this.context = context;
	}

	protected Context context;
	protected MediaPlayer player = new MediaPlayer();
	protected static String[] musicExt = {".mp3", ".flac"};
	protected Button playBtn;
	protected Button prevBtn;
	protected Button fwdBtn;
	protected SeekBar playProgress;
	protected Chronometer timer;
	protected OnCompletionListener completePlayback = null;
	protected PlayList playlist = new PlayList();
	protected TextView timeElapsed;
	protected TextView txt_name;

	protected class PlayList {
		private List<File> playlist = new ArrayList<File>();
		private int position = -1;

		public boolean addFile(File file) {
			if (isMusicFile(file) && !playlist.contains(file))
			{
				playlist.add(file);
				return true;
			}
			return false;
		}

		public void removeFile(File file) {
			if (playlist.contains(file) && position > playlist.indexOf(file))
				position--;
			playlist.remove(file);

		}

		public File current() {
			if (!playlist.isEmpty() && playlist.size() > position && position >= 0)
				return playlist.get(position);
			else
				return null;
		}

		public File next() {
			if (!playlist.isEmpty() && playlist.size() > position + 1)
				return playlist.get(++position);
			else
				return null;
		}

		public File prev() {
			if (!playlist.isEmpty() && position > 0)
				return playlist.get(--position);
			else
				return null;
		}

		public File first() {
			position = 0;
			if (!playlist.isEmpty())
				return playlist.get(0);
			else
				return null;
		}

		public boolean hasSingleFile() {
			return playlist.size() == 1;
		}
	}

	protected void initControls(View controls)
	{
		if (controls == null)
			return;

		playBtn = (Button) controls.findViewById(R.id.playBtn);
		prevBtn = (Button) controls.findViewById(R.id.prevBtn);
		fwdBtn = (Button) controls.findViewById(R.id.fwdBtn);
		playProgress = (SeekBar) controls.findViewById(R.id.playProgressBar);
		timer = (Chronometer) controls.findViewById(R.id.playbackTimer);
		timeElapsed = (TextView) controls.findViewById(R.id.curTime);
		txt_name = (TextView) controls.findViewById(R.id.txt_name);
		
		OnClickListener playBtnClicked = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (player == null) {
					if (loadFile(playlist.first()))	{
						play();
					}

				} else if (player.isPlaying()) {
					pause();
				} else {
					play();
				}
			}
		};
		
		OnLongClickListener playBtnLongClick = new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {if (player == null) {
				if (loadFile(playlist.first()))	{
					play();
				}

			} else if (player.isPlaying()) {
				stop();
			} else {
				play();
			}
				return false;
			}
		};

		OnClickListener fwdBtnClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				File next = playlist.next();
				if (next != null) {
					if (player != null)
						release();
					play(next);
				}
			}
		};

		OnClickListener prevBtnClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				File prev = playlist.prev();
				if (prev != null) {
					if (player != null)
						release();
					play(prev);
				}

			}
		};

		OnSeekBarChangeListener progressChanged = new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (player != null)
					player.seekTo(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (timeElapsed != null)
					timeElapsed.setText(timeToString(progress));
			}
		};

		OnChronometerTickListener tickTimer = new OnChronometerTickListener() {

			@Override
			public void onChronometerTick(Chronometer chronometer) {
				if (playProgress != null && player != null && player.isPlaying())
				{
					int pos = player.getCurrentPosition();
					if (timeElapsed != null)
						timeElapsed.setText(timeToString(pos));
					playProgress.setProgress(pos);
				}
						
			}
		};

		completePlayback = new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (timer != null)
					timer.stop();
				if (playProgress != null)
					playProgress.setProgress(0);
				release();
				play(playlist.next());
			}
		};

		playBtn.setOnClickListener(playBtnClicked);
		playBtn.setOnLongClickListener(playBtnLongClick);
		prevBtn.setOnClickListener(prevBtnClick);
		fwdBtn.setOnClickListener(fwdBtnClick);
		playProgress.setOnSeekBarChangeListener(progressChanged);
		timer.setOnChronometerTickListener(tickTimer);
	}

	protected static String timeToString(int time)
	{
		int seconds = time / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int hours = seconds / 60;
		seconds = seconds % 3600;
		String hoursStr = hours == 0 ? "" : hours + ":";
		String minuteStr = String.format("%02d:", minutes);
		String secondsStr = String.format("%02d", seconds);
		return hoursStr + minuteStr + secondsStr;
	}
	
	public boolean addFile(File musicFile) {
		boolean res = playlist.addFile(musicFile);
		if (playlist.hasSingleFile())
			loadFile(playlist.first());
		return res;
	}

	public boolean loadFile(File musicFile) {
		if (musicFile == null)
			return false;
		if (isMusicFile(musicFile))
		{
			if (player != null) {
				if (player.isPlaying())
				{	
					player.stop();
				}
				player.release();
			}

			player = MediaPlayer.create(context, Uri.fromFile(musicFile));
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (completePlayback != null)
				player.setOnCompletionListener(completePlayback);

			if (playProgress != null)
			{
				playProgress.setMax(player.getDuration());
				playProgress.setProgress(0);
			}
			
			if(txt_name != null) {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				retriever.setDataSource(musicFile.getPath());
				String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				txt_name.setText(title);
			}
			return true;
		}
		return false;
	}

	public void play(File musicFile) {
		if (loadFile(musicFile))
			this.play();
	}

	public void play()
	{
		if (player != null) {
			if (timer != null)
				timer.start();
			player.start();
			if (playBtn != null)
				playBtn.setText("Pause");
		}
	}

	public void stop()
	{
		if (player != null)
		{
			if (timer != null)
				timer.stop();
			if (timeElapsed != null)
				timeElapsed.setText("0");
			player.stop();
			if (playBtn != null)
				playBtn.setText("Play");
		}
	}

	public void pause()
	{
		if (player != null)
		{
			if (timer != null)
				timer.stop();
			player.pause();
			if (playBtn != null)
				playBtn.setText("Play");
		}
	}

	public void release()
	{
		if (player != null)
		{
			this.stop();
			player.release();
			player = null;
		}
	}

	public static boolean isMusicFile(File file){
		boolean res = false;
		for (String ext : musicExt)
			res |= file.getName().endsWith(ext);
		return res;
	}
}
