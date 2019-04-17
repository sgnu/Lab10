package edu.temple.bookcase;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {


    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putString("bookTitle", book.getTitle());
        args.putString("bookAuthor", book.getAuthor());
        args.putString("bookCover", book.getCoverURL());
        args.putInt("bookId", (book.getId()));
        args.putString("bookPublished", String.valueOf(book.getPublished()));
        args.putInt("duration", book.getDuration());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.fragment_book_details, container, false);

        if (getArguments() != null) {
            ((TextView) inflated.findViewById(R.id.detailTitle)).setText(getArguments().getString("bookTitle"));
            ((TextView) inflated.findViewById(R.id.detailAuthor)).setText(getArguments().getString("bookAuthor"));
            Picasso.get().load(getArguments().getString("bookCover")).into((ImageView) inflated.findViewById(R.id.detailImage));
            ((TextView) inflated.findViewById(R.id.detailId)).setText(String.valueOf(getArguments().getInt("bookId")));
            ((TextView) inflated.findViewById(R.id.detailPublished)).setText(getArguments().getString("bookPublished"));
            ((SeekBar) inflated.findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            inflated.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("edu.temple.bookcase.PLAY_BOOK");
                    intent.putExtra("bookId", getArguments().getInt("bookId"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().sendBroadcast(intent);
                }
            });
        }
        // Inflate the layout for this fragment
        return inflated;
    }

}
