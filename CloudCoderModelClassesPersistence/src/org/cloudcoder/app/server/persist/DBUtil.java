// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectIndexType;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC utility methods.
 * 
 * @author David Hovemeyer
 */
public class DBUtil {
    private static final Logger logger=LoggerFactory.getLogger(DBUtil.class);
    
    /**
     * Quietly close a {@link PreparedStatement}.
     * 
     * @param stmt the PreparedStatement to close
     */
	public static void closeQuietly(PreparedStatement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close prepared statement",e);
		}
	}

	/**
	 * Quietly close a {@link ResultSet}.
	 * 
	 * @param resultSet the ResultSet to close
	 */
	public static void closeQuietly(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close result set",e);
		}
	}
	
	/**
	 * Get placeholders for an insert statement.
	 * 
	 * @param schema the schema for the object being inserted
	 * @return placeholders for an insert statement
	 */
	public static String getInsertPlaceholders(ModelObjectSchema schema) {
		StringBuilder buf = new StringBuilder();
		
		for (int i = 0; i < schema.getNumFields(); i++) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append("?");
		}
		
		return buf.toString();
	}

	/**
	 * Get insert placeholders for all fields except the unique id.
	 * For the unique id field, a literal NULL value will be specified.
	 * This is useful for generating an insert statement where the unique
	 * id is an autoincrement column.
	 * 
	 * @param schema the schema of a model object
	 * @return insert placeholders for all fields except the unique id
	 */
	public static String getInsertPlaceholdersNoId(ModelObjectSchema schema) {
		StringBuilder buf = new StringBuilder();
		
		for (ModelObjectField field : schema.getFieldList()) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(field.isUniqueId() ? "NULL" : "?");
		}
		
		return buf.toString();
	}
	
	/**
	 * Get placeholders for an update statement where all fields
	 * will be updated.
	 * 
	 * @return placeholders for an update statement where all fields will be updated
	 */
	public static String getUpdatePlaceholders(ModelObjectSchema schema) {
		return doGetUpdatePlaceholders(schema, true);
	}
	
	/**
	 * Get placeholders for an update statement where all fields except for
	 * the unique id field will be updated.
	 * 
	 * @return placeholders for an update statement where all fields except the
	 *         unique id field will be update
	 */
	public static String getUpdatePlaceholdersNoId(ModelObjectSchema schema) {
		return doGetUpdatePlaceholders(schema, false);
	}

	private static String doGetUpdatePlaceholders(ModelObjectSchema schema, boolean includeUniqueId) {
		StringBuilder buf = new StringBuilder();
		
		for (ModelObjectField field : schema.getFieldList()) {
			if (!field.isUniqueId() || includeUniqueId) {
				if (buf.length() > 0) {
					buf.append(", ");
				}
				buf.append(field.getName());
				buf.append(" = ?");
			}
		}
		
		return buf.toString();
	}
	
	/**
	 * Get a CREATE TABLE statement for creating a table with the given schema and name.
	 * 
	 * @param schema     the table's schema
	 * @param tableName  the name of the table
	 * @return the text of the CREATE TABLE statement
	 */
	public static String getCreateTableStatement(ModelObjectSchema schema, String tableName) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("CREATE TABLE `");
		sql.append(tableName);
		sql.append("` (");
		
		int count = 0;
		
		// Field descriptors
		for (ModelObjectField field : schema.getFieldList()) {
			if (count > 0) {
				sql.append(",");
			}
			sql.append("\n  `");
			sql.append(field.getName());
			sql.append("` ");
			sql.append(getSQLDatatype(field));
			
			// At the moment, we don't need to allow NULL field values.
			sql.append(" NOT NULL");
			
			if (field.getIndexType() == ModelObjectIndexType.IDENTITY) {
				sql.append(" AUTO_INCREMENT");
			}
			
			count++;
		}
		
		// Keys
		for (ModelObjectField field : schema.getFieldList()) {
			if (field.getIndexType() == ModelObjectIndexType.NONE) {
				continue;
			}
			
			if (count > 0) {
				sql.append(",");
			}
			sql.append("\n  ");
			
			switch (field.getIndexType()) {
			case IDENTITY:
				sql.append("PRIMARY KEY (`");
				sql.append(field.getName());
				sql.append("`)");
				break;
				
			case UNIQUE:
			case NON_UNIQUE:
				sql.append(field.getIndexType() == ModelObjectIndexType.UNIQUE ? "UNIQUE " : "");
				sql.append("KEY `");
				sql.append(field.getName());
				sql.append("` (`");
				sql.append(field.getName());
				sql.append("`)");
				break;
			}
		}
		
		sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8");
		
		return sql.toString();
	}

	private static Object getSQLDatatype(ModelObjectField field) {
		if (field.getType() == String.class) {
			// If the field length is Integer.MAX_VALUE, make it a text field.
			// Otherwise, make it VARCHAR.
			return field.getSize() == Integer.MAX_VALUE ? "text" : ("varchar(" + field.getSize() + ")");
		} else if (field.getType() == Short.class) {
			return "mediumint(9)";
		} else if (field.getType() == Integer.class) {
			return "int(11)";
		} else if (field.getType() == Long.class) {
			return "bigint(20)";
		} else if (field.getType() == Boolean.class) {
			return "tinyint(1)";
		} else if (Enum.class.isAssignableFrom(field.getType())) {
			// Enumeration values are represented as integers (their ordinal values)
			// in the database
			return "int(11)";
		} else {
			throw new IllegalArgumentException("Unknown field type: " + field.getType().getName());
		}
	}

	/**
	 * Execute a literal SQL statement.
	 * There is no provision for binding parameters, getting results, etc.
	 * 
	 * @param conn  the Connection to use to execute the statement
	 * @param sql   the statement
	 * @throws SQLException if an error occurs
	 */
	public static void execSql(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt  = conn.prepareStatement(sql);
			stmt.execute();
		} finally {
			closeQuietly(stmt);
		}
	}
}
