package edu.temple.bookcase;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    ArrayList<Book> books = new ArrayList<>();
    boolean twoPanes;
    final FragmentManager manager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        books.add(new Book("title", "author", "cover", 1, 202));

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
            BookAdapter adapter = new BookAdapter(this, books);
            bookList.setAdapter(adapter);

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

            PageAdapter adapter = new PageAdapter(manager, this, books);
            pager.setAdapter(adapter);
        }

    }

    public class Book {
        private String title, author, coverURL;
        private int id, published;

        public Book(String title, String author, String coverURL, int id, int published) {
            this.title = title;
            this.author = author;
            this.coverURL = coverURL;
            this.id = id;
            this.published = published;
        }

        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCoverURL() { return coverURL; }
        public int getId() { return id; }
        public int getPublished() { return published; }
    }

    public class BookAdapter extends BaseAdapter {
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
    }

    public class PageAdapter extends FragmentPagerAdapter {
        private Context context;
        private ArrayList<Book> books;

        public PageAdapter(FragmentManager fm, Context context, ArrayList<Book> books) {
            super(fm);
            this.context = context;
            this.books = books;
        }

        @Override
        public Fragment getItem(int i) {
            return BookDetailsFragment.newInstance(books.get(i));
        }

        @Override
        public int getCount() {
            return books.size();
        }


    }
}
