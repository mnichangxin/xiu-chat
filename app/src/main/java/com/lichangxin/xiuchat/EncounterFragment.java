package com.lichangxin.xiuchat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lichangxin.xiuchat.utils.BaseFragment;
import com.lichangxin.xiuchat.utils.NetRequest;
import com.lichangxin.xiuchat.utils.ProperTies;
import com.lichangxin.xiuchat.utils.RecyclerAdapter;

import java.io.IOException;

import okhttp3.Request;

class EncounterRecyclerAdapter extends RecyclerAdapter {
    private JsonArray jsonArray;
    private JsonObject jsonObject;
    private TextView encounterNickname;
    private TextView encounterStatus;
    private TextView encounterSignature;

    public EncounterRecyclerAdapter(int layout, int id, JsonArray jsonArray) {
        super(layout, id);

        this.jsonArray = jsonArray;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        jsonObject = jsonArray.get(position).getAsJsonObject();

        encounterNickname = holder.itemView.findViewById(R.id.encounter_nickname);
        encounterStatus = holder.itemView.findViewById(R.id.encounter_status);
        encounterSignature = holder.itemView.findViewById(R.id.encounter_signature);

        encounterNickname.setText(jsonObject.get("nickname").getAsString());
        encounterSignature.setText(jsonObject.get("signature").getAsString());
    }
    @Override
    public int getItemCount() {
        return jsonArray.size();
    }
}

public class EncounterFragment extends BaseFragment {
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    EncounterRecyclerAdapter encounterRecyclerAdapter;
    private String URL;


    private void request() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NetRequest.getFormRequest(URL + "/api/recommendedUsers", null, new NetRequest.DataCallBack() {
                    @Override
                    public void requestSuccess(String result) throws Exception {
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = jsonParser.parse(result).getAsJsonObject();

                        if (jsonObject.get("status").getAsInt() == 0) {
                            Toast.makeText(getContext(), "暂无推荐", Toast.LENGTH_SHORT).show();
                        } else if (jsonObject.get("status").getAsInt() == 1) {
                            encounterRecyclerAdapter = new EncounterRecyclerAdapter(R.layout.encounter_fragment_item, 0, jsonObject.get("data").getAsJsonArray());

                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(encounterRecyclerAdapter);

                            encounterRecyclerAdapter.notifyDataSetChanged();

                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            Toast.makeText(getContext(), "系统错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void requestFailure(Request request, IOException e) {
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 300);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.encounter_fragment, container, false);

        isPrepared = true;
        loadData();

        return view;
    }
    @SuppressLint("ResourceAsColor")
    @Override
    protected void loadData() {
        if (!isPrepared || !isVisble) {
            return;
        }

        swipeRefreshLayout = view.findViewById(R.id.encounter_swiperefreshlayout);
        recyclerView = view.findViewById(R.id.encounter_recyclerview);
        layoutManager = new LinearLayoutManager(getContext());

        URL = ProperTies.getProperties().getProperty("URL");

        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                request();
            }
        });

        request();
    }
}
