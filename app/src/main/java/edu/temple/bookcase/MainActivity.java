package edu.temple.bookcase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> books = new ArrayList<>();
    boolean twoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Create string array of book titles
        books.add("Book 1");
        books.add("Book 2");
        books.add("Book 3");
        books.add("Book 4");
        books.add("Book 5");
        books.add("Book 6");
        books.add("Book 7");
        books.add("Book 8");
        books.add("Book 9");
        books.add("Book 10");

        twoPanes = (findViewById(R.id.bookList) != null);

        final FragmentManager manager = getSupportFragmentManager();

        if (twoPanes) {
            manager.beginTransaction()
                    .add(R.id.listFrag, new BookListFragment())
                    .addToBackStack(null)
                    .commit();
            manager.beginTransaction()
                    .add(R.id.detailFrag, new BookDetailsFragment())
                    .addToBackStack(null)
                    .commit();

            ListView bookList = findViewById(R.id.bookList);
            BookAdapter adapter = new BookAdapter(this, books);
            bookList.setAdapter(adapter);

            bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    manager.beginTransaction()
                            .replace(R.id.detailFrag, BookDetailsFragment.newInstance(books.get(position)))
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

    public class BookAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> books;

        public BookAdapter(Context context, ArrayList<String> books) {
            this.context = context;
            this.books = books;
        }

        @Override
        public int getCount() {
            return books.size();
        }

        @Override
        public Object getItem(int position) {
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

            String currentItem = (String) getItem(position);
            TextView bookName = convertView.findViewById(R.id.bookName);

            bookName.setText(currentItem);
            return convertView;
        }
    }

    public class PageAdapter extends FragmentPagerAdapter {
        private Context context;
        private ArrayList<String> books;

        public PageAdapter(FragmentManager fm, Context context, ArrayList<String> books) {
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
