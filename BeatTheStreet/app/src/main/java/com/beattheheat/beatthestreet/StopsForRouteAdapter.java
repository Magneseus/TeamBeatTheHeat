package com.beattheheat.beatthestreet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;

import java.util.ArrayList;

/**
 * Created by lauramcdougall on 2017-11-14.
 */

public class StopsForRouteAdapter extends RecyclerView.Adapter<com.beattheheat.beatthestreet.StopsForRouteAdapter.StopsForRouteViewHolder> {

    private Context context;
    private ArrayList<OCStop> stopList;

    StopsForRouteAdapter(Context context, ArrayList<OCStop> stopList) {
        this.context = context;
        this.stopList = stopList;
    }

    @Override
    public StopsForRouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_layout, parent, false);
        return new StopsForRouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StopsForRouteViewHolder viewHolder, int position) {
        // Set the route number view
        final String stopNameStr = stopList.get(position).getStopName();
        final String stopCodeStr = "" + stopList.get(position).getStopCode();
        final String stopIdStr = stopList.get(position).getStopId();
        
        viewHolder.stopName.setText(stopNameStr.replace("\"", ""));
        viewHolder.stopCode.setText(stopCodeStr);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get stopCode and pass it back to DisplayStopsForRouteActivity
                ((DisplayStopsForRouteActivity)context).onClick(stopCodeStr, stopNameStr, stopIdStr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    // Helper class that takes info and puts it into the layout at the appropriate position
        static class StopsForRouteViewHolder extends RecyclerView.ViewHolder {
            TextView stopName;
            TextView stopCode;

            public StopsForRouteViewHolder(View itemView) {
                super(itemView);
                stopName = (TextView)itemView.findViewById(R.id.stop_name);
                stopCode = (TextView)itemView.findViewById(R.id.stop_code);
            }
        }
}


