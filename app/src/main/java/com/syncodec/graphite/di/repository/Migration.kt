package com.syncodec.graphite.di.repository

import android.util.Log
import io.realm.kotlin.dynamic.DynamicMutableRealm
import io.realm.kotlin.dynamic.DynamicRealm
import io.realm.kotlin.migration.AutomaticSchemaMigration


class RealmMigrator : AutomaticSchemaMigration {

	override fun migrate(migrationContext : AutomaticSchemaMigration.MigrationContext) {
	}

	private fun migrate0to1(oldRealm : DynamicRealm, newRealm : DynamicMutableRealm) {
		val oldSchema = oldRealm.schema()
		val newSchema = newRealm.schema()
	}

	private fun migrate0to2(oldRealm : DynamicRealm, newRealm : DynamicMutableRealm) {
		val oldSchema = oldRealm.schema()
		val newSchema = newRealm.schema()
	}
	private fun migrate1to2(oldRealm : DynamicRealm, newRealm : DynamicMutableRealm) {
		val oldSchema = oldRealm.schema()
		val newSchema = newRealm.schema()
	}
}
