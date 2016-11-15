# VictorOpsAlarmCallback Plugin for Graylog

[![Build Status](https://travis-ci.org/mcdermj/graylog-plugin-victorops-alarmcallback.svg?branch=master)](https://travis-ci.org/mcdermj/graylog-plugin-victorops-alarmcallback)

This plugin provides an alert callback to post alerts from Graylog to VictorOps.

**Required Graylog version:** 2.0 and later

Installation
------------

[Download the plugin](https://github.com/mcdermj/graylog-plugin-victorops-alarmcallback/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Development
-----------

You can improve your development experience for the web interface part of your plugin
dramatically by making use of hot reloading. To do this, do the following:

* `git clone https://github.com/Graylog2/graylog2-server.git`
* `cd graylog2-server/graylog2-web-interface`
* `ln -s $YOURPLUGIN plugin/`
* `npm install && npm start`

Usage
-----

You will need to obtain an API key from VictorOps.  This is available from
Settings -> Integrations.  Click on the "Rest Endpoint" link in the right hand column
and find the API key, which will be in a UUID format such as
"8e3ba8c2-2efb-4321-9793-21a6f1115125".  On the Graylog callback configuration dialog,
paste this value into the "VictorOps API Key" field.  This field is the only one that is
strictly necessary.

The "Message Type" configuration field allows you to determine the message type passed
to the VictorOps API.  This can allow you to, for example, post a recovery alert based
on an appropriate log message, or cause VictorOps to merely post something to a timeline
rather than running through the alert process.

The "Custom Entity ID" field allows you to choose a static entity for alerts coming from
this instance of the callback.  Normally entity IDs are automatically generated in the
form "graylog/<stream_id>/<rule_id>".  This will not work when you have a different
Rule ID that posts recovery for a message because the original message and the recovery
will not be correlated.  This allows you to statically assign the entity id so that the
two rules can be correlated correctly.

The "Routing Key" field passes a routing key to VictorOps.  These correspond to the
Routing Keys entered in the VictorOps Settings->Integrations page at the bottom.  These
keys can be used to only route certain alerts to certain personnel.

This plugin will fill in a variety of optional fields in the VictorOps alert:
* `message_text` represents the entirety of the last message causing the alert
* `message_id` contains the UUID of the message in Graylog
* `message_url` contains the URL of the message in the Graylog web interface
* `rule_id` contains the UUID of the rule in Graylog
* `stream_id` contains the UUID of the stream in Graylog
* `stream_url` contains the URL of the stream in the Graylog web interface restricted to a 1 hour timeframe around the alerted message

The stream_url and message_url fields can be used to create annotations in VictorOps by
using the transmogrifier on the appropriate fields. 

Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

Plugin Release
--------------

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
