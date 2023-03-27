package com.example.user.store3c;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;

/**
 * Created by user on 2016/10/22.
 */

public class TabFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static ArrayList<ProductItem> PhoneDataBrand1 = new ArrayList<>();
    private static ArrayList<ProductItem> PhoneDataBrand2 = new ArrayList<>();
    private static ArrayList<ProductItem> PhoneDataBrand3 = new ArrayList<>();
    private static ArrayList<ProductItem> PhoneData1 = new ArrayList<>();
    private static ArrayList<ProductItem> PhoneData2 = new ArrayList<>();
    private static ArrayList<ProductItem> PhoneData3 = new ArrayList<>();
    private static boolean[] tabLoadAll = {true, true, true};

    private OnTabFragmentInteractionListener mListener;
    private PhoneAdapter phoneAdapter = null;
    private int position;
    private ArrayList<ProductItem> PhoneDataList = new ArrayList<>();
    private ArrayList<ProductItem> PhoneData = new ArrayList<>();
    private RecyclerView phoneRecyclerView;
    private RecyclerView phoneTypeRecyclerView;
    private LinearLayoutManager layoutManager;
    private String[][] PhoneTypeList = { {"HTC U", "HTC One", "HTC Butterfly", "HTC Desire"},
            {"ZenFone", "ZenFone Deluxe", "ZenFone Zoom", "ZenFone Selfie", "ZenFone Max", "ZenFone Ultra", "ZenFone AR", "ZenFone Live"},
            {"Galaxy S", "Galaxy Note", "Galaxy A", "Galaxy J"}};

    public PhoneTypeAdapter phoneTypeAdapter;

    public static TabFragment newInstance(int position, ArrayList<ProductItem> PhoneData) {
        switch (position) {
            case 0:
                PhoneDataBrand1 = PhoneData;
                break;
            case 1:
                PhoneDataBrand2 = PhoneData;
                break;
            case 2:
                PhoneDataBrand3 = PhoneData;
                break;
            default:
                Log.i(" ===>", "The fragment number is error ");
        }

        TabFragment f = new TabFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View row;

        row = inflater.inflate(R.layout.fragment_tab, container, false);
        phoneTypeRecyclerView = row.findViewById(R.id.recyclerViewPhoneType_id);
        phoneRecyclerView = row.findViewById(R.id.recyclerViewPhone_id);
        //ImageView listAllBtn = row.findViewById(R.id.listAllPhone_id);
        //refreshText = (TextView) row.findViewById(R.id.refreshText_id);
        //linLayout = (LinearLayout) row.findViewById(R.id.fragment_tab_id);

        phoneTypeAdapter = new PhoneTypeAdapter(PhoneTypeList, position, this);
        phoneTypeRecyclerView.setAdapter(phoneTypeAdapter);
        layoutManager = new GridLayoutManager(getActivity(), 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        phoneTypeRecyclerView.setLayoutManager(layoutManager);

        switch (position) {
            case 0:
                PhoneDataList = PhoneDataBrand1;
                PhoneData = PhoneData1;
                break;
            case 1:
                PhoneDataList = PhoneDataBrand2;
                PhoneData = PhoneData2;
                break;
            case 2:
                PhoneDataList = PhoneDataBrand3;
                PhoneData = PhoneData3;
                break;
            default:
                Log.i(" ===>", "The fragment number is error ");
        }
        if (PhoneData.size() == 0) {
            phoneAdapter = new PhoneAdapter(PhoneDataList, getActivity(), this, position);
        }
        else {
            phoneAdapter = new PhoneAdapter(PhoneData, getActivity(), this, position);
        }
        phoneRecyclerView.setAdapter(phoneAdapter);
        phoneRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        //listAllBtn.setImageBitmap(PhoneDataList.get(position).getImg());
        //refreshText.setText("refresh product");
        //linLayout.setLayoutParams(params2);
/*
        listAllBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // process and construct uri
                onButtonPressed(position);
            }
        });
*/
        return row;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int imgId) {
        if (mListener != null) {
            mListener.onTabFragmentInteraction(imgId);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTabFragmentInteractionListener) {
            mListener = (OnTabFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTabFragmentInteractionListener");
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
    public interface OnTabFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTabFragmentInteraction(int position);
    }

    void reloadAdapter(int framePosition, boolean listAll, int typePosition) {

        String brandTypePre = null;
        tabLoadAll[framePosition] = listAll;
        PhoneData.clear();
        if (listAll) {
            phoneAdapter.setItemList(PhoneDataList, tabLoadAll);
        }
        else {
            if (framePosition == 1) {
                switch(typePosition) {
                    case 0:
                        brandTypePre = "";
                        break;
                    case 1:
                        brandTypePre = "Deluxe";
                        break;
                    case 2:
                        brandTypePre = "Zoom";
                        break;
                    case 3:
                        brandTypePre = "Selfie";
                        break;
                    case 4:
                        brandTypePre = "Max";
                        break;
                    case 5:
                        brandTypePre = "Ultra";
                        break;
                    case 6:
                        brandTypePre = "AR";
                        break;
                    case 7:
                        brandTypePre = "Live";
                        break;
                    default:
                        Toast.makeText(getActivity()," error type position:  " + typePosition  , Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            if (framePosition == 0) {
                if (typePosition == 1) {
                    for (int i=0;i<PhoneDataList.size();i++) {
                        String phoneName = PhoneDataList.get(i).getName();
                        if (phoneName.contains(PhoneTypeList[framePosition][typePosition]) || phoneName.contains("HTC 10")) {
                            PhoneData1.add(PhoneDataList.get(i));
                        }
                    }
                }
                else {
                    for (int i=0;i<PhoneDataList.size();i++) {
                        String phoneName = PhoneDataList.get(i).getName();
                        if (phoneName.contains(PhoneTypeList[framePosition][typePosition])) {
                            PhoneData1.add(PhoneDataList.get(i));
                        }
                    }
                }
            }
            else if (framePosition == 1){
                for (int i=0;i<PhoneDataList.size();i++) {
                    String phoneName = PhoneDataList.get(i).getName();
                    if (brandTypePre != null) {
                        if (brandTypePre.equals("")) {
                            if (!phoneName.contains("Deluxe") && !phoneName.contains("Zoom")
                                    && !phoneName.contains("Selfie") && !phoneName.contains("Max")
                                    && !phoneName.contains("Ultra") && !phoneName.contains("AR")
                                    && !phoneName.contains("Ares") && !phoneName.contains("Live")) {
                                PhoneData2.add(PhoneDataList.get(i));
                            }
                        } else {
                            if (typePosition == 6) {
                                if (phoneName.contains(brandTypePre) || phoneName.contains("Ares")) {
                                    PhoneData2.add(PhoneDataList.get(i));
                                }
                            } else {
                                if (phoneName.contains(brandTypePre)) {
                                    PhoneData2.add(PhoneDataList.get(i));
                                }
                            }
                        }
                    }
                }
            }
            else {
                for (int i=0;i<PhoneDataList.size();i++) {
                    String phoneName = PhoneDataList.get(i).getName();
                    if (phoneName.contains(PhoneTypeList[framePosition][typePosition])) {
                        PhoneData3.add(PhoneDataList.get(i));
                    }
                }
            }
            switch (position) {
                case 0:
                    PhoneData = PhoneData1;
                    break;
                case 1:
                    PhoneData = PhoneData2;
                    break;
                case 2:
                    PhoneData = PhoneData3;
                    break;
                default:
                    Log.i(" ===>", "The fragment number is error ");
            }
            phoneAdapter.setItemList(PhoneData, tabLoadAll);
        }
        phoneAdapter.notifyDataSetChanged();

        //Toast.makeText(getActivity(), "fragment " + position + " reloadAdapter " , Toast.LENGTH_SHORT).show();
    }

}