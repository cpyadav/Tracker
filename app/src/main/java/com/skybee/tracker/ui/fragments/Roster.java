package com.skybee.tracker.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.skybee.tracker.Factory;
import com.skybee.tracker.R;
import com.skybee.tracker.constants.API;
import com.skybee.tracker.constants.Constants;
import com.skybee.tracker.core.BaseFragment;
import com.skybee.tracker.model.RosterPojo;
import com.skybee.tracker.model.User;
import com.skybee.tracker.preferences.UserStore;
import com.skybee.tracker.ui.adapters.RosterAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Roster extends BaseFragment {

    private RecyclerView roasterCards;
    private LinearLayoutManager linearLayoutManager;
    private List<RosterPojo> roasterCardList;
    private RosterAdapter rosterAdapter;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserStore userStore = new UserStore(getContext());
        user = new User();
        user = userStore.getUserDetails();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_roster, container, false);
        roasterCards = (RecyclerView) view.findViewById(R.id.accepted_roaster_list);
        roasterCards.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roasterCards.setLayoutManager(linearLayoutManager);
        roasterCardList = new ArrayList<>();
        rosterAdapter = new RosterAdapter(roasterCardList);
        roasterCards.setAdapter(rosterAdapter);
        getRoasterList();
        return view;
    }

    public void getRoasterList() {
        ListenableFuture<JSONObject> getRoaster = Factory.getUserService().roasterList(API.ROSTER_LIST, user);
        Futures.addCallback(getRoaster, new FutureCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (result.has(Constants.JsonConstants.DATA)) {
                        JSONArray resultRosterList = new JSONArray();
                        resultRosterList = result.getJSONArray(Constants.JsonConstants.DATA);
                        for (int i = 0; i < resultRosterList.length(); i++) {
                            JSONObject roasterJsonObject = resultRosterList.getJSONObject(i);
                            if (roasterJsonObject != null) {
                                Gson gson=new Gson();
                                final RosterPojo rosterPojo =gson.fromJson(roasterJsonObject.toString(),RosterPojo.class);
                                if(rosterPojo !=null){
                                    roasterCardList.add(rosterPojo);
                                }
                            }
                        }
                        if(roasterCardList.size()>=1)
                            rosterAdapter.notifyItemInserted(roasterCardList.size()-1);
                    }
                } catch (JSONException jsonException) {
                    Log.d(getTAG(), Constants.Exception.JSON_EXCEPTION);
                } catch (Exception exception) {
                    Log.d(getTAG(), Constants.Exception.EXCEPTION);
                }

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    public void onError() {

    }
}