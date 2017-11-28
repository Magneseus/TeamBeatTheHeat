package com.beattheheat.beatthestreet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Laura McDougall on 2017-10-30
 *
 * Takes data from OCRoute objects and puts it into the RecyclerView layout
 */

class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private ArrayList<OCRoute> routes;
    private Context context;
    private FavoritesStorage faveRoutes;

    RouteAdapter(final Context context, ArrayList<OCRoute> routeList) {
        faveRoutes = new FavoritesStorage(context);
        this.context = context;
        this.routes = sortRoutes(routeList);
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_layout, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RouteViewHolder viewHolder , final int position) {
        // Set the route name and direction
        String direction = routes.get(position).getRouteNames().get(0).replaceAll("\"", "");
        viewHolder.routeName.setText(String.valueOf(routes.get(position).getRouteNo()));
        viewHolder.routeDirection.setText(direction);
        final String routeDesc = routes.get(position).getRouteNo() + "~" + routes.get(position).getRouteNames().get(0).replaceAll("\"", "");

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get stopCode and pass it back to DisplayRoutes
                ((DisplayRoutesActivity)context).onClick(routeDesc);
            }
        });

        // Set whether we start with a fav or unfav icon
        if (faveRoutes.isFav(routeDesc, FavoritesStorage.FAV_TYPE.ROUTE))
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_favorite);
        else
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_unfavorite);

        // Favorite/Unfavorite functionality
        viewHolder.favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (faveRoutes.toggleFav(routeDesc, FavoritesStorage.FAV_TYPE.ROUTE)) {
                    // Route was added to favorites
                    viewHolder.favIcon.setBackgroundResource(R.drawable.ic_favorite);
                } else {
                    // Route was removed from favorites
                    viewHolder.favIcon.setBackgroundResource(R.drawable.ic_unfavorite);
                }
            }
        });
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
        ImageView favIcon;

        public RouteViewHolder(View itemView) {
            super(itemView);
            routeName = (TextView)itemView.findViewById(R.id.route_name);
            routeDirection = (TextView)itemView.findViewById(R.id.route_direction);
            favIcon  = itemView.findViewById(R.id.stop_fav_button);
        }
    }

    // Replaces the current list of stops with the routes that match the searchp
    public void setFilter(ArrayList<OCRoute> newList) {
        //routes = new ArrayList<OCRoute>();
        //routes.addAll(newList);
        routes = sortRoutes(newList);
        notifyDataSetChanged(); // Refresh the adapter
    }

    private ArrayList<OCRoute> sortRoutes(ArrayList<OCRoute> routeList){
        ArrayList<OCRoute> faves   = new ArrayList<>();
        ArrayList<OCRoute> nonFaves = new ArrayList<>();
        for (OCRoute route : routeList) {
            if (faveRoutes.isFav("" + route.getRouteNo() + "~" + route.getRouteNames().get(0).replaceAll("\"", ""), FavoritesStorage.FAV_TYPE.ROUTE))
                faves.add(route);
            else nonFaves.add(route);
        }
        ArrayList<OCRoute> returnList   = new ArrayList<>();
        Collections.sort(faves);
        Collections.sort(nonFaves);
        returnList.addAll(faves);
        returnList.addAll(nonFaves);

        return returnList;
    }
}