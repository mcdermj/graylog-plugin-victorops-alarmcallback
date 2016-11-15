/**
 * Copyright 2016 Micro. of Oregon, Inc.
 *
 * This file is part of the VictorOps Alert Plugin for Graylog.
 *
 * The VictorOps Alert Plugin for Graylog is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The VictorOps Alert Plugin for Graylog is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The VictorOps Alert Plugin for Graylog.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.ipservices.alarmcallback.victorops;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class VictorOpsAlarmCallbackMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "com/ipservices/alarmcallback/victorops/graylog-plugin-victorops-alarmcallback/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "com.ipservices.alarmcallback.victorops.VictorOpsAlarmCallbackPlugin";
    }

    @Override
    public String getName() {
        return "VictorOps Alarm Callback";
    }

    @Override
    public String getAuthor() {
        return "Jeremy McDermond <jeremy.mcdermond@ipservices.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/mcdermj/graylog-plugin-victorops-alarmcallback");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "A plugin for forwarding alarms to VictorOps";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
