package com.rsisland.plugin.teleportplugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DBAdapter
{
	private final FormattedLogger logger;
	
	private final Connection connection;

	private final String playersTable = "teleport_players";
	private final String policyTable = "teleport_player_policy";

	private final PreparedStatement stmtCreateUser;
	private final PreparedStatement stmtDeletePolicy;
	private final PreparedStatement stmtUpdatePolicy;
	private final PreparedStatement stmtGetPolicy;

	public DBAdapter(String jdbcURL, FormattedLogger logger) throws ClassNotFoundException, SQLException
	{
		this.logger = logger;
		
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection(jdbcURL);
		
		stmtCreateUser = connection.prepareStatement(
				"INSERT IGNORE INTO " + playersTable + " (uuid_l, uuid_m)"
				+ " VALUES (?, ?);");
		
		stmtDeletePolicy = connection.prepareStatement(
				  "DELETE " + policyTable + ""
				+ " FROM " + policyTable + ""
				+ " INNER JOIN " + playersTable + " ON " + playersTable + ".id = " + policyTable + ".player"
				+ " WHERE " + playersTable + ".uuid_l = ? AND " + playersTable + ".uuid_m = ?;");
		
		stmtUpdatePolicy = connection.prepareStatement(
				  "INSERT INTO " + policyTable + " (player, policy)"
				+ " SELECT " + playersTable + ".id, ?"
				+ " FROM " + playersTable + ""
				+ " WHERE " + playersTable + ".uuid_l = ? AND " + playersTable + ".uuid_m = ?"
				+ " ON DUPLICATE KEY UPDATE " + policyTable + ".policy = ?;");
		
		stmtGetPolicy = connection.prepareStatement(
				  "SELECT " + policyTable + ".policy"
				+ " FROM " + policyTable + ""
				+ " INNER JOIN " + playersTable + " ON " + playersTable + ".id = " + policyTable + ".player"
				+ " WHERE " + playersTable + ".uuid_l = ? AND " + playersTable + ".uuid_m = ?;");
	}

	public void createTables() throws SQLException
	{
		try(Statement stmt = connection.createStatement())
		{
			stmt.executeUpdate(
					  "CREATE TABLE IF NOT EXISTS " + playersTable + " ("
					+ " id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
					+ " uuid_l BIGINT NOT NULL,"
					+ " uuid_m BIGINT NOT NULL);");
			try
			{
				stmt.executeUpdate(
					  "CREATE UNIQUE INDEX " + playersTable + "_uuid_uindex"
					+ " ON " + playersTable + " (uuid_l, uuid_m);");
			}
			catch(SQLException e)
			{
				//Oops key seems to be present already.
			}
			stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + policyTable + " ("
				+ " id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
				+ " player INT NOT NULL,"
				+ " policy INT NOT NULL,"
				+ " CONSTRAINT " + policyTable + "_player_fk FOREIGN KEY (player)"
				+ " REFERENCES " + playersTable + " (id)"
				+ " ON DELETE CASCADE ON UPDATE CASCADE);");
			try
			{
				stmt.executeUpdate(
					  "CREATE UNIQUE INDEX " + policyTable + "_player_uindex"
					+ " ON " + policyTable + " (player);");
			}
			catch(SQLException e)
			{
				//Oops key seems to be present already.
			}
		}
	}

	public void close() throws SQLException
	{
		stmtCreateUser.close();
		stmtDeletePolicy.close();
		stmtUpdatePolicy.close();
		stmtGetPolicy.close();
		connection.close();
	}

	public void savePolicy(UUID uuid, Policy policy)
	{
		try
		{
			//Add a user (If not already done):
			stmtCreateUser.setLong(1, uuid.getLeastSignificantBits());
			stmtCreateUser.setLong(2, uuid.getMostSignificantBits());
			stmtCreateUser.executeUpdate();
			
			//Save the policy:
			if(policy == null)
			{
				stmtDeletePolicy.setLong(1, uuid.getLeastSignificantBits());
				stmtDeletePolicy.setLong(2, uuid.getMostSignificantBits());
				stmtDeletePolicy.executeUpdate();
			}
			else
			{
				stmtUpdatePolicy.setInt(1, policy.getIdDB());
				stmtUpdatePolicy.setLong(2, uuid.getLeastSignificantBits());
				stmtUpdatePolicy.setLong(3, uuid.getMostSignificantBits());
				stmtUpdatePolicy.setInt(4, policy.getIdDB());
				stmtUpdatePolicy.executeUpdate();
			}
		}
		catch(SQLException e)
		{
			logger.error("Could not save data for %v. Check stacktrace.", uuid);
			e.printStackTrace();
		}
	}

	public Policy loadPolicy(UUID uuid)
	{
		try
		{
			stmtGetPolicy.setLong(1, uuid.getLeastSignificantBits());
			stmtGetPolicy.setLong(2, uuid.getMostSignificantBits());
			ResultSet rs = stmtGetPolicy.executeQuery();
			
			if(rs.next())
			{
				return Policy.getPolicyFromID(rs.getInt(1));
			}
		}
		catch(SQLException e)
		{
			logger.error("Could not load data for %v. Check stacktrace.", uuid);
			e.printStackTrace();
		}
		return null;
	}
}
