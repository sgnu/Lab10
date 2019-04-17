package edu.temple.bookcase;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends FragmentActivity {

    ArrayList<Book> books = new ArrayList<>();
    int bookPlaying;
    boolean twoPanes;
    final FragmentManager manager = getSupportFragmentManager();
    PageAdapter pAdapter;
    BookAdapter bAdapter;
    AudiobookService audioService;
    AudiobookService.MediaControlBinder binder;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent("edu.temple.bookcase.PROGRESS_UPDATE");
            intent.putExtra("position", msg.what);
            intent.putExtra("bookId", bookPlaying);
            MainActivity.this.sendBroadcast(intent);
        }
    };

    BroadcastReceiver playReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int bookId = intent.getIntExtra("bookId", 1);
            if (binder != null) {
                binder.setProgressHandler(handler);
                binder.play(bookId, intent.getIntExtra("position", 0));
                bookPlaying = bookId;
            }
            System.out.println(intent);
        }
    };
    BroadcastReceiver pauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binder != null)
                binder.pause();
        }
    };
    BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binder != null)
                binder.stop();
        }
    };
    BroadcastReceiver seekReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binder != null)
                if (intent.getIntExtra("bookId", 0) == bookPlaying)
                    binder.seekTo(intent.getIntExtra("position", 0));
        }
    };

    ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (AudiobookService.MediaControlBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioService = new AudiobookService();

        startService(new Intent(MainActivity.this, edu.temple.audiobookplayer.AudiobookService.class));
        bindService(new Intent(MainActivity.this, AudiobookService.class), sConn, Context.BIND_AUTO_CREATE);

        registerReceiver(playReceiver, new IntentFilter("edu.temple.bookcase.PLAY_BOOK"));
        registerReceiver(pauseReceiver, new IntentFilter("edu.temple.bookcase.PAUSE_BOOK"));
        registerReceiver(stopReceiver, new IntentFilter("edu.temple.bookcase.STOP_BOOK"));
        registerReceiver(seekReceiver, new IntentFilter("edu.temple.bookcase.SEEK_BOOK"));

        new GetBooksTask().execute();

        twoPanes = (findViewById(R.id.bookList) != null);

        manager.findFragmentByTag("");

        if (twoPanes) {
            manager.beginTransaction()
                    .add(R.id.listContainer, new BookListFragment())
                    .addToBackStack(null)
                    .commit();
            manager.beginTransaction()
                    .add(R.id.detailContainer, new BookDetailsFragment())
                    .addToBackStack(null)
                    .commit();

            ListView bookList = findViewById(R.id.bookList);
            bAdapter = new BookAdapter(this, books);
            bookList.setAdapter(bAdapter);

            bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    manager.beginTransaction()
                            .replace(R.id.detailContainer, BookDetailsFragment.newInstance(books.get(position)))
                            .addToBackStack(null)
                            .commit();
                }
            });
        } else {
            manager.beginTransaction()
                    .add(R.id.pagerFragment, new PagerFragment())
                    .commit();
            ViewPager pager = findViewById(R.id.bookPager);

            pAdapter = new PageAdapter(manager, books);
            pager.setAdapter(pAdapter);
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetBooksTask().execute(((EditText) findViewById(R.id.editText)).getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), edu.temple.audiobookplayer.AudiobookService.class));
        unbindService(sConn);
        unregisterReceiver(playReceiver);
        unregisterReceiver(pauseReceiver);
        unregisterReceiver(stopReceiver);
        unregisterReceiver(seekReceiver);
    }

    private class BookAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<Book> books;

        public BookAdapter(Context context, ArrayList<Book> books) {
            this.context = context;
            this.books = books;
        }

        @Override
        public int getCount() {
            return books.size();
        }

        @Override
        public Book getItem(int position) {
            return books.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_row, parent, false);
            }

            String title = getItem(position).getTitle();
            TextView bookName = convertView.findViewById(R.id.bookName);

            bookName.setText(title);
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    public class PageAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Book> books;

        public PageAdapter(FragmentManager fm, ArrayList<Book> books) {
            super(fm);
            this.books = books;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.POSITION_NONE;
        }

        @Override
        public Fragment getItem(int i) {
            return BookDetailsFragment.newInstance(books.get(i));
        }

        @Override
        public int getCount() {
            return books.size();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    public class GetBooksTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... search) {
            try {
                books.removeAll(books);
                URL url;
                if (search.length > 0 && !search[0].equals("")) {
                    url = new URL("https://kamorris.com/lab/audlib/booksearch.php?search=" + search[0]);
                } else {
                    url = new URL("https://kamorris.com/lab/audlib/booksearch.php");
                }
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(2000);
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8);
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                JSONArray array = new JSONArray(builder.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    Book newBook = new Book(object.getString("title"), object.getString("author"), object.getString("cover_url"), object.getInt("book_id"), object.getInt("published"), object.getInt("duration"));
                    books.add(newBook);
                }
                stream.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            if (pAdapter != null) {
                pAdapter.notifyDataSetChanged();
            }
            if (bAdapter != null) {
                bAdapter.notifyDataSetChanged();
            }
        }
    }

}
