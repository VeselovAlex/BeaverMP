package com.example.beavermp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends Activity {
	
	protected File currentFile;
	ListView view;
	TextView currentPath;
	MusicPlayer player;
	
	OnItemClickListener fileClicked = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
			File copy = currentFile;
			try {
				String filepath = (String)parent.getItemAtPosition(position);

				if (filepath != null) {
					if (!filepath.equals(".."))	{
						File chosen = new File(currentFile.getPath() + "/" + filepath);
						if (chosen.isDirectory()) {
							currentFile = chosen;
						} else if (chosen.isFile()) {
							if (player.addFile(chosen))
								Toast.makeText(getApplicationContext(), "Loaded " + chosen.getName(), 
										Toast.LENGTH_SHORT).show();
						}
					} else if (currentFile.getParent() != null) {
						currentFile = currentFile.getParentFile();
					}
					showFiles();
				}
			} catch (Exception e){
				Toast msg = Toast.makeText(getApplicationContext(), "Can not open this directory", Toast.LENGTH_SHORT);
				msg.show();
				currentFile = copy;
				showFiles();
			} 
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		View controls = (View)findViewById(R.id.controlLayout);
		player = new MusicPlayer(getApplicationContext(), controls);
		currentFile = new File(Environment.getExternalStorageDirectory().getPath());
		currentPath = (TextView) findViewById(R.id.clickStatus);
		view = (ListView) findViewById(R.id.fileListView);
		view.setOnItemClickListener(fileClicked);
		showFiles();
	}
	
	public void showFiles()
	{
		if (currentFile != null && currentFile.isDirectory())
		{
			if (currentPath != null)
				currentPath.setText(currentFile.getPath());
			List<String> dirs = new LinkedList<String>();
			if (currentFile.getParent() != null)
				dirs.add("..");
			File[] files = currentFile.listFiles();
			for (File file : files)
				dirs.add(file.getName());
			
			ArrayAdapter<String>filenames = new ArrayAdapter<String>(this, R.layout.file_mgr_row, dirs);
			view.setAdapter(filenames);
		}
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.player, menu);
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
