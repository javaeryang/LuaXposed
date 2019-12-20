package com.sll.luahook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
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
        if(!TextUtils.isEmpty(luaScriptTv.getText().toString())){
            preferences.edit().putString(App.PreferenerScript,luaScriptTv.getText().toString()).apply();
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        }

    }
}
