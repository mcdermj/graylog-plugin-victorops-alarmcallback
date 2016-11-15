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

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

/**
 * Implement the Plugin interface here.
 */
public class VictorOpsAlarmCallbackPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new VictorOpsAlarmCallbackMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new VictorOpsAlarmCallbackModule());
    }
}
