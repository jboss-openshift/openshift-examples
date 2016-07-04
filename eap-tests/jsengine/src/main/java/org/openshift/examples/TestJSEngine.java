/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openshift.examples;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.logging.Logger;

/**
 * Created by fspolti on 7/1/16.
 */

@Singleton
@Startup
public class TestJSEngine {

    private Logger log = Logger.getLogger(TestJSEngine.class.getName());

    /*
    * Test if the EAP has the native Java Script
    */
    @PostConstruct
    private void testEngine () {

        try {
            // getting the available JavaScript engines
            ScriptEngine engineByName = new ScriptEngineManager().getEngineByName("JavaScript");
            log.info("Engine found: " + engineByName);

            // checking for the JavaScript engine native class
            Class.forName("jdk.nashorn.api.scripting.NashornScriptEngine");
            log.info("Engine class provider found.");

        } catch (ClassNotFoundException e) {
            log.warning("JavaScript engine not found.");
        }
    }
}