package com.j.y.daddy.F50RG;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import com.j.y.daddy.F50RG.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class main extends Activity {
    
	private static Common OCOM = null;
	private boolean RootFlag = false;
	
	AssetManager am = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        OCOM = new Common(this);
        am = this.getResources().getAssets();
        
        String VersionStr = "", PkgName = "";
        try {
        	PackageInfo pkgInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
        	VersionStr = pkgInfo.versionName;
        	PkgName = pkgInfo.packageName;
        } catch(NameNotFoundException e) { }
        
        RootFlag = OCOM.rootCheck();
        //if(RootFlag) BusyboxInstall();
        
        TextView txtinfo = (TextView)findViewById(R.id.txtinfo1);
        txtinfo.setText(this.getString(R.string.appinfo)+VersionStr);
        
    	Button btn = (Button)findViewById(R.id.button1);
    	btn.setOnClickListener(new View.OnClickListener() {
 		   public void onClick(View v) {
 			   if(RootFlag) {
 				   BusyboxInstall();
 				   long dataSize = OCOM.getDiskFree("/data");
 				   if(dataSize < 30 * 1024) {
 					   OCOM.showAlert("내부 저장 공간이 부족합니다. 최소 30MB 이상 확보한 후 실행 해주세요!");
 				   } else {
 					   confirmMain(1);
 				   }
 			   } else {
 				   OCOM.showAlert("루팅이 되어 있지 않습니다! 루팅을 먼저 하세요!");
 			   }
 		   }
    	});
    	
    	Button btn2 = (Button)findViewById(R.id.button2);
    	btn2.setOnClickListener(new View.OnClickListener() {
 		   public void onClick(View v) {
 			   if(RootFlag) {
 				   BusyboxInstall();
 				   long dataSize = OCOM.getDiskFree("/data");
 				   if(dataSize < 30 * 1024) {
 					   OCOM.showAlert("내부 저장 공간이 부족합니다. 최소 30MB 이상 확보한 후 실행 해주세요!");
 				   } else {
 					   confirmMain(2);
 				   }
 			   } else {
 				   OCOM.showAlert("루팅이 되어 있지 않습니다! 루팅을 먼저 하세요!");
 			   }
 		   }
    	});
    	
    	if(!RootFlag) OCOM.showAlert("루팅이 되어 있지 않습니다! 루팅을 먼저 하세요!");
    	
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
	private int CurrMethod;
    private void confirmMain(int meth) {
    	CurrMethod = meth;
    	new AlertDialog.Builder(this).setTitle(this.getString(R.string.dialogtitle))
        .setMessage(this.getString(R.string.message01))
        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick( DialogInterface dialog, int which ) {
		    	Excute50RQPatch(CurrMethod,true);
		    }
		 })
        .setNegativeButton("취소", new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick( DialogInterface dialog, int which ) {
		        dialog.dismiss();
		    }
		 }).show();
    }

    
    //busybox check & install
    private void BusyboxInstall(){
		DataOutputStream os = null;
		String dataDir="/data/data/"+OCOM.PkgName+"/";
		
		try {
	    	Process p = Runtime.getRuntime().exec("su");
	    	os = new DataOutputStream(p.getOutputStream());
	    	os.writeBytes("mount -t yaffs2 -o rw,remount /dev/block/mtdblock6 /system\n");

	    	//busybox install
	    	OCOM.createFileFromAssets(am,"bin/busybox",dataDir+"busybox");
	    	os.writeBytes("rm /system/xbin/busybox\n");
    		os.writeBytes("cp -f "+dataDir+"busybox /system/xbin/busybox\n");
	    	os.writeBytes("chmod 755 /system/xbin/busybox\n");
    		os.writeBytes("rm "+dataDir+"busybox\n");
        	os.writeBytes("exit\n");
        	os.flush();

    		p.waitFor();
    		os.close();

    	} catch(IOException ie) {
    		ie.printStackTrace();
    		Log.d(this.getString(R.string.logerrtag),ie.toString());
    		//OCOM.showAlert(ie.toString());
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    		Log.d(this.getString(R.string.logerrtag),e.toString());
    		//OCOM.showAlert(e.toString());
    		
    	} finally {
    		try { if(os != null) os.close(); } catch(Exception e2) {}
    	}
    }
    
	//Froyo 50RQ 
    private boolean Excute50RQPatch(int meth, boolean msgFlag) {
		boolean rstFlag = false;
		DataOutputStream os = null;
		String dataDir="/data/data/"+OCOM.PkgName+"/";
		//String dataDir="/sdcard/";
		
		try {
	    	OCOM.createFileFromAssets(am,"Froyo50RG/Froyo50RG_Apptargz",dataDir+"Froyo50RG_App.tar.gz");
	    	OCOM.createFileFromAssets(am,"Froyo50RG/Froyo50RG_Frameworktargz",dataDir+"Froyo50RG_Framework.tar.gz");
	    	
	    	Process p = Runtime.getRuntime().exec("su");
	    	os = new DataOutputStream(p.getOutputStream());
	    	os.writeBytes("mount -t yaffs2 -o rw,remount /dev/block/mtdblock6 /system\n");

	    	String bbox = "/system/xbin/busybox";
	    	
	    	os.writeBytes(bbox+" rm -f /system/app/tdmb.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/TDMB.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/Mynet.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/DioIME.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/Settings.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/SettingsProvider.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/Contacts.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/ContactsProvider.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/Phone.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/PhoneConfig.apk\n");
	    	os.writeBytes(bbox+" rm -f /system/app/Mms.apk\n");
	    	
	    	os.writeBytes(bbox+" gunzip "+dataDir+"Froyo50RG_App.tar.gz\n");
	    	os.writeBytes(bbox+" gunzip "+dataDir+"Froyo50RG_Framework.tar.gz\n");
	    	
	    	os.writeBytes(bbox+" tar -xvf "+dataDir+"Froyo50RG_App.tar -C /system/app\n");
	    	os.writeBytes(bbox+" tar -xvf "+dataDir+"Froyo50RG_Framework.tar -C /system/framework\n");
	    	
	    	if(meth == 1) {
		    	os.writeBytes("wipe cache\n");
		    	os.writeBytes("wipe data\n");
		    	os.writeBytes("reboot\n");
	    	} else {
	    		os.writeBytes("reboot recovery\n");
	    	}
	    	
        	os.writeBytes("exit\n");
        	os.flush();

    		p.waitFor();
    		os.close();
			rstFlag = true;

    	} catch(IOException ie) {
    		ie.printStackTrace();
    		Log.d(this.getString(R.string.logerrtag),ie.toString());
    		OCOM.showAlert("Tar: "+ie.toString());
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    		Log.d(this.getString(R.string.logerrtag),e.toString());
    		OCOM.showAlert("Tar: "+e.toString());
    		
    	} finally {
    		try { if(os != null) os.close(); } catch(Exception e2) {}
    	}
		
		return rstFlag;
	}
    
}