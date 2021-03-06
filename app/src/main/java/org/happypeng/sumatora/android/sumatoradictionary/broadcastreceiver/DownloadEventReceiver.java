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

package org.happypeng.sumatora.android.sumatoradictionary.broadcastreceiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.happypeng.sumatora.android.sumatoradictionary.DictionaryApplication;
import org.happypeng.sumatora.android.sumatoradictionary.db.InstalledDictionary;
import org.happypeng.sumatora.android.sumatoradictionary.db.LocalDictionaryObject;
import org.happypeng.sumatora.android.sumatoradictionary.db.PersistentDatabase;
import org.happypeng.sumatora.android.sumatoradictionary.db.RemoteDictionaryObject;
import org.happypeng.sumatora.android.sumatoradictionary.db.tools.ValueHolder;

import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

// Receives notifications for downloads, matches them with actions and performs them
public class DownloadEventReceiver extends BroadcastReceiver {
    private final PersistentDatabase mDB;
    private final DictionaryApplication mApp;

    public DownloadEventReceiver(@NonNull final DictionaryApplication aApp,
                                 @NonNull final PersistentDatabase aDB) {
        mDB = aDB;
        mApp = aApp;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        final int status = getStatusForDownload(downloadId, context);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final ValueHolder<List<RemoteDictionaryObject>> actions = new ValueHolder<>(null);

                mDB.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        actions.setValue(mDB.remoteDictionaryObjectDao().getAllForDownloadId(downloadId));
                    }
                });

                if (actions.getValue() == null) {
                    return null;
                }

                for (final RemoteDictionaryObject d : actions.getValue()) {
                    System.out.println("Matching action: " + d.getType() + " " + d.getLang());

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        if (mDB.remoteDictionaryObjectDao().getUpdatableCount() > 0) {
                            List<RemoteDictionaryObject> remaining = mDB.remoteDictionaryObjectDao().getUpdatableRemaining();

                            if (remaining.size() == 1 &&
                                remaining.get(0).type.equals(d.type) &&
                                remaining.get(0).lang.equals(d.lang)) {

                                System.out.println("All updates have been downloaded.");

                                performUpdates();
                            }

                            /* System.out.println("Waiting for updates remaining: ");

                            for (RemoteDictionaryObject r : remaining) {
                                System.out.println(r.description);
                            } */
                        } else {
                            installRemoteDictionaryObject(d);
                            d.setLocalFile("");
                        }
                    }

                    // Due to a bug probably inside room itself, if we don't do this, nobody gets notified
                    int downloadId = mDB.remoteDictionaryObjectDao().getDownloadId(d.type, d.lang);

                    if (downloadId == -1) {
                        System.err.println("downloadId is negative for " + d.type + " " + d.lang);
                    }

                    d.setDownloadId(-1);
                    mDB.remoteDictionaryObjectDao().insert(d);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                // mApp.updateDownloadService();
            }
        }.execute();
    }

    @WorkerThread
    void performUpdates() {
        PersistentDatabase db = mDB;

        // mApp.postDetachDatabase();
    }

    @WorkerThread
    void installRemoteDictionaryObject(RemoteDictionaryObject aAction) {
        LocalDictionaryObject localDictionary = new LocalDictionaryObject(aAction);
        InstalledDictionary installedDictionary = localDictionary.install(mApp.getDatabasePath(DictionaryApplication.DATABASE_NAME).getParentFile());
        mDB.installedDictionaryDao().insert(installedDictionary);
        installedDictionary.attach(mDB);
    }

    static int getStatusForDownload(long aDownloadId, Context aContext) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(aDownloadId);

        DownloadManager downloadManager= (DownloadManager) aContext.getSystemService(DOWNLOAD_SERVICE);
        Cursor cur = downloadManager.query(query);

        int status = -1;

        if (cur.moveToNext()) {
            status =  cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }

        cur.close();

        return status;
    }
}
