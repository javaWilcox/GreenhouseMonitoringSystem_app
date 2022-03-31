package com.example.wenshi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBOpenHelper extends SQLiteOpenHelper {

	public MyDBOpenHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
		//重写构造方法，在这里创建一个名为SC_Database的数据库
		super(context,name ,factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE wenshi(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"device INTEGER," +
				"trwendu INTEGER," +
				"trshidu INTEGER," +
				"hwendu INTEGER," +
				"hshidu INTEGER," +
				"guangq INTEGER," +
				"dianliu INTEGER," +
				"shijian TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
		// 在数据库需要升级时被调用，一般用来删除旧的数据库表，并将数据转移到新版本的数据库表中
		_db.execSQL("DROP TABLE IF EXISTS wenshi");
		onCreate(_db);
	}
}

