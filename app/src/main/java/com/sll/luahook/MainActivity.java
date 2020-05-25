package com.sll.luahook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;


public class MainActivity extends Activity implements Runnable{
    SharedPreferences preferences;
    EditText luaScriptTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences=getSharedPreferences(App.PreferenerName,MODE_WORLD_READABLE);
        luaScriptTv = findViewById(R.id.lua_script);
        if(TextUtils.isEmpty(luaScriptTv.getText().toString())){
           String localScript= preferences.getString(App.PreferenerScript,luaScriptTv.getText().toString());
           luaScriptTv.setText(localScript);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @SuppressLint({"CommitPrefEdits", "WorldReadableFiles"})
    public void save(View view) {
        new Thread(this).start();

    }

    @Override
    public void run() {
        try{
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "luaT.lua");
            if (!file.exists()){
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len;
            byte[] bytes = new byte[1024];
            while ((len = fis.read(bytes)) != -1){
                byteArrayOutputStream.write(bytes, 0, len);
            }
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            fis.close();

            final String lua_script = new String(byteArrayOutputStream.toByteArray());
            if (lua_script.isEmpty()){
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    luaScriptTv.setText(lua_script);


                    if(!TextUtils.isEmpty(luaScriptTv.getText().toString())){
                        preferences.edit().putString(App.PreferenerScript,luaScriptTv.getText().toString()).apply();
                        Toast.makeText(MainActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }catch (Throwable throwable){

        }
    }
}
