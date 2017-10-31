package com.beattheheat.beatthestreet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Matt on 2017-10-30.
 *
 * Places route and trip information into the RecyclerView layout
 */

class RoutesForStopAdapter extends RecyclerView.Adapter<RoutesForStopAdapter.RoutesForStopViewHolder> {

    private Context context;
    private HashMap<Integer, OCBus[]> buses;
    private ArrayList<Integer> routeNumbers;

    RoutesForStopAdapter(Context context, HashMap<Integer, OCBus[]> buses) {
        this.context = context;
        this.buses = buses;
        if (buses != null)
            this.routeNumbers = new ArrayList<>(buses.keySet());
    }

    @Override
    public RoutesForStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.routes_for_stop_layout, parent, false);
        return new RoutesForStopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RoutesForStopViewHolder viewHolder, int position) {
        // Set the route number
        viewHolder.routeNumber.setText(routeNumbers.get(position));
    }

    @Override
    public int getItemCount() { return buses == null ? 0 : buses.size(); }

    // Helper class that takes info and puts it into the layout at the appropriate position
    static class RoutesForStopViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumber;

        RoutesForStopViewHolder(View itemView) {
            super(itemView);
            routeNumber = (TextView)itemView.findViewById(R.id.rfs_route_number);
        }
    }
}
