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
    private ArrayList<OCBus[]> busList;

    RoutesForStopAdapter(Context context, ArrayList<OCBus[]> busList) {
        this.context = context;
        this.busList = new ArrayList<>(busList);
    }

    @Override
    public RoutesForStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.routes_for_stop_layout, parent, false);
        return new RoutesForStopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RoutesForStopViewHolder viewHolder, int position) {
        // Set the route number view
        OCBus[] currentList = busList.get(position);
        String currentNameNumber = ("" + currentList[0].getRouteNo() + " " + currentList[0].getRouteHeading());
        viewHolder.routeNumberName.setText(currentNameNumber);

        // Set the trip views
        for (int i = 0; i < currentList.length; i++)
            viewHolder.trips[i].setText("" + currentList[i].getMinsTilArrival() + " min");
    }

    @Override
    public int getItemCount() { return busList.size(); }

    // Helper class that takes info and puts it into the layout at the appropriate position
    static class RoutesForStopViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumberName;
        TextView[] trips = new TextView[3];

        RoutesForStopViewHolder(View itemView) {
            super(itemView);
            routeNumberName = (TextView)itemView.findViewById(R.id.rfs_route_number_name);
            trips[0] = (TextView)itemView.findViewById((R.id.rfs_stop_time_0));
            trips[1] = (TextView)itemView.findViewById((R.id.rfs_stop_time_1));
            trips[2] = (TextView)itemView.findViewById((R.id.rfs_stop_time_2));
        }
    }
}
