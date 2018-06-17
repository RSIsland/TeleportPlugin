package com.rsisland.plugin.teleportplugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DBAdapter extends GenericDBAdapter
{
	private final static String table_players = "teleport_players";
	private final static String table_policy = "teleport_player_policy";

	private PreparedStatement stmtCreateUser;
	private PreparedStatement stmtDeletePolicy;
	private PreparedStatement stmtUpdatePolicy;
	private PreparedStatement stmtGetPolicy;

	public DBAdapter(String jdbcURL, FormattedLogger logger) throws SQLException
	{
		super(jdbcURL, logger);
	}
	
	@Override
	protected void setupConnection() throws SQLException
	{
		stmtCreateUser = connection.prepareStatement(
				"INSERT IGNORE INTO " + table_players + " (uuid_l, uuid_m)"
						+ " VALUES (?, ?);");
		
		stmtDeletePolicy = connection.prepareStatement(
				"DELETE " + table_policy + ""
						+ " FROM " + table_policy + ""
						+ " INNER JOIN " + table_players + " ON " + table_players + ".id = " + table_policy + ".player"
						+ " WHERE " + table_players + ".uuid_l = ? AND " + table_players + ".uuid_m = ?;");
		
		stmtUpdatePolicy = connection.prepareStatement(
				"INSERT INTO " + table_policy + " (player, policy)"
						+ " SELECT " + table_players + ".id, ?"
						+ " FROM " + table_players + ""
						+ " WHERE " + table_players + ".uuid_l = ? AND " + table_players + ".uuid_m = ?"
						+ " ON DUPLICATE KEY UPDATE " + table_policy + ".policy = ?;");
		
		stmtGetPolicy = connection.prepareStatement(
				"SELECT " + table_policy + ".policy"
						+ " FROM " + table_policy + ""
						+ " INNER JOIN " + table_players + " ON " + table_players + ".id = " + table_policy + ".player"
						+ " WHERE " + table_players + ".uuid_l = ? AND " + table_players + ".uuid_m = ?;");
	}

	@Override
	protected void createTables() throws SQLException
	{
		try(Statement stmt = connection.createStatement())
		{
			stmt.executeUpdate(
				  "CREATE TABLE IF NOT EXISTS " + table_players + " ("
				+ " id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
				+ " uuid_l BIGINT NOT NULL,"
				+ " uuid_m BIGINT NOT NULL);");
			try
			{
				stmt.executeUpdate(
					  "CREATE UNIQUE INDEX " + table_players + "_uuid_uindex"
					+ " ON " + table_players + " (uuid_l, uuid_m);");
			}
			catch(SQLException e)
			{
				//Oops key seems to be present already.
			}
			stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + table_policy + " ("
				+ " id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,"
				+ " player INT NOT NULL,"
				+ " policy INT NOT NULL,"
				+ " CONSTRAINT " + table_policy + "_player_fk FOREIGN KEY (player)"
				+ " REFERENCES " + table_players + " (id)"
				+ " ON DELETE CASCADE ON UPDATE CASCADE);");
			try
			{
				stmt.executeUpdate(
					  "CREATE UNIQUE INDEX " + table_policy + "_player_uindex"
					+ " ON " + table_policy + " (player);");
			}
			catch(SQLException e)
			{
				//Oops key seems to be present already.
			}
		}
	}

	public void close()
	{
		try
		{
			stmtCreateUser.close();
			stmtDeletePolicy.close();
			stmtUpdatePolicy.close();
			stmtGetPolicy.close();
			
			connection.close();
		}
		catch (SQLException e)
		{
			//Well oops.
		}
	}

	public void savePolicy(UUID uuid, Policy policy)
	{
		try
		{
			valid();
			
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
			logger.error("Could not save data for %v.", uuid);
			e.printStackTrace();
		}
	}

	public Policy loadPolicy(UUID uuid)
	{
		try
		{
			valid();
			
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
			logger.error("Could not load data for %v.", uuid);
			e.printStackTrace();
		}
		
		return null;
	}
}
