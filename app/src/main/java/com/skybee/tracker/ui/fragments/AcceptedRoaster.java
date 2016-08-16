package com.skybee.tracker.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skybee.tracker.R;
import com.skybee.tracker.model.RoasterPojo;
import com.skybee.tracker.model.User;
import com.skybee.tracker.preferences.UserStore;
import com.skybee.tracker.ui.adapters.RoasterAdapter;

import java.util.ArrayList;
import java.util.List;

public class AcceptedRoaster extends Fragment {

    private RecyclerView roasterCards;
    private LinearLayoutManager linearLayoutManager;
    private List<RoasterPojo> roasterCardList;
    private RoasterAdapter roasterAdapter;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        UserStore userStore = new UserStore(getContext());
        user = new User();
        user = userStore.getUserDetails();
        View view = inflater.inflate(R.layout.fragment_accepted_roaster, container, false);
        roasterCards=(RecyclerView)view.findViewById(R.id.accepted_roaster_list);
        roasterCards.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roasterCards.setLayoutManager(linearLayoutManager);
        roasterCardList=new ArrayList<>();
        roasterAdapter=new RoasterAdapter(roasterCardList);
        roasterCards.setAdapter(roasterAdapter);
        return view;
    }

}