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

import com.github.lzyzsd.circleprogress.DonutProgress;

import de.evolutionid.fcbmock0.R;

public class PointsFragment extends Fragment{

    static int currentScore = 0;

    DonutProgress donutProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public PointsFragment() {
        // Required empty public constructor
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
        donutProgress.setProgress(currentScore);


        // Inflate the layout for this fragment
        return view;
    }

    public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.textView);
        textView.setText(text);
    }


    public void addPoints(final int pointsGained) {
        final int originalScore = currentScore;
        //((ViewPager) getActivity().findViewById(R.id.viewpager)).setCurrentItem(1);
        donutProgress.setProgress(originalScore);
        currentScore = originalScore + pointsGained;
        if (originalScore + pointsGained >= 1000) {
            setText("LEVEL UP!!! \nClaim your prize\nover at fcbayern.de!");
            donutProgress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "http://www.fcbayern.de"; //TODO: Don't hardcode this, obviously
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        } else {
            setText("You got " + pointsGained + " points!\nOnly " + (1000 - currentScore) + " to lvlup!");
        }
        new CountDownTimer(pointsGained, 20) {

            @Override
            public void onTick(long millisUntilFinished) {
                donutProgress.setProgress(pointsGained - (int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                donutProgress.setProgress(currentScore);
            }
        }.start();
    }



}