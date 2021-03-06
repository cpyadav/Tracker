package com.skybee.tracker.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skybee.tracker.R;
import com.skybee.tracker.core.BaseFragment;
import com.skybee.tracker.model.User;

public class Profile extends BaseFragment {

    private ImageView userImage;
    private TextView userName;
    private TextView userCompany;
    private TextView userLocation;
    private TextView userEmail;
    private TextView userMobileNumber;
    private TextView userImageText;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userImage = (ImageView) view.findViewById(R.id.user_image);
        userName = (TextView) view.findViewById(R.id.user_name);
        userCompany = (TextView) view.findViewById(R.id.user_company);
        userLocation = (TextView) view.findViewById(R.id.location_text);
        userEmail = (TextView) view.findViewById(R.id.email_text);
        userMobileNumber = (TextView) view.findViewById(R.id.mobile_num_text);
        userImageText=(TextView)view.findViewById(R.id.user_image_text);
        user=getLocalUser();
        if(user!=null){
            if(!TextUtils.isEmpty(user.getUserName())) {
                userName.setText(user.getUserName());
                userImageText.setText(String.valueOf(user.getUserName().charAt(0)));
            }
            if(user.getUserEmail()!=null)
                userEmail.setText(user.getUserEmail());
            if(user.getUserMobileNumber()!=null)
                userMobileNumber.setText(user.getUserMobileNumber());
            if(user.getUserLatitude()!=0&&user.getUserLongitude()!=0)
                userLocation.setText(user.getUserLongitude()+" "+user.getUserLatitude());
        }
        return view;
    }

}
