package de.evolutionid.fcbmock0.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;

import de.evolutionid.fcbmock0.MainActivity;
import de.evolutionid.fcbmock0.R;

public class PointsFragment extends Fragment{

    DonutProgress donutProgress;

    public PointsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final int pointsGained = getArguments().getInt("name");
            setText(pointsGained + " points gained! \n");
        }
        View view = inflater.inflate(R.layout.fragment_points, container, false);
        donutProgress = (DonutProgress) view.findViewById(R.id.donut_progress);
        donutProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new CountDownTimer(1000, 10) {

                    public void onTick(long millisUntilFinished) {
                        donutProgress.setProgress(100 - (int)millisUntilFinished / 10);
                    }

                    public void onFinish() {
                        donutProgress.setProgress(100);
                        Toast.makeText(getActivity(), "Level up!", Toast.LENGTH_SHORT).show();
                        setText("LEVEL UP!\n\nClaim your prize over at fcbayern.de! \nRedirecting...");

                        new CountDownTimer(4000, 10) {

                            public void onTick(long millisUntilFinished) {
                                //do nothing
                            }

                            public void onFinish() {
                                String url = "http://www.fcbayern.de"; //TODO: Don't hardcode this, obviously
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        }.start();
                    }
                }.start();

            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.textView);
        textView.setText(text);
    }

}