package com.skybee.tracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.skybee.tracker.activities.HomeActivity;
import com.skybee.tracker.activities.MainActivity;
import com.skybee.tracker.constants.API;
import com.skybee.tracker.constants.Constants;
import com.skybee.tracker.model.AttendancePojo;
import com.skybee.tracker.model.TimeCard;
import com.skybee.tracker.model.User;
import com.skybee.tracker.model.UserServer;
import com.skybee.tracker.network.ExecutorUtils;
import com.skybee.tracker.preferences.UserStore;
import com.skybee.tracker.ui.dialog.ErrorDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    private static final String TAG = "Utility";
    private static ErrorDialog errorDialog;
    private static ErrorDialog getErrorDialog;
    private static boolean error;
    private static String errorMessage = null;

    public static TimeCard setEventType(TimeCard timeCard, int size) {
        switch (size) {
            case 1:
                timeCard.setEvent(Constants.HeadingText.CHECK_IN);
                break;
            case 2:
                timeCard.setEvent(Constants.HeadingText.CHECK_LUNCH);
                break;
            case 3:
                timeCard.setEvent(Constants.HeadingText.CHECK_OUT);
                break;
            case 4:
                timeCard.setEvent(Constants.HeadingText.CHECK_OFF);
                break;
        }
        return timeCard;
    }

    public static void startActivity(@NonNull Context context, @NonNull boolean isAdmin) {
        Intent intent;
        if (isAdmin == true) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void authenticate(@NonNull final Context context, @NonNull final ProgressDialog progressDialog, @NonNull String url, @NonNull final User user) {
        ListenableFuture<JSONObject> authenticateUser = Factory.getUserService().authenticateUser(user, url);
        Futures.addCallback(authenticateUser, new FutureCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONObject data = new JSONObject();
                    Gson gson = new Gson();
                    if (result.has(Constants.JsonConstants.DATA)) {
                        data = Utility.getUserResultObject(result.toString());
                        final UserServer userServer = gson.fromJson(data.toString(), UserServer.class);
                        User userDetail = new User();
                        if (userServer != null) {
                            if (userServer.getId() != 0)
                                userDetail.setId(userServer.getId());
                            if (userServer.getDevice_id() != null)
                                userDetail.setDevice_id(userServer.getDevice_id());
                            if (userServer.getRegistration_key() != null)
                                userDetail.setRegistrationCode(userServer.getRegistration_key());
                            if (userServer.getPhone() != null)
                                userDetail.setUserMobileNumber(userServer.getPhone());
                            if (userServer.getMobileNo() != null)
                                userDetail.setUserMobileNumber(userServer.getMobileNo());
                            if (userServer.getAuthToken() != null)
                                userDetail.setAuthToken(userServer.getAuthToken());
                            if (userServer.getName() != null)
                                userDetail.setUserName(userServer.getName());
                            if (userServer.getUserEmail() != null)
                                userDetail.setUserEmail(userServer.getUserEmail());
                            if (userServer.getUserType().equals("Admin"))
                                userDetail.setAdmin(true);
                            saveUserDetailsPreference(context, userDetail);
                            startActivity(context, userDetail.isAdmin());
                        }
                        checkProgressDialog(progressDialog);
                    } else if (result.has(Constants.JsonConstants.MESSAGE)) {
                        checkProgressDialog(progressDialog);
                        errorMessage = result.getString(Constants.JsonConstants.MESSAGE);
                        showErrorDialog(context, errorMessage);
                    } else {
                        checkProgressDialog(progressDialog);
                        errorMessage = Constants.ERROR_OCCURRED;
                        showErrorDialog(context, errorMessage);
                    }
                } catch (JSONException jsonException) {
                    checkProgressDialog(progressDialog);
                    errorMessage = "JSON" + Constants.ERROR_OCCURRED;
                    showErrorDialog(context, errorMessage);
                } catch (Exception e) {
                    checkProgressDialog(progressDialog);
                    if (e != null) {
                        if (e.getMessage() != null) {
                            errorMessage = e.getMessage();
                        }
                    }
                    errorMessage = Constants.ERROR_OCCURRED;
                    showErrorDialog(context, errorMessage);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t != null) {
                    if (t.getMessage() != null) {
                        errorMessage = t.getMessage();
                    } else {
                        errorMessage = Constants.ERROR_OCCURRED;
                    }
                } else {
                    errorMessage = Constants.ERROR_OCCURRED;
                }
                Utility.checkProgressDialog(progressDialog);
                showErrorDialog(context, errorMessage);
            }
        }, ExecutorUtils.getUIThread());
    }

    public static JSONObject getUserResultObject(String body) throws JSONException {
        JSONObject jsonObject = new JSONObject(body);
        JSONObject result = jsonObject.getJSONObject(Constants.JsonConstants.DATA);
        return result;
    }

    public static void showErrorDialog(@NonNull Context context, @NonNull String messsage) {
        try {
            errorDialog = new ErrorDialog(context, messsage);
            errorDialog.show();
        } catch (Exception e) {

        }
    }

    public static void saveUserDetailsPreference(@NonNull Context context, @NonNull User user) {
        UserStore userStore = new UserStore(context);
        if (user.getUserName() != null) {
            userStore.saveUserName(user.getUserName());
        }
        userStore.saveIsAdmin(user.isAdmin());
        if (user.getAuthToken() != null) {
            userStore.saveAuthToken(user.getAuthToken());
        }
        if (user.getUserEmail() != null) {
            userStore.saveUserEmail(user.getUserEmail());
        }
//                    userStore.saveUserEmail(result.getUserImage());
        if (user.getUserMobileNumber() != null) {
            userStore.saveUserMobileNumber(user.getUserMobileNumber());
        }
//                    userStore.saveId(result.getId());
        if (user.getRegistrationCode() != null) {
            userStore.saveRegistrationCode(user.getRegistrationCode());
        }
    }

    public static void shareRegistrationCode(@NonNull Context context, @NonNull String registrationCode) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\nDownload app from this url."
                + "\nhttps://play.google.com/store/apps/details?id=com.skybee.tracker&hl=en"
                + "\nRegistration Code: " + registrationCode);
        context.startActivity(Intent.createChooser(sharingIntent, "Share registration code with employee."));
    }

    public static void checkProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    public static void rosterAction(final Context context, String url, String userAction, final ProgressDialog progressDialog, long[] ids) {
        UserStore userStore = new UserStore(context);
        User user = new User();
        user = userStore.getUserDetails();
        ListenableFuture<JSONObject> getRosterActionResult = Factory.getUserService().acceptOrRejectRoster(url, user, userAction, ids);
        Futures.addCallback(getRosterActionResult, new FutureCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    Utility.checkProgressDialog(progressDialog);
                    if (result.has(Constants.JsonConstants.MESSAGE)) {
                        if (result.getString(Constants.JsonConstants.MESSAGE).equals(Constants.JsonConstants.SUCCESS)) {
                            Toast.makeText(context, "Your attendance is marked", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException jsonException) {
                    errorMessage = Constants.ERROR_OCCURRED;
                    Utility.checkProgressDialog(progressDialog);
                    showErrorDialog(context, errorMessage);
                } catch (Exception exception) {
                    errorMessage = Constants.ERROR_OCCURRED;
                    Utility.checkProgressDialog(progressDialog);
                    showErrorDialog(context, errorMessage);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                errorMessage = Constants.ERROR_OCCURRED;
                Utility.checkProgressDialog(progressDialog);
                showErrorDialog(context, errorMessage);
            }
        }, ExecutorUtils.getUIThread());
    }

    public static void saveAttendance(@NonNull final Context context, @NonNull AttendancePojo attendancePojo, @NonNull User user, @NonNull final ProgressDialog progressDialog) {
        ListenableFuture<JSONObject> saveLocationResult = Factory.getUserService().markAttendance(API.SAVE_ATTENDANCE, user, attendancePojo);
        Futures.addCallback(saveLocationResult, new FutureCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    checkProgressDialog(progressDialog);
                    if (result.has(Constants.JsonConstants.MESSAGE)) {
                        if (result.getString(Constants.JsonConstants.MESSAGE).equals(Constants.JsonConstants.SUCCESS)) {
                            Toast.makeText(context, "Your attendance is marked", Toast.LENGTH_LONG).show();
                            context.startActivity(new Intent(context, HomeActivity.class));
                            ((Activity) context).finish();
                        }
                    }
                } catch (JSONException jsonException) {
                    checkProgressDialog(progressDialog);
                    errorMessage = Constants.ERROR_OCCURRED;
                    showErrorDialog(context, errorMessage);
                } catch (Exception exception) {
                    checkProgressDialog(progressDialog);
                    errorMessage = Constants.ERROR_OCCURRED;
                    showErrorDialog(context, errorMessage);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                errorMessage = Constants.ERROR_OCCURRED;
                Utility.checkProgressDialog(progressDialog);
                showErrorDialog(context, errorMessage);
            }
        }, ExecutorUtils.getUIThread());
    }

    public static void saveNotPresent(@NonNull final Context context, @NonNull AttendancePojo attendancePojo) {
        UserStore userStore = new UserStore(context);
        User user = new User();
        user = userStore.getUserDetails();
        ListenableFuture<JSONObject> saveLocationResult = Factory.getUserService().markAttendance(API.SAVE_ATTENDANCE, user, attendancePojo);
        Futures.addCallback(saveLocationResult, new FutureCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (result.has(Constants.JsonConstants.MESSAGE)) {
                        if (result.getString(Constants.JsonConstants.MESSAGE).equals(Constants.JsonConstants.SUCCESS)) {

                        }
                    }
                } catch (JSONException jsonException) {
                } catch (Exception exception) {
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, ExecutorUtils.getUIThread());
    }

    public static void populateGeofenceList(List<Geofence> geofenceList, @NonNull User user) {
        if (geofenceList == null)
            geofenceList = new ArrayList<>();
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(Constants.GEOFENCES_LOCATION)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        user.getCompanyLatitude(),
                        user.getCompanyLongitude(),
                        user.getCompanyRadius()
                )
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());
    }

}
