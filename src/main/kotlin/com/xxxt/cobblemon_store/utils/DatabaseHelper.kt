package com.xxxt.cobblemon_store.utils

import com.xxxt.cobblemon_store.utils.DatabaseHelper.getUserById
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types

object DatabaseHelper {
    private const val URL = "jdbc:mysql://localhost:3306/your_database"
    private const val USER = "your_username"
    private const val PASSWORD = "your_password"

    val connection: Connection
        get() = DriverManager.getConnection(URL, USER, PASSWORD)


    // Create
    fun createUser(name: String, email: String) {
        val sql = "INSERT INTO users (name, email) VALUES (?, ?)"
        executeUpdate(sql, arrayOf(name, email))
        println("User created successfully!")
    }

    // Read all
    fun getAllUsers(): List<User> {
        return selectAll()
    }

    // Update
    fun updateUser(id: Int, name: String?, email: String?) {
        val sql = "UPDATE users SET ${getUpdateColumns(name,email)} WHERE id = ?"

        try {
            connection.use { conn ->
                val preparedStatement: PreparedStatement = conn.prepareStatement(sql)

                if (name != null) {
                    preparedStatement.setString(1, name)
                } else {
                    preparedStatement.setNull(1, Types.NULL)
                }

                if (email != null) {
                    preparedStatement.setString(2, email)
                } else {
                    preparedStatement.setNull(2, Types.NULL)
                }

                preparedStatement.setInt(3, id)

                val rowsAffected = preparedStatement.executeUpdate()
                println("Rows affected: $rowsAffected")
            }
        } catch (e: SQLException) {
            println("Error updating user: ${e.message}")
        }
    }

    // Delete
    fun deleteUser(id: Int) {
        val sql = "DELETE FROM users WHERE id = ?"

        try {
            connection.use { conn ->
                val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
                preparedStatement.setInt(1, id)

                val rowsAffected = preparedStatement.executeUpdate()
                println("Rows affected: $rowsAffected")
            }
        } catch (e: SQLException) {
            println("Error deleting user: ${e.message}")
        }
    }

    // Select by ID
    fun getUserById(id: Int): User? {
        return selectById(id)
    }

    private fun executeUpdate(sql: String, params: Array<String>) {
        try {
            connection.use { conn ->
                val preparedStatement: PreparedStatement = conn.prepareStatement(sql)

                for (i in 0 until params.size) {
                    preparedStatement.setString(i + 1, params[i])
                }

                val rowsAffected = preparedStatement.executeUpdate()
                println("Rows affected: $rowsAffected")
            }
        } catch (e: SQLException) {
            println("Error executing update: ${e.message}")
        }
    }

    private fun selectAll(): List<User> {
        val users = mutableListOf<User>()

        try {
            connection.use { conn ->
                val statement: Statement = conn.createStatement()
                val resultSet: ResultSet = statement.executeQuery("SELECT * FROM users")

                while (resultSet.next()) {
                    users.add(
                        User(
                            id = resultSet.getInt("id"),
                            name = resultSet.getString("name") ?: "",
                            email = resultSet.getString("email") ?: ""
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            println("Error selecting users: ${e.message}")
        }

        return users
    }

    private fun selectById(id: Int): User? {
        var user: User? = null

        try {
            connection.use { conn ->
                val preparedStatement: PreparedStatement = conn.prepareStatement("SELECT * FROM users WHERE id =?")
                preparedStatement.setInt(1, id)

                val resultSet: ResultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    user = User(
                        id = resultSet.getInt("id"),
                        name = resultSet.getString("name") ?: "",
                        email = resultSet.getString("email") ?: ""
                    )
                }
            }
        } catch (e: SQLException) {
            println("Error selecting user by ID: ${e.message}")
        }

        return user
    }

    private fun getUpdateColumns(name: String?, email: String?): String {
        // Returns a string of column names with placeholders for null values
        val columns = mutableListOf<String>()
        if (!name.isNullOrEmpty()) columns.add("name = ?")
        if (email != null) columns.add("email = ?")

        return "name = ${columns[0]}, email = ${columns[1]}"
    }

    data class User(
        val id: Int,
        val name: String,
        val email: String
    ) {
        override fun toString(): String {
            return "User(id=$id, name='$name', email='$email')"
        }
    }
}

fun main() {
    // Initialize the database driver
    Class.forName("com.mysql.cj.jdbc.Driver")

    println("Starting MySQL CRUD Operations Demo...")

    // Example usage:
    try {
        DatabaseHelper.connection.use { conn ->
            val sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"

            conn.prepareStatement(sql).execute()
        }
    } catch (e: ClassNotFoundException) {
        println("MySQL JDBC Driver not found. Make sure you have the MySQL Connector/J library in your classpath.")
    } catch (e: SQLException) {
        println("Error creating table: ${e.message}")
    }

    // Create a new user
//    DatabaseHelper.createUser("John Doe", "john@example.com")

    // Get all users
//    val users = getAllUsers()
//    users.forEach { println(it) }

    // Update a user (uncomment to use)
//    updateUser(1, "New Name", null)

    // Delete a user (uncomment to use)
//    deleteUser(2)
}

data class User(val id: Int, val name: String, val email: String)

fun mainDemo() {
    println("MySQL CRUD Operations Demo")
    println("---------------------------")

    println("\n1. Create Users:")
    DatabaseHelper.createUser("Alice", "alice@example.com")
    DatabaseHelper.createUser("Bob", "bob@example.com")
    DatabaseHelper.createUser("Charlie", "charlie@example.com")

    val allUsers = DatabaseHelper.getAllUsers()
    println("\n2. All users in database:")
    allUsers.forEach { user ->
        println(user)
    }

    val userIdToSelect = 1
    println("\n3. User by ID $userIdToSelect:")
    val user = getUserById(userIdToSelect)
    if (user != null) {
        println(user.toString())
    } else {
        println("User not found")
    }
}

    // Clean up the example database connection properties and table name are placeholders
    // Replace with your actual database configuration