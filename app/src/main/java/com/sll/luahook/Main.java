package com.sll.luahook;


import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
            XSharedPreferences preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, App.PreferenerName);
            String luaScript = preferences.getString(App.PreferenerScript, "");
            if (TextUtils.isEmpty(luaScript)) return;
            runScript(luaScript, lpparam);
    }

    private void runScript(String luaScript, XC_LoadPackage.LoadPackageParam lpparam) {
        Globals globals = JsePlatform.standardGlobals();
        if (BuildConfig.DEBUG) globals.load(new DebugLib());
        globals.set("Log", CoerceJavaToLua.coerce(Log.class));
        globals.set("Class", CoerceJavaToLua.coerce(Class.class));
        globals.set("LocalLog", CoerceJavaToLua.coerce(LocalLog.class));
        globals.set("NewXCMethodHook", new NewXCMethodHook());
        globals.set("NewXCMethodReplacement",new NewXCMethodReplacement());
        globals.set("ParametersBuilder", CoerceJavaToLua.coerce(ParametersBuilder.class));
        globals.set("XposedHelper", CoerceJavaToLua.coerce(XposedHelpers.class));
        globals.set("XposedBridge", CoerceJavaToLua.coerce(XposedBridge.class));
        globals.load(luaScript).call();
        LuaValue luaHandleLoadPackage = globals.get("handleLoadPackage");
        if(!luaHandleLoadPackage.isnil()){
            luaHandleLoadPackage.call(CoerceJavaToLua.coerce(lpparam));
        }
    }
    public static class NewXCMethodReplacement extends OneArgFunction{

        @Override
        public LuaValue call(LuaValue arg) {
            return CoerceJavaToLua.coerce(new LuaXCMethodReplacement(arg));
        }
        static class LuaXCMethodReplacement extends XC_MethodReplacement {
            private LuaValue replaceHookedMethod;
            LuaXCMethodReplacement(LuaValue replaceHookedMethod) {
                this.replaceHookedMethod = replaceHookedMethod;
            }

            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return this.replaceHookedMethod.isnil()?null:this.replaceHookedMethod.call(CoerceJavaToLua.coerce(param));
            }
        }

    }
    public static class NewXCMethodHook extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return CoerceJavaToLua.coerce(new LuaXCMethodHook(arg1, arg2));
        }

        static class LuaXCMethodHook extends XC_MethodHook {
            private LuaValue beforeHookedMethod;
            private LuaValue afterHookedMethod;
             LuaXCMethodHook(LuaValue beforeHookedMethod, LuaValue afterHookedMethod) {
                this.afterHookedMethod = afterHookedMethod;
                this.beforeHookedMethod = beforeHookedMethod;
            }

            @Override
            public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if(!this.beforeHookedMethod.isnil()) {
                    this.beforeHookedMethod.call(CoerceJavaToLua.coerce(param));
                }
            }

            @Override
            public void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if(!this.afterHookedMethod.isnil()) {
                    this.afterHookedMethod.call(CoerceJavaToLua.coerce(param));
                }
            }
        }
    }
    public static class ParametersBuilder{

        private List<Object> paramList=new ArrayList<>();
        private XC_MethodHook methodHook;
        public ParametersBuilder addParameterType(Object object){
            paramList.add(object);
            return this;
        }
        public ParametersBuilder setCallback(XC_MethodHook methodHook){
            this.methodHook=methodHook;
            return this;
        }
        public Object[] toParams(){
            paramList.add(this.methodHook);
            return paramList.toArray();
        }
    }

    private static class LocalLog {
        public static String byteArrayToString(byte[] bytes) {
            String hs = "";
            String tmp = "";
            for (int n = 0; n < bytes.length; n++) {
                //整数转成十六进制表示
                tmp = (java.lang.Integer.toHexString(bytes[n] & 0XFF));
                if (tmp.length() == 1) {
                    hs = hs + "0" + tmp;
                } else {
                    hs = hs + tmp;
                }
            }
            tmp = null;
            return hs.toUpperCase(); //转成大写
        }

        public static String GetStack()
        {
            String result = "";
            Throwable ex = new Throwable();
            StackTraceElement[] stackElements = ex.getStackTrace();
            if (stackElements != null) {

                int range_start = 5;
                int range_end = Math.min(stackElements.length,7);
                if(range_end < range_start)
                    return  "";

                for (int i = range_start; i < range_end; i++) {

                    result = result + (stackElements[i].getClassName()+"->");
                    result = result + (stackElements[i].getMethodName())+"  ";
                    result = result + (stackElements[i].getFileName()+"(");
                    result = result + (stackElements[i].getLineNumber()+")\n");
                    result = result + ("-----------------------------------\n");
                }
            }
            return result;
        }
        public  static void logString(String packname,String info,String data)
        {

            String path = "/sdcard/luaxposed/"+packname+"/";
            File pather = new File(path);
            if(!pather.exists())
                pather.mkdirs();
            String currentThreadName =Thread.currentThread().getName();
            String filename = path + currentThreadName +".txt";

            //v1
//        if(data.length >= 256)
//            return;
            //v2 当大于4096时已省略号显示
            String newData;
            if(data.length()>4096){
                newData=data.substring(0,4096);
            }else {
                newData=data;
            }
            try
            {
                info = info + "\n";
                info = info + GetStack() + "\n";
                info = info + newData + "\n------------------------------------------------------------------------------------------------------------------------\n\n";
                FileWriter fw = new FileWriter(filename, true);
                fw.write(info);
                fw.close();

                Log.d("q_"+packname,info);
                XposedBridge.log("["+ packname+"]"+info);

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        public  static void logBytes(String packname,String info,byte[] data)
        {

            String path = "/sdcard/luaxposed/"+packname+"/";
            File pather = new File(path);
            if(!pather.exists())
                pather.mkdirs();
            String currentThreadName =Thread.currentThread().getName();
            String filename = path + currentThreadName +".txt";

            //v1
//        if(data.length >= 256)
//            return;
            //v2 当大于4096时已省略号显示
            byte[] newData;
            if(data.length>4096){
                newData=new byte[4096];
                System.arraycopy(data,0,newData,0,4096);
            }else {
                newData=data;
            }
            try
            {
                info = info + "\n";
                info = info + GetStack() + "\n";
                info = info + HexDumper.dumpHexString(newData) + "\n------------------------------------------------------------------------------------------------------------------------\n\n";
                FileWriter fw = new FileWriter(filename, true);
                fw.write(info);
                fw.close();

                Log.d("q_"+packname,info);
                XposedBridge.log("["+ packname+"]"+info);

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        public static void log(String s){
            Log.i("lua_xposed", s);
        }
    }
}
