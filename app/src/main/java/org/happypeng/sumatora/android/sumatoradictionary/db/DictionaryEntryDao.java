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

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DictionaryEntryDao {
    @Query("SELECT * FROM DictionaryEntry WHERE (readings LIKE '%' || :expr || '%' OR writings LIKE '%' || :expr || '%') AND lang=:lang " +
            "ORDER BY writings LIKE '%/' || :expr || '/%' DESC, readings LIKE '%/' || :expr || '/%' DESC, " +
            "writings LIKE '%-' || :expr || '-%' DESC, readings LIKE '%-' || :expr || '-%' DESC, " +
            "writings LIKE '%/' || :expr || '%' DESC, readings LIKE '%/' || :expr || '%' DESC, " +
            "writings LIKE '%-' || :expr || '%' DESC, readings LIKE '%-' || :expr || '%' DESC ")
    DataSource.Factory<Integer, DictionaryEntry> search(String expr, String lang);

    @Query("SELECT * FROM DictionaryEntry WHERE bookmark = :bookmark AND lang = :lang")
    DataSource.Factory<Integer, DictionaryEntry> listBookmarks(String bookmark, String lang);

    @Update
    void update(DictionaryEntry entry);
}
