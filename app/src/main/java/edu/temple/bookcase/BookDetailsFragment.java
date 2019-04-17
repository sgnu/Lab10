package edu.temple.bookcase;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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

    SeekBar seekBar;
    BroadcastReceiver progressReceiver;

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
            seekBar = inflated.findViewById(R.id.seekBar);
            progressReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (getArguments().getInt("bookId") == intent.getIntExtra("bookId", 0))
                        seekBar.setProgress(intent.getIntExtra("position", 0));
                }
            };

            getActivity().registerReceiver(progressReceiver, new IntentFilter("edu.temple.bookcase.PROGRESS_UPDATE"));

            ((TextView) inflated.findViewById(R.id.detailTitle)).setText(getArguments().getString("bookTitle"));
            ((TextView) inflated.findViewById(R.id.detailAuthor)).setText(getArguments().getString("bookAuthor"));
            Picasso.get().load(getArguments().getString("bookCover")).into((ImageView) inflated.findViewById(R.id.detailImage));
            ((TextView) inflated.findViewById(R.id.detailId)).setText(String.valueOf(getArguments().getInt("bookId")));
            ((TextView) inflated.findViewById(R.id.detailPublished)).setText(getArguments().getString("bookPublished"));
            seekBar.setMax(getArguments().getInt("duration"));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Intent intent = new Intent("edu.temple.bookcase.SEEK_BOOK");
                    intent.putExtra("position", seekBar.getProgress());
                    intent.putExtra("bookId", getArguments().getInt("bookId"));
                    getContext().sendBroadcast(intent);
                }
            });
            inflated.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("edu.temple.bookcase.PLAY_BOOK");
                    intent.putExtra("bookId", getArguments().getInt("bookId"));
                    getContext().sendBroadcast(intent);
                }
            });
            inflated.findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().sendBroadcast(new Intent("edu.temple.bookcase.PAUSE_BOOK"));
                }
            });
            inflated.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().sendBroadcast(new Intent("edu.temple.bookcase.STOP_BOOK"));
                }
            });
        }
        // Inflate the layout for this fragment
        return inflated;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(progressReceiver);
    }
}
