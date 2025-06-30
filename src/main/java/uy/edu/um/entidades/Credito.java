package uy.edu.um.entidades;

import lombok.Getter;
import uy.edu.um.tad.linkedlist.MyList;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;

import java.util.regex.Pattern;

@Getter
public class Credito {
    private final String movieId;
    private final String nombre;
    private final String rol;

    public Credito(String movieId, String nombre, String rol) {
        this.movieId = movieId;
        this.nombre  = nombre;
        this.rol     = rol;
    }


    public static MyList<Credito> fromCSVLine(String linea, Pattern splitter) {
        String[] parts = splitter.split(linea, -1);
        MyList<Credito> resultados = new MyLinkedListImpl<>();
        if (parts.length < 3) return resultados;

        String castJson = trimQuotes(parts[0]);
        String crewJson = trimQuotes(parts[1]);
        String movieId  = parts[2].trim();

        // --- Actores ---
        if (!castJson.isEmpty()) {
            String interior = castJson.substring(1, castJson.length()-1);
            for (String item : interior.split("\\},\\{")) {
                if (!item.startsWith("{")) item = "{" + item;
                if (!item.endsWith("}")) item = item + "}";
                String name = extractField(item, "name");
                if (name != null) resultados.add(new Credito(movieId, name, "Actor"));
            }
        }

        // --- Directores ---
        if (!crewJson.isEmpty()) {
            String interior = crewJson.substring(1, crewJson.length()-1);
            for (String item : interior.split("\\},\\{")) {
                if (!item.startsWith("{")) item = "{" + item;
                if (!item.endsWith("}")) item = item + "}";
                String job = extractField(item, "job");
                if ("Director".equalsIgnoreCase(job)) {
                    String name = extractField(item, "name");
                    if (name != null) resultados.add(new Credito(movieId, name, "Director"));
                }
            }
        }

        return resultados;
    }


    @Deprecated
    public static MyList<Credito> fromCSVLine(String linea) {
        return fromCSVLine(linea,
                Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
    }



    private static String trimQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length()-1);
        }
        return s;
    }

    private static String extractField(String jsonObject, String key) {
        String pattern = "'" + key + "':";
        int idx = jsonObject.indexOf(pattern);
        if (idx < 0) return null;
        int start = jsonObject.indexOf('\'', idx + pattern.length());
        int end   = jsonObject.indexOf('\'', start + 1);
        if (start>=0 && end>start) {
            return jsonObject.substring(start+1, end).trim();
        }
        return null;
    }
}
