package uy.edu.um.consultas;

import uy.edu.um.datos.BaseDeDatos;
import uy.edu.um.entidades.*;
import uy.edu.um.tad.hash.*;
import uy.edu.um.tad.heap.*;
import uy.edu.um.tad.linkedlist.*;

public class Consultas {

    private BaseDeDatos base;

    public Consultas(BaseDeDatos base) {
        this.base = base;
    }

    public void top5PeliculasConMasEvaluacionesPorIdioma() {
        String[] idiomas = {"en", "fr", "it", "es", "pt"};
        MyHash<String, MyHash<String, Integer>> acumuladoPorIdioma = new MyHashImpl<>();

        for (String idioma : idiomas) {
            acumuladoPorIdioma.put(idioma, new MyHashImpl<>());
        }

        MyList<Calificacion> calificaciones = base.getCalificaciones();
        for (int i = 0; i < calificaciones.size(); i++) {
            Calificacion c = calificaciones.get(i);
            Pelicula p = base.getPeliculas().get(c.getMovieId());
            if (p == null || p.getIdiomaOriginal() == null) continue;

            MyHash<String, Integer> hash = acumuladoPorIdioma.get(p.getIdiomaOriginal());
            if (hash == null) continue;

            Integer count = hash.get(p.getId());
            if (count == null) count = 0;
            hash.put(p.getId(), count + 1);
        }

        for (String idioma : idiomas) {
            MyHeap<ElementoHeap<Pelicula>> heap = new MyHeapImpl<>();
            MyHash<String, Integer> hash = acumuladoPorIdioma.get(idioma);
            MyList<String> pelis = hash.keys();

            for (int i = 0; i < pelis.size(); i++) {
                String movieId = pelis.get(i);
                int cant = hash.get(movieId);
                Pelicula p = base.getPeliculas().get(movieId);
                if (p != null) {
                    heap.insert(new ElementoHeap<>(p, cant));
                }
            }

            System.out.println("Top 5 películas para idioma: " + idioma);
            int top = 0;
            while (top < 5 && heap.size() > 0) {
                ElementoHeap<Pelicula> e = heap.delete();
                Pelicula p = e.getValor();
                System.out.println(p.getId() + "," + p.getTitulo() + "," + e.getPrioridad() + "," + idioma);
                top++;
            }
        }
    }

    public void top10PeliculasMejorPromedioMas100Calificaciones() {
        MyHash<String, Integer> contador = new MyHashImpl<>();
        MyHash<String, Float> acumulador = new MyHashImpl<>();

        MyList<Calificacion> calificaciones = base.getCalificaciones();

        for (int i = 0; i < calificaciones.size(); i++) {
            Calificacion c = calificaciones.get(i);
            String id = c.getMovieId();

            Integer cant = contador.get(id);
            if (cant == null) cant = 0;
            contador.put(id, cant + 1);

            Float suma = acumulador.get(id);
            if (suma == null) suma = 0f;
            acumulador.put(id, suma + c.getPuntuacion());
        }

        MyHeap<ElementoHeap<Pelicula>> heap = new MyHeapImpl<>();
        MyList<String> ids = contador.keys();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            int cant = contador.get(id);
            if (cant > 100) {
                float promedio = acumulador.get(id) / cant;
                Pelicula p = base.getPeliculas().get(id);
                if (p != null) {
                    heap.insert(new ElementoHeap<>(p, promedio));
                }
            }
        }

