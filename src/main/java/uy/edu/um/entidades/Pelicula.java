package uy.edu.um.entidades;

import uy.edu.um.tad.linkedlist.MyList;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class Pelicula {
    private final String            id;
    private final String            titulo;
    private final int               anio;
    private final float             presupuesto;
    private final float             ingresos;
    private final String            coleccion;
    private final MyList<String>    generos;
    private final String            idiomaOriginal;

    public Pelicula(
            String id,
            String titulo,
            int anio,
            float presupuesto,
            float ingresos,
            String coleccion,
            MyList<String> generos,
            String idiomaOriginal
    ) {
        this.id             = id;
        this.titulo         = titulo;
        this.anio           = anio;
        this.presupuesto    = presupuesto;
        this.ingresos       = ingresos;
        this.coleccion      = coleccion;
        this.generos        = generos;
        this.idiomaOriginal = idiomaOriginal;
    }


    public static Pelicula fromCSV(String linea, Pattern splitter) {
        try {
            String[] p = splitter.split(linea, -1);
            if (p.length < 16) return null;

            String idRaw = trimQuotes(p[5]);
            if (idRaw.isEmpty()) return null;

            String titleRaw = trimQuotes(p[15]);
            String titulo  = titleRaw.isEmpty() ? "N/A" : titleRaw;


            String fecha = trimQuotes(p[9]);
            int anio = 0;
            if (fecha.length() >= 4) {
                try { anio = Integer.parseInt(fecha.substring(0,4)); }
                catch(Exception ignored) {}
            }

            float presupuesto = parseFloatOrDefault(trimQuotes(p[2]), 0f);
            float ingresos    = parseFloatOrDefault(trimQuotes(p[10]), 0f);


            String colRaw = trimQuotes(p[1]);
            String coleccion = colRaw.isEmpty()
                    ? idRaw
                    : extractCollectionId(colRaw, idRaw);


            MyList<String> generos = new MyLinkedListImpl<>();
            String genField = trimQuotes(p[3]);
            for (String part : genField.split("\\{")) {
                int idx = part.indexOf("name");
                if (idx >= 0) {
                    int s = part.indexOf('\'', idx+4);
                    int e = part.indexOf('\'', s+1);
                    if (s>0 && e>s) generos.add(part.substring(s+1,e).trim());
                }
            }

            String idiomaRaw = trimQuotes(p[7]);
            String idiomaOriginal = idiomaRaw.isEmpty() ? null : idiomaRaw;

            return new Pelicula(
                    idRaw, titulo, anio,
                    presupuesto, ingresos,
                    coleccion, generos,
                    idiomaOriginal
            );
        } catch (Exception ex) {
            System.err.println("Error parseando Pelicula: " + ex.getMessage());
            return null;
        }
    }


    @Deprecated
    public static Pelicula fromCSV(String linea) {
        return fromCSV(linea, Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
    }



    private static String trimQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length()>=2) {
            return s.substring(1, s.length()-1);
        }
        return s;
    }

    private static String extractCollectionId(String rawJson, String def) {
        int idx = rawJson.indexOf("id");
        if (idx>=0) {
            int c = rawJson.indexOf(':', idx);
            int v = rawJson.indexOf(',', c);
            if (c>0 && v>c) {
                String num = rawJson.substring(c+1,v).replaceAll("\\D","");
                if (!num.isEmpty()) return num;
            }
        }
        return def;
    }

    private static float parseFloatOrDefault(String s, float def) {
        try { return Float.parseFloat(s); }
        catch(Exception e) { return def; }
    }
}
