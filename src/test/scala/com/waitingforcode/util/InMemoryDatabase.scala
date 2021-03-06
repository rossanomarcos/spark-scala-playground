package com.waitingforcode.util

import org.h2.tools.DeleteDbFiles
import java.sql.{DriverManager, ResultSet, Statement}

import com.waitingforcode.util.sql.data.DataOperation

object InMemoryDatabase {

  val DbName = "testdb"
  val DbDriver = "org.h2.Driver"
  val DbConnection = "jdbc:h2:~/" + DbName+";TRACE_LEVEL_FILE=3"
  val DbUser = "root"
  val DbPassword = ""

  lazy val connection = {
    Class.forName(DbDriver)
    val dbConnection = DriverManager.getConnection(DbConnection, DbUser, DbPassword)
    dbConnection.setAutoCommit(false)
    dbConnection
  }

  def createTable(query: String): InMemoryDatabase.type= {
    val createPreparedStatement = connection.prepareStatement(query)
    createPreparedStatement.executeUpdate()
    createPreparedStatement.close()
    connection.commit()
    this
  }

  def cleanDatabase() {
    DeleteDbFiles.execute("~", DbName, false)
  }

  def populateTable[T <: DataOperation](populateQuery: String, dataToInsert: Seq[T]) = {
    for (data <- dataToInsert) {
      val preparedStatement = connection.prepareStatement(populateQuery)
      data.populatePreparedStatement(preparedStatement)
      preparedStatement.execute()
      preparedStatement.close()
    }
    connection.commit()
  }

  // TODO : same as in InMemory - use common function
  def getRows[T](query: String, mappingFunction: ResultSet => T): Seq[T] = {
    val statement: Statement = connection.createStatement()
    val resultSet: ResultSet = statement.executeQuery(query)
    val results = new scala.collection.mutable.ListBuffer[T]()
    while (resultSet.next()) {
      results.append(mappingFunction(resultSet))
    }
    results
  }
}
