function handleLoadPackage(lpparam)
start()
end
function after(param)
    local result = param:getResult()
    log("result:"..result)
end
function before(param)
    local arg0 = param.args[5]
    log("arg0:"..tostring(arg0))
end
function start()
    log('start ::')
    --local mHook =methodHook(function(param)
    --local arg0 = param.args[5]
    --log("arg0:"..tostring(arg0))
    --end,function(param)
    --local result = param:getResult()
    --log("result:"..result)
    --end)
    local paramsBuilder=ParametersBuilder.new()
    paramsBuilder:setCallback(methodHook(function(param)
        local arg0 = param.args[5]
        log("arg0:"..tostring(arg0))
    end,function(param)
        local result = param:getResult()
        log("result:"..result..param.thisObject)
    end))
    XposedHelper:findAndHookMethod(XposedHelper:findClass("com.sll.luatest.Main",nil),"main",paramsBuilder:toParams())
    --mHook:beforeHookedMethod(methodParam)
    --mHook:afterHookedMethod(methodParam)
    log('end ::')
end
