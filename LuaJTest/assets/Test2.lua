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
        LocalLog:logString(glpparam.packageName,"after","result:"..tostring(result))
    end))
    XposedHelper:findAndHookMethod(XposedHelper:findClass("android.widget.Toast",nil),"makeText",paramsBuilder:toParams())
    --mHook:beforeHookedMethod(methodParam)
    --mHook:afterHookedMethod(methodParam)
    XposedBridge:log('end ::')
    LocalLog:logString(glpparam.packageName,"end","::")
end