        int top = 0;
        while (top < 10 && heap.size() > 0) {
            ElementoHeap<Pelicula> e = heap.delete();
            Pelicula p = e.getValor();
            System.out.println(p.getId() + "," + p.getTitulo() + "," + e.getPrioridad());
            top++;
        }
    }

    public void top5ColeccionesIngresos() {
        MyHash<String, Float> ingresosPorColeccion = new MyHashImpl<>();
        MyHash<String, Integer> contadorPeliculasPorColeccion = new MyHashImpl<>();

        MyHash<String, Pelicula> peliculas = base.getPeliculas();
        MyList<String> ids = peliculas.keys();

        for (int i = 0; i < ids.size(); i++) {
            Pelicula p = peliculas.get(ids.get(i));
            String coleccion = (p.getColeccion() == null || p.getColeccion().isEmpty())
                    ? "PELÍCULA_" + p.getId()
                    : p.getColeccion();

            Float ingresos = ingresosPorColeccion.get(coleccion);
            if (ingresos == null) ingresos = 0f;
            ingresos += p.getIngresos();
            ingresosPorColeccion.put(coleccion, ingresos);

            Integer contador = contadorPeliculasPorColeccion.get(coleccion);
            if (contador == null) contador = 0;
            contadorPeliculasPorColeccion.put(coleccion, contador + 1);
        }

        MyHeap<ElementoHeap<String>> heap = new MyHeapImpl<>();
        MyList<String> colecciones = ingresosPorColeccion.keys();
        for (int i = 0; i < colecciones.size(); i++) {
            String col = colecciones.get(i);
            float ingreso = ingresosPorColeccion.get(col);
            heap.insert(new ElementoHeap<>(col, ingreso));
        }

        int top = 0;
        while (top < 5 && heap.size() > 0) {
            ElementoHeap<String> e = heap.delete();
            String col = e.getValor();
            int cantidadPeliculas = contadorPeliculasPorColeccion.get(col);
            System.out.println(col + "," + cantidadPeliculas + "," + e.getPrioridad());
            top++;
        }
    }

    public void top10DirectoresMediana() {
        MyHash<String, MyList<Float>> ratingsPorDirector = new MyHashImpl<>();
        MyHash<String, Integer> evaluacionesPorDirector = new MyHashImpl<>();
        MyHash<String, MyList<String>> peliculasPorDirector = new MyHashImpl<>();

        MyList<Credito> creditos = base.getCreditos();
        MyList<Calificacion> calificaciones = base.getCalificaciones();

        MyHash<String, String> directoresPorPelicula = new MyHashImpl<>();
        for (int i = 0; i < creditos.size(); i++) {
            Credito c = creditos.get(i);
            if (c.getRol().equalsIgnoreCase("Director")) {
                directoresPorPelicula.put(c.getMovieId(), c.getNombre());
                MyList<String> pelis = peliculasPorDirector.get(c.getNombre());
                if (pelis == null) pelis = new MyLinkedListImpl<>();
                pelis.add(c.getMovieId());
                peliculasPorDirector.put(c.getNombre(), pelis);
            }
        }

        for (int i = 0; i < calificaciones.size(); i++) {
            Calificacion c = calificaciones.get(i);
            String director = directoresPorPelicula.get(c.getMovieId());
            if (director != null) {
                MyList<Float> lista = ratingsPorDirector.get(director);
                if (lista == null) lista = new MyLinkedListImpl<>();
                lista.add((float) c.getPuntuacion());
                ratingsPorDirector.put(director, lista);

                Integer count = evaluacionesPorDirector.get(director);
                if (count == null) count = 0;
                evaluacionesPorDirector.put(director, count + 1);
            }
        }

        MyHeap<ElementoHeap<String>> heap = new MyHeapImpl<>();
        MyList<String> directores = ratingsPorDirector.keys();
        for (int i = 0; i < directores.size(); i++) {
            String dir = directores.get(i);
            if (peliculasPorDirector.get(dir).size() > 1 && evaluacionesPorDirector.get(dir) > 100) {
                float mediana = calcularMediana(ratingsPorDirector.get(dir));
                heap.insert(new ElementoHeap<>(dir, mediana));
            }
        }

        int top = 0;
        while (top < 10 && heap.size() > 0) {
            ElementoHeap<String> e = heap.delete();
            System.out.println(e.getValor() + "," + peliculasPorDirector.get(e.getValor()).size() + "," + e.getPrioridad());
            top++;
        }
    }

    private float calcularMediana(MyList<Float> lista) {
        MyHeap<ElementoHeap<Float>> heap = new MyHeapImpl<>();
        for (int i = 0; i < lista.size(); i++) {
            heap.insert(new ElementoHeap<>(lista.get(i), -lista.get(i)));
        }

        MyList<Float> ordenada = new MyLinkedListImpl<>();
        while (heap.size() > 0) {
            ordenada.add(heap.delete().getValor());
        }

        int n = ordenada.size();
        if (n % 2 == 1) return ordenada.get(n / 2);
        return (ordenada.get(n / 2 - 1) + ordenada.get(n / 2)) / 2f;
    }
    public void actorMasCalificadoPorMes() {
        MyHash<String, MyHash<String, Integer>> calificacionesPorMesActor = new MyHashImpl<>();
        MyHash<String, MyHash<String, MyList<String>>> peliculasPorMesActor = new MyHashImpl<>();

        MyList<Calificacion> calificaciones = base.getCalificaciones();
        MyList<Credito> creditos = base.getCreditos();

        MyHash<String, MyList<String>> actoresPorPelicula = new MyHashImpl<>();
        for (int i = 0; i < creditos.size(); i++) {
            Credito c = creditos.get(i);
            if (c.getRol().equalsIgnoreCase("Actor")) {
                MyList<String> lista = actoresPorPelicula.get(c.getMovieId());
                if (lista == null) {
                    lista = new MyLinkedListImpl<>();
                    actoresPorPelicula.put(c.getMovieId(), lista);
                }
                lista.add(c.getNombre());
            }
        }

        for (int i = 0; i < calificaciones.size(); i++) {
            Calificacion c = calificaciones.get(i);
            String movieId = c.getMovieId();
            Pelicula p = base.getPeliculas().get(movieId);
            if (p == null) continue;

            int mes = c.getFecha().getMonthValue();
            String mesStr = String.valueOf(mes);

            MyList<String> actores = actoresPorPelicula.get(movieId);
            if (actores == null) continue;

            for (int j = 0; j < actores.size(); j++) {
                String actor = actores.get(j);

                MyHash<String, Integer> porActor = calificacionesPorMesActor.get(mesStr);
                if (porActor == null) {
                    porActor = new MyHashImpl<>();
                    calificacionesPorMesActor.put(mesStr, porActor);
                }
                Integer cantidad = porActor.get(actor);
                if (cantidad == null) cantidad = 0;
                porActor.put(actor, cantidad + 1);

                MyHash<String, MyList<String>> porActorPeliculas = peliculasPorMesActor.get(mesStr);
                if (porActorPeliculas == null) {
                    porActorPeliculas = new MyHashImpl<>();
                    peliculasPorMesActor.put(mesStr, porActorPeliculas);
                }
                MyList<String> pelis = porActorPeliculas.get(actor);
                if (pelis == null) {
                    pelis = new MyLinkedListImpl<>();
                    porActorPeliculas.put(actor, pelis);
                }
                if (!contiene(pelis, movieId)) pelis.add(movieId);
            }
        }

        MyList<String> meses = calificacionesPorMesActor.keys();
        for (int i = 0; i < meses.size(); i++) {
            String mes = meses.get(i);
            MyHash<String, Integer> porActor = calificacionesPorMesActor.get(mes);
            MyList<String> actores = porActor.keys();

            String mejorActor = null;
            int max = -1;
            for (int j = 0; j < actores.size(); j++) {
                String actor = actores.get(j);
                int cant = porActor.get(actor);
                if (cant > max) {
                    max = cant;
                    mejorActor = actor;
                }
            }

            if (mejorActor != null) {
                int calif = porActor.get(mejorActor);
                int pelis = peliculasPorMesActor.get(mes).get(mejorActor).size();
                System.out.println(mes + "," + mejorActor + "," + pelis + "," + calif);
            }
        }
    }

    public void usuariosMasActivosPorGenero() {
        MyHash<String, MyHash<String, Integer>> generoUsuario = new MyHashImpl<>();
        MyHash<String, Integer> totalPorGenero = new MyHashImpl<>();
        MyList<Calificacion> calificaciones = base.getCalificaciones();

        for (int i = 0; i < calificaciones.size(); i++) {
            Calificacion c = calificaciones.get(i);
            Pelicula p = base.getPeliculas().get(c.getMovieId());
            if (p == null) continue;

            for (int j = 0; j < p.getGeneros().size(); j++) {
                String genero = p.getGeneros().get(j);

                MyHash<String, Integer> usuarios = generoUsuario.get(genero);
                if (usuarios == null) {
                    usuarios = new MyHashImpl<>();
                    generoUsuario.put(genero, usuarios);
                }

                Integer total = totalPorGenero.get(genero);
                if (total == null) total = 0;
                totalPorGenero.put(genero, total + 1);

                Integer cant = usuarios.get(c.getUserId());
                if (cant == null) cant = 0;
                usuarios.put(c.getUserId(), cant + 1);
            }
        }

        MyHeap<ElementoHeap<String>> heapGeneros = new MyHeapImpl<>();
        MyList<String> generos = totalPorGenero.keys();
        for (int i = 0; i < generos.size(); i++) {
            String genero = generos.get(i);
            heapGeneros.insert(new ElementoHeap<>(genero, totalPorGenero.get(genero)));
        }

        int topGen = 0;
        while (topGen < 10 && heapGeneros.size() > 0) {
            ElementoHeap<String> g = heapGeneros.delete();
            String genero = g.getValor();
            MyHash<String, Integer> usuarios = generoUsuario.get(genero);
            MyList<String> ids = usuarios.keys();

            String mejor = null;
            int max = -1;
            for (int j = 0; j < ids.size(); j++) {
                String user = ids.get(j);
                int cant = usuarios.get(user);
                if (cant > max) {
                    max = cant;
                    mejor = user;
                }
            }
            System.out.println(genero + "," + mejor + "," + max);
            topGen++;
        }
    }

    private boolean contiene(MyList<String> lista, String valor) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).equals(valor)) return true;
        }
        return false;
    }
}

