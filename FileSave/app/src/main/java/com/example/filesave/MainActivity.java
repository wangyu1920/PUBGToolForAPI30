package com.example.filesave;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import permission.GetPermission;

public class MainActivity extends FragmentActivity {
    Button getPermission;
    Button showCode;
    Button writeCode;
    Button changeCode;
    Button resetCode;
    Button setQuick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FileInputStream fis=openFileInput("isQuick");
            InputStreamReader isQuick = new InputStreamReader(fis);
            int is=isQuick.read();
            if (is == 1) {
                quickStartGame();
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        //    初始化按钮控件
        getPermission = findViewById(R.id.Button_GetPermission);
        showCode = findViewById(R.id.Button_ShowCode);
        writeCode = findViewById(R.id.Button_WriteCode);
        changeCode = findViewById(R.id.Button_ChangeCode);
        resetCode = findViewById(R.id.Button_ResetCode);
        setQuick = findViewById(R.id.Button_setQuick);
        //  《获取权限》按钮的点击监听------------------------------------------------------------
        getPermission.setOnClickListener(v -> {
            //创建一个记录日志的StringBuilder
            StringBuilder builder = new StringBuilder();
            //获取读写权限并判断是否成功
            GetPermission.verifyStoragePermissions(this);
            judgePermission();
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
            //代码文件自动写入内部存储
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.code);
                FileOutputStream fos = openFileOutput("UserCustom.txt",MODE_PRIVATE);
                int readNum;
                byte[] bytes=new byte[1024];
                while (-1 != (readNum = inputStream.read(bytes))) {
                    fos.write(bytes,0,readNum);
                }
                fos.flush();
                fos.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                builder.append("代码写入应用存储失败\n");
            }
            //判断是否有安卓11data的访问权限，没有则申请
            if (isGrant(this)) {
                if (fileCanUse(getDocumentFile(this, "Android/data"))) {
                    builder.append("data目录可读写\n");
                    if (fileCanUse(getDocumentFile(this,
                            "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"))) {
                        builder.append("UserCustom.ini可读写\n");
                    } else {
                        builder.append("访问UserCustom.ini错误：不可写\n");
                    }
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                    showCodeOnTextView(builder.toString());
                    return;
                }else{
                    builder.append("访问data目录错误：data目录未授权\n");
                }
            }
            //      申请安卓11data的访问权限
            startForRoot(this,1);
            showCodeOnTextView(builder.toString());
        });
        //  《显示代码》按钮的点击监听------------------------------------------------------------
        showCode.setOnClickListener(v -> {
            InputStream inputStream;
            try {
                inputStream= openFileInput("UserCustom.txt");
            } catch (FileNotFoundException e) {
                inputStream = getResources().openRawResource(R.raw.code);
            }
            showCodeOnTextView(inputStream);
        });
        //  写入代码文件按钮监听------------------------------------------------------------
        writeCode.setOnClickListener(v -> {
            writeCodeMethod();
            DocumentFile documentFile = getDocumentFile(this,
                    "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");

            assert documentFile != null;
            try {
                FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(documentFile.getUri());
                showCodeOnTextView(inputStream);
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
            GetPermission.verifyStoragePermissions(this);
            judgePermission();
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
                InputStream inputStream = getResources().openRawResource(R.raw.code);
                FileOutputStream fos = openFileOutput("UserCustom.txt",MODE_PRIVATE);
                int readNum;
                byte[] bytes=new byte[1024];
                while (-1 != (readNum = inputStream.read(bytes))) {
                    fos.write(bytes,0,readNum);
                }
                fos.flush();
                fos.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "重置成功", Toast.LENGTH_SHORT).show();
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





























    }
    //      读取代码，显示在屏幕
    private void showCodeOnTextView(InputStream inputStream){
        InputStreamReader reader = new InputStreamReader(inputStream);
        try {
            int readNum;
            StringBuilder builder = new StringBuilder();
            char[] chars = new char[4];
            while (-1 != (readNum = reader.read(chars))) {
                builder.append(chars,0,readNum);
            }
            ((TextView)findViewById(R.id.Text_ShowCode)).setText(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showCodeOnTextView(String s){
        System.out.println(s);
        ((TextView)findViewById(R.id.Text_ShowCode)).setText(s);
    }

    private void writeCodeMethod() {
        //设置文件地址
        DocumentFile documentFile = getDocumentFile(this,
                "Android/data/com.tencent.tmgp.pubgmhd/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini");
        assert documentFile != null;
        //文件无法访问则return
        if (!fileCanUse(documentFile)) { return; }
        //写入
        try {
            FileInputStream inputStream = openFileInput("UserCustom.txt");//代码输入流
            OutputStream os=getContentResolver().openOutputStream(documentFile.getUri());//写入流
            InputStream inputStream1 = getContentResolver().openInputStream(documentFile.getUri());//原来的代码文件，获取行数
            byte[] bytes = new byte[1024 * 512];
            byte[] bytes1 = new byte[1024 * 512];
            int data=inputStream.read(bytes);
            int oldData = inputStream1.read(bytes1);
            while ( data>0) {
                os.write(bytes,0,data);
                data=inputStream.read(bytes);
                oldData=inputStream1.read(bytes1);
            }
            if (oldData != -1) {
                int next =inputStream1.available();
                for (; next > 0; next--) {
                    os.write(10);
                }
            }
            os.flush();
            os.close();
            inputStream.close();
            inputStream1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();

    }

    private void quickStartGame() {
        writeCodeMethod();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.tencent.tmgp.pubgmhd",
                "com.epicgames.ue4.SplashActivity"));
        startActivity(intent);

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



    public static String changeToUri(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2;
    }

    //直接获取data权限，推荐使用这种方案
    public static void startForRoot(Activity context, int REQUEST_CODE_FOR_DIR) {
        String uri = changeToUri(Environment.getExternalStorageDirectory().getPath());
        uri = uri + "/document/primary%3A" + Environment.getExternalStorageDirectory().getPath().replace("/storage/emulated/0/", "").replace("/", "%2F");
        Uri parse = Uri.parse(uri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, parse);
        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        assert documentFile != null;
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR);

    }

    //根据路径获得document文件
    public static DocumentFile getDocumentFile(Context context, String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return DocumentFile.fromSingleUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2));
    }

    //判断是否已经获取了Data权限
    public static boolean isGrant(Context context) {
        for (UriPermission persistedUriPermission : context.getContentResolver().getPersistedUriPermissions()) {
            if (persistedUriPermission.isReadPermission() && persistedUriPermission.getUri().toString()
                    .equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")) {
                return true;
            }
        }
        return false;
    }

    //判断DocumentFile是否可用
    public boolean fileCanUse(DocumentFile documentFile) {
        if (documentFile.exists()) {
            if (!documentFile.canRead()) {
                Log.e("读文件夹错误", "文件夹不可读");
                return false;
            }
            if (!documentFile.canWrite()) {
                Log.e("读文件夹错误", "文件夹不可写");
                return false;
            }
            Log.d("读文件夹成功", "文件夹可读写");
            return true;
        } else {
            Log.e("读文件夹错误", "文件夹不存在");
            return false;
        }
    }

    //获取动态权限
    protected void judgePermission() {

        // 检查该权限是否已经获取
        // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

        // sd卡权限
        String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予该权限，就去提示用户请求
            ActivityCompat.requestPermissions(this, SdCardPermission, 100);
        }


        String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予该权限，就去提示用户请求
            ActivityCompat.requestPermissions(this, READ_EXTERNAL_STORAGE, 500);
        }

        String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予该权限，就去提示用户请求
            ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_STORAGE, 600);
        }

        String[] MOUNT_UNMOUNT_FILESYSTEMS = {Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
        if (ContextCompat.checkSelfPermission(this, MOUNT_UNMOUNT_FILESYSTEMS[0]) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予该权限，就去提示用户请求
            ActivityCompat.requestPermissions(this, MOUNT_UNMOUNT_FILESYSTEMS, 600);
        }

    }


}
