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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends FragmentActivity {

    String search;
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
            int bookId = intent.getIntExtra("bookId", 0);
            int position;
            if (bookId == bookPlaying)
                position = intent.getIntExtra("position", 0) - 10;
            else {
                try {
                    FileInputStream inputStream = context.openFileInput(String.valueOf(bookId));
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    position = Integer.parseInt(bufferedReader.readLine());

                    if (bookPlaying != 0)
                        writePosition(bookPlaying, intent.getIntExtra("position", 0));
                } catch (Exception e) {
                    position = 0;
                }
            }
            if (binder != null) {
                File book = new File(getFilesDir(), "Book" + bookId + ".mp3");
                binder.setProgressHandler(handler);
                if (book.exists()) {
                    binder.play(book, position);
                } else {
                    binder.play(bookId, position);
                }
                bookPlaying = bookId;
            }
            System.out.println(intent);
        }
    };
    BroadcastReceiver pauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int bookId = intent.getIntExtra("bookId", 0);
            int position = intent.getIntExtra("position", 0);
            if (binder != null)
                binder.pause();
            writePosition(bookId, position);
        }
    };
    BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binder != null)
                binder.stop();
            writePosition(intent.getIntExtra("bookId", 0), 0);
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

        if (savedInstanceState != null) {
            search = savedInstanceState.getString("search");
            bookPlaying = savedInstanceState.getInt("bookId");
        }

        audioService = new AudiobookService();

        startService(new Intent(MainActivity.this, edu.temple.audiobookplayer.AudiobookService.class));
        bindService(new Intent(MainActivity.this, AudiobookService.class), sConn, Context.BIND_AUTO_CREATE);

        registerReceiver(playReceiver, new IntentFilter("edu.temple.bookcase.PLAY_BOOK"));
        registerReceiver(pauseReceiver, new IntentFilter("edu.temple.bookcase.PAUSE_BOOK"));
        registerReceiver(stopReceiver, new IntentFilter("edu.temple.bookcase.STOP_BOOK"));
        registerReceiver(seekReceiver, new IntentFilter("edu.temple.bookcase.SEEK_BOOK"));

        ((TextView) findViewById(R.id.editText)).setText(search);

        new GetBooksTask().execute(((TextView) findViewById(R.id.editText)).getText().toString());

        twoPanes = (findViewById(R.id.listContainer) != null);

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

            ListView bookList = findViewById(R.id.listContainer).findViewById(R.id.bookList);
            bookList.setSelection(bookPlaying);
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
                    .add(R.id.pagerContainer, new PagerFragment())
                    .addToBackStack(null)
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
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("search", ((TextView) findViewById(R.id.editText)).getText().toString());
        savedInstanceState.putInt("bookId", bookPlaying);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        search = savedInstanceState.getString("search");
        bookPlaying = savedInstanceState.getInt("bookId");
    }

    private void writePosition(int bookId, int position) {
        String file = String.valueOf(bookId);
        String stringPosition;
        if (position > 10) {
            stringPosition = String.valueOf(position - 10);
        } else {
            stringPosition = "0";
        }
        System.out.println(stringPosition);
        try {
            FileOutputStream outputStream = openFileOutput(file, Context.MODE_PRIVATE);
            outputStream.write(stringPosition.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), edu.temple.audiobookplayer.AudiobookService.class));
        if (sConn != null)
            unbindService(sConn);

        if (playReceiver != null)
        unregisterReceiver(playReceiver);

        if (pauseReceiver != null)
        unregisterReceiver(pauseReceiver);

        if (stopReceiver != null)
        unregisterReceiver(stopReceiver);

        if (seekReceiver != null)
        unregisterReceiver(seekReceiver);

        try {
            pAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    File file = new File(MainActivity.this.getFilesDir(), "Book" + newBook.getId() + ".mp3");
                    if (file.exists()) {
                        newBook.setDownloaded(true);
                    } else {
                        newBook.setDownloaded(false);
                    }
                    books.add(newBook);
                }
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
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
