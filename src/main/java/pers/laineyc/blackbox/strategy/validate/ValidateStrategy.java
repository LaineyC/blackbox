package pers.laineyc.blackbox.strategy.validate;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.springframework.util.StringUtils;
import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.util.HashMap;
import java.util.Map;

public abstract class ValidateStrategy {

    private static final Map<ValueType, ValidateStrategy> STRATEGY_MAP = new HashMap<>();

    public static ValidateStrategy getStrategy(ValueType valueType){
        return STRATEGY_MAP.get(valueType);
    }

    protected void register(ValueType valueType){
        STRATEGY_MAP.put(valueType, this);
    }

    public abstract Object validateAndBuildValue(boolean checkValue, VerifiableValue verifiableValue, Object value, String path);

    protected String buildPath(String... paths){
        return String.join(".", paths);
    }

    public static void validScript(String script, Map<String, Object> context, String fullPath){
        if(!StringUtils.hasText(script)) {
            return;
        }

        SimpleScriptContext scriptContext = new SimpleScriptContext();
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.putAll(context);

        try {
            Source source = Source.newBuilder("js", script, "$").build();
            Object message = ENGINE_LOCAL.get().eval(String.valueOf(source.getCharacters()), bindings);
            if(message != null) {
                throw new CommonException(fullPath + message);
            }
        }
        catch (CommonException e){
            throw e;
        }
        catch (Exception e){
            throw new CommonException(fullPath + "验证脚本执行异常", e);
        }
    }

    private static final ThreadLocal<ScriptEngine> ENGINE_LOCAL = ThreadLocal.withInitial(() -> GraalJSScriptEngine.create(null,
            Context.newBuilder("js")
                    //.allowAllAccess(true)
                    .allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(s -> true)
                    .option("js.ecmascript-version", "2022"))
    );

}
