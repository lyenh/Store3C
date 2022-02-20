package com.example.user.store3c;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;

import java.util.Objects;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.example.user.store3c.MainActivity.isTab;

public class YouTubeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static String ARG_PARAM1 = "param1";
    // TODO: Rename and change types of parameters
    //private Bundle pData;

    private OnFragmentInteractionListener mListener;
    private YouTubePlayerSupportFragmentX youtubePlayerFragment;

    public String videoId = "_7d1knBJe8k";
    public static YouTubePlayer YPlayer = null;
    public boolean youTubePlayerBoolean = true;

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onAdStarted() {
            //Toast.makeText(getActivity(), "AdStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            String error = errorReason.toString();
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoaded(String arg0) {
            //Toast.makeText(getActivity(), "Loaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoading() {
            //Toast.makeText(getActivity(), "Loading", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVideoEnded() {
            // finish();
        }

        @Override
        public void onVideoStarted() {
            //Toast.makeText(getActivity(), "VideoStarted", Toast.LENGTH_SHORT).show();
        }
    };

    public YouTubeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment LowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static YouTubeFragment newInstance(String param1) {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        YouTubeFragment fragment = new YouTubeFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //pData.getIntArray(ARG_PARAM1);
            videoId = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_youtube, container,false);

        youtubePlayerFragment = YouTubePlayerSupportFragmentX.newInstance();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtubeFragment_id, youtubePlayerFragment);
        fragmentTransaction.commit();

        Activity activity = getActivity();
        if (activity != null && youtubePlayerFragment != null) {
            if (InternetConnection.checkConnection(activity)) {
                if (YouTubeIntents.isYouTubeInstalled(activity) ||
                        (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(activity) == YouTubeInitializationResult.SUCCESS)) {
                    youtubePlayerFragment.initialize(DeveloperKey.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider arg0, YouTubePlayer youTubePlayer, boolean b) {
                            //Toast.makeText(getActivity(), "Initialization Success", Toast.LENGTH_SHORT).show();
                            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                            youTubePlayerBoolean = b;
                            youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);

                            if (!b) {
                                YPlayer = youTubePlayer;
                                if (screenWidth > 800 && !isTab) {
                                    if (YPlayer.isPlaying()) {
                                        YPlayer.setFullscreen(true);
                                        YPlayer.play();
                                    } else {
                                        YPlayer.setFullscreen(true);
                                        YPlayer.cueVideo(videoId);
                                    }
                                } else {
                                    if (YPlayer.isPlaying()) {
                                        Toast.makeText(getActivity(), "isPlaying", Toast.LENGTH_LONG).show();
                                        YPlayer.setFullscreen(false);
                                        YPlayer.play();
                                    } else {
                                        //Toast.makeText(getActivity(), "loadVideo", Toast.LENGTH_LONG).show();
                                        YPlayer.cueVideo(videoId);
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), "youTubePlayerBoolean error", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
                            int RECOVERY_REQUEST = 1;
                            // TODO Auto-generated method stub
                            if (arg1.isUserRecoverableError()) {
                                arg1.getErrorDialog(getActivity(), RECOVERY_REQUEST).show();
                            } else {
                                String error = String.format(getString(R.string.player_error), arg1.toString());
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    //Toast.makeText(getActivity(), "YouTube ok ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "YouTube busy, can try again ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "網路未連線! ", Toast.LENGTH_SHORT).show();
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteractionL(uri);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
/*
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().remove(youtubePlayerFragment).commit();
        fragmentManager.executePendingTransactions();
*/
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionL(Uri uri);
    }
}

