package com.example.voca.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "voca_db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"

        // Finance table
        private const val TABLE_FINANCE = "finance"
        private const val COLUMN_FIN_ID = "id"
        private const val COLUMN_FIN_TITLE = "title"
        private const val COLUMN_FIN_AMOUNT = "amount"
        private const val COLUMN_FIN_TYPE = "type" // "income" or "expense"
        private const val COLUMN_FIN_CATEGORY = "category"
        private const val COLUMN_FIN_DATE = "date"

        // Savings Goals table
        private const val TABLE_GOALS = "savings_goals"
        private const val COLUMN_GOAL_ID = "id"
        private const val COLUMN_GOAL_NAME = "name"
        private const val COLUMN_GOAL_TARGET_AMOUNT = "target_amount"
        private const val COLUMN_GOAL_CURRENT_AMOUNT = "current_amount"
        private const val COLUMN_GOAL_DEADLINE = "deadline"

        // Event table (added for XAMPP integration example)
        const val TABLE_EVENTS = "events"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_DATE = "date"
        const val COL_LOCATION = "location"
        const val COL_PRICE = "price"
        const val COL_REGISTERED = "is_registered"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USER_NAME TEXT," +
                "$COLUMN_USER_EMAIL TEXT UNIQUE," +
                "$COLUMN_USER_PASSWORD TEXT)")
        
        val createFinanceTable = ("CREATE TABLE $TABLE_FINANCE (" +
                "$COLUMN_FIN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_FIN_TITLE TEXT," +
                "$COLUMN_FIN_AMOUNT REAL," +
                "$COLUMN_FIN_TYPE TEXT," +
                "$COLUMN_FIN_CATEGORY TEXT," +
                "$COLUMN_FIN_DATE TEXT)")

        val createGoalsTable = ("CREATE TABLE $TABLE_GOALS (" +
                "$COLUMN_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_GOAL_NAME TEXT," +
                "$COLUMN_GOAL_TARGET_AMOUNT REAL," +
                "$COLUMN_GOAL_CURRENT_AMOUNT REAL," +
                "$COLUMN_GOAL_DEADLINE TEXT)")

        val createEventsTable = ("CREATE TABLE $TABLE_EVENTS (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_NAME TEXT," +
                "$COL_DATE TEXT," +
                "$COL_LOCATION TEXT," +
                "$COL_PRICE INTEGER," +
                "$COL_REGISTERED INTEGER DEFAULT 0)")

        db?.execSQL(createUsersTable)
        db?.execSQL(createFinanceTable)
        db?.execSQL(createGoalsTable)
        db?.execSQL(createEventsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FINANCE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    // User operations
    fun addUser(name: String, email: String, pass: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, name)
        values.put(COLUMN_USER_EMAIL, email)
        values.put(COLUMN_USER_PASSWORD, pass)
        return db.insert(TABLE_USERS, null, values)
    }

    fun checkUser(email: String, pass: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=? AND $COLUMN_USER_PASSWORD=?", arrayOf(email, pass))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Finance operations
    fun addTransaction(title: String, amount: Double, type: String, category: String, date: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FIN_TITLE, title)
        values.put(COLUMN_FIN_AMOUNT, amount)
        values.put(COLUMN_FIN_TYPE, type)
        values.put(COLUMN_FIN_CATEGORY, category)
        values.put(COLUMN_FIN_DATE, date)
        return db.insert(TABLE_FINANCE, null, values)
    }

    fun getAllTransactions(): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FINANCE ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, Any>()
                map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FIN_ID))
                map["title"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_TITLE))
                map["amount"] = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FIN_AMOUNT))
                map["type"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_TYPE))
                map["category"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_CATEGORY))
                map["date"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_DATE))
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTransactionById(id: Int): Map<String, Any>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FINANCE WHERE $COLUMN_FIN_ID=?", arrayOf(id.toString()))
        var map: MutableMap<String, Any>? = null
        if (cursor.moveToFirst()) {
            map = mutableMapOf()
            map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FIN_ID))
            map["title"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_TITLE))
            map["amount"] = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FIN_AMOUNT))
            map["type"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_TYPE))
            map["category"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_CATEGORY))
            map["date"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIN_DATE))
        }
        cursor.close()
        return map
    }

    fun deleteTransaction(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_FINANCE, "$COLUMN_FIN_ID=?", arrayOf(id.toString()))
    }

    // Savings Goals operations
    fun addGoal(name: String, targetAmount: Double, currentAmount: Double, deadline: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_GOAL_NAME, name)
        values.put(COLUMN_GOAL_TARGET_AMOUNT, targetAmount)
        values.put(COLUMN_GOAL_CURRENT_AMOUNT, currentAmount)
        values.put(COLUMN_GOAL_DEADLINE, deadline)
        return db.insert(TABLE_GOALS, null, values)
    }

    fun getAllGoals(): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GOALS", null)
        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, Any>()
                map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID))
                map["name"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_NAME))
                map["target_amount"] = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET_AMOUNT))
                map["current_amount"] = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CURRENT_AMOUNT))
                map["deadline"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DEADLINE))
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateGoalAmount(id: Int, newAmount: Double): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_GOAL_CURRENT_AMOUNT, newAmount)
        return db.update(TABLE_GOALS, values, "$COLUMN_GOAL_ID=?", arrayOf(id.toString()))
    }

    fun deleteGoal(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_GOALS, "$COLUMN_GOAL_ID=?", arrayOf(id.toString()))
    }

    fun updatePassword(email: String, newPass: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_PASSWORD, newPass)
        return db.update(TABLE_USERS, values, "$COLUMN_USER_EMAIL=?", arrayOf(email))
    }
}