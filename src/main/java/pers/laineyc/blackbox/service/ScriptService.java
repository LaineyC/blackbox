package pers.laineyc.blackbox.service;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.springframework.stereotype.Component;
import javax.script.*;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public final class ScriptService {

//    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByName("graal.js");

//    private static final ScriptEngine ENGINE = GraalJSScriptEngine.create(null,
//            Context.newBuilder("js")
//                    .allowHostAccess(HostAccess.ALL)
//                    .allowHostClassLookup(s -> true)
//                    .option("js.ecmascript-version", "2022"));

    private static final ThreadLocal<ScriptEngine> ENGINE_LOCAL = ThreadLocal.withInitial(() -> GraalJSScriptEngine.create(null,
        Context.newBuilder("js")
            //.allowAllAccess(true)
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(s -> true)
            .option("js.ecmascript-version", "2022"))
    );

    private Map<String, Source> scriptSourceCache = new ConcurrentHashMap<>();

    public void eval(Map<String, Object> context, Reader scriptReader, String scriptName) throws ScriptException, IOException {
        SimpleScriptContext scriptContext = new SimpleScriptContext();
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.putAll(context);
//        bindings.put("polyglot.js.allowAllAccess",true);
//        bindings.put("polyglot.js.allowHostAccess", true);
//        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
        if(scriptName != null) {
            Source source = scriptSourceCache.get(scriptName);
            if(source == null) {
                source = Source.newBuilder("js", scriptReader, scriptName).build();
                scriptSourceCache.put(scriptName, source);
            }
            ENGINE_LOCAL.get().eval(String.valueOf(source.getCharacters()), bindings);
        }
        else{
            ENGINE_LOCAL.get().eval(scriptReader, bindings);
        }
    }

    public void clear(){
        scriptSourceCache = new ConcurrentHashMap<>();
    }

}
