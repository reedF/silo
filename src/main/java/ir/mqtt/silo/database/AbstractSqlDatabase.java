/*
 *  Copyright 2018 Mohammad Taqi Soleimani
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

package ir.mqtt.silo.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import ir.mqtt.silo.conf.Constants;
import ir.mqtt.silo.dispatcher.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSqlDatabase extends DatabaseHelper implements DatabaseOperation {

	protected Connection connection = null;
	protected DataSource ds = null;

	public AbstractSqlDatabase(String host, int port, String dbUsername, String dbPassword, String dbName,
			long dbReconnectSeconds) {

		super(host, port, dbUsername, dbPassword, dbName, dbReconnectSeconds);
	}

	public AbstractSqlDatabase(String host, int port, String dbUsername, String dbPassword, String dbName) {
		super(host, port, dbUsername, dbPassword, dbName);
	}

	public AbstractSqlDatabase(String host, int port, String dbName) {
		super(host, port, dbName);
	}

	public AbstractSqlDatabase() {
		super();
	}

	@Override
	public void onConnected() {

	}

	public abstract void tryConnecting();

	@Override
	public void shutdown() {
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				//e.printStackTrace();
				log.error("====silo DB connection close error====", e);
			}
	}

	@Override
	public void bulkInsert(List<Message> list) {

		if (list == null || list.size() == 0)
			return;

		PreparedStatement statement = null;

		try {
			checkDbConnect();
			statement = connection.prepareStatement("INSERT INTO " + Constants.SILO_COLLECTION_NAME + " ("
					+ Constants.TOPIC + "," + Constants.MESSAGE + "," + Constants.CREATED + ") VALUES(?,?,?)");

			for (Message entry : list) {
				statement.setString(1, entry.topic);
				statement.setString(2, entry.message.toString());
				statement.setLong(3, entry.receivedAt);
				statement.addBatch();
			}

			statement.executeBatch();

		} catch (SQLException e) {
			//e.printStackTrace();
			log.error("====silo DB bulkInsert error====", e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					log.error("====silo DB bulkInsert close error====", e);
				}
		}

	}

	/**
	 * check connection for mysql to avoid mysql 8h timeout setting when it is timeout make connection unavailable
	 */
	public void checkDbConnect() {
		try {
			//check conn valid
			if (connection == null || !connection.isValid(60)) {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
				//new conn
				if (ds != null) {
					connection = ds.getConnection();
				}
			}
		} catch (SQLException e) {
			log.error("====silo DB check connection error====", e);
		}
	}
}
