/* Copyright 2004-2005 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.mapping;

import groovy.lang.Script;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.commons.GrailsUrlMappingsClass;
import org.codehaus.groovy.grails.commons.UrlMappingsArtefactHandler;
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * A FactoryBean for constructing the UrlMappingsHolder from the registered UrlMappings class within a
 * GrailsApplication
 *
 * @author Graeme Rocher
 * @since 0.5
 *
 * 
 *        <p/>
 *        Created: Mar 6, 2007
 *        Time: 6:48:57 PM
 */
public class UrlMappingsHolderFactoryBean implements FactoryBean, InitializingBean, GrailsApplicationAware, ServletContextAware {
    private GrailsApplication grailsApplication;
    private UrlMappingsHolder urlMappingsHolder;
    private UrlMappingEvaluator mappingEvaluator;
    private ServletContext servletContext;

    public Object getObject() throws Exception {
        return this.urlMappingsHolder;
    }

    public Class getObjectType() {
        return UrlMappingsHolder.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if(grailsApplication == null) throw new IllegalStateException("Property [grailsApplication] must be set!");

        List urlMappings = new ArrayList();
        List excludePatterns = new ArrayList();
        
        GrailsClass[] mappings = grailsApplication.getArtefacts(UrlMappingsArtefactHandler.TYPE);

        this.mappingEvaluator = new DefaultUrlMappingEvaluator(servletContext);

        for (GrailsClass mapping : mappings) {
            GrailsUrlMappingsClass mappingClass = (GrailsUrlMappingsClass) mapping;
            List grailsClassMappings;
            if (Script.class.isAssignableFrom(mappingClass.getClass())) {
                grailsClassMappings = mappingEvaluator.evaluateMappings(mappingClass.getClazz());
            }
            else {
                grailsClassMappings = mappingEvaluator.evaluateMappings(mappingClass.getMappingsClosure());
            }

            urlMappings.addAll(grailsClassMappings);
            if (mappingClass.getExcludePatterns() != null) excludePatterns.addAll(mappingClass.getExcludePatterns());
        }



        this.urlMappingsHolder = new DefaultUrlMappingsHolder(urlMappings, excludePatterns);

    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
