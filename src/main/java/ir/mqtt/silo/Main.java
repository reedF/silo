/*
 *  Copyright 2018 Mohammad Taqi Soleimani
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

package ir.mqtt.silo;

import java.io.File;
import java.net.URL;

import ir.mqtt.silo.client.MyMqttClient;
import ir.mqtt.silo.conf.SysConfig;
import ir.mqtt.silo.database.DatabaseWorkerPool;
import ir.mqtt.silo.dispatcher.SimpleDispatcher;

public class Main {

	public static void main(String[] args) {

		SysConfig sysConfig = null;
		try {
			String f = System.getProperty("user.dir") + "\\" + SysConfig.default_confFilePath;
			URL conf = new File(f).toURI().toURL();
			sysConfig = SysConfig.getInstance(conf);
		} catch (Exception e) {
			System.err.println("ERROR...bad conf file.");
			e.printStackTrace();
			System.exit(0);
		}

		DatabaseWorkerPool.getInstance(sysConfig);

		MyMqttClient client = MyMqttClient.getInstance(sysConfig.mqttConf);
		SimpleDispatcher dispatcher = new SimpleDispatcher(sysConfig);
		client.setMqttListener(dispatcher);
		client.tryConnecting();
	}

}
