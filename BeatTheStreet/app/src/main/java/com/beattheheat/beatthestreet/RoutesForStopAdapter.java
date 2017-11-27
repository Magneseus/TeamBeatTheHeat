package com.beattheheat.beatthestreet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.ActionReceiver;
import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;

import java.util.ArrayList;

/**
 * Created by Matt on 2017-10-30.
 *
 * Places route and trip information into the RecyclerView layout
 */

class RoutesForStopAdapter extends RecyclerView.Adapter<RoutesForStopAdapter.RoutesForStopViewHolder> {

    private Context context;
    private String stopCode;
    private ArrayList<OCBus[]> busList;

    RoutesForStopAdapter(Context context, String stopCode, ArrayList<OCBus[]> busList) {
        this.context = context;
        this.stopCode  = stopCode;
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
        final OCBus[] currentList = busList.get(position);
        String currentNameNumber = (currentList[0].getRouteNo() + " " + currentList[0].getRouteHeading());
        viewHolder.routeNumberName.setText(currentNameNumber);

        // Set up the trip views
        String minsTilArrival;
        String busTimeIsLive = "GPS";
        for (int i = 0; i < currentList.length; i++) {
            final TextView currentTrip = viewHolder.trips[i];
            final CardView currentCard = viewHolder.cards[i];

            // Set time till arrival
            minsTilArrival = currentList[i].getMinsTilArrival() + " min";
            currentTrip.setText(minsTilArrival);

            // Set whether time is live
            if (currentList[i].isTimeLive())
                viewHolder.gps[i].setText(busTimeIsLive);



            // Set up a click listener so we can set an alarm for this trip
            final int index = i;
            currentTrip.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent();
                   intent.putExtra("action", "start");
                   intent.putExtra("route_num", currentList[index].getRouteNo());
                   intent.putExtra("stop_code", stopCode);
                   intent.putExtra("start_time", currentList[index].getTripStart());
                   intent.putExtra("mins_until_bus", currentList[index].getMinsTilArrival());
                   intent.putExtra("last_available", index==2);

                   ActionReceiver.makeNotification(context, intent);
               }});

        }

        // Click listener to go to timetable for chosen route at current stop
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get route number and pass it back to DisplayStopsActivity
                ((DisplayRoutesForStopActivity)context).onClick(currentList[0].getRouteNo());
            }
        });
    }

    @Override
    public int getItemCount() { return busList.size(); }

    // Helper class that connects data in the adapter to the appropriate parts of the layout
    static class RoutesForStopViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumberName;
        TextView[]  trips = new TextView[3];
        ImageView[] icons = new ImageView[3];
        TextView[]  gps   = new TextView[3];
        CardView[]  cards = new CardView[3];

        RoutesForStopViewHolder(View itemView) {
            super(itemView);
            routeNumberName = itemView.findViewById(R.id.rfs_route_number_name);
            trips[0] = itemView.findViewById(R.id.rfs_stop_time_0);
            trips[1] = itemView.findViewById(R.id.rfs_stop_time_1);
            trips[2] = itemView.findViewById(R.id.rfs_stop_time_2);
            icons[0] = itemView.findViewById(R.id.rfs_icon_0);
            icons[1] = itemView.findViewById(R.id.rfs_icon_1);
            icons[2] = itemView.findViewById(R.id.rfs_icon_2);
            gps[0]   = itemView.findViewById(R.id.rfs_gps_0);
            gps[1]   = itemView.findViewById(R.id.rfs_gps_1);
            gps[2]   = itemView.findViewById(R.id.rfs_gps_2);
            cards[0] = itemView.findViewById(R.id.rfs_card_0);
            cards[1] = itemView.findViewById(R.id.rfs_card_1);
            cards[2] = itemView.findViewById(R.id.rfs_card_2);
        }
    }
}
