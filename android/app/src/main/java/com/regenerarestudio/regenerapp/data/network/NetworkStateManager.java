package com.regenerarestudio.regenerapp.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

import retrofit2.Response;

/**
 * Manejador de estado de red y errores de API
 * Proporciona utilidades para verificar conectividad y manejar respuestas de red
 */
public class NetworkStateManager {

    private static final String TAG = "NetworkStateManager";

    /**
     * Estados posibles de carga
     */
    public enum LoadingState {
        IDLE,       // No está cargando
        LOADING,    // Cargando datos
        SUCCESS,    // Carga exitosa
        ERROR,      // Error en la carga
        NO_NETWORK  // Sin conexión a internet
    }

    /**
     * Clase para encapsular el estado de una operación de red
     */
    public static class NetworkState {
        private LoadingState state;
        private String message;
        private Throwable error;

        public NetworkState(LoadingState state) {
            this.state = state;
        }

        public NetworkState(LoadingState state, String message) {
            this.state = state;
            this.message = message;
        }

        public NetworkState(LoadingState state, String message, Throwable error) {
            this.state = state;
            this.message = message;
            this.error = error;
        }

        // Estados predefinidos comunes
        public static NetworkState loading() {
            return new NetworkState(LoadingState.LOADING, "Cargando datos...");
        }

        public static NetworkState success() {
            return new NetworkState(LoadingState.SUCCESS, "Datos cargados correctamente");
        }

        public static NetworkState success(String message) {
            return new NetworkState(LoadingState.SUCCESS, message);
        }

        public static NetworkState error(String message) {
            return new NetworkState(LoadingState.ERROR, message);
        }

        public static NetworkState error(String message, Throwable error) {
            return new NetworkState(LoadingState.ERROR, message, error);
        }

        public static NetworkState noNetwork() {
            return new NetworkState(LoadingState.NO_NETWORK, "Sin conexión a internet");
        }

        public static NetworkState idle() {
            return new NetworkState(LoadingState.IDLE);
        }

        // Getters
        public LoadingState getState() { return state; }
        public String getMessage() { return message; }
        public Throwable getError() { return error; }

        public boolean isLoading() { return state == LoadingState.LOADING; }
        public boolean isSuccess() { return state == LoadingState.SUCCESS; }
        public boolean isError() { return state == LoadingState.ERROR; }
        public boolean hasNoNetwork() { return state == LoadingState.NO_NETWORK; }
        public boolean isIdle() { return state == LoadingState.IDLE; }
    }

    /**
     * Verificar si hay conexión a internet
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    /**
     * Verificar si hay conexión WiFi
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) return false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
    }

    /**
     * Procesar respuesta de Retrofit y devolver estado apropiado
     */
    public static <T> NetworkState processResponse(Response<T> response) {
        if (response == null) {
            return NetworkState.error("Respuesta nula del servidor");
        }

        if (response.isSuccessful()) {
            return NetworkState.success();
        } else {
            String errorMessage = getErrorMessage(response.code(), response.message());
            Log.e(TAG, "Error HTTP " + response.code() + ": " + errorMessage);
            return NetworkState.error(errorMessage);
        }
    }

    /**
     * Procesar excepción y devolver estado apropiado
     */
    public static NetworkState processError(Throwable error) {
        if (error == null) {
            return NetworkState.error("Error desconocido");
        }

        Log.e(TAG, "Error de red: " + error.getMessage(), error);

        if (error instanceof IOException) {
            return NetworkState.error("Error de conexión. Verifique su internet.", error);
        } else if (error.getMessage() != null) {
            if (error.getMessage().contains("timeout")) {
                return NetworkState.error("Tiempo de espera agotado. Intente nuevamente.", error);
            } else if (error.getMessage().contains("Unable to resolve host")) {
                return NetworkState.error("No se puede conectar al servidor.", error);
            }
        }

        return NetworkState.error("Error inesperado: " + error.getMessage(), error);
    }

    /**
     * Obtener mensaje de error según código HTTP
     */
    private static String getErrorMessage(int code, String message) {
        switch (code) {
            case 400:
                return "Solicitud incorrecta - Datos inválidos";
            case 401:
                return "No autorizado - Sesión expirada";
            case 403:
                return "Acceso denegado";
            case 404:
                return "Recurso no encontrado";
            case 408:
                return "Tiempo de espera agotado";
            case 500:
                return "Error interno del servidor";
            case 502:
                return "Error de conexión con el servidor";
            case 503:
                return "Servicio no disponible temporalmente";
            case 504:
                return "Tiempo de espera del servidor agotado";
            default:
                return message != null && !message.isEmpty() ?
                        message : "Error del servidor (Código: " + code + ")";
        }
    }

    /**
     * Clase helper para manejar estados en ViewModels
     */
    public static class NetworkStateLiveData {
        private final MutableLiveData<NetworkState> networkState = new MutableLiveData<>();

        public NetworkStateLiveData() {
            networkState.setValue(NetworkState.idle());
        }

        public LiveData<NetworkState> getNetworkState() {
            return networkState;
        }

        public void setLoading() {
            networkState.setValue(NetworkState.loading());
        }

        public void setLoading(String message) {
            networkState.setValue(new NetworkState(LoadingState.LOADING, message));
        }

        public void setSuccess() {
            networkState.setValue(NetworkState.success());
        }

        public void setSuccess(String message) {
            networkState.setValue(NetworkState.success(message));
        }

        public void setError(String message) {
            networkState.setValue(NetworkState.error(message));
        }

        public void setError(Throwable error) {
            networkState.setValue(NetworkStateManager.processError(error));
        }

        public void setNoNetwork() {
            networkState.setValue(NetworkState.noNetwork());
        }

        public void setIdle() {
            networkState.setValue(NetworkState.idle());
        }

        public NetworkState getCurrentState() {
            return networkState.getValue();
        }

        public boolean isCurrentlyLoading() {
            NetworkState current = getCurrentState();
            return current != null && current.isLoading();
        }
    }

    /**
     * Verificar conectividad antes de realizar operación de red
     */
    public static boolean checkConnectivityAndNotify(Context context, NetworkStateLiveData stateLiveData) {
        if (!isNetworkAvailable(context)) {
            if (stateLiveData != null) {
                stateLiveData.setNoNetwork();
            }
            return false;
        }
        return true;
    }
}