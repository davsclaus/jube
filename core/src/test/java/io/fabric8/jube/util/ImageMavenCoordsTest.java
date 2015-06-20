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

import io.fabric8.utils.Strings;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class ImageMavenCoordsTest {

    private static Logger LOG = LoggerFactory.getLogger(ImageMavenCoordsTest.class);

    @Test
    public void testParseMavenCoordsUseDefaultPrefix() throws Exception {
        ImageMavenCoords coords = ImageMavenCoords.parse("fabric8/java", true);
        System.out.println("Parsed: " + coords);

        assertEquals("getGroupId()", "io.fabric8.jube.images.fabric8", coords.getGroupId());
        assertEquals("getArtifactId()", "java", coords.getArtifactId());
        assertTrue("Should have a valid version", Strings.isNotBlank(coords.getVersion()));
        assertEquals("getType()", "zip", coords.getType());
        assertEquals("getClassifier()", "image", coords.getClassifier());
    }

    @Test
    public void testParseMavenCoordsVersion() throws Exception {
        ImageMavenCoords coords = ImageMavenCoords.parse("fabric8/java:-1.3");
        System.out.println("Parsed: " + coords);

        assertEquals("getGroupId()", "fabric8", coords.getGroupId());
        assertEquals("getArtifactId()", "java", coords.getArtifactId());
        assertTrue("Should have a valid version", Strings.isNotBlank(coords.getVersion()));
        assertEquals("getType()", "zip", coords.getType());
        assertEquals("getClassifier()", "image", coords.getClassifier());
        assertEquals("getVersion", "-1.3", coords.getVersion());
    }

    @Test
    public void testParseMavenCoords() throws Exception {
        ImageMavenCoords coords = ImageMavenCoords.parse("fabric8/java");
        LOG.info("Parsed image maven coords: {}", coords);

        assertEquals("getGroupId()", "fabric8", coords.getGroupId());
        assertEquals("getArtifactId()", "java", coords.getArtifactId());
        assertTrue("Should have a valid version", Strings.isNotBlank(coords.getVersion()));
        assertEquals("getVersion", JubeVersionUtils.getReleaseVersion(), coords.getVersion());
        assertEquals("getType()", "zip", coords.getType());
        assertEquals("getClassifier()", "image", coords.getClassifier());
    }

}
