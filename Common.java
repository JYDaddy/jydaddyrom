package com.j.y.daddy.F50RG;

/**
 * Common.java
 * Created 2011/04/07 by Kevin, Song 
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;;;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

public class Common {
	
	private static Context currActivity = null;
  private static int ssss= 0;
	
	public String VersionStr = "";
	public String PkgName = "";

	public String FN_MOT_BOOT_MODE = "";
	public String SCRIPT_DIR_PATH = "";
	public String RAMDISK_DIR_PATH = "";
	public String[] ScriptFileName = null;
	
	//Shared Preferences Key
	public static final String KEY_OPTIMIZER_UPDATE_ALERT = "j_y_daddy_optimzer_key_update_alert";
	public String KEY_OPTIMIZER_UPDATE_ALERT_FLAG = "";	//Version String
	
	private SharedPreferences sharedprefs;
	
	public Common(Context context) {
		currActivity = context;
		
        try {
        	PackageInfo pkgInfo = currActivity.getPackageManager().getPackageInfo(currActivity.getPackageName(), 0);
        	VersionStr = pkgInfo.versionName;
        	PkgName = pkgInfo.packageName;
        } catch(NameNotFoundException e) { }
        
        KEY_OPTIMIZER_UPDATE_ALERT_FLAG = VersionStr;
	}
	
	//check phone
	public boolean CheckPhone() {
		boolean rstFlag = true;
		
		//XT720 ¸¸ Ã¼Å©ÇÏÀÚ!
		if(!currActivity.getString(R.string.Build_MODEL).equalsIgnoreCase(Build.MODEL)) rstFlag = false;
		//if(!currActivity.getString(R.string.Build_ID).equalsIgnoreCase(Build.ID)) {
		//	//IS-ROM Pass
		//	if(!currActivity.getString(R.string.Build_ID_ISROM).equalsIgnoreCase(Build.ID)) rstFlag = false;
		//}
		
		return rstFlag;
	}
	
	//SDÄ«µå °¡¿ë»óÅÂ Ã¼Å©
	public boolean CheckSD() {
		return (new StorageStatus().externalMemoryAvailable());
	}
	
	//optimizer first time alert popup with preference
	public void firstMessagePop() {
		sharedprefs = currActivity.getSharedPreferences("PrefName", currActivity.MODE_PRIVATE);
        String opt = sharedprefs.getString(KEY_OPTIMIZER_UPDATE_ALERT, "");
		if(!opt.equalsIgnoreCase(KEY_OPTIMIZER_UPDATE_ALERT_FLAG)) {
	    	new AlertDialog.Builder(currActivity).setTitle(currActivity.getString(R.string.FirstMessagesTitle)+"(v"+VersionStr+")")
	        .setMessage(currActivity.getString(R.string.FirstMessages))
	        .setPositiveButton(currActivity.getString(R.string.ok), new DialogInterface.OnClickListener(){
			    @Override
			    public void onClick( DialogInterface dialog, int which ) {
					SharedPreferences.Editor sed = sharedprefs.edit();
			        sed.putString(KEY_OPTIMIZER_UPDATE_ALERT, KEY_OPTIMIZER_UPDATE_ALERT_FLAG);
			        sed.commit();
			        
			        dialog.dismiss();
			    }
			 }).show();
	    	/*
	        .setNegativeButton(currActivity.getString(R.string.cancel), new DialogInterface.OnClickListener(){
			    @Override
			    public void onClick( DialogInterface dialog, int which ) {
			        dialog.dismiss();
			    }
			 }).show();
			 */
		} 
	}
	
    //Alert Popup
	public void showAlert(String msg){
    	new AlertDialog.Builder(currActivity).setTitle("Notification")
        .setMessage(msg)
        .setPositiveButton(currActivity.getString(R.string.ok), new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick( DialogInterface dialog, int which ) {
		        dialog.dismiss();
		    }
		 }).show();
    }
	
    //Alert Popup by Resource ID
	public void showAlert(int resid){
    	new AlertDialog.Builder(currActivity).setTitle("Notification")
        .setMessage(currActivity.getString(resid))
        .setPositiveButton(currActivity.getString(R.string.ok), new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick( DialogInterface dialog, int which ) {
		        dialog.dismiss();
		    }
		 }).show();
    }
	
	
	//mount disk free(Kbyte)
	public int getDiskFree(String MountPath) {
		int dfmb = 0;

		try {
			//Filesystem           1M-blocks      Used Available Use% Mounted on
			///dev/block/mtdblock7       170       164         6  97% /system			
			String retStr = ExcuteProcessResult("/system/xbin/busybox df -k "+MountPath);
			if(!retStr.equals("")) {
				String[] retLine = retStr.split("\n");
				if(retLine.length > 1) {
					String[] retCol = retLine[1].split(" ");
					int col=0;
					for(int i=0; i<retCol.length; i++) {
						if(!retCol[i].equals("")) {
							col++;
							if(col >= 4) {
								dfmb = Integer.parseInt(retCol[i]);
								break;
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			
		}
		return dfmb;
	}
	
	//Process Command Execute
    public String ExcuteProcessResult(String cmd) {
		String rstr = "";
		
		try {
	    	Process p = Runtime.getRuntime().exec(cmd);
        	p.waitFor();
    		rstr = getProcessReturnString(p.getInputStream());
	    	
    	} catch(IOException ie) {
    		Log.d(currActivity.getString(R.string.logerrtag),ie.toString());
    		showAlert(ie.toString());
    		
    	} catch(Exception e) {
    		Log.d("FOM_ERR",e.toString());
    		showAlert(e.toString());
    	}
		
		return rstr;
	}

	//Process InputStream Convert String
    public String getProcessReturnString(InputStream is) {
		String rstr = "";
		
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line="";
			while((line=br.readLine()) != null) {
				if(rstr.length() > 0) rstr += "\n";
				rstr += line;
			}
			
		} catch(IOException ie){
			
		}
		
		return rstr;
	}


    //asset file control
    public boolean createFileFromAssets(AssetManager am, String destFile, String targetFile){
    	File f = new File(targetFile);
    	FileOutputStream fos = null;
    	BufferedOutputStream bos = null;
    	DataOutputStream os = null;
    	
    	boolean rstFlag = false;
    	
    	try {
    		InputStream is = am.open(destFile);
    		BufferedInputStream bis = new BufferedInputStream(is);
    		if(f.exists()) {	f.delete(); f.createNewFile();	}
    		fos = new FileOutputStream(f);
    		bos = new BufferedOutputStream(fos);
    		
    		int read = -1;
    		byte[] buff = new byte[1024];
    		while((read = bis.read(buff,0,1024)) != -1)
    			bos.write(buff,0,read);
    		bos.flush();
    		bos.close();
    		fos.close();
    		bis.close();
    		is.close();
    		
    		rstFlag = true;
    		
    	} catch(IOException ie) {
    		Log.d("MOFError",ie.toString());
    		//showAlert("AAAAAAAAA"+ie.toString());
    		
    	} catch(Exception e) {
    		Log.d("MOFError",e.toString());
    		//showAlert("AAAAAAAAA"+e.toString());
    		
    	} finally {
    		try {
	    		if(bos != null) bos.close();
	    		if(fos != null) fos.close();
	    		if(os != null) os.close();
    		} catch(Exception ee) {
    			
    		}
    	}
    	
    	return rstFlag;
    }
    
    //Rooting check
    public boolean rootCheck(){
    	boolean rootflag = false;
    	String dataDir="/data/data/"+PkgName+"/";
    	AssetManager am = currActivity.getResources().getAssets();
    	
        try {
        	File f1 = new File("/system/app/superuser.apk");
        	File f2 = new File("/system/app/Superuser.apk");
        	File f3 = new File("/system/app/SuperUser.apk");
        	if(f1.exists() || f2.exists() || f3.exists()) {
	        	Process p = Runtime.getRuntime().exec("su");
	        	DataOutputStream os = new DataOutputStream(p.getOutputStream());
	        	os.writeBytes("echo \"Do I have root?\" > /system/temp.txt\n");
	        	os.writeBytes("exit\n");
	        	os.flush();
	        	try {
	        		p.waitFor();
	        		if(p.exitValue() != 255) {
	        			rootflag = true;	//root
	        		} else {
	        			rootflag = false;	//not root
	        		}
	        	} catch(InterruptedException ie) {
	        		rootflag = false;	//not root
	        	} catch (Exception ie2) {
	        		rootflag = false;	//not root
	        	}
	        	os.close();
        	
	        	//busybox check
	        	if(rootflag) {
	        		File f = new File("/system/xbin/busybox");
	        		if(!f.exists()) {
	        			//busybox install
	    	        	Process pp = Runtime.getRuntime().exec("su");
	    	        	os = new DataOutputStream(pp.getOutputStream());
	    	        	
	    	        	File fln = new File("/system/xbin/mount");
	    	        	if(!fln.exists()) {
	    	        		os.writeBytes("/system/xbin/busybox ln -s /system/xbin/busybox /system/xbin/mount\n");
	    	        	}
	    	        	os.writeBytes("/system/xbin/mount -t yaffs2 -o rw,remount /dev/block/mtdblock6 /system\n");
	    	        	
	    		    	createFileFromAssets(am, "Bin/busybox",dataDir+"busybox");
	    	    		os.writeBytes("cp -f "+dataDir+"busybox /system/bin/busybox\n");
	    	    		os.writeBytes("cp -f "+dataDir+"busybox /system/xbin/busybox\n");
	    		    	os.writeBytes("chmod 755 /system/bin/busybox\n");
	    		    	os.writeBytes("chmod 755 /system/xbin/busybox\n");
	    	        	
	    		    	os.writeBytes("/system/xbin/busybox ln -s /system/xbin/busybox /system/xbin/gunzip\n");
	    		    	os.writeBytes("/system/xbin/busybox ln -s /system/xbin/busybox /system/xbin/gzip\n");
	    		    	os.writeBytes("/system/xbin/busybox ln -s /system/xbin/busybox /system/xbin/kill\n");
	    		    	os.writeBytes("/system/xbin/busybox ln -s /system/xbin/busybox /system/xbin/killall\n");
	    		    	
	    	    		os.writeBytes("rm "+dataDir+"busybox\n");
	    		    	
	    	        	//os.writeBytes("/system/xbin/mount -t yaffs2 -o ro,remount /dev/block/mtdblock6 /system\n");
	    	        	os.writeBytes("exit\n");
	    	        	os.flush();
	    	        	
	            		p.waitFor();
	    	        	os.close();
	        		}
	        	}
        	} else {
        		//showAlert(currActivity.getString(R.string.messsage_AE));
        	}
        	
        } catch (Exception e) { 
        	rootflag = false;	//not root
        }
        
    	return rootflag;
    }
    
}
