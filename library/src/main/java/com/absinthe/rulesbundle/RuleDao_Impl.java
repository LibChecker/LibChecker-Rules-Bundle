package com.absinthe.rulesbundle;

import android.database.Cursor;
import android.os.CancellationSignal;

import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Generated;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RuleDao_Impl implements RuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RuleEntity> __insertionAdapterOfRuleEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllRules;

  public RuleDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRuleEntity = new EntityInsertionAdapter<RuleEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `rules_table` (`_id`,`name`,`label`,`type`,`iconIndex`,`isRegexRule`,`regexName`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, RuleEntity value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        if (value.getLabel() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getLabel());
        }
        stmt.bindLong(4, value.getType());
        stmt.bindLong(5, value.getIconIndex());
        final int _tmp = value.isRegexRule() ? 1 : 0;
        stmt.bindLong(6, _tmp);
        if (value.getRegexName() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getRegexName());
        }
      }
    };
    this.__preparedStmtOfDeleteAllRules = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM rules_table";
        return _query;
      }
    };
  }

  @Override
  public Object insertRules(final List<RuleEntity> items,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRuleEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public void deleteAllRules() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllRules.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAllRules.release(_stmt);
    }
  }

  @Override
  public Object getRule(final String name, final Continuation<? super RuleEntity> continuation) {
    final String _sql = "SELECT * from rules_table WHERE name LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RuleEntity>() {
      @Override
      public RuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIconIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "iconIndex");
          final int _cursorIndexOfIsRegexRule = CursorUtil.getColumnIndexOrThrow(_cursor, "isRegexRule");
          final int _cursorIndexOfRegexName = CursorUtil.getColumnIndexOrThrow(_cursor, "regexName");
          final RuleEntity _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final int _tmpType;
            _tmpType = _cursor.getInt(_cursorIndexOfType);
            final int _tmpIconIndex;
            _tmpIconIndex = _cursor.getInt(_cursorIndexOfIconIndex);
            final boolean _tmpIsRegexRule;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRegexRule);
            _tmpIsRegexRule = _tmp != 0;
            final String _tmpRegexName;
            if (_cursor.isNull(_cursorIndexOfRegexName)) {
              _tmpRegexName = null;
            } else {
              _tmpRegexName = _cursor.getString(_cursorIndexOfRegexName);
            }
            _result = new RuleEntity(_tmpId,_tmpName,_tmpLabel,_tmpType,_tmpIconIndex,_tmpIsRegexRule,_tmpRegexName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAllRules(final Continuation<? super List<? extends RuleEntity>> continuation) {
    final String _sql = "SELECT * from rules_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RuleEntity>>() {
      @Override
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIconIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "iconIndex");
          final int _cursorIndexOfIsRegexRule = CursorUtil.getColumnIndexOrThrow(_cursor, "isRegexRule");
          final int _cursorIndexOfRegexName = CursorUtil.getColumnIndexOrThrow(_cursor, "regexName");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final RuleEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final int _tmpType;
            _tmpType = _cursor.getInt(_cursorIndexOfType);
            final int _tmpIconIndex;
            _tmpIconIndex = _cursor.getInt(_cursorIndexOfIconIndex);
            final boolean _tmpIsRegexRule;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRegexRule);
            _tmpIsRegexRule = _tmp != 0;
            final String _tmpRegexName;
            if (_cursor.isNull(_cursorIndexOfRegexName)) {
              _tmpRegexName = null;
            } else {
              _tmpRegexName = _cursor.getString(_cursorIndexOfRegexName);
            }
            _item = new RuleEntity(_tmpId,_tmpName,_tmpLabel,_tmpType,_tmpIconIndex,_tmpIsRegexRule,_tmpRegexName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getRegexRules(final Continuation<? super List<? extends RuleEntity>> continuation) {
    final String _sql = "SELECT * from rules_table WHERE isRegexRule = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RuleEntity>>() {
      @Override
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIconIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "iconIndex");
          final int _cursorIndexOfIsRegexRule = CursorUtil.getColumnIndexOrThrow(_cursor, "isRegexRule");
          final int _cursorIndexOfRegexName = CursorUtil.getColumnIndexOrThrow(_cursor, "regexName");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final RuleEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final int _tmpType;
            _tmpType = _cursor.getInt(_cursorIndexOfType);
            final int _tmpIconIndex;
            _tmpIconIndex = _cursor.getInt(_cursorIndexOfIconIndex);
            final boolean _tmpIsRegexRule;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRegexRule);
            _tmpIsRegexRule = _tmp != 0;
            final String _tmpRegexName;
            if (_cursor.isNull(_cursorIndexOfRegexName)) {
              _tmpRegexName = null;
            } else {
              _tmpRegexName = _cursor.getString(_cursorIndexOfRegexName);
            }
            _item = new RuleEntity(_tmpId,_tmpName,_tmpLabel,_tmpType,_tmpIconIndex,_tmpIsRegexRule,_tmpRegexName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Cursor selectAllRules() {
    final String _sql = "SELECT * FROM rules_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @Override
  public Cursor selectRuleByName(final String name) {
    final String _sql = "SELECT * FROM rules_table WHERE name LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
