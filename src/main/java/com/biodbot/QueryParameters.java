package com.biodbot;

public class QueryParameters{
    private String metric;
    private String dimension;
    private String filter_type;
    private String filter;
    
    public QueryParameters(){
    }
    public String getMetric(){
        return metric;
    }
    public void setMetric(String metric){
        this.metric = metric;
    }
    public String getDimension(){
        return dimension;
    }
    public void setDimension(String dimension){
        this.dimension = dimension;
    }
    public String getFilterType(){
        return filter_type;
    }
    public void setFilterType(String filter_type){
        this.filter_type = filter_type;
    }
    public String getFilter(){
        return filter;
    }
    public void setFilter(String filter){
        this.filter = filter;
    }
}