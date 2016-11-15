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

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.Nullable;

public class VictorOpsAlarmCallback implements AlarmCallback {
    private Configuration config;
    private VictorOpsClient client;
    private VictorOpsClient.VictorOpsMessageType messageType;
    private final String baseUrl;
    private URI proxyUri;

    @Inject
    public VictorOpsAlarmCallback(@Named("web_endpoint_uri") @Nullable URI webUri, @Named("rest_transport_uri") URI transportUri, @Named("web_listen_uri") URI webListenUri, @Named("http_proxy_uri") @Nullable URI httpProxyUri) {
        if(webUri != null) {
            baseUrl = webUri.toString();
        } else {
            baseUrl = transportUri.getScheme() + "://" + transportUri.getAuthority() + webListenUri.getPath();
        }

        this.proxyUri = proxyUri;
    }

    @Override
    public void initialize(Configuration configuration) throws AlarmCallbackConfigurationException {
        config = new Configuration(configuration.getSource());
        final VictorOpsClient.VictorOpsMessageType type;
        switch(configuration.getString("messageType")) {
            case "INFO":
                type = VictorOpsClient.VictorOpsMessageType.INFO;
                break;
            case "WARNING":
                type =  VictorOpsClient.VictorOpsMessageType.WARNING;
                break;
            case "ACKNOWLEDGEMENT":
                type =  VictorOpsClient.VictorOpsMessageType.ACKNOWLEDGEMENT;
                break;
            case "CRITICAL":
                type =  VictorOpsClient.VictorOpsMessageType.CRITICAL;
                break;
            case "RECOVERY":
                type =  VictorOpsClient.VictorOpsMessageType.RECOVERY;
                break;
            default:
                throw new AlarmCallbackConfigurationException("Invalid message type");
        }

        client = new VictorOpsClient(configuration.getString("apiKey"), type, baseUrl, configuration.getString("routingKey"), proxyUri);
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult checkResult) throws AlarmCallbackException {
        //  Fill this with chocolaty goodness
        if(!config.getString("customEntityId").isEmpty()) {
            client.sendAlert(stream, checkResult, config.getString("customEntityId"));
        } else {
            client.sendAlert(stream, checkResult);
        }
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        final Map<String, String> messageTypes = ImmutableMap.of(
                "INFO", "INFO",
                "WARNING", "WARNING",
                "ACKNOWLEDGEMENT", "ACKNOWLEDGEMENT",
                "CRITICAL", "CRITICAL",
                "RECOVERY", "RECOVERY");

        final ConfigurationRequest configurationRequest = new ConfigurationRequest();
        configurationRequest.addField(new TextField("apiKey", "VictorOps API Key", "", "API Key from VictorOps website.", ConfigurationField.Optional.NOT_OPTIONAL));
        configurationRequest.addField(new DropdownField("messageType", "Message Type", "WARNING", messageTypes, "VictorOps message type.", ConfigurationField.Optional.NOT_OPTIONAL));
        configurationRequest.addField(new TextField("customEntityId", "Custom Entity ID", "", "Overrides automatically constructed Entity ID with static alternative.  Useful for cases where you need implement both alert and recovery in different streams or rules.", ConfigurationField.Optional.OPTIONAL));
        configurationRequest.addField(new TextField("routingKey", "Routing Key", "everyone", "VictorOps routing key to determine routing of this alert.", ConfigurationField.Optional.NOT_OPTIONAL));

        return configurationRequest;
    }

    @Override
    public String getName() {
        return "VictorOps Alarm Callback";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return config.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        String apiKey = config.getString("apiKey");
        if(apiKey.isEmpty())
            throw new ConfigurationException("Please enter the VictorOps API Key");
    }
}