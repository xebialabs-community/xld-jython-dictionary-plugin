/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.xebialabs.deployit.plugin.community.dictionary;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.exception.DeployitException;
import com.xebialabs.deployit.plugin.api.udm.AbstractDictionary;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.IDictionary;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;
import com.xebialabs.xlplatform.documentation.PublicApiRef;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.TypeIcon; 

import static com.xebialabs.deployit.plugin.api.udm.Metadata.ConfigurationItemRoot.ENVIRONMENTS;


@Metadata(root = ENVIRONMENTS, description = "A Jython Based Dictionary.", virtual = true)
@TypeIcon(value = "icons/types/udm.Dictionary.svg")
public class AbstractJythonDictionary extends Dictionary implements IDictionary {

    private static final long serialVersionUID = -8887875970429192146L;

    private transient DictionaryContext context;

    @Property(required = true, description = "path to the Python script to execute to compute the entries")
    private String pythonScript;

    @Override
    public IDictionary applyTo(final DictionaryContext context) {
        this.context = context;
        return super.applyTo(context);
    }

    @Override
    public Map<String, String> getEntries() {
        logger.debug("get entries for "+this.getId());
        try{
            final EntriesWrapper wrapper = new EntriesWrapper();
            ScriptRunner.executeScript(wrapper, context, pythonScript, this);
            if (wrapper.getValues() == null) {
                throw new DeployitException("Empty entries for " + pythonScript);
            }
            return wrapper.getValues();
        } catch (final DeployitException dex){
            logger.error("Script execution error :", dex.getMessage());
            throw dex;
        } catch (final Throwable t){
            logger.error("Script execution error :",t);
            throw new DeployitException(t);
        }
    }

    @Override
    public String getValue(final String key) {        
        return getEntries().get(key);
    }

    protected static final Logger logger = LoggerFactory.getLogger(AbstractJythonDictionary.class);

    
}