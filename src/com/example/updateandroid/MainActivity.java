package com.example.updateandroid;

import java.io.BufferedWriter;
//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {
	
//	FileInputStream fis;
	Button update, reset;
	EditText name, version, model;
	File orig = new File("/system", "build.prop");
	File temp = new File("/system",	"build1.prop");
	//File temp = new File(MainActivity.this.getFilesDir(), "build.prop");
	
	Runtime r = Runtime.getRuntime();
	Process suProcess;
	static DataOutputStream dos;
//	DataInputStream dis;
	
	Scanner scanner;
	List<String> lines;
	int lineIndex = 0;
	String string;
	String mod = "ro.product.model=";
	String build = "ro.build.display.id=";
	String ver = "ro.build.version.release=";
	BufferedWriter writer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialise();
		
		try {
			suProcess = r.exec("su");
			dos = new DataOutputStream(suProcess.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(getApplicationContext(), e1.toString(), Toast.LENGTH_SHORT).show();
			//make toast to report error
		}
		
		backupOriginal();
		
		update.setOnClickListener(this);
		reset.setOnClickListener(this);
		
		showCurrentProperties();
		
	}

	private void showCurrentProperties() {
		// TODO Auto-generated method stub
		try {
			dos.writeBytes("chmod 777 /system/build.prop\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		int flag = 3;
		
		try {
			scanner = new Scanner(new FileInputStream(orig));
			while((flag != 0) && (scanner.hasNextLine())){
		        
				string = scanner.nextLine();
	    	
				if(string.contains(ver)){
					version.setHint(string.substring(string.indexOf("=") + 1));
					flag--;
					continue;
				}
				
				if(string.contains(build)){
					name.setHint(string.substring(string.indexOf("=") + 1));
					flag--;
					continue;
				}
				
				if(string.contains(mod)){
					model.setHint(string.substring(string.indexOf("=") + 1));
					flag--;
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			dos.writeBytes("chmod 644 /system/build.prop\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void backupOriginal() {
		// TODO Auto-generated method stub
		if(!(temp.exists())){
			try {
				dos.writeBytes("mount -o rw,remount /system\n");
				dos.writeBytes("cp /system/build.prop /system/build1.prop\n");
				dos.writeBytes("mount -o ro,remount /system\n");
				Toast.makeText(getApplicationContext(), "Original file backuped", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
		
		else{
			Toast.makeText(getApplicationContext(), "Original file already exists", Toast.LENGTH_SHORT).show();
		}
		
		updateVisibility();
	}
	
	private void restoreOriginal() {
		// TODO Auto-generated method stub
		
		if(temp.exists()){
			try {
				dos.writeBytes("mount -o rw,remount /system\n");
				dos.writeBytes("mv /system/build1.prop /system/build.prop\n");
				dos.writeBytes("mount -o ro,remount /system\n");
				Toast.makeText(getApplicationContext(), "Original file restored", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
			
			updateVisibility();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	private void initialise() {
		// TODO Auto-generated method stub
		update = (Button) findViewById(R.id.button1);
		reset = (Button) findViewById(R.id.button2);
		
		name = (EditText) findViewById(R.id.editText1);
		version = (EditText) findViewById(R.id.editText2);
		model = (EditText) findViewById(R.id.editText3);
		
		//fis = openFileInput(name);
		
		updateVisibility();
	}	
	
	private void updateVisibility() {
		// TODO Auto-generated method stub
		if(!(temp.exists())){
			reset.setClickable(false);
		}
		
		else{
			reset.setClickable(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()){
		
			case R.id.button1 : 
			
				try {
					dos.writeBytes("mount -o rw,remount /system\n");
					dos.writeBytes("chmod 777 /system/build.prop\n");
					scanner = new Scanner(new FileInputStream(orig));
					lines = new LinkedList<String>();
			    
					while(scanner.hasNextLine()){
			        
						string = scanner.nextLine();
			    	
						if(string.contains(ver)){
							lines.add(ver + version.getText().toString() + "\n");
							continue;
						}
						
						if(string.contains(build)){
							lines.add(build + name.getText().toString() + "\n");
							continue;
						}
						
						if(string.contains(mod)){
							lines.add(mod + model.getText().toString() + "\n");
							continue;
						}
			    	
						lines.add(string + "\n");
						lineIndex++;
					}
			    
/*build.prop*/  	/*dos.writeBytes("chmod 777 /system/build.prop\n");*/ //this line has been writen above since writing here leaves no time for changing the permission of build.prop to w (writable) before the just next line is executed 
			    
					writer = new BufferedWriter(new FileWriter(orig, false));
					for(final String line : lines){
						writer.write(line);
					}
					writer.flush();
					writer.close();
					
					dos.writeBytes("chmod 644 /system/build.prop\n");    /*build.prop*/
					dos.writeBytes("mount -o ro,remount /system\n");
					
					Toast.makeText(getApplicationContext(), "Changes Commited!!!", Toast.LENGTH_SHORT).show();

				} catch (IOException e) {
					e.printStackTrace();
					string = e.toString();
					Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
				}
				
				//------------------
				
				new MyCustomDialog().show(getSupportFragmentManager(), "reboot_dialog");
				
				//------------------
				
//				updateVisibility();			not needed here as this only do changes in build.prop
			
				break;
		
			case R.id.button2 : 
			
				restoreOriginal();
			
				break;
		
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		try {
			dos.writeBytes("chmod 644 /system/build.prop\n");    /*build.prop*/
			dos.writeBytes("mount -o ro,remount /system\n");
			
			// reboot?????????
			
			dos.writeBytes("exit\n");
			suProcess.waitFor();
			Toast.makeText(getApplicationContext(), "Exiting Application", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class MyCustomDialog extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("Commiting these changes require you to reboot your device.")
	               .setPositiveButton("Reboot", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   try {
							dos.writeBytes("mount -o ro,remount /system\n");
							dos.writeBytes("reboot\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
						}
	                   }
	               })
	               .setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}

}

