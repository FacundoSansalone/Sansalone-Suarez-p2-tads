package uy.edu.um.entidades;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Pattern;

@Getter
public class Calificacion {
    private final String    userId;
    private final String    movieId;
    private final float     puntuacion;
    private final LocalDate fecha;

    public Calificacion(String userId, String movieId, float puntuacion, LocalDate fecha) {
        this.userId     = userId;
        this.movieId    = movieId;
        this.puntuacion = puntuacion;
        this.fecha      = fecha;
    }


    public static Calificacion fromCSV(String linea, Pattern splitter) {
        try {
            String[] p = splitter.split(linea, -1);
            if (p.length < 4) return null;
            // cabecera
            if (p[2].trim().equalsIgnoreCase("rating")) return null;

            String userId  = p[0].trim();
            String movieId = p[1].trim();
            float puntuacion = Float.parseFloat(p[2].trim());
            long epoch = Long.parseLong(p[3].trim());
            LocalDate fecha = Instant.ofEpochSecond(epoch)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            return new Calificacion(userId, movieId, puntuacion, fecha);
        } catch (Exception e) {
            System.err.println("Error parseando Calificacion: " + e.getMessage());
            return null;
        }
    }


    @Deprecated
    public static Calificacion fromCSV(String linea) {
        return fromCSV(linea, Pattern.compile(",", Pattern.LITERAL));
    }
}
