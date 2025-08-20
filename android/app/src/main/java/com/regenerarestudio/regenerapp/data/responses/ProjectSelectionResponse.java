package com.regenerarestudio.regenerapp.data.responses;

import com.google.gson.annotations.SerializedName;
import com.regenerarestudio.regenerapp.data.models.Project;

/**
 * Respuesta al seleccionar un proyecto
 */
public class ProjectSelectionResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("project")
    private Project project;

    @SerializedName("previous_selected")
    private Long previousSelectedId;

    // Constructor vacío
    public ProjectSelectionResponse() {}

    // Getters y Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Long getPreviousSelectedId() { return previousSelectedId; }
    public void setPreviousSelectedId(Long previousSelectedId) { this.previousSelectedId = previousSelectedId; }

    public boolean isSuccessful() {
        return success != null && success;
    }
}