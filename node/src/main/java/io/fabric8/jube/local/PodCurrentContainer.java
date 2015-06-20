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
package io.fabric8.jube.local;

import io.fabric8.jube.KubernetesModel;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a container in a pod in a model
 */
public class PodCurrentContainer {
    private static final transient Logger LOG = LoggerFactory.getLogger(PodCurrentContainer.class);

    private final KubernetesModel model;
    private final String podId;
    private final Pod pod;
    private final String containerId;
    private final ContainerStatus currentContainer;

    public PodCurrentContainer(KubernetesModel model, String podId, Pod pod, String containerId, ContainerStatus currentContainer) {
        this.model = model;
        this.podId = podId;
        this.pod = pod;
        this.containerId = containerId;
        this.currentContainer = currentContainer;
    }

    public KubernetesModel getModel() {
        return model;
    }

    public String getPodId() {
        return podId;
    }

    public Pod getPod() {
        return pod;
    }

    public String getContainerId() {
        return containerId;
    }

    public ContainerStatus getCurrentContainer() {
        return currentContainer;
    }

    public void containerAlive(final String id, final boolean alive) {
        NodeHelper.containerAlive(pod, id, alive);
    }
}
