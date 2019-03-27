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
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    ArrayList<String> books = new ArrayList<>();
    boolean twoPanes;
    final FragmentManager manager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        books.add("1984");
        books.add("Moby Dick");
        books.add("Divergent");
        books.add("Cat in the Hat");
        books.add("Diary of a Wimpy Kid");
        books.add("Metamorphosis");
        books.add("The Catcher in the Rye");
        books.add("Romeo and Juliet");
        books.add("Pride and Prejudice");
        books.add("The Art of War");
        books.add("Sidereus Nuncius");
        books.add("Cat's Cradle");

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
