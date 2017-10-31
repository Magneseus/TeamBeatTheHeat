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
        // Set the route number
        OCBus[] currentList = busList.get(position);
        // TODO: Figure out if we want routeHeading or tripDestination
        String currentNameNumber = ("" + currentList[0].getRouteNo() + " Heading: " + currentList[0].getRouteHeading() + " Destination: " + currentList[0].getTripDestination());
        viewHolder.routeNumberName.setText(currentNameNumber);
    }

    @Override
    public int getItemCount() { return busList.size(); }

    // Helper class that takes info and puts it into the layout at the appropriate position
    static class RoutesForStopViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumberName;

        RoutesForStopViewHolder(View itemView) {
            super(itemView);
            routeNumberName = (TextView)itemView.findViewById(R.id.rfs_route_number_name);
        }
    }
}
