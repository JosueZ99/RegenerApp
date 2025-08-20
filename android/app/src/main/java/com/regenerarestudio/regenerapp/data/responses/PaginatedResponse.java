package com.regenerarestudio.regenerapp.data.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Clase para manejar respuestas paginadas de Django REST Framework
 * Django devuelve: {"count": 3, "next": null, "previous": null, "results": [...]}
 */
public class PaginatedResponse<T> {

    @SerializedName("count")
    private Integer count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<T> results;

    // Constructor vacío
    public PaginatedResponse() {}

    // Getters y Setters
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }

    // Métodos de utilidad
    public boolean hasNext() {
        return next != null && !next.isEmpty();
    }

    public boolean hasPrevious() {
        return previous != null && !previous.isEmpty();
    }

    public boolean isEmpty() {
        return results == null || results.isEmpty();
    }

    public int getResultsCount() {
        return results != null ? results.size() : 0;
    }

    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "count=" + count +
                ", results=" + getResultsCount() + " items" +
                ", hasNext=" + hasNext() +
                ", hasPrevious=" + hasPrevious() +
                '}';
    }
}