package edu.temple.bookcase;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> books = new ArrayList<>();

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

        ListView bookList = findViewById(R.id.bookList);
        final BookAdapter adapter = new BookAdapter(this, books);
        bookList.setAdapter(adapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView detailTitle = findViewById(R.id.detailTitle);
                detailTitle.setText((String) adapter.getItem(position));
            }
        });
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
}
