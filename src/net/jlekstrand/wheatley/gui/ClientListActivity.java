/*
 * Copyright © 2013-2014 Jason Ekstrand
 *
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation, and
 * that the name of the copyright holders not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission.  The copyright holders make no representations
 * about the suitability of this software for any purpose.  It is provided "as
 * is" without express or implied warranty.
 *
 * THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
 * EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 * OF THIS SOFTWARE.
 */
package net.jlekstrand.wheatley.gui;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.jlekstrand.wheatley.R;
import net.jlekstrand.wheatley.WaylandActivity;
import net.jlekstrand.wheatley.config.Client;
import net.jlekstrand.wheatley.config.DatabaseHelper;

public class ClientListActivity extends ListActivity
{
    private static final String LOG_TAG = "wheatley:ClientListActivity";

    private static final int MODE_DEFAULT = 0;
    private static final int MODE_CONFIGURE_WIDGET = 1;

    private class ClientCursorAdapter extends CursorAdapter
    {
        public ClientCursorAdapter(Context context, Cursor cursor)
        {
            super(context, cursor);
        }

        public void bindView(View view, Context context, Cursor cursor)
        {
            Client client = Client.createForCursor(context, cursor);

            ((TextView)view.findViewById(R.id.title)).setText(
                    client.getTitle());
            ((TextView)view.findViewById(R.id.command)).setText(
                    client.getCommand());

            view.findViewById(R.id.run_as_root_icon).setVisibility(
                    client.getRunAsRoot() ? View.VISIBLE : View.GONE);

            ImageView iconView = (ImageView)view.findViewById(R.id.icon);

            Bitmap icon = client.getIcon();
            if (icon != null)
                iconView.setImageBitmap(icon);
            else
                iconView.setImageResource(R.drawable.ic_launcher);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.client_list_item, parent, false);
            bindView(view, context, cursor);
            return view;
        }
    }

    private SQLiteDatabase _database;
    private ClientCursorAdapter _adapter;
    private String _action;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        Intent intent = getIntent();
        _action = intent.getAction();

        if (Intent.ACTION_CREATE_SHORTCUT.equals(_action))
            // Cancel if the press the back button
            setResult(RESULT_CANCELED);

        DatabaseHelper helper = new DatabaseHelper(this);
        _database = helper.getReadableDatabase();

        _adapter = new ClientCursorAdapter(this, null);
        setListAdapter(_adapter);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Cursor cursor = _database.query(Client.DB.DATABASE_TABLE,
                Client.DB.DATABASE_PROJECTION,
                null, null, null, null, null, null);
        _adapter.swapCursor(cursor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.client_list_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.action_add_client:
            onActionAddClient();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_TAG, "onListItemCliek(,,, " + id + ")");

        if (Intent.ACTION_CREATE_SHORTCUT.equals(_action)) {
            Intent shortcutIntent = new Intent(this, WaylandActivity.class);
            shortcutIntent.setAction(Intent.ACTION_RUN);
            shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            shortcutIntent.setData(Client.BASE_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build());

            Client client = Client.createForId(this, _database, id);

            Intent addIntent = new Intent();
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, client.getTitle());
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, client.getIcon());

            setResult(RESULT_OK, addIntent);
            finish();
        } else {
            Intent intent = new Intent(this, ClientEditActivity.class);
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(Client.BASE_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build());
            startActivity(intent);
        }
    }

    private void onActionAddClient()
    {
        Log.d(LOG_TAG, "onActionAddClient()");

        Intent intent = new Intent(this, ClientEditActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        startActivity(intent);
    }
}

// vim: set ts=4 sw=4 sts=4 expandtab:
