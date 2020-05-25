glpparam=nil

function handleLoadPackage(lpparam)
    glpparam=lpparam
    Log:e('Test3','tostring(glpparam.packageName)~=\'com.tencent.wework\':'..tostring(glpparam.packageName)~='com.tencent.wework')
    if(tostring(glpparam.packageName)~='com.tencent.wework')then
        Log:e('Test3','Not apk:'..glpparam.packageName)
        return
    end
    Log:e('Test3','setUseDbEncrypt:start Hook:')
    dexFind(function(clsLoader)
        Log:e('Test3','setUseDbEncrypt:start Hook:'..tostring(clsLoader))
        local paramsBuilder=ParametersBuilder.new()
        paramsBuilder:addParameterType('boolean')
        paramsBuilder:setCallback(NewXCMethodHook(function(param)
            Log:e('Test3','setUseDbEncrypt:'..tostring(param.args[1]))
            LocalLog:logString(glpparam.packageName,"setUseDbEncrypt","arg0"..tostring(param.args[1]))
        end,function (param) end))
        pcall(function (clsLoader2,paramsBuilder2)
            XposedHelper:findAndHookMethod(XposedHelper:findClass("com.tencent.wework.foundation.logic.Application",clsLoader2),"setUseDbEncrypt",paramsBuilder2:toParams())
            Log:e('Test3','setUseDbEncrypt:end Hook:'..tostring(clsLoader2))
        end,clsLoader,paramsBuilder)

    end)
end
function dexFind(doHookTask)
    XposedBridge:hookAllMethods(XposedHelper:findClass('android.content.ContextWrapper'),'attachBaseContext',NewXCMethodHook(function(param)
        local cl=param.args[1]:getClassLoader()
        if(cl~=nil)then
            if(doHookTask~=nil)then
                doHookTask(cl)
            end
        end
    end,function(param) end))
    XposedBridge:hookAllConstructors(XposedHelper:findClass('java.lang.ClassLoader'),NewXCMethodHook(function(param)
        local cl=param.args[1]
        if(cl~=nil)then
            if(doHookTask~=nil)then
                doHookTask(cl)
            end
        end
    end,function(param) end))
end
