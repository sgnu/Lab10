package edu.temple.bookcase;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    SeekBar seekBar;
    Button downloadButton;
    BroadcastReceiver progressReceiver;
    Bundle arguments;

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
        args.putBoolean("downloaded", book.getDownloaded());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.fragment_book_details, container, false);

        if (getArguments() != null) {
            arguments = getArguments();
            seekBar = inflated.findViewById(R.id.seekBar);
            downloadButton = inflated.findViewById(R.id.download);
            progressReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (getArguments().getInt("bookId") == intent.getIntExtra("bookId", 0))
                        seekBar.setProgress(intent.getIntExtra("position", 0));
                }
            };

            getActivity().registerReceiver(progressReceiver, new IntentFilter("edu.temple.bookcase.PROGRESS_UPDATE"));

            updateDownloadButton();

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

        try {
            getActivity().unregisterReceiver(progressReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDownloadButton() {
        if (getArguments().getBoolean("downloaded")) {
            downloadButton.setText("Delete");
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "This would be delete", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            downloadButton.setText("Download");
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadBook(arguments.getInt("bookId"));
                }
            });
        }
    }

    private void downloadBook(int bookId) {
        DownloadBookThread thread = new DownloadBookThread(bookId, getContext());
        thread.start();
        try {
            Thread.sleep(1000);
            updateDownloadButton();
            System.out.println("Thread finished sleeping");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DownloadBookThread extends Thread {
        private int bookId;
        private Context context;

        DownloadBookThread(int bookId, Context context) {
            this.bookId = bookId;
            this.context = context;
        }

        public void run() {
            String filename = "Book" + this.bookId + ".mp3";
            int count;
            try {
                URL url = new URL("https://kamorris.com/lab/audlib/download.php?id=" + bookId);

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                File file = new File(context.getFilesDir(), filename);

                OutputStream output = new FileOutputStream(file.getPath());

                byte data[] = new byte[1024];

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                System.out.println("Finished downloading book");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
