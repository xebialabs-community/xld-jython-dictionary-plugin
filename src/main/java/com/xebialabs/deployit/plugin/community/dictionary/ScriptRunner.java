/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.deployit.plugin.community.dictionary;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.languageFeature.reflectiveCalls;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.xebialabs.deployit.engine.spi.exception.DeployitException;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.IDictionary.DictionaryContext;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.engine.api.RepositoryService;
import com.xebialabs.deployit.engine.api.ServiceHolder;

public class ScriptRunner {
	
	public final static String KEY_CONTEXT         = "context";
	public final static String KEY_SCRIPT_NAME     = "script_name";
	public final static String KEY_LOGGER          = "logger";
	public final static String KEY_DICTIONARY      = "dictionary";
	public final static String KEY_DICT_ID         = "dictionary_id";
	public final static String KEY_ENTRIES         = "entries";
	
	
	public final static String SCRIPT_PATH             = "./ext";

	private final static ScriptEngine JYTHON_ENGINE = initJythonEngine();
	

	public static Object executeScript(EntriesWrapper entries, DictionaryContext context,String scriptName, AbstractJythonDictionary theDictionary){
		logger.debug("Executing dictionary script for {} with scriptname {}",  context, scriptName);
		Map<String, Object> pythonContext = new HashMap<String, Object>();
		pythonContext.put(KEY_CONTEXT, context);
		pythonContext.put(KEY_LOGGER, logger);
		pythonContext.put(KEY_SCRIPT_NAME, scriptName);
		pythonContext.put(KEY_DICT_ID, theDictionary.getId());
		pythonContext.put(KEY_DICTIONARY, theDictionary);
		pythonContext.put(KEY_ENTRIES, entries);
		
		String scriptClasspath = "";
		
		ScriptEngine se;
		try {
			se = loadScriptEngine(getLibraryScripts(scriptClasspath));
			Bindings bindings = createBindings(pythonContext);
			return loadLibraryScriptsAndEval(scriptName, se, bindings, scriptClasspath);
		} catch (IOException e) {
			logger.error("Error on dictionary script : scriptname {} scriptclasspath {} for dictionary {} ", scriptName, scriptClasspath, theDictionary.getId());
			logger.error("Exception on script ",e);
			throw new DeployitException(e);
		}	
	}

	

	protected static Object loadLibraryScriptsAndEval(String scriptName, ScriptEngine scriptEngine,
			Bindings localBindings, String scriptClasspath) {
		Bindings origEngineBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		String script = "";
		try {
			script = loadScript(scriptName);
			Bindings engineAndLocalScope = new SimpleBindings();
			engineAndLocalScope.putAll(origEngineBindings);
			engineAndLocalScope.putAll(localBindings);
			scriptEngine.setBindings(engineAndLocalScope, ScriptContext.ENGINE_SCOPE);
			loadLibraryScripts(getLibraryScripts(scriptClasspath), scriptEngine);
			logger.debug("Executing script " + scriptName);			
			if (logger.isTraceEnabled()) {
				logger.trace(script);
			}			
			scriptEngine.eval(script);			
			return scriptEngine.get("entries");
		} catch (IOException e){
			logger.error("IOException caught during script load : {}", scriptName, e);
			throw new ScriptExecutionException(scriptName+" "+ e.getMessage(), e);
		} catch (ScriptException e) {
			logger.error("Error while executing script",e);
			throw new ScriptExecutionException(scriptName+" "+ e.getMessage(), e);
		} finally {
			scriptEngine.setBindings(origEngineBindings, ScriptContext.ENGINE_SCOPE);
		}
	}

	protected static Bindings createBindings(Map<String, Object> variables) {
		Bindings bindings = new SimpleBindings();
		bindings.putAll(variables);
		return bindings;
	}


	protected static ScriptEngine loadScriptEngine(List<String> libraryScripts) throws IOException {
		ScriptEngine scriptEngine = ScriptRunner.JYTHON_ENGINE;		
		loadLibraryScripts(libraryScripts, scriptEngine);
		return scriptEngine;
	}

	protected static void loadLibraryScripts(List<String> libs, ScriptEngine scriptEngine) throws IOException {
		if (!libs.isEmpty()) {
			for (String library : libs) {
				String script = loadScript(library);
				checkNotNull(script, "Library {} cannot be found on class path.", library);
				try {
					scriptEngine.eval(script);
				} catch (ScriptException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	protected static String loadScript(String scriptName){
		String script = null;
		try {
			script = loadScriptFs(scriptName);
		} catch (IOException e1) {
			logger.warn("Cannot locate script on filesystem "+scriptName);
		}
		if (script!=null) return script;
		try {
			script = loadScriptResource(scriptName);
		} catch (IOException e) {
			throw new DeployitException(e);
		}
		if (script == null) throw new DeployitException("Cannot locate script "+scriptName);
		return script;
	}
	
	protected static String loadScriptResource(String scriptName) throws IOException{
		String scriptPath = scriptName;
		String script = Resources.toString(Resources.getResource(scriptPath), Charset.defaultCharset());
		return script;
	}
	
	protected static String loadScriptFs(String path) throws IOException{
		File f = new File(SCRIPT_PATH,path); 
		if (!f.exists()){
			 logger.debug("directory script not found at {}",f.getAbsolutePath());
			 return null;
		}
		String script = Files.toString(f, Charset.defaultCharset());
		return script;
	}
	
	protected static List<String> getLibraryScripts(String scriptClassPath){
		List<String> scripts = Lists.newArrayList();
		if (scriptClassPath==null) return scripts;
		if (scriptClassPath.trim().length()==0) return scripts;
		scripts.addAll(Arrays.asList(scriptClassPath.split(":")));
		return scripts;
	}
	
	private static ScriptEngine initJythonEngine() {
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("jython");
		checkNotNull(scriptEngine, "Jython Script Engine cannot be initialized. Make sure jython jars are on the class path.");
		return scriptEngine;
	}

	protected static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
}
