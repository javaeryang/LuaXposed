package com.sll.luatest;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] argv) {
        Globals globals = JsePlatform.standardGlobals();
        globals.load(new DebugLib());
        globals.set("log", new LuaLog());
        globals.set("methodHook", new MethodHook());
        MyHookParam param = new MyHookParam();
        param.args = new Object[]{1, "2", true, 1.4};
        param.result = "this is result!!";
        globals.set("ParametersBuilder",CoerceJavaToLua.coerce(ParametersBuilder.class));
        globals.set("methodParam", CoerceJavaToLua.coerce(param));
        globals.set("XposedHelper", CoerceJavaToLua.coerce(XposedHelper.class));
        globals.loadfile("assets/Test.lua").call();

    }


    private static class LuaLog extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            System.out.println("Log : " + arg.toString());
            return LuaValue.NIL;
        }
    }

    public static class MethodHook extends VarArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return CoerceJavaToLua.coerce(new MyMethodHook(arg1, arg2));
        }

    }

    public static class MyMethodHook {
        private LuaValue beforeHookedMethod;
        private LuaValue afterHookedMethod;

        public MyMethodHook(LuaValue beforeHookedMethod, LuaValue afterHookedMethod) {
            this.afterHookedMethod = afterHookedMethod;
            this.beforeHookedMethod = beforeHookedMethod;
        }

        public void beforeHookedMethod(MyHookParam param) {
            this.beforeHookedMethod.call(CoerceJavaToLua.coerce(param));
        }

        public void afterHookedMethod(MyHookParam param) {
            this.afterHookedMethod.call(CoerceJavaToLua.coerce(param));
        }

        @Override
        public String toString() {
            return "MyMethodHook";
        }
    }

    public static class MyHookParam {
        /**
         * @hide
         */
        @SuppressWarnings("deprecation")
        public MyHookParam() {
            super();
        }

        /**
         * The hooked method/constructor.
         */
        public Member method;

        /**
         * The {@code this} reference for an instance method, or {@code null} for static methods.
         */
        public Object thisObject;

        /**
         * Arguments to the method call.
         */
        public Object[] args;

        private Object result = null;
        private Throwable throwable = null;
        /* package */ boolean returnEarly = false;

        /**
         * Returns the result of the method call.
         */
        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }

        /**
         * Returns the {@link Throwable} thrown by the method, or {@code null}.
         */
        public Throwable getThrowable() {
            return throwable;
        }

        /**
         * Returns true if an exception was thrown by the method.
         */
        public boolean hasThrowable() {
            return throwable != null;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }

        /**
         * Returns the result of the method call, or throws the Throwable caused by it.
         */
        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null)
                throw throwable;
            return result;
        }

        @Override
        public String toString() {
            return "MyHookParam";
        }
    }
    public static class XposedHelper{
        public static Class<?> findClass(String className, ClassLoader classLoader) {
            if (classLoader == null)
                classLoader = ClassLoader.getSystemClassLoader();
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        public static void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
            System.out.println(clazz.getName()+","+methodName+","+parameterTypesAndCallback.length);
        }
    }
    public static class ParametersBuilder{

        private List<Object> paramList=new ArrayList<>();
        private MyMethodHook methodHook;
        public void addParameterType(Object object){
            paramList.add(object);
        }
        public void setCallback(MyMethodHook methodHook){
            this.methodHook=methodHook;
        }
        public Object[] toParams(){
            paramList.add(this.methodHook);
            return paramList.toArray();
        }
    }
}
