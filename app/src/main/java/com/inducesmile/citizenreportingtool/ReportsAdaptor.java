package com.inducesmile.citizenreportingtool;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ReportsAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    IncidentReport[] incidentReportsArray;
    public ReportsAdaptor (Context context, IncidentReport[] incidentReportsArray){
        this.context = context;
        this.incidentReportsArray = incidentReportsArray;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.custom_row, parent, false);
        //Item item = new Item(row);
        IncidentReportViewHolder incidentReportViewHolder = new IncidentReportViewHolder(row);
        return incidentReportViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        /*((Item)viewHolder).textView.setText(items[position]);*/
        ((IncidentReportViewHolder)viewHolder).myReportsDescriptionView.setText(incidentReportsArray[position].getmIncidentDescription());
        Integer emergencyLevel = incidentReportsArray[position].getmEmergencyLevel();
        String emergencyLevelStr = "Emergency Level: " + emergencyLevel.toString() + "%";
        ((IncidentReportViewHolder)viewHolder).myReportsEmergencyLevelView.setText(emergencyLevelStr);
        if (emergencyLevel <= 33){
            ((IncidentReportViewHolder)viewHolder).myReportsEmergencyLevelView.setTextColor(ContextCompat.getColor(context, R.color.emergency_level_low));
        }else if(emergencyLevel <= 66){
            ((IncidentReportViewHolder)viewHolder).myReportsEmergencyLevelView.setTextColor(ContextCompat.getColor(context, R.color.emergency_level_medium));
        }else{
            ((IncidentReportViewHolder)viewHolder).myReportsEmergencyLevelView.setTextColor(ContextCompat.getColor(context, R.color.emergency_level_high));
        }
        File imageFile = new File(incidentReportsArray[position].getmImagePath());
        Picasso.get().load(imageFile).into(((IncidentReportViewHolder)viewHolder).myReportsImageView);
        /*((IncidentReportViewHolder)viewHolder).myReportsImageView.*/
    }

    @Override
    public int getItemCount() {
        return incidentReportsArray.length;
    }

    public class IncidentReportViewHolder extends RecyclerView.ViewHolder {

        TextView myReportsDescriptionView;
        TextView myReportsEmergencyLevelView;
        ImageView myReportsImageView;

        public IncidentReportViewHolder(@NonNull View itemView) {
            super(itemView);
            myReportsDescriptionView = itemView.findViewById(R.id.myReportsDescription);
            myReportsImageView = itemView.findViewById(R.id.myReportsImage);
            myReportsEmergencyLevelView = itemView.findViewById(R.id.myReportsEmergencyLevel);
        }
    }
}
