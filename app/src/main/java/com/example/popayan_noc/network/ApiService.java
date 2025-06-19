package com.example.popayan_noc.network;

import com.example.popayan_noc.model.Lugar;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {
    @GET("lugares") // Este es el path relativo a tu BASE_URL
    Call<List<Lugar>> listarLugares(@Header("Authorization") String authToken);
    
    // Aquí puedes añadir más endpoints en el futuro
    // Ejemplo: @GET("eventos")
    // Call<List<Evento>> listarEventos(@Header("Authorization") String authToken);
}
