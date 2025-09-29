/**
 *
 * @author Jorge Emilio
 */

package com.cliente;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Cliente2025 {

    private static final int ELEMENTOS_POR_PAGINA = 10; // Constante para definir cuántos elementos se muestran por página

    /**
     * Punto de entrada principal para la aplicación cliente.
     * Establece una conexión con el servidor y maneja la interacción del usuario,
     * incluyendo inicio de sesión, registro, envío/recepción de mensajes y gestión de cuenta.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        try (Socket socketCliente = new Socket("localhost", 8080);
             PrintWriter escritor = new PrintWriter(socketCliente.getOutputStream(), true);
             BufferedReader lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            boolean sesionIniciada = false;
            String usuarioActual = "";

            while (true) {
                if (!sesionIniciada) {
                    System.out.println("-------------------------------------------------");
                    System.out.println("Bienvenido al Bulletin Board, escriba una opción:");
                    System.out.println("1. Iniciar sesión");
                    System.out.println("2. Registrar nuevo usuario");
                    System.out.println("3. Salir");
                    System.out.println("-------------------------------------------------");
                    String opcionMenu = teclado.readLine();

                    switch (opcionMenu) {
                        case "1":
                            // Opción para iniciar sesión
                            escritor.println("Inicio");
                            String respuestaInicio = lector.readLine();
                            if (respuestaInicio == null) break;
                            if (respuestaInicio.equalsIgnoreCase("FIN")) {
                                System.out.println("Conexión cerrada por el servidor.");
                                break;
                            }
                            System.out.println(respuestaInicio);

                            String nombreUsuario = teclado.readLine();
                            escritor.println(nombreUsuario);

                            System.out.println(lector.readLine());
                            String contrasena = teclado.readLine();
                            escritor.println(contrasena);

                            String resultadoInicioSesion = lector.readLine();
                            System.out.println(resultadoInicioSesion);

                            if (resultadoInicioSesion != null && resultadoInicioSesion.startsWith("Inicio de sesión exitoso")) {
                                sesionIniciada = true;
                                usuarioActual = nombreUsuario;
                            }
                            break;

                        case "2":
                            // Opción para registrar un nuevo usuario
                            escritor.println("Registrar");
                            String respuestaRegistro = lector.readLine();
                            if (respuestaRegistro == null) break;
                            if (respuestaRegistro.equalsIgnoreCase("FIN")) {
                                System.out.println("Conexión cerrada por el servidor.");
                                break;
                            }
                            System.out.println(respuestaRegistro);

                            String nuevoNombreUsuario = teclado.readLine();
                            escritor.println(nuevoNombreUsuario);

                            System.out.println(lector.readLine());
                            String nuevaContrasena = teclado.readLine();
                            escritor.println(nuevaContrasena);

                            System.out.println(lector.readLine());
                            break;

                        case "3":
                            // Opción para salir de la aplicación
                            escritor.println("Salir");
                            String respuestaSalir = lector.readLine();
                            if (respuestaSalir != null && respuestaSalir.equalsIgnoreCase("FIN")) {
                                System.out.println("Conexión cerrada por el servidor.");
                            }
                            return;

                        default:
                            System.out.println("Opción no válida. Intente de nuevo.");
                            break;
                    }
                } else {
                    // Menú de navegación tras iniciar sesión
                    System.out.println("------ Menú de usuario " + usuarioActual + " ------");
                    System.out.println("1. Ver todos los usuarios registrados");
                    System.out.println("2. Ver tu buzón de mensajes");
                    System.out.println("3. Enviar un mensaje a un usuario");
                    System.out.println("4. Cerrar sesión");
                    System.out.println("5. Borrar buzón de mensajes");
                    System.out.println("6. Borrar un mensaje específico");
                    System.out.println("7. Darse de baja (eliminar cuenta)");
                    System.out.println("--------------------------------------------------");
                    System.out.print("Seleccione una opción: ");
                    String opcionUsuario = teclado.readLine();

                    switch (opcionUsuario) {
                        case "1":
                            // Opción para ver todos los usuarios registrados con paginación
                            escritor.println("VerUsuarios");
                            List<String> listaUsuarios = new ArrayList<>();
                            String lineaUsuario;
                            while ((lineaUsuario = lector.readLine()) != null) {
                                if (lineaUsuario.equals("FIN_USUARIOS")) break;
                                listaUsuarios.add(lineaUsuario);
                            }
                            mostrarYNavegarPaginas(listaUsuarios, teclado, false); // No es seleccionable
                            break;
                        case "2":
                            // Opción para ver el buzón de mensajes del usuario actual con paginación
                            escritor.println("VerBuzon");
                            escritor.println(usuarioActual);
                            List<String> listaMensajesBuzon = new ArrayList<>();
                            String mensajeBuzon;
                            while ((mensajeBuzon = lector.readLine()) != null) {
                                if (mensajeBuzon.equals("FIN_BUZON")) break;
                                listaMensajesBuzon.add(mensajeBuzon);
                            }
                            mostrarYNavegarPaginas(listaMensajesBuzon, teclado, false); // No es seleccionable
                            guardarBuzonLocal(usuarioActual, listaMensajesBuzon); // Guardar copia local del buzón
                            break;
                        case "3":
                            // Opción para enviar un mensaje a otro usuario
                            escritor.println("EnviarMensaje");
                            escritor.println(usuarioActual); // Remitente
                            System.out.print("Ingrese el nombre del destinatario: ");
                            String destinatario = teclado.readLine();
                            escritor.println(destinatario); // Destinatario
                            System.out.print("Escriba el mensaje: ");
                            String mensajeAEnviar = teclado.readLine();
                            escritor.println(mensajeAEnviar); // Contenido del mensaje
                            System.out.println(lector.readLine());
                            guardarMensajeEnviadoLocal(usuarioActual, destinatario, mensajeAEnviar); // Guardar copia local del mensaje enviado
                            break;
                        case "4":
                            // Opción para cerrar la sesión del usuario actual
                            sesionIniciada = false;
                            usuarioActual = "";
                            System.out.println("Sesión cerrada. Regresando al menú principal.");
                            break;
                        case "5":
                            // Opción para borrar el buzón de mensajes del usuario actual
                            escritor.println("BorrarBuzon");
                            escritor.println(usuarioActual);
                            System.out.println(lector.readLine());
                            // Opcional: Borrar el archivo de buzón local también
                            borrarArchivoLocal("buzon_local_" + usuarioActual + ".txt");
                            break;
                        case "6":
                            // Opción para borrar un mensaje específico enviado por el usuario actual con paginación
                            System.out.print("Ingrese el nombre del usuario al que enviaste el mensaje a borrar: ");
                            String usuarioObjetivoBorrar = teclado.readLine();

                            escritor.println("BorrarMensaje");
                            escritor.println(usuarioActual);
                            escritor.println(usuarioObjetivoBorrar);

                            List<String> mensajesParaBorrar = new ArrayList<>();
                            String respuestaServidorMensaje;
                            boolean errorOcurrio = false;
                            while ((respuestaServidorMensaje = lector.readLine()) != null) {
                                if (respuestaServidorMensaje.equals("FIN_MENSAJES")) {
                                    break;
                                }
                                if (respuestaServidorMensaje.startsWith("Error:") || respuestaServidorMensaje.startsWith("No tienes")) {
                                    System.out.println(respuestaServidorMensaje);
                                    errorOcurrio = true;
                                    break; // Salir del bucle si hay un error
                                }
                                // Solo añadir las líneas de mensaje numeradas a la lista para paginar
                                if (respuestaServidorMensaje.matches("^\\d+\\..*")) { // Coincide con el formato "N. mensaje"
                                    mensajesParaBorrar.add(respuestaServidorMensaje);
                                } else if (!respuestaServidorMensaje.equals("0. Salir")) { // Imprimir otros mensajes del servidor como encabezados, pero no "0. Salir"
                                    System.out.println(respuestaServidorMensaje);
                                }
                            }

                            if (!errorOcurrio && !mensajesParaBorrar.isEmpty()) {
                                String seleccionMensaje = mostrarYNavegarPaginas(mensajesParaBorrar, teclado, true); // Es seleccionable
                                if (seleccionMensaje != null && !seleccionMensaje.equals("0")) { // Si se seleccionó un mensaje
                                    escritor.println(seleccionMensaje); // Enviar el número seleccionado al servidor
                                    String resultadoOperacion = lector.readLine();
                                    System.out.println(resultadoOperacion);
                                    // Opcional: Actualizar el archivo local de mensajes enviados si se borró uno
                                    // Esto requeriría leer el archivo local, borrar la línea y reescribirlo.
                                    // Por simplicidad, no se implementa aquí, ya que el servidor es la fuente de verdad.
                                } else {
                                    escritor.println("0"); // El usuario eligió salir sin seleccionar
                                    String resultadoOperacion = lector.readLine(); // El servidor enviará "Regresando al menú principal..."
                                    System.out.println(resultadoOperacion);
                                }
                            } else if (!errorOcurrio && mensajesParaBorrar.isEmpty()) {
                                // Si no hay mensajes para borrar y no hubo error, el servidor ya lo informó.
                                // No se necesita hacer nada más aquí.
                            }
                            break;
                        case "7":
                            // Opción para darse de baja (eliminar la cuenta del usuario actual)
                            System.out.print("¿Estás seguro de que deseas darte de baja? Esto borrará tu cuenta y tu buzón. (S/N): ");
                            String confirmacion = teclado.readLine();
                            if (confirmacion.equalsIgnoreCase("S")) {
                                escritor.println("BorrarUsuario");
                                escritor.println(usuarioActual);
                                String respuestaBorrado = lector.readLine();
                                System.out.println(respuestaBorrado);
                                if (respuestaBorrado.contains("exitosa")) {
                                    sesionIniciada = false;
                                    // Opcional: Borrar los archivos locales del usuario
                                    borrarArchivoLocal("buzon_local_" + usuarioActual + ".txt");
                                    borrarArchivoLocal("mensajes_enviados_local_" + usuarioActual + ".txt");
                                    usuarioActual = "";
                                }
                            } else {
                                System.out.println("Operación cancelada.");
                            }
                            break;
                        default:
                            System.out.println("Opción no válida. Intente de nuevo.");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }

    /**
     * Muestra una lista de elementos con paginación y permite la navegación.
     * Si la lista representa mensajes numerados para borrar, permite seleccionar uno.
     *
     * @param elementos La lista de cadenas a mostrar.
     * @param teclado El BufferedReader para leer la entrada del usuario.
     * @param esSeleccionable Indica si los elementos son seleccionables (para borrar).
     * @return La opción seleccionada (número de mensaje como String) si `esSeleccionable` es true y se selecciona un mensaje,
     *         "0" si se elige salir de una lista seleccionable, o `null` si se navega o se sale de una lista no seleccionable.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private static String mostrarYNavegarPaginas(List<String> elementos, BufferedReader teclado, boolean esSeleccionable) throws IOException {
        int paginaActual = 0;
        String seleccionUsuario = null;

        if (elementos.isEmpty()) {
            System.out.println("No hay elementos para mostrar.");
            return null;
        }

        while (true) {
            int inicio = paginaActual * ELEMENTOS_POR_PAGINA;
            int fin = Math.min(inicio + ELEMENTOS_POR_PAGINA, elementos.size());

            System.out.println("\n--- Página " + (paginaActual + 1) + " de " + (int) Math.ceil((double) elementos.size() / ELEMENTOS_POR_PAGINA) + " ---");
            for (int i = inicio; i < fin; i++) {
                System.out.println(elementos.get(i));
            }
            System.out.println("--------------------------------------------------");

            System.out.print("(A) Anterior, (S) Siguiente, (X) Salir");
            if (esSeleccionable) {
                System.out.print(", (Número) para borrar mensaje: ");
            } else {
                System.out.print(": ");
            }

            String opcion = teclado.readLine().trim().toLowerCase();

            if (opcion.equals("s")) {
                if (fin < elementos.size()) {
                    paginaActual++;
                } else {
                    System.out.println("Ya estás en la última página.");
                }
            } else if (opcion.equals("a")) {
                if (paginaActual > 0) {
                    paginaActual--;
                } else {
                    System.out.println("Ya estás en la primera página.");
                }
            } else if (opcion.equals("x")) {
                if (esSeleccionable) {
                    seleccionUsuario = "0"; // Indicar salida para el contexto de borrado
                }
                break;
            } else if (esSeleccionable) {
                try {
                    int numeroMensaje = Integer.parseInt(opcion);
                    // El número de mensaje que el cliente envía al servidor es el número tal cual lo ve en la lista completa (1-basado)
                    if (numeroMensaje > 0 && numeroMensaje <= elementos.size()) {
                        seleccionUsuario = opcion; // Devolver el número seleccionado como String
                        break;
                    } else {
                        System.out.println("Número de mensaje inválido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opción no válida. Ingrese 'A', 'S', 'X' o el número de un mensaje.");
                }
            } else {
                System.out.println("Opción no válida. Ingrese 'A', 'S' o 'X'.");
            }
        }
        return seleccionUsuario;
    }

    /**
     * Guarda una copia local del buzón de mensajes de un usuario.
     * El archivo se guarda como "buzon_local_[usuario].txt" en el directorio de ejecución del cliente.
     *
     * @param usuario El nombre de usuario.
     * @param mensajes La lista de mensajes a guardar.
     */
    private static void guardarBuzonLocal(String usuario, List<String> mensajes) {
        String nombreArchivo = "buzon_local_" + usuario + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {
            if (mensajes.isEmpty() || (mensajes.size() == 1 && mensajes.get(0).equals("No tienes mensajes en tu buzón."))) {
                pw.println("No tienes mensajes en tu buzón.");
            } else {
                for (String mensaje : mensajes) {
                    pw.println(mensaje);
                }
            }
            System.out.println("Copia local del buzón guardada en: " + nombreArchivo);
        } catch (IOException e) {
            System.out.println("Error al guardar el buzón local: " + e.getMessage());
        }
    }

    /**
     * Guarda una copia local de un mensaje enviado por un usuario.
     * El mensaje se añade al archivo "mensajes_enviados_local_[remitente].txt" en el directorio de ejecución del cliente.
     *
     * @param remitente El nombre de usuario que envía el mensaje.
     * @param destinatario El nombre de usuario del destinatario.
     * @param mensaje El contenido del mensaje enviado.
     */
    private static void guardarMensajeEnviadoLocal(String remitente, String destinatario, String mensaje) {
        String nombreArchivo = "mensajes_enviados_local_" + remitente + ".txt";
        try (FileWriter fw = new FileWriter(nombreArchivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("Para [" + destinatario + "]: " + mensaje);
            bw.newLine();
            System.out.println("Copia local del mensaje enviado guardada en: " + nombreArchivo);
        } catch (IOException e) {
            System.out.println("Error al guardar el mensaje enviado localmente: " + e.getMessage());
        }
    }

    /**
     * Borra un archivo local específico.
     *
     * @param nombreArchivo El nombre del archivo a borrar.
     */
    private static void borrarArchivoLocal(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (archivo.exists()) {
            if (archivo.delete()) {
                System.out.println("Archivo local borrado: " + nombreArchivo);
            } else {
                System.out.println("Error al borrar el archivo local: " + nombreArchivo);
            }
        }
    }
}

