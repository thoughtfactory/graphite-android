package com.syncodec.graphite.database.quote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface QuoteTableDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(quoteDbEntry: QuoteDbEntry)

	@Query(value = "SELECT * FROM quote_table WHERE date = :date")
	suspend fun get(date: String): QuoteDbEntry?

	@Query(value = "SELECT * FROM quote_table WHERE date = :date")
	fun getAsFlow(date: String): Flow<QuoteDbEntry?>

	@Query(value = "SELECT date FROM quote_table")
	fun getAllKeys(): List<String>

	@Query(value = "SELECT date FROM quote_table")
	fun getAllKeysAsFlow(): Flow<List<String>>

	@Query(value = "DELETE FROM quote_table WHERE date = :date")
	suspend fun delete(date: String)
}
