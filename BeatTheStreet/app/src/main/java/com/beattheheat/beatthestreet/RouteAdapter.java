package com.beattheheat.beatthestreet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

/**
 * Created by Laura McDougall on 2017-10-30
 *
 * Takes data from OCRoute objects and puts it into the RecyclerView layout
 */

class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private ArrayList<OCRoute> routes;
    private Context context;

    RouteAdapter(Context context, ArrayList<OCRoute> routeList) {
        this.context = context;
        this.routes = routeList;
        Collections.sort(routes);
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_layout, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RouteViewHolder viewHolder , int position) {

        // Set the route name and direction
        viewHolder.routeName.setText(String.valueOf(routes.get(position).getRouteNo()));

        String dir1 = routes.get(position).getRouteNames().get(0);
        String dir2 = "";
        for(int i = 0; i <routes.get(position).getRouteNames().size(); i++){
            if (!routes.get(position).getRouteNames().get(i).equals(dir1)){
                dir2 = routes.get(position).getRouteNames().get(i);
                break;
            }
        }

        String directions = dir1 + " / " + dir2;
        directions = directions.replaceAll("\"", "");

        viewHolder.routeDirection.setText(directions);

        // TODO: On click, go to detailed routes page
        // Implement setOnClickListener event
       /*viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    /* Helper class that takes OCRoute info and
       puts it into the layout at the appropriate position */
    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeName;
        TextView routeDirection;

        public RouteViewHolder(View itemView) {
            super(itemView);
            routeName = (TextView)itemView.findViewById(R.id.route_name);
            routeDirection = (TextView)itemView.findViewById(R.id.route_direction);
        }
    }

    // Replaces the current list of stops with the routes that match the searchp
    public void setFilter(ArrayList<OCRoute> newList) {
        routes = new ArrayList<OCRoute>();
        routes.addAll(newList);
        notifyDataSetChanged(); // Refresh the adapter
    }
}