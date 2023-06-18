package com.example.user.store3c;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Slide2Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Slide2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Slide2Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private byte[] mParam1;
    private String mParam2;
    private static byte[] gParam1;

    private OnFragmentInteractionListener mListener;
    private final int imgId = 2;

    public Slide2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Slide2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Slide2Fragment newInstance(byte[] param1, String param2) {
        gParam1 = param1;

        Slide2Fragment fragment = new Slide2Fragment();
        Bundle args = new Bundle();
        //args.putByteArray("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getByteArray("param1");
            mParam2 = getArguments().getString("param2");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getArguments() != null) {
            synchronized (getArguments()) {
                //getArguments().putByteArray("param1", mParam1);
                getArguments().putString("param2", mParam2);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View row;
        ImageView image;

        row = inflater.inflate(R.layout.fragment_slide2, container, false);
        image = row.findViewById(R.id.imageView3);

        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setPadding(2,2,2,2);
        image.setImageBitmap(BitmapFactory.decodeByteArray(gParam1, 0, gParam1.length));
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // process and construct uri
                onButtonPressed(imgId);
            }
        });

        return row;
    }

    // TODO: Rename method, update argument and hook method into UI event
    private void onButtonPressed(int imgId) {
        if (mListener != null) {
            mListener.onFragmentInteraction(imgId);
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
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int imgId);
    }
}
