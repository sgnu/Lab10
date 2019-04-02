package edu.temple.bookcase;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {


    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(MainActivity.Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putString("bookTitle", book.getTitle());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.fragment_book_details, container, false);

        if (getArguments() != null) {
            String title = getArguments().getString("bookTitle");
            TextView titleView = inflated.findViewById(R.id.detailTitle);
            titleView.setText(title);
        }
        // Inflate the layout for this fragment
        return inflated;
    }

}
