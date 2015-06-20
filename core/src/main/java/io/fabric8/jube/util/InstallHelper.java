/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.jube.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import io.fabric8.utils.Closeables;
import io.fabric8.utils.Files;
import io.fabric8.utils.Objects;
import io.fabric8.utils.Strings;

/**
 * Utilities for install apps.
 */
public final class InstallHelper {
    public static final String PORTS_PROPERTIES_FILE = "ports.properties";
    public static final String ENVIRONMENT_VARIABLE_SCRIPT = "env.sh";
    public static final String ENVIRONMENT_VARIABLE_SCRIPT_WINDOWS = "env.bat";

    private static final Logger LOG = Logger.getLogger(InstallHelper.class.getName());
    private static final Matcher DEFAULT_MATCHER = new DefaultMatcher();
    private static final Matcher ALL_MATCHER = new AllMatcher();

    private InstallHelper() {
        // utulity class
    }

    /**
     * chmods the various scripts in the installation
     */
    public static void chmodAllScripts(File installDir) {
        if (installDir == null) {
            System.out.println("WARN: installDir is null!");
            return;
        }

        // make scripts executable for current dir
        chmodScripts(installDir, DEFAULT_MATCHER);
        // and all files in the bin directory, as we assume they are all scripts
        // as some images like Apache Karaf has bin scripts with no file extension (eg bin/karaf) so we use all matcher
        chmodScripts(new File(installDir, "bin"), ALL_MATCHER);

        File executables = new File(installDir, "executables.properties");
        if (executables.exists()) {
            try {
                Properties properties = new Properties();
                try (InputStream stream = new FileInputStream(executables)) {
                    properties.load(stream);
                }
                for (String dir : properties.stringPropertyNames()) {
                    String property = properties.getProperty(dir);
                    Matcher matcher = new RegexpMatcher(property);
                    LOG.info(String.format("CHMOD %s with pattern %s", dir, property));
                    chmodScripts(new File(installDir, dir), matcher);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Lets make sure all shell scripts are executable
     */
    protected static void chmodScripts(File dir, Matcher matcher) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (matcher.match(file)) {
                        //noinspection ResultOfMethodCallIgnored
                        file.setExecutable(true);
                    }
                }
            }
        }
    }

    /**
     * Inserts the environment variables to the env.sh/env.bat script file in the top
     */
    public static void writeEnvironmentVariables(File envScriptFile, Map<String, String> environmentVariables) throws IOException {
        // make sure to create file
        if (!envScriptFile.exists()) {
            envScriptFile.createNewFile();

        }
        List<String> lines = FilesHelper.readLines(envScriptFile);
        // find position to insert variables, which is before first existing environment is set
        int pos = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (Strings.isNullOrBlank(line)) {
                continue;
            }
            line = line.trim();
            if (line.startsWith("set ") || line.startsWith("export ")) {
                pos = i;
                break;
            } else {
                pos = i;
            }
        }

        // insert blank line
        lines.add(pos, "");

        Set<Map.Entry<String, String>> entries = environmentVariables.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();

            String line;
            if (envScriptFile.getName().endsWith(".bat")) {
                // do not quote on windows
                line = "set " + name + "=" + value;
            } else {
                line = "export " + name + "=\"" + value + "\"";
            }
            lines.add(++pos, line);
        }

        // insert blank line
        lines.add(++pos, "");

        // write lines to file
        Files.writeLines(envScriptFile, lines);
    }

    /**
     * Reads the {@link #PORTS_PROPERTIES_FILE} file in the given directory
     * for the map of port name to default value
     */
    public static Map<String, String> readPortsFromDirectory(File directory) throws IOException {
        File propertiesFile = new File(directory, PORTS_PROPERTIES_FILE);
        return readPorts(propertiesFile);
    }

    /**
     * Reads the properties file returning a map of port name to default port value
     */
    public static Map<String, String> readPorts(File propertiesFile) throws IOException {
        Map<String, String> answer = new Hashtable<>();
        Properties properties = new Properties();
        if (propertiesFile.exists() && propertiesFile.isFile()) {
            properties.load(new FileInputStream(propertiesFile));
            Set<Map.Entry<Object, Object>> entries = properties.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && value != null) {
                    answer.put(key.toString(), value.toString());
                }
            }
        }
        return answer;
    }

    /**
     * Writes the ports to the ports.properties file
     */
    public static void writePorts(File portFile, Map<String, String> portMap) throws IOException {
        // lets add the old ports if there were any
        Map<String, String> oldPorts = readPorts(portFile);

        // new ports should override the old
        Map<String, String> fullPortMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        fullPortMap.putAll(oldPorts);
        fullPortMap.putAll(portMap);

        PrintStream writer = new PrintStream(new FileOutputStream(portFile));
        try {
            writer.println();

            Set<Map.Entry<String, String>> entries = fullPortMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String name = entry.getKey();
                String value = entry.getValue();

                writer.println(name.toUpperCase() + " = " + value);
            }
            writer.println();
        } finally {
            Closeables.closeQuietly(writer);
        }
    }

    /**
     * Converts the given port name to a host environment variable name
     */
    public static String portNameToHostEnvVarName(String portName) {
        return portName.toUpperCase() + "_PORT";
    }

    private interface Matcher {
        boolean match(File file);
    }

    private static final class DefaultMatcher implements Matcher {

        public boolean match(File file) {
            String name = file.getName();
            String extension = Files.getFileExtension(name);
            return Objects.equal(name, "launcher") || Objects.equal(extension, "sh") || Objects.equal(extension, "bat") || Objects.equal(extension, "cmd");
        }

    }

    private static final class AllMatcher implements Matcher {

        public boolean match(File file) {
            // always match for files
            return file.isFile();
        }

    }

    private static final class RegexpMatcher implements Matcher {
        private final Pattern pattern;

        private RegexpMatcher(String regexp) {
            pattern = Pattern.compile(regexp);
        }

        public boolean match(File file) {
            return pattern.matcher(file.getName()).matches();
        }
    }

}
