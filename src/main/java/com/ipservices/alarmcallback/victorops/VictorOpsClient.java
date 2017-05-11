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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.MessageSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URI;
import java.net.Proxy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;

public class VictorOpsClient {
	private final Logger LOG = LoggerFactory.getLogger(VictorOpsClient.class);

    private static final String restUrl = "https://alert.victorops.com/integrations/generic/20131114/alert/";

    private final String baseUrl;
    private final URI proxyUri;

    private final ObjectMapper objectMapper;
	private final URL apiUrl;

    private final VictorOpsMessageType messageType;

    public VictorOpsClient(final String apiKey, final VictorOpsMessageType messageType, final String baseUrl, final String routingKey, final URI proxyUri) throws AlarmCallbackConfigurationException {
		try {
            this.apiUrl = new URL(restUrl + apiKey + "/" + routingKey);
        } catch (MalformedURLException e) {
			throw new AlarmCallbackConfigurationException("Malformed URL for VictorOps API");
		}

		this.baseUrl = baseUrl;
        this.proxyUri = proxyUri;

		this.messageType = messageType;

        objectMapper = new ObjectMapper();
	}

    public void sendAlert(Stream stream, final AlertCondition.CheckResult checkResult) throws AlarmCallbackException {
        sendAlert(stream, checkResult, stream.getId() + "/" + checkResult.getTriggeredCondition().getId());
    }

    public void sendAlert(Stream stream, final AlertCondition.CheckResult checkResult, final String entityId) throws AlarmCallbackException {
        final HttpURLConnection conn;

        try {
            if(proxyUri != null) {
                final InetSocketAddress proxyAddress = new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort());
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
                conn = (HttpURLConnection) apiUrl.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) apiUrl.openConnection();
            }
            conn.setRequestMethod("POST");
        } catch (IOException e) {
            throw new AlarmCallbackException("Error while opening connection to VictorOps API.", e);
        }

        conn.setDoOutput(true);
        try (final OutputStream requestStream = conn.getOutputStream()){
            final VictorOpsAlert alert = new VictorOpsAlert(messageType, baseUrl);
            alert.build(stream, checkResult, entityId);

            requestStream.write(objectMapper.writeValueAsBytes(alert));
            requestStream.flush();

            final InputStream responseStream;
            if (conn.getResponseCode() == 200) {
                responseStream = conn.getInputStream();
            } else {
                LOG.warn("Received response {} from server.", conn.getResponseCode());
                responseStream = conn.getErrorStream();
            }

            final VictorOpsAlertReply reply = objectMapper.readValue(responseStream, VictorOpsAlertReply.class);
            if("success".equals(reply.result)) {
                LOG.debug("Successfully sent alert to VictorOps with entity id {}", reply.entityId);
            } else {
                LOG.warn("Error while creating VictorOps alert: {}", reply.message);
                throw new AlarmCallbackException("Error while creating VictorOps event: " + reply.message);
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST alert notification to VictorOps API.", e);
        }
    }
	
	public static enum VictorOpsMessageType {
		INFO, WARNING, ACKNOWLEDGEMENT, CRITICAL, RECOVERY
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class VictorOpsAlertReply {
        @JsonProperty("entity_id")
        public String entityId;
        @JsonProperty
        public String result;
        @JsonProperty
        public String message;
    }

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class VictorOpsAlert {
		@JsonProperty("message_type")
		public VictorOpsMessageType messageType;
		@JsonProperty("entity_id")
		public String entityId;
		@JsonProperty
		public Integer timestamp;
		@JsonProperty("state_start_time")
		public Long stateStartTime;
		@JsonProperty("state_message")
		public String stateMessage;
		@JsonProperty("monitoring_tool")
		public String monitoringTool;
		@JsonProperty("entity_display_name")
		public String entityDisplayName;
		@JsonProperty("ack_msg")
		public String ackMsg;
		@JsonProperty("ack_author")
		public String ackAuthor;
        @JsonProperty("stream_url")
        public String streamUrl;
        @JsonProperty("message_text")
        public String messageText;
        @JsonProperty("message_url")
        public String messageUrl;
        @JsonProperty("stream_id")
        public String streamId;
        @JsonProperty("message_id")
        public String messageId;
        @JsonProperty("rule_id")
        public String ruleId;

        private final String baseUrl;

		public VictorOpsAlert(VictorOpsMessageType messageType, String baseUrl) {
			this.messageType = messageType;
            this.baseUrl = baseUrl;
		}

		public void build(final Stream stream, final AlertCondition.CheckResult checkResult, final String entityId) {
            this.entityId = "graylog/" +  entityId;
            entityDisplayName = stream.getTitle() + "/" + checkResult.getTriggeredCondition().getTitle();

            ruleId = checkResult.getTriggeredCondition().getId();

            monitoringTool = "graylog";

            streamId = stream.getId();
            streamUrl = baseUrl + "streams/" + stream.getId() + "/messages?q=*&rangetype=relative&relative=3600";

            stateStartTime = checkResult.getTriggeredAt().toDate().getTime();
            stateMessage = checkResult.getResultDescription();

            if(checkResult.getMatchingMessages() != null &&
               !checkResult.getMatchingMessages().isEmpty()) {
                final MessageSummary message = checkResult.getMatchingMessages().get(0);
                messageText = message.getMessage();
                messageId = message.getId();
                messageUrl = baseUrl + "messages/" + message.getIndex() + "/" + message.getId();
            }
        }
	}
}
