/* Sumatora Dictionary
        Copyright (C) 2019 Nicolas Centa

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package org.happypeng.sumatora.android.sumatoradictionary.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface DictionaryBookmarkDao {
    @Query("SELECT * FROM DictionaryBookmark")
    LiveData<List<DictionaryBookmark>> getAllLive();

    @Query("SELECT DictionaryBookmark.seq, "
            + "DictionaryEntry.readingsPrio, DictionaryEntry.readings, "
            + "DictionaryEntry.writingsPrio, DictionaryEntry.writings, "
            + "DictionaryTranslation.lang, DictionaryTranslation.gloss, "
            + "DictionaryBookmark.bookmark "
            + "FROM DictionaryBookmark, DictionaryEntry, DictionaryTranslation "
            + "WHERE DictionaryBookmark.seq = DictionaryEntry.seq AND "
            + " DictionaryTranslation.seq = DictionaryBookmark.seq AND DictionaryTranslation.lang = :lang "
            + "ORDER BY DictionaryBookmark.seq")
    DataSource.Factory<Integer, DictionaryBookmarkElement> getAllDetails(String lang);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DictionaryBookmark aBookmark);

    @Delete
    void delete(DictionaryBookmark aBookmark);

    @Query("DELETE FROM DictionaryBookmark WHERE seq = :seq")
    void delete(long seq);
}