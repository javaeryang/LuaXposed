glpparam=nil

function handleLoadPackage(lpparam)
    glpparam=lpparam
    start()
end
function after(param)
    local result = param:getResult()
    LocalLog:logString(glpparam.packageName,"after","result:"..tostring(result))
end
function before(param)
    local arg0 = param.args[2]
    LocalLog:logString(glpparam.packageName,"before",arg0)
    param.args[2]=param.args[2]..',,,,test2'
end
function start()
    XposedBridge:log('start ::')
    LocalLog:logString(glpparam.packageName,"start","::")
    --local mHook =
    local paramsBuilder=ParametersBuilder.new()
    paramsBuilder:addParameterType('android.content.Context')
    paramsBuilder:addParameterType('java.lang.CharSequence')
    paramsBuilder:addParameterType('int')
    paramsBuilder:setCallback(NewXCMethodHook(function(param)
        local arg0 = param.args[2]
        LocalLog:logString(glpparam.packageName,"before",arg0)
        param.args[2]=param.args[2]..',,,,test3'
    end,function(param)
        local result = param:getResult()
        LocalLog:logString(glpparam.packageName,"after","result:"..tostring(result) .. "===>时长:".. param:getResult():getDuration())
    end))
    XposedHelper:findAndHookMethod(XposedHelper:findClass("android.widget.Toast",nil),"makeText",paramsBuilder:toParams())
    --mHook:beforeHookedMethod(methodParam)
    --mHook:afterHookedMethod(methodParam)
    XposedBridge:log('end ::')
    LocalLog:logString(glpparam.packageName,"end","::")

    hookActivity()

    hookWeixin()
end

function after_onCreate(param)
    local ac = param.thisObject
    LocalLog.logString("okio_jp", "Activity Create", "Name is ===>"..ac:getClass():getName())
end

function hookActivity()
    local paramsBuilder = ParametersBuilder.new()
    paramsBuilder:addParameterType('android.os.Bundle')
    paramsBuilder:setCallback(NewXCMethodHook(function (param)
        LocalLog:logString(glpparam.packageName,"before on create",param.thisObject)
    end, function (param)
        local ac = param.thisObject
        if ac == nil then
            LocalLog.log("ac is null")
        end
        local cls = ac:getClass()
        if cls == nil then
            LocalLog.log("cls is null")
            return
        end
        Log:i("lua_xposed", "========================okiiii===========================")
        LocalLog:log("hello xxxxxxxxxxxxxx"..cls:getName())
        LocalLog:logString(glpparam.packageName, "after on create ccccccccccc", param.thisObject)
    end))
    XposedHelper:findAndHookMethod(XposedHelper:findClass("android.app.Activity", glpparam.classLoader), "onCreate", paramsBuilder:toParams())
end

function hookWeixin()

end
