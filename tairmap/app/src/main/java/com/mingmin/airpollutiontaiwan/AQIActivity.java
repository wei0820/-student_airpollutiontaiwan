package com.mingmin.airpollutiontaiwan;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class AQIActivity extends AppCompatActivity implements ServiceConnection, DataService.Callback {

    private SharedPreferences settings;
    public ArrayList<AQI> aqiList = new ArrayList<>();
    public ArrayList<AQI> filteredAqiList = new ArrayList<>();
    private AQIAdapter aqiAdapter;
    private DataService dataService;
    private RecyclerView recyclerView;
    private TextView tvPublishTime;
    private TextView tvStatus;
    private ImageButton ibFilter;
    private Button ibSort;
    private Button imap;
    private ProgressTask progressTask;

    public static AQIActivity my;

    int area = -1;
    int commute = -1;

    String suggestion[]=  {"建議人行道或騎樓行走，盡量沿著遠離馬路的內側走，避免吸入更多廢氣，建議配戴PM2.5口罩或是N95口罩",
            "騎腳踏車時心跳和呼吸較為急促，建議配戴PM2.5口罩，騎腳踏車最容易吸入大量PM2.5，霧霾嚴重時建議您換種交通方式",
            "建議配戴附有擋風鏡片的安全帽，以保護眼睛較不會乾癢，口罩要選用PM2.5口罩或是N95口罩",
            "車內冷氣濾網建議選用PM2.5靜電型冷氣濾網，幫您過濾空氣中的懸浮微粒、有害人體的過敏源和塵螨，下車後記得要配戴PM2.5或是N95口罩哦"};

    int flag=0;

    private SharedPreferences settings2;
    private static final String data = "DATA";
    private static final String nameField = "NAME";
    private static final String nameField1 = "NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqi);

        my = this;

        settings2 = getSharedPreferences(data,0);
        area = settings2.getInt(nameField, -1);
        commute = settings2.getInt(nameField1, -1);

        findViews();
        switchUIStatus(false);
        settings = getSharedPreferences(Settings.PREF_FILE_NAME, MODE_PRIVATE);
        progressTask = new ProgressTask(this);
        progressTask.execute();
    }

    private void findViews() {
        tvPublishTime = findViewById(R.id.activity_aqi_publishTime);
        recyclerView = findViewById(R.id.activity_aqi_recyclerView);
        tvStatus = findViewById(R.id.activity_aqi_status);
        ibFilter = findViewById(R.id.activity_aqi_filter_data);
        ibSort = findViewById(R.id.activity_aqi_sort);
        //ibSort.setTag(R.drawable.ic_sort_descending);
        imap = findViewById(R.id.activity_aqi_map);

        imap.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent sp = new Intent(AQIActivity.this, LocationMAP.class);
                startActivity(sp);

            }
        });
    }

    private void switchUIStatus(boolean isTurnedOn) {
        if (isTurnedOn) {
            ibFilter.setClickable(true);

            //ibSort.setClickable(true);
            tvPublishTime.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.INVISIBLE);
        } else {
            ibFilter.setClickable(false);
            //ibSort.setClickable(false);
            tvPublishTime.setVisibility(View.GONE);
            recyclerView.setVisibility(View.INVISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
        }
    }

    private void updatePublishTime(Date newDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        tvPublishTime.setText(sdf.format(newDate));
    }

    private void updateRecycleView(ArrayList<AQI> newList) {
        if (aqiAdapter == null && recyclerView.getAdapter() == null) {
            aqiAdapter = new AQIAdapter(newList);
            recyclerView.setAdapter(aqiAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            aqiAdapter.updateAQIList(newList);
        }
    }

    private ArrayList<AQI> filterCheckedCounties(ArrayList<AQI> list, boolean[] checkedCounties) {
        ArrayList<AQI> newList = new ArrayList<>();
        for (AQI aqi:list) {
            if (checkedCounties[aqi.getCountyIndex()])
            {
                newList.add(aqi);
            }
        }
        return newList;
    }

    public void check(ArrayList<AQI> newList, int normal)
    {
        String[] some_array = getResources().getStringArray(R.array.counties);
        for (int i=0; i<newList.size(); i++)
        {
            Log.i("TAG", aqiList.get(i).getCounty() + " " + some_array[area]);
            if (aqiList.get(i).getCounty().equals(some_array[area]))
            {
                if (aqiList.get(i).getAQI() < 201 && aqiList.get(i).getAQI() > 101 && flag == 0)
                {
                    flag = 1;
                    if (commute != -1) {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle("建議")
                                .setMessage(suggestion[commute])
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create();
                        dialog.show();
                    }
                    else if (aqiList.get(i).getAQI() > 201 &&  flag == 0) {
                        flag = 1;
                        if (commute != -1) {
                            AlertDialog dialog = new AlertDialog.Builder(this)
                                    .setTitle("建議使用者不宜出門")
                                    .setMessage(suggestion[commute])
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create();
                            dialog.show();
                        }
                        break;

                    }
                    else
                    {
                        flag = 0;
                        if (normal == 1)
                        {
                            AlertDialog dialog = new AlertDialog.Builder(this)
                                    .setTitle("空氣品質正常")
                                    .setMessage("未超標")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create();
                            dialog.show();
                        }
                    }
                }
            }
        }
    }

    public void checkcommute()
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("選擇地區")
                .setSingleChoiceItems(R.array.commute, 0,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //utility.toast(" "+charSequence);
                        commute = which;
                    }
                })
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings2.edit().putInt(nameField, area).commit();
                        settings2.edit().putInt(nameField1, commute).commit();
                        flag = 0;
                        check(aqiList, 1);

                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    public void filter(View view)
    {
        area = 0;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("設定我的最愛: 地區")
                .setSingleChoiceItems(R.array.counties, 0,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //utility.toast(" "+charSequence);
                        area = which;
                    }
                })
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkcommute();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();

        /*
        final boolean[] checkedCounties = readCheckedCounties();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("選擇顯示縣市")
                .setMultiChoiceItems(R.array.counties, checkedCounties, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedCounties[which] = isChecked;
                    }
                })
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<checkedCounties.length; i++) {
                            sb.append(checkedCounties[i]);
                            if (i < (checkedCounties.length - 1)) {
                                sb.append(",");
                            }
                        }
                        settings.edit()
                                .putString(Settings.CHECKED_COUNTIES, sb.toString())
                                .apply();
                        boolean isAscending = settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
                        filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());
                        updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();*/
    }

    private boolean[] readCheckedCounties() {
        boolean[] checkedCounties = new boolean[AQI.COUNTIES.length];

        String strCheckedCounties = settings.getString(Settings.CHECKED_COUNTIES, "");
        if (strCheckedCounties.equals("")) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<checkedCounties.length; i++) {
                checkedCounties[i] = true;
                sb.append(true);
                if (i < (checkedCounties.length - 1)) {
                    sb.append(",");
                }
            }
            settings.edit()
                    .putString(Settings.CHECKED_COUNTIES, sb.toString())
                    .apply();
        } else {
            String[] items = strCheckedCounties.split(",");
            if (items.length == checkedCounties.length) {
                for (int i=0; i<checkedCounties.length; i++) {
                    checkedCounties[i] = Boolean.parseBoolean(items[i]);
                }
            }
        }
        return checkedCounties;
    }

    private ArrayList<AQI> sortByAQIValue(ArrayList<AQI> list, final boolean isAscending) {
        Collections.sort(list, new Comparator<AQI>() {
            @Override
            public int compare(AQI o1, AQI o2) {
                if (isAscending) {
                    return o1.getAQI() - o2.getAQI();
                } else {
                    return o2.getAQI() - o1.getAQI();
                }
            }
        });
        return list;
    }

    public void sort(View view)
    {
        Intent sp = new Intent(AQIActivity.this, RssActivity.class);
        startActivity(sp);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DataService.DataBinder binder = (DataService.DataBinder) service;
        dataService = binder.getService();
        dataService.registerCallback(this);
        dataService.updateData();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        dataService = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onUpdateDataCompleted(ArrayList<AQI> newList) {
        aqiList = newList;

        //check AQI
        if (area != -1)
        {
            check(aqiList, 0);
        }
        else
        {
            Log.i("TAG", "no area");
        }

        updatePublishTime(aqiList.get(0).getPublishTime());
        boolean isAscending =settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
        /*
        if (isAscending && (Integer)ibSort.getTag() == R.drawable.ic_sort_descending) {
            //ibSort.setTag(R.drawable.ic_sort_ascending);
            //ibSort.setImageResource(R.drawable.ic_sort_ascending);
        }*/
        filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());


        updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));


        switchUIStatus(true);
        cancelProgressTask();
    }

    @Override
    public void onUpdateDataError(int error) {
        switch (error) {
            case ERROR_NO_NEW_DATA:
                Snackbar.make(this.recyclerView, "沒有更新資料",  Snackbar.LENGTH_LONG).show();
                break;
            case ERROR_DOWNLOAD_DATA_FAIL:
                Snackbar.make(this.recyclerView, "下載資料失敗",  Snackbar.LENGTH_LONG).show();
                break;
        }
        dataService.loadLastAQIData();
    }

    @Override
    public void onLoadLastDataCompleted(ArrayList<AQI> lastList) {
        if (lastList.size() != 0) {
            aqiList = lastList;
            updatePublishTime(aqiList.get(0).getPublishTime());
            switchUIStatus(true);
            boolean isAscending =settings.getBoolean(Settings.IS_ASCENDING_AQI_SORT, false);
            /*
            if (isAscending && (Integer)ibSort.getTag() == R.drawable.ic_sort_descending) {
                ibSort.setTag(R.drawable.ic_sort_ascending);
                ibSort.setImageResource(R.drawable.ic_sort_ascending);
            }*/
            filteredAqiList = filterCheckedCounties(aqiList, readCheckedCounties());
            updateRecycleView(sortByAQIValue(filteredAqiList, isAscending));
        } else {
            tvStatus.setText("載入資料失敗\n點擊重新載入");
            tvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressTask = new ProgressTask(v.getContext());
                    progressTask.execute();
                    dataService.updateData();
                }
            });
        }
        cancelProgressTask();
    }

    private class ProgressTask extends AsyncTask<Void, Integer, Void> {

        private Context context;
        private AlertDialog dialog;

        private ProgressTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(context)
                    .setView(R.layout.progress_spinner)
                    .setCancelable(false)
                    .show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Auto close this task after 15 seconds.
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private void cancelProgressTask() {
        if (progressTask.getStatus() != AsyncTask.Status.FINISHED) {
            progressTask.cancel(true);
        }
    }
}