package com.example.ikoala.database;

public class LocalOpportunitiesItem {

    private String customSearchQuery;
    private String mapSearchTerm;

    public LocalOpportunitiesItem(){}

    public LocalOpportunitiesItem(String customSearchQuery, String mapSearchTerm){
        this.customSearchQuery = customSearchQuery;
        this.mapSearchTerm = mapSearchTerm;
    }

    public String getCustomSearchQuery() { return customSearchQuery; }
    public String getMapSearchTerm() {  return mapSearchTerm; }

}