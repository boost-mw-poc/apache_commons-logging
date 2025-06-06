/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Simple wrapper API around multiple logging APIs.
 *
 * <h2>Overview</h2>
 * <p>This package provides an API for logging in server-based applications that
 * can be used around a variety of different logging implementations, including
 * prebuilt support for the following:</p>
 * <ul>
 * <li><a href="https://logging.apache.org/log4j/">Log4J</a> (version 1.2 or later)
 * from Apache's Logging project.  Each named <a href="Log.html">Log</a>
 * instance is connected to a corresponding Log4J Logger.</li>
 * <li><a href="https://java.sun.com/j2se/1.4/docs/guide/util/logging/index.html">
 * JDK Logging API</a>, included in JDK 1.4 or later systems.  Each named
 * <a href="Log.html">Log</a> instance is connected to a corresponding
 * {@code java.util.logging.Logger} instance.</li>
 * <li><a href="https://avalon.apache.org/logkit/">LogKit</a> from Apache's
 * Avalon project.  Each named <a href="Log.html">Log</a> instance is
 * connected to a corresponding LogKit {@code Logger}.</li>
 * <li><a href="impl/NoOpLog.html">NoOpLog</a> implementation that simply swallows
 * all log output, for all named <a href="Log.html">Log</a> instances.</li>
 * <li><a href="impl/SimpleLog.html">SimpleLog</a> implementation that writes all
 * log output, for all named <a href="Log.html">Log</a> instances, to
 * System.err.</li>
 * </ul>
 * <h2>Quick Start Guide</h2>
 * <p>For those impatient to just get on with it, the following example
 * illustrates the typical declaration and use of a logger that is named (by
 * convention) after the calling class:
 * <pre>
 * import org.apache.commons.logging.Log;
 * import org.apache.commons.logging.LogFactory;
 * public class Foo {
 * private Log log = LogFactory.getLog(Foo.class);
 * public void foo() {
 * ...
 * try {
 * if (log.isDebugEnabled()) {
 * log.debug("About to do something to object " + name);
 * }
 * name.bar();
 * } catch (IllegalStateException e) {
 * log.error("Something bad happened to " + name, e);
 * }
 * ...
 * }
 * </pre>
 * <p>Unless you configure things differently, all log output will be written
 * to System.err.  Therefore, you really will want to review the remainder of
 * this page in order to understand how to configure logging for your
 * application.</p>
 * <h2>Configuring the Commons Logging Package</h2>
 * <h3>Choosing a {@code LogFactory} Implementation</h3>
 * <p>From an application perspective, the first requirement is to retrieve an
 * object reference to the {@code LogFactory} instance that will be used
 * to create {@code <a href="Log.html">Log</a>} instances for this
 * application.  This is normally accomplished by calling the static
 * {@code getFactory()} method.  This method implements the following
 * discovery algorithm to select the name of the {@code LogFactory}
 * implementation class this application wants to use:</p>
 * <ul>
 * <li>Check for a system property named
 * {@code org.apache.commons.logging.LogFactory}.</li>
 * <li>Use the JDK 1.3 JAR Services Discovery mechanism (see
 * <a href="https://java.sun.com/j2se/1.3/docs/guide/jar/jar.html">
 * https://java.sun.com/j2se/1.3/docs/guide/jar/jar.html</a> for
 * more information) to look for a resource named
 * {@code META-INF/services/org.apache.commons.logging.LogFactory}
 * whose first line is assumed to contain the desired class name.</li>
 * <li>Look for a properties file named {@code commons-logging.properties}
 * visible in the application class path, with a property named
 * {@code org.apache.commons.logging.LogFactory} defining the
 * desired implementation class name.</li>
 * <li>Fall back to a default implementation, which is described
 * further below.</li>
 * </ul>
 * <p>If a {@code commons-logging.properties} file is found, all of the
 * properties defined there are also used to set configuration attributes on
 * the instantiated {@code LogFactory} instance.</p>
 * <p>Once an implementation class name is selected, the corresponding class is
 * loaded from the current Thread context class loader (if there is one), or
 * from the class loader that loaded the {@code LogFactory} class itself
 * otherwise.  This allows a copy of {@code commons-logging.jar} to be
 * shared in a multiple class loader environment (such as a servlet container),
 * but still allow each web application to provide its own {@code LogFactory}
 * implementation, if it so desires.  An instance of this class will then be
 * created, and cached per class loader.
 * <h3>The Default {@code LogFactory} Implementation</h3>
 * <p>The Logging Package APIs include a default {@code LogFactory}
 * implementation class (<a href="impl/LogFactoryImpl.html">
 * org.apache.commons.logging.impl.LogFactoryImpl</a>) that is selected if no
 * other implementation class name can be discovered.  Its primary purpose is
 * to create (as necessary) and return <a href="Log.html">Log</a> instances
 * in response to calls to the {@code getInstance()} method.  The default
 * implementation uses the following rules:</p>
 * <ul>
 * <li>At most one {@code Log} instance of the same name will be created.
 * Subsequent {@code getInstance()} calls to the same
 * {@code LogFactory} instance, with the same name or {@code Class}
 * parameter, will return the same {@code Log} instance.</li>
 * <li>When a new {@code Log} instance must be created, the default
 * {@code LogFactory} implementation uses the following discovery
 * process:
 * <ul>
 * <li>Look for a configuration attribute of this factory named
 * {@code org.apache.commons.logging.Log} (for backwards
 * compatibility to pre-1.0 versions of this API, an attribute
 * {@code org.apache.commons.logging.log} is also consulted).</li>
 * <li>Look for a system property named
 * {@code org.apache.commons.logging.Log} (for backwards
 * compatibility to pre-1.0 versions of this API, a system property
 * {@code org.apache.commons.logging.log} is also consulted).</li>
 * <li>If the Log4J logging system is available in the application
 * class path, use the corresponding wrapper class
 * (<a href="impl/Log4JLogger.html">Log4JLogger</a>).</li>
 * <li>If the application is executing on a JDK 1.4 system, use
 * the corresponding wrapper class
 * (<a href="impl/Jdk14Logger.html">Jdk14Logger</a>).</li>
 * <li>Fall back to the default simple logging implementation
 * (<a href="impl/SimpleLog.html">SimpleLog</a>).</li>
 * </ul></li>
 * <li>Load the class of the specified name from the thread context class
 * loader (if any), or from the class loader that loaded the
 * {@code LogFactory} class otherwise.</li>
 * <li>Instantiate an instance of the selected {@code Log}
 * implementation class, passing the specified name as the single
 * argument to its constructor.</li>
 * </ul>
 * <p>See the <a href="impl/SimpleLog.html">SimpleLog</a> Javadocs for detailed
 * configuration information for this default implementation.</p>
 * <h3>Configuring the Underlying Logging System</h3>
 * <p>The basic principle is that the user is totally responsible for the
 * configuration of the underlying logging system.
 * Commons Logging should not change the existing configuration.</p>
 * <p>Each individual <a href="Log.html">Log</a> implementation may
 * support its own configuration properties.  These will be documented in the
 * class descriptions for the corresponding implementation class.</p>
 * <p>Finally, some {@code Log} implementations (such as the one for Log4J)
 * require an external configuration file for the entire logging environment.
 * This file should be prepared in a manner that is specific to the actual logging
 * technology being used.</p>
 * <h2>Using the Logging Package APIs</h2>
 * <p>Use of the Logging Package APIs, from the perspective of an application
 * component, consists of the following steps:</p>
 * <ol>
 * <li>Acquire a reference to an instance of
 * <a href="Log.html">org.apache.commons.logging.Log</a>, by calling the
 * factory method
 * <a href="LogFactory.html#getInstance(java.lang.String)">
 * LogFactory.getInstance(String name)</a>.  Your application can contain
 * references to multiple loggers that are used for different
 * purposes.  A typical scenario for a server application is to have each
 * major component of the server use its own Log instance.</li>
 * <li>Cause messages to be logged (if the corresponding detail level is enabled)
 * by calling appropriate methods ({@code trace()}, {@code debug()},
 * {@code info()}, {@code warn()}, {@code error}, and
 * {@code fatal()}).</li>
 * </ol>
 * <p>For convenience, {@code LogFactory} also offers a static method
 * {@code getLog()} that combines the typical two-step pattern:</p>
 * <pre>
 * Log log = LogFactory.getFactory().getInstance(Foo.class);
 * </pre>
 * <p>into a single method call:</p>
 * <pre>
 * Log log = LogFactory.getLog(Foo.class);
 * </pre>
 * <p>For example, you might use the following technique to initialize and
 * use a <a href="Log.html">Log</a> instance in an application component:</p>
 * <pre>
 * import org.apache.commons.logging.Log;
 * import org.apache.commons.logging.LogFactory;
 * public class MyComponent {
 * protected Log log =
 * LogFactory.getLog(MyComponent.class);
 * // Called once at startup time
 * public void start() {
 * ...
 * log.info("MyComponent started");
 * ...
 * }
 * // Called once at shutdown time
 * public void stop() {
 * ...
 * log.info("MyComponent stopped");
 * ...
 * }
 * // Called repeatedly to process a particular argument value
 * // which you want logged if debugging is enabled
 * public void process(String value) {
 * ...
 * // Do the string concatenation only if logging is enabled
 * if (log.isDebugEnabled())
 * log.debug("MyComponent processing " + value);
 * ...
 * }
 * }
 * </pre>
 */
package org.apache.commons.logging;