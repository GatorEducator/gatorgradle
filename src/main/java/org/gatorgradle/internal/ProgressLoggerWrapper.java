// Copyright 2013-2016 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
//
// MODIFIED FROM ORIGINAL BY Saejin Mahlau-Heinert

package org.gatorgradle.internal;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps around Gradle's internal progress logger. Uses reflection
 * to provide as much compatibility to different Gradle versions
 * as possible. Note that Gradle's progress logger does not belong
 * to its public API.
 * @author Michel Kraemer
 * @author Saejin Mahlau-Heinert
 */
public class ProgressLoggerWrapper implements Serializable {
    private Logger logger;
    private Object progressLogger;

    /**
     * Create a progress logger wrapper.
     * @param project the current Gradle project
     * @param description the description for the logging
     */
    public ProgressLoggerWrapper(Project project, String description) {
        logger = project.getLogger();

        // we are about to access an internal class. Use reflection here to provide
        // as much compatibility to different Gradle versions as possible

        // get ProgressLoggerFactory class
        Class<?> progressLoggerFactoryClass = null;
        try {
            progressLoggerFactoryClass =
                Class.forName("org.gradle.internal.logging.progress.ProgressLoggerFactory");
        } catch (ClassNotFoundException ex) {
            System.err.println("ProgressLoggerWrapper Error: " + ex);
        }

        // get ProgressLoggerFactory service
        Object serviceFactory        = null;
        Object progressLoggerFactory = null;
        try {
            serviceFactory        = invoke(project, "getServices");
            progressLoggerFactory = invoke(serviceFactory, "get", progressLoggerFactoryClass);
            // get actual progress logger
            progressLogger = invoke(progressLoggerFactory, "newOperation", getClass());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            System.err.println("ProgressLoggerWrapper Error: " + ex);
        }

        try {
            invoke(progressLogger, "setDescription", description);
            invoke(progressLogger, "setLoggingHeader", description);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            System.err.println("ProgressLoggerWrapper Error: " + ex);
        }
    }

    /**
     * Invoke a method using reflection.
     * @param obj the object whose method should be invoked
     * @param method the name of the method to invoke
     * @param args the arguments to pass to the method
     * @return the method's return value
     * @throws NoSuchMethodException if the method was not found
     * @throws InvocationTargetException if the method could not be invoked
     * @throws IllegalAccessException if the method could not be accessed
    */
    private static Object invoke(Object obj, String method, Object... args)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] argumentTypes = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argumentTypes[i] = args[i].getClass();
        }
        Method met = obj.getClass().getMethod(method, argumentTypes);
        met.setAccessible(true);
        return met.invoke(obj, args);
    }

    /**
     * Invoke a method using reflection but don't throw any exceptions.
     *  Just log errors instead.
     * @param obj the object whose method should be invoked
     * @param method the name of the method to invoke
     * @param args the arguments to pass to the method
     */
    private void invokeIgnoreExceptions(Object obj, String method, Object... args) {
        try {
            invoke(obj, method, args);
        } catch (NoSuchMethodException ex) {
            logger.trace("Unable to log progress", ex);
        } catch (InvocationTargetException ex) {
            logger.trace("Unable to log progress", ex);
        } catch (IllegalAccessException ex) {
            logger.trace("Unable to log progress", ex);
        }
    }

    /**
     * Start an operation.
     */
    public void started() {
        invokeIgnoreExceptions(progressLogger, "started");
    }

    /**
     * Complete an operation.
     */
    public void completed() {
        invokeIgnoreExceptions(progressLogger, "completed");
    }

    /**
     * Set the current operation's progress.
     * @param msg the progress message
     */
    public void progress(String msg) {
        invokeIgnoreExceptions(progressLogger, "progress", msg);
    }
}
