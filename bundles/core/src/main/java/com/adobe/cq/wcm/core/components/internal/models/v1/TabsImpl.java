/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2018 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.models.v1;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ContainerExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.ListItem;
import com.adobe.cq.wcm.core.components.models.Tabs;
import com.adobe.cq.wcm.core.components.models.datalayer.ComponentData;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = {Tabs.class, ComponentExporter.class, ContainerExporter.class},
       resourceType = TabsImpl.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
          extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class TabsImpl extends PanelContainerImpl implements Tabs {

    public final static String RESOURCE_TYPE = "core/wcm/components/tabs/v1/tabs";

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String activeItem;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String accessibilityLabel;

    @SlingObject
    private Resource resource;

    @Self
    SlingHttpServletRequest request;

    private String activeItemName;

    @Override
    public String getActiveItem() {
        if (activeItemName == null) {
            Resource active = resource.getChild(activeItem);
            if (active != null) {
                activeItemName = activeItem;
            } else {
                ComponentManager componentManager = request.getResourceResolver().adaptTo(ComponentManager.class);
                if (componentManager != null) {
                    for (Resource res : resource.getChildren()) {
                        if (res != null) {
                            Component component = componentManager.getComponentOfResource(res);
                            if (component != null) {
                                activeItemName = res.getName();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return activeItemName;
    }

    @Override
    public String getAccessibilityLabel() {
        return accessibilityLabel;
    }

    /*
     * DataLayerProvider implementation of field getters
     */

    @Override
    public String[] getDataLayerShownItems() {
        String activeItemName = getActiveItem();
        List<ListItem> items = getItems();
        return Optional.ofNullable(
            items.stream()
                .filter(e -> StringUtils.equals(e.getName(), activeItemName))
                .findFirst()
                .orElse(items.get(0))
                .getData())
            .map(ComponentData::getId)
            .map(item -> new String[]{item})
            .orElseGet(() -> new String[0]);
    }
}
