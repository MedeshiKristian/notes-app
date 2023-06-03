package com.uzhnu.notesapp.utilities.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NotificationModel;
import com.uzhnu.notesapp.network.ApiClient;
import com.uzhnu.notesapp.network.ApiService;
import com.uzhnu.notesapp.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingUtil {

    @NonNull
    public static String getTopic(@NonNull FolderModel folderModel) {
        return folderModel.getCreatedBy() + folderModel.getCollectionName();
    }

    public static void sendMessage(String topic, @NonNull NotificationModel notificationModel) {
        try {
            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_TO, "/topics/" + topic);
            JSONObject data = NotificationModel.toJson(notificationModel);
            body.put(Constants.REMOTE_MSG_DATA, data);

            sendNotification(body.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                Log.e(Constants.TAG, error.getString("error"));
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(Constants.TAG, "Notification sent successfully");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(Constants.TAG, t.getMessage());
            }
        });
    }
}
