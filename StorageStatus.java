package com.j.y.daddy.F50RG;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class StorageStatus {

        static final int ERROR = -1 ;
        
        public boolean externalMemoryAvailable() {
            return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        }
        
        public long getAvailableInternalMemorySize() {
                File path = Environment.getDataDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long availableBlocks = stat.getAvailableBlocks();
                return availableBlocks * blockSize;
        }
        
        public long getTotalInternalMemorySize() {
                File path = Environment.getDataDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                return totalBlocks * blockSize;
        }
        
        public long getAvailableExternalMemorySize() {
                if(externalMemoryAvailable()) {
                        File path = Environment.getExternalStorageDirectory();
                        StatFs stat = new StatFs(path.getPath());
                        long blockSize = stat.getBlockSize();
                        long availableBlocks = stat.getAvailableBlocks();
                        return availableBlocks * blockSize;
                } else {
                        return ERROR;
                }
        }
        
        public long getTotalExternalMemorySize() {
                if(externalMemoryAvailable()) {
                        File path = Environment.getExternalStorageDirectory();
                        StatFs stat = new StatFs(path.getPath());
                        long blockSize = stat.getBlockSize();
                        long totalBlocks = stat.getBlockCount();
                        return totalBlocks * blockSize;
                } else {
                        return ERROR;
                }
        }
        
        public String formatSize(long size) {
                String suffix = null;
        
                if (size >= 1024) {
                        suffix = "KiB";
                        size /= 1024;
                        if (size >= 1024) {
                                suffix = "MiB";
                                size /= 1024;
                        }
                }
        
                StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        
                int commaOffset = resultBuffer.length() - 3;
                while (commaOffset > 0) {
                        resultBuffer.insert(commaOffset, ',');
                        commaOffset -= 3;
                }
        
                if (suffix != null) resultBuffer.append(suffix);
                return resultBuffer.toString();
        }
}
