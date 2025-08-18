package com.regenerarestudio.regenerapp.data.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente de APIs con configuración de Retrofit para consumir backend Django
 * Maneja la configuración de red, timeouts, logs y conversión JSON
 */
public class ApiClient {

    private static final String TAG = "ApiClient";

    // URL base del backend Django - CAMBIAR SEGÚN TU CONFIGURACIÓN
    private static final String BASE_URL_LOCAL = "http://10.0.2.2:8000/api/"; // Para emulador Android
    private static final String BASE_URL_DEVICE = "http://192.168.100.7:8000/api/"; // Para dispositivos físicos - CAMBIAR IP
    private static final String BASE_URL_PRODUCTION = "https://regenerapp-backend.azurewebsites.net/api/"; // Para producción

    // Timeouts en segundos
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static String currentBaseUrl = BASE_URL_LOCAL; // Por defecto usar local

    /**
     * Obtener instancia única de Retrofit (Singleton)
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = createRetrofit();
            Log.i(TAG, "Nueva instancia de Retrofit creada con URL: " + currentBaseUrl);
        }
        return retrofit;
    }

    /**
     * Obtener instancia del servicio de APIs
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
            Log.i(TAG, "Nueva instancia de ApiService creada");

            // Verificar URL actual de Retrofit para debug
            String actualBaseUrl = retrofit != null ? retrofit.baseUrl().toString() : "null";
            Log.i(TAG, "URL real de Retrofit: " + actualBaseUrl);
        }
        return apiService;
    }

    /**
     * Crear instancia de Retrofit con todas las configuraciones
     */
    private static Retrofit createRetrofit() {
        // Configurar logging para desarrollo
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> Log.d(TAG, "HTTP: " + message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Configurar cliente OkHttp
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        // Agregar interceptor de logging solo en modo desarrollo
        try {
            // Intentar acceder a BuildConfig generado por Android
            Class<?> buildConfigClass = Class.forName("com.regenerarestudio.regenerapp.BuildConfig");
            boolean isDebug = buildConfigClass.getField("DEBUG").getBoolean(null);
            if (isDebug) {
                httpClientBuilder.addInterceptor(loggingInterceptor);
            }
        } catch (Exception e) {
            // Si no se puede acceder a BuildConfig, agregar el interceptor por defecto
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        // Configurar Gson para manejar fechas y nulls
        Gson gson = new GsonBuilder()
                .setLenient() // Ser más flexible con JSON malformado
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // Formato de fechas Django
                .serializeNulls() // Incluir campos null en JSON
                .create();

        // Crear instancia de Retrofit
        return new Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * Cambiar URL base y recrear Retrofit (para switch entre desarrollo/producción)
     */
    public static void setBaseUrl(String baseUrl) {
        if (!baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;
            // FORZAR recreación inmediata
            retrofit = null;
            apiService = null;
            Log.i(TAG, "URL base cambiada a: " + baseUrl);

            // Recrear inmediatamente para evitar usar URL anterior
            getRetrofitInstance();
            Log.i(TAG, "Cliente Retrofit recreado con nueva URL");
        }
    }

    /**
     * Configurar para desarrollo local (emulador)
     */
    public static void useLocalServer() {
        setBaseUrl(BASE_URL_LOCAL);
    }

    /**
     * Configurar para desarrollo local (dispositivo físico)
     * NOTA: Cambiar la IP por la IP de tu máquina de desarrollo
     */
    public static void useDeviceServer() {
        setBaseUrl(BASE_URL_DEVICE);
    }

    /**
     * Configurar para producción (Azure)
     */
    public static void useProductionServer() {
        setBaseUrl(BASE_URL_PRODUCTION);
    }

    /**
     * Obtener URL base actual
     */
    public static String getCurrentBaseUrl() {
        return currentBaseUrl;
    }

    /**
     * Verificar si está usando servidor local
     */
    public static boolean isUsingLocalServer() {
        return currentBaseUrl.contains("10.0.2.2") || currentBaseUrl.contains("192.168.") || currentBaseUrl.contains("localhost");
    }

    /**
     * Reiniciar cliente de APIs (útil para testing o cambios de configuración)
     */
    public static void resetClient() {
        retrofit = null;
        apiService = null;
        Log.i(TAG, "Cliente de APIs reiniciado");
    }

    /**
     * Método de debug para verificar configuración actual
     */
    public static void debugCurrentConfiguration() {
        Log.d(TAG, "=== DEBUG CONFIGURACIÓN API ===");
        Log.d(TAG, "URL base configurada: " + currentBaseUrl);

        if (retrofit != null) {
            Log.d(TAG, "URL real de Retrofit: " + retrofit.baseUrl().toString());
        } else {
            Log.d(TAG, "Retrofit: No inicializado");
        }

        Log.d(TAG, "ApiService: " + (apiService != null ? "Inicializado" : "No inicializado"));
        Log.d(TAG, "¿Servidor local?: " + isUsingLocalServer());
        Log.d(TAG, "=== FIN DEBUG ===");
    }

    /**
     * Configurar automáticamente la URL según el contexto
     */
    public static void configureForEnvironment(Context context, boolean isEmulator) {
        try {
            // Intentar acceder a BuildConfig para determinar si es DEBUG
            Class<?> buildConfigClass = Class.forName("com.regenerarestudio.regenerapp.BuildConfig");
            boolean isDebug = buildConfigClass.getField("DEBUG").getBoolean(null);

            if (isDebug) {
                if (isEmulator) {
                    useLocalServer();
                    Log.i(TAG, "Configurado para emulador - URL: " + BASE_URL_LOCAL);
                } else {
                    useDeviceServer();
                    Log.i(TAG, "Configurado para dispositivo físico - URL: " + BASE_URL_DEVICE);
                }
            } else {
                useProductionServer();
                Log.i(TAG, "Configurado para producción - URL: " + BASE_URL_PRODUCTION);
            }
        } catch (Exception e) {
            // Si no se puede determinar, usar desarrollo por defecto
            if (isEmulator) {
                useLocalServer();
                Log.i(TAG, "Configurado para emulador (por defecto) - URL: " + BASE_URL_LOCAL);
            } else {
                useDeviceServer();
                Log.i(TAG, "Configurado para dispositivo físico (por defecto) - URL: " + BASE_URL_DEVICE);
            }
        }
    }
}