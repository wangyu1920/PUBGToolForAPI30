package com.example.filesave;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;

import com.hjq.permissions.XXPermissions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import SetCode.SetCode;
import permission.GetPermissionAndTools;

public class MainActivity extends FragmentActivity {
    private final String isDelete_Key = "isDelete";
    Button getPermission;
    Button showCode;
    Button writeCode;
    Button changeCode;
    Button resetCode;
    Button setQuick;
    Button getCode;
    CheckBox isDelete;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取简单存储器  （存储是否删除服饰资源包的数据）
        SharedPreferences preferences = getSharedPreferences(isDelete_Key, MODE_PRIVATE);
        //快速模式检测和执行
        try {
            FileInputStream fis=openFileInput("isQuick");
            InputStreamReader isQuick = new InputStreamReader(fis);
            int is=isQuick.read();
            if (is == 1) {
                if (preferences.getBoolean(isDelete_Key, false)) {
                    System.out.println("删除执行");
                    setContentView(R.layout.activity_main);
                    quickStartGame();
                    deletePak();
                }
                finish();
            }
        } catch (IOException ignore) {
            System.out.println("没有开启快速模式");
        }
        setContentView(R.layout.activity_main);
        //    初始化按钮控件
        getPermission = findViewById(R.id.Button_GetPermission);
        showCode = findViewById(R.id.Button_ShowCode);
        writeCode = findViewById(R.id.Button_WriteCode);
        changeCode = findViewById(R.id.Button_ChangeCode);
        resetCode = findViewById(R.id.Button_ResetCode);
        setQuick = findViewById(R.id.Button_setQuick);
        getCode = findViewById(R.id.Button_GetCode);
        isDelete = (CheckBox) findViewById(R.id.isDelete);
        isDelete.setChecked(preferences.getBoolean(isDelete_Key,false));
        isDelete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(isDelete_Key, isChecked).apply();
            showCodeOnTextView("删除服饰资源包需要较长时间\n"+
                    "此操作只能删除而不能阻止和平精英再次下载，" +
                    "无root权限可以尝试采用修改host拦截服饰资源数据来阻止和平精英自动下载服饰资源，" +
                    "具体方法自查");
        });
        //在屏幕上显示帮助文字
        showCodeOnTextView("一款安卓11一键修改和平精英画质的应用\n\n" +
                "   帮助：\n"+"授予读写权限：\n点击将会打开系统文件管理器，需要点击下方的授权按钮\n\n"+
                "写入代码：\n将该应用的代码文件复制到和平精英的代码文件中\n\n"+
                "快速模式：\n开启后点击该应用图标将不进入该应用而直接写入代码并打开和平精英，该模式没有关闭的方法，只能通过清除数据来重置\n\n"+
                "显示代码：\n将该应用的代码文件显示出来\n\n"+
                "自定义代码：\n进入代码自定义页面，仅支持部分代码的修改\n\n"+
                "使用默认代码：\n将我设计的默认代码代码写入该应用的代码文件中\n\n"+
                "获取和平精英文件中的代码：\n顾名思义，将和平精英的代码文件复制到该应用中存储，写入代码按钮的反向操作\n\n\n"+
                "首次使用请先授权直至显示可读写\n" +
                "一种使用建议：点击使用默认代码，点击自定义代码，把编辑框里的代码复制，返回主界面，" +
                "点击获取和平精英中的代码，点击自定义代码，将编辑框的代码替换为你刚刚复制的代码，点确定返回主界面，" +
                "之后写入代码即可最安全的获得90hz+1080p+全最低画质，你也可以选择打开快速模式直接一键启动\n" +
                "注意：默认代码只兼容安卓11，对安卓10设备无效，不同安卓11设备支持的代码也可能不同（总之是很玄学），" +
                "该软件虽然可用于安卓10设备，但内部逻辑均针对安卓11设置，软件在安卓10上运行部分逻辑可能异常（不影响使用）\n\n\n" +
                "应用文件目录:" + getFilesDir().toString() +"\n (可以在此处完全修改代码)\n"+
                "该软件将和平精英代码分为三部分存储在上面目录的三个文本文件中：\nBackUp.txt（无法修改部分）\nProfile.txt（可修改但本软件不支持修改的部分）\n" +
                "canChange.txt（本软件支持自定义修改的部分）\n\n\n");
        //  《获取权限》按钮的点击监听------------------------------------------------------------
        getPermission.setOnClickListener(v -> getPermission());
        //  《显示代码》按钮的点击监听------------------------------------------------------------
        showCode.setOnClickListener(v -> {
            InputStream[] inputStreams;
            try {
                inputStreams=new FileInputStream[]{
                        openFileInput(SetCode.BackUpFile),
                        openFileInput(SetCode.ProfileFile),
                        openFileInput(SetCode.canChangeFile),
                };
                showCodeOnTextView(inputStreams);
            } catch (FileNotFoundException e) {
                showCodeOnTextView("当前没有画质代码");
            }

        });
        //  写入代码文件按钮监听------------------------------------------------------------
        writeCode.setOnClickListener(v -> {
            if (isDelete.isChecked()) {
                System.out.println("checked");
                deletePak();
            }
            try {
                if (Build.VERSION.SDK_INT == 30) {
                    writeCodeMethod(getContentResolver().openOutputStream(
                            GetPermissionAndTools.getDocumentFile(this,
                            "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerEx" +
                                    "tra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini").getUri()),
                            new InputStream[]{
                                    openFileInput(SetCode.BackUpFile),
                                    openFileInput(SetCode.ProfileFile),
                                    openFileInput(SetCode.canChangeFile)
                            });
                } else {
                    writeCodeMethod(getContentResolver().openOutputStream(
                            GetPermissionAndTools.getDocumentFile(this,
                            "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerEx" +
                                    "tra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini").getUri()),
                            new InputStream[]{
                                    openFileInput(SetCode.ProfileFile),
                                    openFileInput(SetCode.canChangeFile),
                                    openFileInput(SetCode.BackUpFile)
                            });
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "写入失败：未知错误", Toast.LENGTH_SHORT).show();
                return;
            }
            DocumentFile documentFile =
                    GetPermissionAndTools.getDocumentFile(this,
                    "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
            assert documentFile != null;
            try {
                FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(documentFile.getUri());
                showCodeOnTextView(new InputStream[]{inputStream});
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });
        //自定义代码按钮监听设置-->打开新Activity-------------------------------------------
        changeCode.setOnClickListener(v ->
                startActivity(new Intent(this, SetCodeActivity.class)));
        //重置代码按钮监听设置-->重置代码------------------------------------------------------
        resetCode.setOnClickListener(v -> {
            //获取读写权限并判断是否成功
            GetPermissionAndTools.verifyStoragePermissions(this);
            GetPermissionAndTools.judgePermission(this);
            //  动态获取读写权限,判断权限是否获取成功
            XXPermissions.with(this).permission(
                    new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.WRITE_EXTERNAL_STORAGE"});
            XXPermissions.with(this).permission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
            if (!XXPermissions.isGranted(this, new String[]{
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"}
            )) { Log.e("动态权限获取错误", "读写权限获取失败"); }
            if (!XXPermissions.isGranted(this,
                    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
            )) { Log.e("动态权限获取错误", "移动和修改权限获取失败"); }
            //代码文件自动写入内部存储
            try {
                SetCode.getBackUpCode(getResources().openRawResource(R.raw.const_code),
                        openFileOutput(SetCode.BackUpFile, MODE_PRIVATE));
                SetCode.getProfileCode(getResources().openRawResource(R.raw.const_code),
                        openFileOutput(SetCode.ProfileFile,MODE_PRIVATE),
                        openFileOutput(SetCode.canChangeFile,MODE_PRIVATE)
                        );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            showCodeOnTextView("默认代码是1080p分辨率，不抗锯齿，鲜艳\n"+"默认代码仅支持安卓11");
            Toast.makeText(this, "重置并使用默认代码成功", Toast.LENGTH_SHORT).show();
        });
        //快速模式监听器-->弹出警告对话框等等--------------------------------------------------
        setQuick.setOnClickListener(v -> new AlertDialog.Builder(MainActivity.this)
                .setMessage("警告！\n设置快速模式将不再启动该界面而直接写入你设置的代码并进入和平精" +
                        "英，此模式一旦开启只能通过清除该应用的数据来关闭，是否启用？")
                .setPositiveButton("开启", (dialog, which) -> {
                    try {
                        FileOutputStream fos = openFileOutput("isQuick", MODE_PRIVATE);
                        OutputStreamWriter isQuick = new OutputStreamWriter(fos);
                        isQuick.write(1);
                        isQuick.flush();
                        isQuick.close();
                        Toast.makeText(this, "已开启快速模式", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("取消",null).show());
        //获取代码按钮监听器-->获取和平精英UserCustom.ini文件中的代码
        getCode.setOnClickListener(v -> {
            //代码文件自动写入内部存储
            try {
                DocumentFile documentFile = GetPermissionAndTools.getDocumentFile(this,
                        "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
                assert documentFile != null;
                FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(documentFile.getUri());
                FileOutputStream backUp = openFileOutput(SetCode.BackUpFile,MODE_PRIVATE);
                FileOutputStream profile = openFileOutput(SetCode.ProfileFile,MODE_PRIVATE);
                FileOutputStream canChange = openFileOutput(SetCode.canChangeFile,MODE_PRIVATE);
                if (!SetCode.getBackUpCode(inputStream, backUp)) {
                    showCodeOnTextView("无法获取代码文件");
                    backUp.flush();
                    profile.flush();
                    canChange.flush();
                    backUp.close();
                    profile.close();
                    canChange.close();
                    inputStream.close();
                    return;
                }
                inputStream.close();
                inputStream = (FileInputStream) getContentResolver().openInputStream(documentFile.getUri());
                SetCode.getProfileCode(inputStream, profile, canChange);
                backUp.flush();
                profile.flush();
                canChange.flush();
                backUp.close();
                profile.close();
                canChange.close();
                inputStream.close();
                showCodeOnTextView(new FileInputStream[]{
                        openFileInput(SetCode.BackUpFile),
                        openFileInput(SetCode.ProfileFile),
                        openFileInput(SetCode.canChangeFile)});
            } catch (IOException e) {
                e.printStackTrace();
                showCodeOnTextView("获取代码失败");
            }
        });




























    }

    private void getPermission(){
        //创建一个记录日志的StringBuilder
        StringBuilder builder = new StringBuilder();

        //获取读写权限并判断是否成功
        GetPermissionAndTools.verifyStoragePermissions(this);
        GetPermissionAndTools.judgePermission(this);
        //  动态获取读写权限,判断权限是否获取成功
        XXPermissions.with(this).permission(
                new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"});
        XXPermissions.with(this).permission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        if (!XXPermissions.isGranted(this, new String[]{
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"}
        )) {
            Log.e("动态权限获取错误", "读写权限获取失败");
            builder.append("动态权限获取错误:读写权限获取失败\n");
        }
        if (!XXPermissions.isGranted(this,
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
        )) {
            Log.e("动态权限获取错误", "移动和修改权限获取失败");
        }
        //判断是否有安卓11data的访问权限，没有则申请
        if (GetPermissionAndTools.isGrant(this)) {
            if (GetPermissionAndTools.fileCanUse(GetPermissionAndTools.getDocumentFile(this, "Android/data"))) {
                builder.append("data目录可读写\n");
                if (GetPermissionAndTools.fileCanUse(GetPermissionAndTools.getDocumentFile(this,
                        "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"))) {
                    builder.append("UserCustom.ini可读写\n");
                } else {
                    builder.append("访问UserCustom.ini错误：不可写\n");
                    showCodeOnTextView(builder.toString()+"\n"
                            +"授权失败，请再次尝试");
                    return;
                }
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                showCodeOnTextView(builder.toString()+"\n"+
                        "权限齐全可以写入代码");
                return;
            }else{
                builder.append("访问data目录错误：data目录未授权\n");
            }
        }
        //      申请安卓11data的访问权限
        GetPermissionAndTools.startForRoot(this,1);
    }

    //      读取代码，显示在屏幕
    private void showCodeOnTextView(InputStream[] inputStreams){
        StringBuilder builder = new StringBuilder();
        for (InputStream inputStream : inputStreams) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            try {
                int readNum;
                char[] chars = new char[4];
                while (-1 != (readNum = reader.read(chars))) {
                    builder.append(chars,0,readNum);
                }
            } catch (IOException e) {
                e.printStackTrace();
                builder.append(e);
            }finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    builder.append(e);
                }
            }
        }
        ((TextView)findViewById(R.id.Text_ShowCode)).setText(builder);

    }
    private void showCodeOnTextView(String s){
        ((TextView)findViewById(R.id.Text_ShowCode)).setText(s);
    }
    //写入代码
    private void writeCodeMethod(OutputStream outputStream,
                                 InputStream[] inputStreams) {
        //设置文件地址
        DocumentFile documentFile = GetPermissionAndTools.getDocumentFile(this,
                "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        assert documentFile != null;
        //文件无法访问则return
        if (!GetPermissionAndTools.fileCanUse(documentFile)) { return; }
        //写入
        try {
            FileOutputStream outputStream1 = openFileOutput("UserCustom.txt", MODE_PRIVATE);
            for (InputStream fis : inputStreams) {
                byte[] bytes = new byte[1024];
                int readNum = fis.read(bytes);
                while (readNum > 0) {
                    outputStream1.write(bytes, 0, readNum);
                    readNum=fis.read(bytes);
                }
                fis.close();
            }
            outputStream1.flush();
            outputStream1.close();
            FileInputStream inputStream = openFileInput("UserCustom.txt");//代码输入流
            InputStream inputStream1 = getContentResolver().openInputStream(documentFile.getUri());//原来的代码文件，获取行数
            byte[] bytes = new byte[1024 * 512];
            byte[] bytes1 = new byte[1024 * 512];
            int data=inputStream.read(bytes);
            int oldData = inputStream1.read(bytes1);
            while ( data>0) {
                outputStream.write(bytes,0,data);
                data=inputStream.read(bytes);
                oldData=inputStream1.read(bytes1);
            }
            if (oldData != -1) {
                int next =inputStream1.available();
                for (; next > 0; next--) {
                    outputStream.write(10);
                }
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            inputStream1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();

    }

    private void quickStartGame() {
        try {
            writeCodeMethod(getContentResolver().openOutputStream(
                    GetPermissionAndTools.getDocumentFile(this,
                    "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerEx" +
                            "tra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini").getUri()),
                    new InputStream[]{
                            openFileInput(SetCode.BackUpFile),
                            openFileInput(SetCode.ProfileFile),
                            openFileInput(SetCode.canChangeFile)
                    });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "写入失败：未知错误", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.tencent.tmgp.pubgmhd",
                "com.epicgames.ue4.SplashActivity"));
        startActivity(intent);

    }

    //删除服饰资源包的方法
    private void deletePak() {
        DocumentFile pakFile= DocumentFile.fromTreeUri(this,
                GetPermissionAndTools.getDocumentFile(this,
                        "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/" +
                                "ShadowTrackerExtra/Saved/Paks/avatarpaks/"
                ).getUri());
        if (GetPermissionAndTools.fileCanUse(pakFile)) {
            System.out.println("ok Can Do Next");
            assert pakFile != null;
            for (DocumentFile each : pakFile.listFiles()) {
                if (GetPermissionAndTools.fileCanUse(each)) {
                    if ("sign.bin".equals(each.getName())) { continue; }
                    if(each.delete()){showCodeOnTextView("正在删除服饰资源包，请勿退出程序...");}
                }
            }
            Toast.makeText(this,"服饰资源包删除完毕",Toast.LENGTH_SHORT).show();
        }
    }

    //返回授权状态
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (data == null) {
            return;
        }
        if (requestCode == 1 && (uri = data.getData()) != null) {
            getContentResolver().takePersistableUriPermission(uri, data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));//关键是这里，这个就是保存这个目录的访问权限

            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        }


    }


}
