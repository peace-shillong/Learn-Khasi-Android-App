package com.sngur.learnkhasi.roomdb;

import android.content.Context;

import com.sngur.learnkhasi.model.room.Editors;
import com.sngur.learnkhasi.model.room.FavoriteWord;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import com.sngur.learnkhasi.model.room.StoredSentence;
import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;
import com.sngur.learnkhasi.model.room.UserWordReported;
import com.sngur.learnkhasi.model.room.UserWordVoted;
import com.sngur.learnkhasi.roomdb.dao.EditorsDao;
import com.sngur.learnkhasi.roomdb.dao.FavoriteWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredEnglishWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredKhasiWordDao;
import com.sngur.learnkhasi.roomdb.dao.StoredSentenceDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserSentenceVotesDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordReportedDao;
import com.sngur.learnkhasi.roomdb.dao.UserWordVotesDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {UserSentenceVotes.class, UserSentenceReported.class, StoredSentence.class,UserWordReported.class,UserWordVoted.class, FavoriteWord.class, StoredKhasiWord.class, StoredEnglishWord.class, Editors.class}, version = 1, exportSchema = false)
public abstract class LearnKhasiDatabase extends RoomDatabase {

    private static volatile LearnKhasiDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserSentenceVotesDao userSentenceVotesDao();
    public abstract UserSentenceReportedDao userSentenceReportedDao();
    public abstract StoredSentenceDao storedSentenceDao();

    public abstract UserWordVotesDao userWordVotesDao();
    public abstract UserWordReportedDao userWordReportedDao();
    public abstract FavoriteWordDao favoriteWordDao();

    public abstract StoredEnglishWordDao storedEnglishWordDao();
    public abstract StoredKhasiWordDao storedKhasiWordDao();
    public abstract EditorsDao editorsDao();

    //Migration
//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            // Since we didn't alter the table, there's nothing else to do here.
//            database.execSQL("CREATE TABLE user_sentence_reported (fromLanguage TEXT, sentenceId TEXT  NOT NULL, reported INTEGER NOT NULL, PRIMARY KEY(sentenceId))");
//        }
//    };
//
//    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            //database.execSQL("ALTER TABLE user_sentence_reported RENAME COLUMN vote TO reported");
//        }
//    };

    //getDatabase returns the singleton.
    public static LearnKhasiDatabase  getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LearnKhasiDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LearnKhasiDatabase.class, "learnkhasi")
                            //.addCallback(sRoomDatabaseCallback)
                            //.addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

}
