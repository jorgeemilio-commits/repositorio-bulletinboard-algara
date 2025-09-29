package com.cliente;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cliente2025 {

    private static final int ELEMENTOS_POR_PAGINA = 10; // Constante para definir cuántos elementos se muestran por página
    // Ajuste de la ruta relativa: asume que el cliente se ejecuta desde el directorio 'clientebulletin'
    // y que 'cliente_archivos_compartidos' está en el directorio padre (repositorio-bulletinboard-algara)
    private static final String DIRECTORIO_CLIENTE_COMPARTIDOS = "../cliente_archivos_compartidos";
    private static final String DIRECTORIO_CLIENTE_DESCARGAS = "../cliente_descargas";

    /**
     * Punto de entrada principal para la aplicación cliente.
     * Establece una conexión con el servidor y maneja la interacción del usuario,
     * incluyendo inicio de sesión, registro, envío/recepción de mensajes y gestión de cuenta.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        // Asegurarse de que los directorios locales existan
        crearDirectorioSiNoExiste(DIRECTORIO_CLIENTE_COMPARTIDOS);
        crearDirectorioSiNoExiste(DIRECTORIO_CLIENTE_DESCARGAS);

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
                                // Al iniciar sesión, el cliente sube sus archivos compartidos
                                subirArchivosCompartidos(usuarioActual, escritor, lector);
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
                    System.out.println("8. Ver/Descargar documentos de otro usuario"); // Nueva opción
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
                            mostrarYNavegarPaginas(listaUsuarios, teclado, false, null, null); // No es seleccionable
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
                            mostrarYNavegarPaginas(listaMensajesBuzon, teclado, false, null, null); // No es seleccionable
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
                            // Borrar el archivo de buzón local también
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
                                // Pasar "borrar" explícitamente para el tipo de acción
                                String seleccionMensaje = mostrarYNavegarPaginas(mensajesParaBorrar, teclado, true, "borrar", null);
                                if (seleccionMensaje != null && !seleccionMensaje.equals("0")) { // Si se seleccionó un mensaje
                                    escritor.println(seleccionMensaje); // Enviar el número seleccionado al servidor
                                    String resultadoOperacion = lector.readLine();
                                    System.out.println(resultadoOperacion);
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
                                    // Borrar los archivos locales del usuario
                                    borrarArchivoLocal("buzon_local_" + usuarioActual + ".txt");
                                    borrarArchivoLocal("mensajes_enviados_local_" + usuarioActual + ".txt");
                                    usuarioActual = "";
                                }
                            } else {
                                System.out.println("Operación cancelada.");
                            }
                            break;
                        case "8":
                            // Nueva opción: Ver/Descargar documentos de otro usuario
                            System.out.print("Ingrese el nombre del usuario del que desea ver los documentos compartidos: ");
                            String usuarioObjetivoCompartidosInput = teclado.readLine();
                            String usuarioObjetivoCompartidos = usuarioObjetivoCompartidosInput.toLowerCase();

                            escritor.println("VerDocumentosCompartidos");
                            escritor.println(usuarioObjetivoCompartidos);

                            List<String> documentosCompartidos = new ArrayList<>();
                            String primeraRespuesta = lector.readLine(); // Leer la primera línea de respuesta del servidor

                            if (primeraRespuesta == null) {
                                System.out.println("Error: No se recibió respuesta del servidor.");
                            } else if (primeraRespuesta.startsWith("El usuario")) {
                                System.out.println(primeraRespuesta); 
                                String lineaConsumo;
                                while ((lineaConsumo = lector.readLine()) != null && !lineaConsumo.equals("FIN_DOCUMENTOS_COMPARTIDOS")) {
                                
                                }
                            } else if (primeraRespuesta.startsWith("Documentos compartidos por")) { // El servidor envió un encabezado de lista
                                System.out.println(primeraRespuesta); // Imprimir el encabezado
                                String lineaDocumento;
                                while ((lineaDocumento = lector.readLine()) != null) {
                                    if (lineaDocumento.equals("FIN_DOCUMENTOS_COMPARTIDOS")) break;
                                    if (lineaDocumento.startsWith("- ")) { // Formato del servidor: "- nombre_archivo.txt"
                                        documentosCompartidos.add(lineaDocumento.substring(2)); // Quitar "- "
                                    }
                                    // Otras líneas se ignoran, asumiendo que son parte del encabezado o inesperadas
                                }

                                // Proceder con la paginación si se encontraron documentos
                                if (!documentosCompartidos.isEmpty()) {
                                    String nombreArchivoSeleccionado = mostrarYNavegarPaginas(documentosCompartidos, teclado, true, "descargar", usuarioObjetivoCompartidos);
                                    if (nombreArchivoSeleccionado != null && !nombreArchivoSeleccionado.equals("0")) {
                                        // El usuario seleccionó un archivo para descargar
                                        escritor.println("DescargarDocumento");
                                        escritor.println(usuarioObjetivoCompartidos); // Usuario que compartió
                                        escritor.println(nombreArchivoSeleccionado); // Nombre del archivo

                                        StringBuilder contenidoDescargado = new StringBuilder();
                                        String lineaArchivo;
                                        boolean inicioArchivo = false;
                                        while ((lineaArchivo = lector.readLine()) != null) {
                                            if (lineaArchivo.equals("INICIO_ARCHIVO")) {
                                                inicioArchivo = true;
                                                continue;
                                            }
                                            if (lineaArchivo.equals("FIN_ARCHIVO")) {
                                                break;
                                            }
                                            if (inicioArchivo) {
                                                contenidoDescargado.append(lineaArchivo).append(System.lineSeparator());
                                            } else {
                                                System.out.println(lineaArchivo); // Mensajes de error del servidor antes de INICIO_ARCHIVO
                                            }
                                        }

                                        if (inicioArchivo && contenidoDescargado.length() > 0) {
                                            guardarArchivoLocal(DIRECTORIO_CLIENTE_DESCARGAS, nombreArchivoSeleccionado, contenidoDescargado.toString());
                                            System.out.println("Archivo '" + nombreArchivoSeleccionado + "' descargado exitosamente en '" + DIRECTORIO_CLIENTE_DESCARGAS + "'.");
                                        } else if (!inicioArchivo) {
                                            System.out.println("No se pudo descargar el archivo o el servidor reportó un error.");
                                        } else {
                                            System.out.println("El archivo '" + nombreArchivoSeleccionado + "' está vacío o no se recibió contenido.");
                                        }
                                    } else {
                                        System.out.println("Regresando al menú principal...");
                                    }
                                } else {
                                    System.out.println("No hay documentos compartidos por '" + usuarioObjetivoCompartidos + "'.");
                                }
                            } else {
                                System.out.println("Respuesta inesperada del servidor al intentar ver documentos compartidos: " + primeraRespuesta);
                                String lineaConsumo;
                                while ((lineaConsumo = lector.readLine()) != null && !lineaConsumo.equals("FIN_DOCUMENTOS_COMPARTIDOS")) {
                                }
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
     * Si el tipo de acción es "descargar", permite seleccionar un archivo para descargar.
     *
     * @param elementos La lista de cadenas a mostrar.
     * @param teclado El BufferedReader para leer la entrada del usuario.
     * @param esSeleccionable Indica si los elementos son seleccionables (para borrar o descargar).
     * @param tipoAccion Puede ser "borrar" o "descargar", o null si no es seleccionable.
     * @param usuarioObjetivo Para el caso de descarga, el usuario que compartió el archivo.
     * @return La opción seleccionada (número de mensaje/documento como String) si `esSeleccionable` es true y se selecciona un elemento,
     *         "0" si se elige salir de una lista seleccionable, o `null` si se navega o se sale de una lista no seleccionable.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private static String mostrarYNavegarPaginas(List<String> elementos, BufferedReader teclado, boolean esSeleccionable, String tipoAccion, String usuarioObjetivo) throws IOException {
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
                // Si es una lista de documentos compartidos, los numeramos para la selección
                if (tipoAccion != null && tipoAccion.equals("descargar")) {
                    System.out.println((i + 1) + ". " + elementos.get(i));
                } else {
                    System.out.println(elementos.get(i));
                }
            }
            System.out.println("--------------------------------------------------");

            System.out.print("(A) Anterior, (S) Siguiente, (X) Salir");
            if (esSeleccionable) {
                if ("borrar".equals(tipoAccion)) {
                    System.out.print(", (Número) para borrar mensaje: ");
                } else if ("descargar".equals(tipoAccion)) {
                    System.out.print(", (Número) para descargar documento: ");
                } else { // Caso genérico seleccionable (ej. borrar mensaje sin tipoAccion específico)
                    System.out.print(", (Número) para seleccionar: ");
                }
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
                    seleccionUsuario = "0"; // Indicar salida para el contexto de selección
                }
                break;
            } else if (esSeleccionable) {
                try {
                    int numeroSeleccionado = Integer.parseInt(opcion);
                    if (numeroSeleccionado >= 1 && numeroSeleccionado <= elementos.size()) {
                        if ("descargar".equals(tipoAccion)) {
                            seleccionUsuario = elementos.get(numeroSeleccionado - 1); // Devolver el nombre del archivo
                        } else {
                            seleccionUsuario = opcion; // Devolver el número seleccionado como String (para borrar mensajes)
                        }
                        break;
                    } else {
                        System.out.println("Número inválido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opción no válida. Ingrese 'A', 'S', 'X' o el número de un elemento.");
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
        StringBuilder contenido = new StringBuilder();
        if (mensajes.isEmpty() || (mensajes.size() == 1 && mensajes.get(0).equals("No tienes mensajes en tu buzón."))) {
            contenido.append("No tienes mensajes en tu buzón.");
        } else {
            for (String mensaje : mensajes) {
                contenido.append(mensaje).append(System.lineSeparator());
            }
        }
        // Usar el método existente para guardar en el directorio de archivos compartidos
        guardarArchivoLocal(DIRECTORIO_CLIENTE_COMPARTIDOS, nombreArchivo, contenido.toString());
        System.out.println("Copia local del buzón guardada en: " + DIRECTORIO_CLIENTE_COMPARTIDOS + File.separator + nombreArchivo);
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

    /**
     * Sube automáticamente los archivos .txt del directorio de compartidos del cliente al servidor.
     *
     * @param usuarioActual El nombre de usuario actual.
     * @param escritor El PrintWriter para enviar datos al servidor.
     * @param lector El BufferedReader para leer respuestas del servidor.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private static void subirArchivosCompartidos(String usuarioActual, PrintWriter escritor, BufferedReader lector) throws IOException {
        Path rutaDirectorioCompartidos = Paths.get(DIRECTORIO_CLIENTE_COMPARTIDOS);
        if (!Files.exists(rutaDirectorioCompartidos) || !Files.isDirectory(rutaDirectorioCompartidos)) {
            // Corregido el error tipográfico aquí
            System.out.println("El directorio de archivos compartidos local '" + DIRECTORIO_CLIENTE_COMPARTIDOS + "' no existe o no es un directorio.");
            return;
        }

        try (Stream<Path> stream = Files.list(rutaDirectorioCompartidos)) {
            List<Path> archivosTxt = stream
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                .collect(Collectors.toList());

            if (archivosTxt.isEmpty()) {
                System.out.println("No hay archivos .txt para compartir en '" + DIRECTORIO_CLIENTE_COMPARTIDOS + "'.");
                return;
            }

            System.out.println("Subiendo archivos compartidos desde '" + DIRECTORIO_CLIENTE_COMPARTIDOS + "'...");
            for (Path archivo : archivosTxt) {
                String nombreArchivo = archivo.getFileName().toString();

                escritor.println("CompartirDocumentos");
                escritor.println(usuarioActual);
                escritor.println(nombreArchivo);

                // Leer el archivo línea por línea y enviar cada línea individualmente
                try (BufferedReader fileReader = new BufferedReader(new FileReader(archivo.toFile()))) {
                    String fileLine;
                    while ((fileLine = fileReader.readLine()) != null) {
                        escritor.println(fileLine);
                    }
                }
                escritor.println("FIN_ARCHIVO"); // Marcador de fin de archivo

                String respuestaServidor = lector.readLine();
                System.out.println("Servidor: " + respuestaServidor);
            }
            System.out.println("Proceso de subida de archivos compartidos finalizado.");

        } catch (IOException e) {
            System.out.println("Error al subir archivos compartidos: " + e.getMessage());
        }
    }

    /**
     * Crea un directorio si no existe.
     * @param nombreDirectorio El nombre del directorio a crear.
     */
    private static void crearDirectorioSiNoExiste(String nombreDirectorio) {
        Path ruta = Paths.get(nombreDirectorio);
        if (!Files.exists(ruta)) {
            try {
                Files.createDirectories(ruta);
                System.out.println("Directorio creado: " + nombreDirectorio);
            } catch (IOException e) {
                System.out.println("Error al crear el directorio '" + nombreDirectorio + "': " + e.getMessage());
            }
        }
    }

    /**
     * Guarda contenido en un archivo local dentro de un directorio específico.
     * @param directorio El directorio donde se guardará el archivo.
     * @param nombreArchivo El nombre del archivo.
     * @param contenido El contenido a guardar.
     */
    private static void guardarArchivoLocal(String directorio, String nombreArchivo, String contenido) {
        Path rutaArchivo = Paths.get(directorio, nombreArchivo);
        try {
            Files.writeString(rutaArchivo, contenido);
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo local '" + nombreArchivo + "': " + e.getMessage());
        }
    }
}