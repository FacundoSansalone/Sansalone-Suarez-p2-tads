package uy.edu.um;

import uy.edu.um.datos.BaseDeDatos;
import uy.edu.um.consultas.Consultas;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BaseDeDatos base = new BaseDeDatos();
        Consultas consultas = null;

        int opcionPrincipal = -1;

        while (opcionPrincipal != 3) {
            System.out.println("Menú principal:");
            System.out.println("1 - Carga de datos");
            System.out.println("2 - Ejecutar consultas");
            System.out.println("3 - Salir");

            try {
                opcionPrincipal = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese una opción válida");
                continue;
            }

            switch (opcionPrincipal) {
                case 1:
                    System.out.println("Cargando datos...");
                    long tiempoInicio = System.currentTimeMillis();
                    long memoriaInicio = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    base.cargarDatos();

                    long tiempoFin = System.currentTimeMillis();
                    long memoriaFin = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    System.out.println("Carga de datos exitosa.");
                    System.out.println("Tiempo de ejecución de la carga: " + (tiempoFin - tiempoInicio) + " ms");
                    System.out.println("Memoria usada: " + (memoriaFin - memoriaInicio) / (1024 * 1024) + " MB");

                    consultas = new Consultas(base);
                    break;

                case 2:
                    if (consultas == null) {
                        System.out.println("Debe cargar los datos antes de ejecutar consultas.");
                        break;
                    }

                    int opcionConsulta = -1;
                    while (opcionConsulta != 7) {
                        System.out.println("Seleccione la consulta que desea ejecutar:");
                        System.out.println("1 - Top 5 de las películas que más calificaciones por idioma");
                        System.out.println("2 - Top 10 de las películas que mejor calificación media tienen");
                        System.out.println("3 - Top 5 de las colecciones que más ingresos generaron");
                        System.out.println("4 - Top 10 de los directores con mejor calificación (mediana)");
                        System.out.println("5 - Actor con más calificaciones recibidas por mes");
                        System.out.println("6 - Usuarios con más calificaciones por género");
                        System.out.println("7 - Volver al menú principal");

                        try {
                            opcionConsulta = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Ingrese una opción válida");
                            continue;
                        }

                        switch (opcionConsulta) {
                            case 1:
                                consultas.top5PeliculasConMasEvaluacionesPorIdioma();
                                break;
                            case 2:
                                consultas.top10PeliculasMejorPromedioMas100Calificaciones();
                                break;
                            case 3:
                                consultas.top5ColeccionesIngresos();
                                break;
                            case 4:
                                consultas.top10DirectoresMediana();
                                break;
                            case 5:
                                consultas.actorMasCalificadoPorMes();
                                break;
                            case 6:
                                consultas.usuariosMasActivosPorGenero();
                                break;
                            case 7:
                                System.out.println("Volviendo al menú principal...");
                                break;
                            default:
                                System.out.println("Opción inválida");
                        }

                        if (opcionConsulta >= 1 && opcionConsulta <= 6) {
                            System.out.println("\nResultado mostrado. Presione ENTER para volver al menú de consultas.");
                            sc.nextLine();
                        }
                    }
                    break;

                case 3:
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Opción inválida");
            }
        }

        sc.close();
    }
}
