package com.camnter.newlife.utils.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.camnter.newlife.framework.robotlegs.robotlegsapplication.MainApplication;
import com.camnter.newlife.utils.DeviceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Description：FileCacheHelper
 * Created by：CaMnter
 * Time：2015-10-28 25:11
 */
public class FileCacheHelper extends CacheHelper {
    private static final String CACHE_DIR = "fileCache";
    private static final String SHARED_PREFERENCE_NAME = FileCacheHelper.class + ".xml";
    private Context context;


    public FileCacheHelper(Context context) {
        super(context);
        this.context = context;
    }


    /**
     * 获取对应缓存文件
     */
    @Override public <T> T getCache(String scope, String model) {
        //TODO 读取index 判断cache
        T result = null;
        File file = new File(getCacheFile(scope, model));
        createFileFolder(file);
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fis);
            result = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 删除对应缓存
     */
    @Override public <T> T delCache(String scope, String model) {
        T result = null;
        File file = new File(getCacheFile(scope, model));
        createFileFolder(file);
        // 存在缓存文件
        if (file.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(fis);
                result = (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (file.delete()) {
            return result;
        } else {
            return null;
        }
    }


    /**
     * 修改对应缓存
     */
    @Override public <T> T modCache(CacheOption cacheOption, Object obj) {
        T result = this.delCache(cacheOption.scope, cacheOption.model);
        this.saveCache(cacheOption, obj);
        return result;
    }


    /**
     * 保存缓存
     */
    @Override public void saveCache(CacheOption cacheOption, Object obj) {
        if (cacheOption.deadlineType == DeadlineType.currentStart) {
            addToCache(cacheOption.model + "_memory", obj);
            return;
        }
        //TODO 保存cache Index
        File file = new File(getCacheFile(cacheOption.scope, cacheOption.model));
        createFileFolder(file);

        saveCacheIndex(cacheOption);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.flush();
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 缓存是否可以使用
     */
    @Override public boolean canUse(String scope, String model, int deadlineType) {
        SharedPreferences sp = context.getSharedPreferences(scope + SHARED_PREFERENCE_NAME, 0);
        String key = model;
        switch (deadlineType) {
            case DeadlineType.deadline: {
                key += "_deadline";
                break;
            }
            case DeadlineType.currentVersion: {
                key += "_currentVersion";
                break;
            }
            case DeadlineType.currentStart:
            default: {
                break;
            }
        }

        if (!sp.contains(key)) return false;

        switch (deadlineType) {
            case DeadlineType.deadline:
                long deadline = sp.getLong(key, 0);
                if (deadline > System.currentTimeMillis()) {
                    String path = getCacheFile(scope, model);
                    File file = new File(path);
                    if (file.exists()) return true;
                }
                return false;

            case DeadlineType.currentVersion:
                String version = sp.getString(key, "");
                String curVersion = DeviceUtils.getVersionCode(context);

                if (version.equals(curVersion)) {
                    String path = getCacheFile(scope, model);
                    File file = new File(path);
                    if (file.exists()) return true;
                }
                return false;

            case DeadlineType.currentStart:
            default:
                //在内存里
                return cacheExit(model + "_memory");
        }
    }


    /**
     * 将索引保存到 SharedPreferences 中
     */
    private void saveCacheIndex(CacheOption cacheOption) {
        SharedPreferences sp = context.getSharedPreferences(
                cacheOption.scope + SHARED_PREFERENCE_NAME, 0);
        String model = cacheOption.model;
        SharedPreferences.Editor edt = sp.edit();
        switch (cacheOption.deadlineType) {
            case DeadlineType.deadline:
                edt.putLong(model + "_deadline", cacheOption.deadline).apply();
                break;

            case DeadlineType.currentVersion:
                String curVersion = DeviceUtils.getVersionCode(this.context);
                edt.putString(model + "_currentVersion", curVersion).apply();
                break;
            case DeadlineType.currentStart:
            default:
                break;
        }
    }


    /**
     * 创建缓存文件夹
     */
    private boolean createFileFolder(File file) {
        File parentFile = file.getParentFile();
        return parentFile.exists() || parentFile.mkdirs();
    }


    /**
     * 获得缓存文件路径
     **/
    private String getCacheFile(String scope, String model) {
        return DeviceUtils.createAPPFolder(
                DeviceUtils.getAppProcessName(this.context, DeviceUtils.getAppProcessId()),
                MainApplication.getInstance()) + File.separator + CACHE_DIR + File.separator +
                scope + "_" + model + ".data";
    }
}
