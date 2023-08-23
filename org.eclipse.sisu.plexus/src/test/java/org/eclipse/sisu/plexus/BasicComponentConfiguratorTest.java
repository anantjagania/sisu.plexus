/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.sisu.plexus;

import java.io.File;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


public class BasicComponentConfiguratorTest {

    private ComponentConfigurator configurator;

    @BeforeEach
    public void setUp() {
        configurator = new BasicComponentConfigurator();
    }

    @Test
    public void testSimplePathOnDefaultFileSystem(@TempDir Path tempDir)
      throws ComponentConfigurationException {
        PathTestComponent component = new PathTestComponent();
        Path absolutePath = Paths.get("").resolve("absolute").toAbsolutePath();
        configure(tempDir, component, "path", "readme.txt", "absolutePath", absolutePath.toString(), "file", "readme.txt",
          "absoluteFile", absolutePath.toString());
        // path must be converted to absolute one
        Assertions.assertEquals(tempDir.getRoot().toAbsolutePath().resolve("readme.txt"), component.path);
        Assertions.assertEquals(FileSystems.getDefault(), component.path.getFileSystem());
        Assertions.assertEquals(absolutePath, component.absolutePath);
        Assertions.assertEquals(new File(tempDir.getRoot().toFile(), "readme.txt"), component.file);
        Assertions.assertEquals(absolutePath.toFile(), component.absoluteFile);
    }

    @Test
    public void testTypeWithoutConverterButConstructorAcceptingString(@TempDir Path tempDir)
      throws ComponentConfigurationException, IOException {
        CustomTypeComponent component = new CustomTypeComponent();
        configure(tempDir, component, "custom", "hello world");
        Assertions.assertEquals("hello world", component.custom.toString());
    }

    private void configure(Path tempDir, Object component, String... keysAndValues)
      throws ComponentConfigurationException {
        final DefaultPlexusConfiguration config = new DefaultPlexusConfiguration("testConfig");
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of keys and values expected");
        }
        for (int i = 0; i < keysAndValues.length; i += 2) {
            config.addChild(keysAndValues[i], keysAndValues[i + 1]);
        }
        configure(component, config, tempDir);
    }

    private void configure(Object component, PlexusConfiguration config, Path tempDir)
      throws ComponentConfigurationException {
        final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator() {
            @Override
            public File alignToBaseDirectory(File path) {
                if (!path.isAbsolute()) {
                    return new File(tempDir.getRoot().toFile(), path.getPath());
                } else {
                    return path;
                }
            }
        };
        configurator.configureComponent(component, config, evaluator, null);
    }

    static final class PathTestComponent {

        Path path;

        Path absolutePath;

        File file;

        File absoluteFile;
    }

    public static final class CustomType {

        private final String input;

        public CustomType() {
            this.input = "invalid";
        }

        public CustomType(String input) {
            this.input = input;
        }

        @Override
        public String toString() {
            return input;
        }
    }

    static final class CustomTypeComponent {

        CustomType custom;
    }
}