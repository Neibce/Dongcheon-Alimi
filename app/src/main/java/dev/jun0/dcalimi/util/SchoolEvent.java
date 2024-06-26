package dev.jun0.dcalimi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import dev.jun0.dcalimi.R;
import dev.jun0.dcalimi.item.SchoolEventListItem;

public class SchoolEvent {
    private final Context mContext;
    private View mView;
    private final FragmentManager mFragmentManager;
    private ArrayList<SchoolEventListItem> mCalenderListItems;
    private static SharedPreferences pref;

    public SchoolEvent(FragmentManager fragmentManager, Context context){
        mFragmentManager = fragmentManager;
        mContext = context;
        pref = mContext.getSharedPreferences("event", Context.MODE_PRIVATE);
    }

    public SchoolEvent(FragmentManager fragmentManager, View view){
        mFragmentManager = fragmentManager;
        mContext = view.getContext();
        mView = view;
        pref = mContext.getSharedPreferences("event", Context.MODE_PRIVATE);
    }

    public ArrayList<SchoolEventListItem> getList(String year, String month) {
        mCalenderListItems = new ArrayList<>();

        String strJSONEvent = pref.getString(year + month,null);
        if(strJSONEvent == null)
            return mCalenderListItems;
        try {
            JSONArray jsonArray = new JSONArray(strJSONEvent);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String strDate = jsonObject.getString("date");
                String strTitle = jsonObject.getString("title");

                SchoolEventListItem schoolEventListItem = new SchoolEventListItem();
                schoolEventListItem.setDate(jsonObject.getString("date"));
                int pos = mCalenderListItems.indexOf(schoolEventListItem);
                if(pos != -1)
                    mCalenderListItems.get(pos).setEvent(mCalenderListItems.get(pos).getEvent() + "/" + strTitle);
                else
                    addEventItem(strDate, getDay(strDate), strTitle);

                Log.i("SE", strDate + ": " + pos);
            }
            return mCalenderListItems;
        } catch (JSONException e) {
            e.printStackTrace();
            return mCalenderListItems;
        }
    }

    public Boolean hasList(String year, String month){
        return (pref.getString(year + month,null) != null);
    }

    public void download(String year, String month){
        Runnable runnable = new SchoolEventDownloadRunnable(mFragmentManager, mView, year, month, mOnDownloadCompleteListener);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private String getDay(String date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, Integer.parseInt(date));

        String strWeek = null;
        int nWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (nWeek == 1) {
            strWeek = "일요일";
        } else if (nWeek == 2) {
            strWeek = "월요일";
        } else if (nWeek == 3) {
            strWeek = "화요일";
        } else if (nWeek == 4) {
            strWeek = "수요일";
        } else if (nWeek == 5) {
            strWeek = "목요일";
        } else if (nWeek == 6) {
            strWeek = "금요일";
        } else if (nWeek == 7) {
            strWeek = "토요일";
        }

        return strWeek;
    }


    private void addEventItem(String date, String day, String event) {
        SchoolEventListItem item = new SchoolEventListItem();

        item.setDate(date);
        item.setDay(day);
        item.setEvent(event);

        mCalenderListItems.add(item);
    }

    public interface OnDownloadCompleteListener {
        void onDownloadComplete();
    }

    private OnDownloadCompleteListener mOnDownloadCompleteListener;
    public void setOnDownloadCompleteListener(OnDownloadCompleteListener onDownloadCompleteListener){
        mOnDownloadCompleteListener = onDownloadCompleteListener;
    }

    private static class SchoolEventDownloadRunnable implements Runnable {
        private final Handler mHandler;
        private final Context mContext;
        private final String mYear;
        private final String mMonth;
        private final OnDownloadCompleteListener mOnDownloadCompleteListener;

        SchoolEventDownloadRunnable(FragmentManager fragmentManager, View view, String year, String month , OnDownloadCompleteListener onDownloadCompleteListener){
            mHandler = new MyHandler(fragmentManager, view);
            mContext = view.getContext();
            mYear = year;
            mMonth = month;
            mOnDownloadCompleteListener = onDownloadCompleteListener;
        }

        @Override
        public void run() {
            try {
                int connectivityStatus = NetworkStatus.getConnectivityStatus(mContext);
                if(connectivityStatus == NetworkStatus.TYPE_NOT_CONNECTED) {
                    throw new Exception();
                }

                sendHandlerShowDialog(mContext.getString(R.string.info),mContext.getString(R.string.downloading_school_event_list), false,false);
                Thread.sleep(1000);

                RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
                String strResponse = requestHttpURLConnection.get("http://school.busanedu.net/dongcheon-h/sv/schdulView/selectSvList.do?sysId=dongcheon-h&monthFirst="+mYear+"/"+mMonth+"/01&monthEnmt="+mYear+"/"+mMonth+"/31", 15000);

                JSONArray jsonArrayResponse = new JSONArray(strResponse);
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < jsonArrayResponse.length(); i++) {
                    if(jsonArrayResponse.getJSONObject(i).has("sysId") && jsonArrayResponse.getJSONObject(i).getString("sysId").equals("dongcheon-h")) {
                        String strDate = jsonArrayResponse.getJSONObject(i).getString("bgnde");
                        strDate = strDate.substring(strDate.length() - 2);
                        String strTitle = jsonArrayResponse.getJSONObject(i).getString("schdulTitle");

                        if(strTitle.equals("토요휴업일"))
                            continue;

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("date", strDate);
                        jsonObject.put("title", strTitle);

                        jsonArray.put(jsonObject);
                    }
                }
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(mYear + mMonth, jsonArray.toString());
                editor.apply();

                Log.d("TAG", "doInBackground: " + jsonArray.toString());

                sendHandlerHideDialog();
                sendHandlerShowSnackbar(mContext.getString(R.string.download_successfully));

                if(mOnDownloadCompleteListener != null)
                    sendHandlerCallDownloadComplete(mOnDownloadCompleteListener);
            } catch (Exception e) {
                sendHandlerHideDialog();
                sendHandlerShowSnackbar(mContext.getString(R.string.download_failed));
                e.printStackTrace();
            }
        }

        private void sendHandlerShowDialog(String dialogTitle, String dialogMessage, boolean hasPositiveButton, boolean cancelable){
            Bundle data = new Bundle();
            data.putString("title", dialogTitle);
            data.putString("msg", dialogMessage);
            data.putBoolean("hasPositive", hasPositiveButton);
            data.putBoolean("cancelable", cancelable);

            Message msg = new Message();
            msg.setData(data);
            msg.what = MyHandler.SHOW_DIALOG;
            mHandler.sendMessage(msg);
        }

        private void sendHandlerHideDialog(){
            Message msg = new Message();
            msg.what = MyHandler.HIDE_DIALOG;
            mHandler.sendMessage(msg);
        }

        private void sendHandlerCallDownloadComplete(OnDownloadCompleteListener onDownloadCompleteListener){
            Message msg = new Message();
            msg.what = MyHandler.CALL_SCHOOL_EVENT_DOWNLOAD_COMPLETE;
            msg.obj = onDownloadCompleteListener;
            mHandler.sendMessage(msg);
        }

        private void sendHandlerShowSnackbar(String text){
            Message msg = new Message();
            msg.obj = text;
            msg.what = MyHandler.SHOW_SNACKBAR;
            mHandler.sendMessage(msg);
        }
    }
}
