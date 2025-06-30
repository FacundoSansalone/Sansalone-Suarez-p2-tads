package uy.edu.um.datos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uy.edu.um.entidades.Pelicula;
import uy.edu.um.entidades.Calificacion;
import uy.edu.um.entidades.Credito;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.linkedlist.MyList;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor
public class BaseDeDatos {

    private final MyHash<String, Pelicula>                 peliculas         = new MyHashImpl<>();
    private final MyHash<String, MyList<String>>           moviesByLanguage  = new MyHashImpl<>();
    private final MyHash<String, MyList<Calificacion>>     ratingsByMovie    = new MyHashImpl<>();
    private final MyHash<String, MyList<String>>           actoresPorPeli    = new MyHashImpl<>();
    private final MyHash<String, MyList<String>>           directoresPorPeli = new MyHashImpl<>();


    private static final Pattern PATTERN_MOVIES_SPLIT   =
            Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final Pattern PATTERN_RATINGS_SPLIT  =
            Pattern.compile(",", Pattern.LITERAL);
    private static final Pattern PATTERN_CREDITS_SPLIT  =
            Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");


    public void cargarDatos() {
        cargarPeliculas();
        cargarCalificaciones();
        cargarCreditos();
    }


    private void cargarPeliculas() {
        int count = 0;
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("movies_metadata.csv");
        if (is == null) {
            System.err.println("No se encontró movies_metadata.csv");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return;

            String linea;
            while ((linea = br.readLine()) != null) {
                Pelicula p = Pelicula.fromCSV(linea, PATTERN_MOVIES_SPLIT);
                if (p == null) continue;
                String pid = p.getId();
                peliculas.put(pid, p);

                String lang = p.getIdiomaOriginal();
                if (lang != null) {
                    MyList<String> lst = moviesByLanguage.get(lang);
                    if (lst == null) {
                        lst = new MyLinkedListImpl<>();
                        moviesByLanguage.put(lang, lst);
                    }
                    lst.add(pid);
                }
                count++;
            }
        } catch (IOException e) {
            System.err.println("Error al cargar películas: " + e.getMessage());
        }
        System.out.println("Películas indexadas: " + count);
    }


    private void cargarCalificaciones() {
        int count = 0;
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("ratings_1mm.csv");
        if (is == null) {
            System.err.println("No se encontró ratings_1mm.csv");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return;

            String linea;
            while ((linea = br.readLine()) != null) {
                Calificacion c = Calificacion.fromCSV(linea, PATTERN_RATINGS_SPLIT);
                if (c == null) continue;
                String mid = c.getMovieId();

                MyList<Calificacion> lst = ratingsByMovie.get(mid);
                if (lst == null) {
                    lst = new MyLinkedListImpl<>();
                    ratingsByMovie.put(mid, lst);
                }
                lst.add(c);
                count++;
            }
        } catch (IOException e) {
            System.err.println("Error al cargar calificaciones: " + e.getMessage());
        }
        System.out.println("Ratings indexados: " + count);
    }


    private void cargarCreditos() {
        int count = 0;
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("credits.csv");
        if (is == null) {
            System.err.println("No se encontró credits.csv");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return;

            String linea;
            while ((linea = br.readLine()) != null) {

                MyList<Credito> lista = Credito.fromCSVLine(linea, PATTERN_CREDITS_SPLIT);
                for (int i = 0; i < lista.size(); i++) {
                    Credito cr = lista.get(i);
                    String mid = cr.getMovieId();

                    MyHash<String, MyList<String>> idx =
                            cr.getRol().equalsIgnoreCase("Actor")
                                    ? actoresPorPeli
                                    : directoresPorPeli;

                    MyList<String> lst = idx.get(mid);
                    if (lst == null) {
                        lst = new MyLinkedListImpl<>();
                        idx.put(mid, lst);
                    }
                    lst.add(cr.getNombre());
                    count++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar créditos: " + e.getMessage());
        }
        System.out.println("Créditos indexados: " + count);
    }

    public MyList<Calificacion> getCalificaciones() {
        MyList<Calificacion> todas = new MyLinkedListImpl<>();
        MyList<String> keys = ratingsByMovie.keys();
        for (int i = 0; i < keys.size(); i++) {
            String id = keys.get(i);
            MyList<Calificacion> parciales = ratingsByMovie.get(id);
            for (int j = 0; j < parciales.size(); j++) {
                todas.add(parciales.get(j));
            }
        }
        return todas;
    }

    public MyList<Credito> getCreditos() {
        MyList<Credito> todos = new MyLinkedListImpl<>();

        MyList<String> actoresKeys = actoresPorPeli.keys();
        for (int i = 0; i < actoresKeys.size(); i++) {
            String mid = actoresKeys.get(i);
            MyList<String> actores = actoresPorPeli.get(mid);
            for (int j = 0; j < actores.size(); j++) {
                todos.add(new Credito(actores.get(j), mid, "Actor"));
            }
        }

        MyList<String> directoresKeys = directoresPorPeli.keys();
        for (int i = 0; i < directoresKeys.size(); i++) {
            String mid = directoresKeys.get(i);
            MyList<String> directores = directoresPorPeli.get(mid);
            for (int j = 0; j < directores.size(); j++) {
                todos.add(new Credito(directores.get(j), mid, "Director"));
            }
        }

        return todos;
    }


}
