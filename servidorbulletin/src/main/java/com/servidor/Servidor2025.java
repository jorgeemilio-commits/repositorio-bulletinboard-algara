package com.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor2025 {

    private static final String ARCHIVO_USUARIOS = "usuarios.txt";
    private static final String DIRECTORIO_COMPARTIDOS_SERVIDOR = "servidor_compartidos";

    // Mapa para almacenar qué archivos ha compartido cada usuario
    // Key: nombre de usuario, Value: Lista de nombres de archivos que ha compartido
    private static Map<String, List<String>> archivosCompartidosPorUsuario = new ConcurrentHashMap<>();

    /**
     * Punto de entrada principal del servidor.
     * Inicializa el ServerSocket y espera conexiones de clientes, manejando cada una en un nuevo hilo.
     */
    public static void main(String[] args) {
        // Asegurarse de que el directorio de archivos compartidos exista
        Path rutaDirectorioCompartidos = Paths.get(DIRECTORIO_COMPARTIDOS_SERVIDOR);
        if (!Files.exists(rutaDirectorioCompartidos)) {
            try {
                Files.createDirectories(rutaDirectorioCompartidos);
                System.out.println("Directorio de archivos compartidos creado: " + DIRECTORIO_COMPARTIDOS_SERVIDOR);
            } catch (IOException e) {
                System.out.println("Error al crear el directorio de archivos compartidos: " + e.getMessage());
                return; // No se puede iniciar el servidor sin este directorio
            }
        } else {
            System.out.println("Directorio de archivos compartidos existente: " + DIRECTORIO_COMPARTIDOS_SERVIDOR);
        }

        // Cargar el estado de los archivos compartidos al iniciar el servidor
        cargarArchivosCompartidos();

        try (ServerSocket socketServidor = new ServerSocket(8080)) {
            System.out.println("Servidor de Bulletin Board iniciado en puerto 8080...");

            while (true) {
                Socket cliente = socketServidor.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress());
                new Thread(() -> manejarCliente(cliente)).start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Maneja la comunicación con un cliente individual.
     * Lee las opciones enviadas por el cliente y ejecuta la lógica correspondiente.
     */
    private static void manejarCliente(Socket cliente) {
        try (
            PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true);
            BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {
            String opcionCliente;
            while ((opcionCliente = lectorSocket.readLine()) != null) {
                switch (opcionCliente.toLowerCase()) {
                    case "inicio":
                        escritor.println("Ingrese su nombre de usuario:");
                        String nombreUsuarioInicio = lectorSocket.readLine();
                        escritor.println("Ingrese su contraseña:");
                        String contrasenaInicio = lectorSocket.readLine();

                        if (validarUsuario(nombreUsuarioInicio, contrasenaInicio)) {
                            escritor.println("Inicio de sesión exitoso. Bienvenido " + nombreUsuarioInicio + "!");
                        } else {
                            escritor.println("Usuario o contraseña incorrectos.");
                        }
                        break;

                    case "registrar":
                        escritor.println("Ingrese un nombre de usuario para registrar:");
                        String nuevoNombreUsuario = lectorSocket.readLine();

                        if (existeUsuario(nuevoNombreUsuario)) {
                            escritor.println("Error: El usuario '" + nuevoNombreUsuario + "' ya existe. Intente con otro nombre.");
                        } else {
                            escritor.println("Ingrese una contraseña:");
                            String nuevaContrasena = lectorSocket.readLine();

                            int nuevoId = obtenerSiguienteId();
                            registrarUsuario(nuevoId, nuevoNombreUsuario, nuevaContrasena);

                            escritor.println("Usuario " + nuevoNombreUsuario + " registrado exitosamente con ID: " + nuevoId);
                        }
                        break;

                    case "verusuarios":
                        List<String> usuariosRegistrados = leerArchivoUsuarios();
                        if (usuariosRegistrados.isEmpty()) {
                            escritor.println("No hay usuarios registrados.");
                        } else {
                            escritor.println("Usuarios registrados:");
                            for (String linea : usuariosRegistrados) {
                                String[] partes = linea.split("\\|");
                                if (partes.length >= 2) {
                                    escritor.println("- " + partes[1]);
                                }
                            }
                            escritor.println("FIN_USUARIOS");
                        }
                        break;

                    case "enviarmensaje":
                        String remitenteMensaje = lectorSocket.readLine();
                        String destinatarioMensaje = lectorSocket.readLine();
                        String contenidoMensaje = lectorSocket.readLine();

                        if (!existeUsuario(destinatarioMensaje)) {
                            escritor.println("Error: El usuario '" + destinatarioMensaje + "' no existe.");
                        } else {
                            guardarMensaje(remitenteMensaje, destinatarioMensaje, contenidoMensaje);
                            escritor.println("Mensaje enviado a " + destinatarioMensaje);
                        }
                        break;

                    case "verbuzon":
                        String usuarioBuzon = lectorSocket.readLine();
                        enviarBuzon(usuarioBuzon, escritor);
                        break;

                    case "borrarbuzon":
                        String usuarioBorrarBuzon = lectorSocket.readLine();
                        borrarBuzon(usuarioBorrarBuzon, escritor);
                        break;

                    case "borrarusuario":
                        String usuarioABorrar = lectorSocket.readLine();
                        borrarUsuario(usuarioABorrar, escritor);
                        break;

                    case "borrarmensaje":
                        String usuarioActual = lectorSocket.readLine();
                        String usuarioObjetivo = lectorSocket.readLine();
                        borrarMensaje(usuarioActual, usuarioObjetivo, lectorSocket, escritor);
                        break;

                    case "compartirdocumentos":
                        String usuarioRemitenteCompartir = lectorSocket.readLine();
                        String nombreArchivoCompartir = lectorSocket.readLine();
                        // Leer el contenido del archivo línea por línea hasta "FIN_ARCHIVO"
                        StringBuilder contenidoArchivo = new StringBuilder();
                        String lineaContenido;
                        while ((lineaContenido = lectorSocket.readLine()) != null && !lineaContenido.equals("FIN_ARCHIVO")) {
                            contenidoArchivo.append(lineaContenido).append(System.lineSeparator());
                        }
                        guardarArchivoCompartido(usuarioRemitenteCompartir, nombreArchivoCompartir, contenidoArchivo.toString());
                        escritor.println("Documento '" + nombreArchivoCompartir + "' compartido exitosamente.");
                        break;

                    case "verdocumentoscompartidos":
                        String usuarioObjetivoVer = lectorSocket.readLine();
                        List<String> documentosCompartidos = archivosCompartidosPorUsuario.getOrDefault(usuarioObjetivoVer, Collections.emptyList());
                        if (documentosCompartidos.isEmpty()) {
                            escritor.println("El usuario '" + usuarioObjetivoVer + "' no ha compartido documentos.");
                        } else {
                            escritor.println("Documentos compartidos por '" + usuarioObjetivoVer + "':");
                            for (String doc : documentosCompartidos) {
                                escritor.println("- " + doc);
                            }
                        }
                        escritor.println("FIN_DOCUMENTOS_COMPARTIDOS");
                        break;

                    case "descargardocumento":
                        String usuarioOrigenDescarga = lectorSocket.readLine();
                        String nombreArchivoDescarga = lectorSocket.readLine();
                        enviarArchivoCompartido(usuarioOrigenDescarga, nombreArchivoDescarga, escritor);
                        break;

                    case "salir":
                        escritor.println("FIN");
                        return;

                    default:
                        escritor.println("Opción no válida. Intente de nuevo.");
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("Error de comunicación con cliente: " + e.getMessage());
        } finally {
            try {
                cliente.close();
                System.out.println("Cliente desconectado.");
            } catch (IOException e) {
                System.out.println("No se pudo cerrar el socket del cliente.");
            }
        }
    }

    /**
     * Valida las credenciales de un usuario comparándolas con el archivo de usuarios.
     * @param nombreUsuario El nombre de usuario a validar.
     * @param contrasena La contraseña a validar.
     * @return true si las credenciales son válidas, false en caso contrario.
     */
    private static boolean validarUsuario(String nombreUsuario, String contrasena) {
        List<String> usuarios = leerArchivoUsuarios();
        for (String linea : usuarios) {
            String[] partes = linea.split("\\|");
            if (partes.length == 3) {
                String nombreLeido = partes[1];
                String contrasenaLeida = partes[2];
                if (nombreLeido.equalsIgnoreCase(nombreUsuario) && contrasenaLeida.equals(contrasena)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un usuario ya existe en el archivo de usuarios.
     * @param nombreUsuario El nombre de usuario a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    private static boolean existeUsuario(String nombreUsuario) {
        List<String> usuarios = leerArchivoUsuarios();
        for (String linea : usuarios) {
            String[] partes = linea.split("\\|");
            if (partes.length >= 2 && partes[1].equalsIgnoreCase(nombreUsuario)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registra un nuevo usuario en el archivo de usuarios.
     * @param id El ID único para el nuevo usuario.
     * @param nombre El nombre del nuevo usuario.
     * @param contrasena La contraseña del nuevo usuario.
     */
    private static void registrarUsuario(int id, String nombre, String contrasena) {
        try (FileWriter fw = new FileWriter(ARCHIVO_USUARIOS, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(id + "|" + nombre + "|" + contrasena);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    /**
     * Obtiene el siguiente ID disponible para un nuevo usuario.
     * Busca el ID más alto existente y devuelve el siguiente número.
     * @return El siguiente ID disponible.
     */
    private static int obtenerSiguienteId() {
        List<String> usuarios = leerArchivoUsuarios();
        int maxId = 0;
        for (String linea : usuarios) {
            String[] partes = linea.split("\\|");
            if (partes.length > 0) {
                try {
                    int id = Integer.parseInt(partes[0]);
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException ignored) {}
            }
        }
        return maxId + 1;
    }

    /**
     * Lee todas las líneas del archivo de usuarios.
     * Si el archivo no existe, lo crea.
     * @return Una lista de cadenas, donde cada cadena es una línea del archivo de usuarios.
     */
    private static List<String> leerArchivoUsuarios() {
        List<String> usuarios = new ArrayList<>();
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
            } catch (IOException e) {
                System.out.println("No se pudo crear el archivo de usuarios.");
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                usuarios.add(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de usuarios.");
        }
        return usuarios;
    }

    /**
     * Guarda un mensaje en el buzón del destinatario.
     * Cada mensaje se guarda en un archivo llamado "buzon_[destinatario].txt".
     * @param remitente El nombre de usuario del remitente.
     * @param destinatario El nombre de usuario del destinatario.
     * @param contenidoMensaje El contenido del mensaje.
     */
    private static void guardarMensaje(String remitente, String destinatario, String contenidoMensaje) {
        File archivo = new File("buzon_" + destinatario + ".txt");
        try (FileWriter fw = new FileWriter(archivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("[" + remitente + "]: " + contenidoMensaje);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar mensaje: " + e.getMessage());
        }
    }

    /**
     * Envía el contenido del buzón de un usuario al cliente.
     * @param usuario El nombre de usuario cuyo buzón se va a enviar.
     * @param escritor El PrintWriter para enviar los mensajes al cliente.
     */
    private static void enviarBuzon(String usuario, PrintWriter escritor) {
        File archivo = new File("buzon_" + usuario + ".txt");
        if (!archivo.exists()) {
            escritor.println("No tienes mensajes en tu buzón.");
            escritor.println("FIN_BUZON");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean hayMensajes = false;
            while ((linea = br.readLine()) != null) {
                escritor.println(linea);
                hayMensajes = true;
            }
            if (!hayMensajes) {
                escritor.println("No tienes mensajes en tu buzón.");
            }
        } catch (IOException e) {
            escritor.println("Error al leer tu buzón.");
        }
        escritor.println("FIN_BUZON");
    }

    /**
     * Borra el archivo de buzón de un usuario.
     * @param usuario El nombre de usuario cuyo buzón se va a borrar.
     * @param escritor El PrintWriter para enviar la confirmación al cliente.
     */
    private static void borrarBuzon(String usuario, PrintWriter escritor) {
        File archivo = new File("buzon_" + usuario + ".txt");
        if (!archivo.exists()) {
            escritor.println("No tienes buzón para borrar.");
            return;
        }

        if (archivo.delete()) {
            escritor.println("Buzón borrado exitosamente.");
        } else {
            escritor.println("Error al borrar el buzón.");
        }
    }

    /**
     * Permite a un usuario borrar un mensaje específico que envió a otro usuario.
     * @param usuarioActual El nombre de usuario que está realizando la acción.
     * @param usuarioObjetivo El nombre de usuario cuyo buzón contiene el mensaje a borrar.
     * @param lector El BufferedReader para leer la selección del cliente.
     * @param escritor El PrintWriter para enviar mensajes al cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private static void borrarMensaje(String usuarioActual, String usuarioObjetivo, BufferedReader lector, PrintWriter escritor) throws IOException {
        File archivoBuzon = new File("buzon_" + usuarioObjetivo + ".txt");
        if (!archivoBuzon.exists()) {
            escritor.println("El usuario " + usuarioObjetivo + " no tiene buzón.");
            return;
        }

        List<String> listaMensajes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoBuzon))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                listaMensajes.add(linea);
            }
        }

        List<Integer> indicesMensajesRemitente = new ArrayList<>();
        for (int i = 0; i < listaMensajes.size(); i++) {
            String mensaje = listaMensajes.get(i);
            if (mensaje.startsWith("[" + usuarioActual + "]:")) {
                indicesMensajesRemitente.add(i);
            }
        }

        if (indicesMensajesRemitente.isEmpty()) {
            escritor.println("No tienes mensajes enviados a " + usuarioObjetivo);
            return;
        }

        escritor.println("=== Mensajes enviados a " + usuarioObjetivo + " ===");
        for (int i = 0; i < indicesMensajesRemitente.size(); i++) {
            escritor.println((i + 1) + ". " + listaMensajes.get(indicesMensajesRemitente.get(i)));
        }
        escritor.println("0. Salir");
        escritor.println("FIN_MENSAJES");

        String opcionSeleccionada = lector.readLine();
        if (opcionSeleccionada == null || opcionSeleccionada.equals("0")) {
            escritor.println("Regresando al menú principal...");
        } else {
            try {
                int numeroMensaje = Integer.parseInt(opcionSeleccionada);
                if (numeroMensaje >= 1 && numeroMensaje <= indicesMensajesRemitente.size()) {
                    int indiceABorrar = indicesMensajesRemitente.get(numeroMensaje - 1);
                    listaMensajes.remove(indiceABorrar);

                    try (PrintWriter escritorArchivo = new PrintWriter(new FileWriter(archivoBuzon))) {
                        for (String msg : listaMensajes) escritorArchivo.println(msg);
                    }
                    escritor.println("Mensaje borrado con éxito.");
                } else {
                    escritor.println("Número inválido.");
                }
            } catch (NumberFormatException e) {
                escritor.println("Entrada inválida, escribe un número.");
            }
        }
    }

    /**
     * Borra un usuario del sistema, incluyendo su entrada en el archivo de usuarios y su buzón.
     * @param usuario El nombre de usuario a borrar.
     * @param escritor El PrintWriter para enviar mensajes al cliente.
     */
    private static void borrarUsuario(String usuario, PrintWriter escritor) {
        File archivoUsuarios = new File(ARCHIVO_USUARIOS);
        File archivoTemporal = new File("temp_usuarios.txt");

        boolean usuarioEncontrado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoUsuarios));
             BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemporal))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 2 && partes[1].equalsIgnoreCase(usuario)) {
                    usuarioEncontrado = true;
                } else {
                    bw.write(linea);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            escritor.println("Error al procesar la solicitud de borrado del usuario.");
            archivoTemporal.delete();
            return;
        }

        if (!usuarioEncontrado) {
            escritor.println("Error: El usuario no fue encontrado.");
            archivoTemporal.delete();
            return;
        }

        if (!archivoUsuarios.delete()) {
            escritor.println("Error al borrar el archivo de usuarios original.");
            archivoTemporal.delete();
            return;
        }
        if (!archivoTemporal.renameTo(archivoUsuarios)) {
            escritor.println("Error al renombrar el archivo temporal.");
            return;
        }

        File archivoBuzon = new File("buzon_" + usuario + ".txt");
        if (archivoBuzon.exists()) {
            if (archivoBuzon.delete()) {
                System.out.println("Buzón de " + usuario + " borrado.");
            } else {
                System.out.println("Error al borrar el buzón de " + usuario + ".");
            }
        }

        // Borrar archivos compartidos por el usuario
        List<String> archivosDelUsuario = archivosCompartidosPorUsuario.remove(usuario);
        if (archivosDelUsuario != null) {
            for (String nombreArchivo : archivosDelUsuario) {
                Path rutaArchivoCompartido = Paths.get(DIRECTORIO_COMPARTIDOS_SERVIDOR, usuario + "_" + nombreArchivo);
                try {
                    Files.deleteIfExists(rutaArchivoCompartido);
                    System.out.println("Archivo compartido de '" + usuario + "' borrado: " + nombreArchivo);
                } catch (IOException e) {
                    System.out.println("Error al borrar archivo compartido de '" + usuario + "': " + nombreArchivo + " - " + e.getMessage());
                }
            }
        }
        escritor.println("Cuenta de usuario y buzón borrados exitosamente.");
    }

    /**
     * Guarda un archivo compartido por un usuario en el directorio del servidor.
     * También actualiza el registro de archivos compartidos.
     * @param usuarioRemitente El nombre de usuario que comparte el archivo.
     * @param nombreArchivo El nombre original del archivo.
     * @param contenido El contenido del archivo.
     */
    private static void guardarArchivoCompartido(String usuarioRemitente, String nombreArchivo, String contenido) {
        // El nombre del archivo en el servidor será "usuario_nombreArchivoOriginal" para evitar colisiones
        Path rutaArchivo = Paths.get(DIRECTORIO_COMPARTIDOS_SERVIDOR, usuarioRemitente + "_" + nombreArchivo);
        try {
            Files.writeString(rutaArchivo, contenido);
            // Asegurarse de que el nombre del archivo sea único en la lista por usuario
            List<String> listaArchivos = archivosCompartidosPorUsuario.computeIfAbsent(usuarioRemitente, k -> new ArrayList<>());
            if (!listaArchivos.contains(nombreArchivo)) {
                listaArchivos.add(nombreArchivo);
            }
            System.out.println("Archivo '" + nombreArchivo + "' compartido por '" + usuarioRemitente + "' guardado/actualizado en el servidor.");
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo compartido '" + nombreArchivo + "': " + e.getMessage());
        }
    }

    /**
     * Envía el contenido de un archivo compartido al cliente que lo solicita.
     * @param usuarioOrigen El nombre de usuario que compartió el archivo.
     * @param nombreArchivo El nombre del archivo a enviar.
     * @param escritor El PrintWriter para enviar el contenido al cliente.
     */
    private static void enviarArchivoCompartido(String usuarioOrigen, String nombreArchivo, PrintWriter escritor) {
        Path rutaArchivo = Paths.get(DIRECTORIO_COMPARTIDOS_SERVIDOR, usuarioOrigen + "_" + nombreArchivo);
        if (Files.exists(rutaArchivo)) {
            try {
                List<String> lineas = Files.readAllLines(rutaArchivo);
                escritor.println("INICIO_ARCHIVO");
                for (String linea : lineas) {
                    escritor.println(linea);
                }
                escritor.println("FIN_ARCHIVO");
                System.out.println("Archivo '" + nombreArchivo + "' enviado a cliente.");
            } catch (IOException e) {
                escritor.println("Error al leer el archivo '" + nombreArchivo + "' en el servidor.");
                escritor.println("FIN_ARCHIVO");
                System.out.println("Error al enviar archivo '" + nombreArchivo + "': " + e.getMessage());
            }
        } else {
            escritor.println("Error: El archivo '" + nombreArchivo + "' no se encontró en el servidor.");
            escritor.println("FIN_ARCHIVO");
            System.out.println("Archivo no encontrado en el servidor: " + rutaArchivo);
        }
    }

    /**
     * Carga el estado de los archivos compartidos desde el directorio del servidor al inicio.
     * Esto reconstruye el mapa `archivosCompartidosPorUsuario`.
     */
    private static void cargarArchivosCompartidos() {
        Path rutaDirectorioCompartidos = Paths.get(DIRECTORIO_COMPARTIDOS_SERVIDOR);
        if (Files.exists(rutaDirectorioCompartidos) && Files.isDirectory(rutaDirectorioCompartidos)) {
            try (var stream = Files.list(rutaDirectorioCompartidos)) {
                stream.forEach(archivo -> {
                    String nombreCompleto = archivo.getFileName().toString();
                    int indiceGuionBajo = nombreCompleto.indexOf('_');
                    if (indiceGuionBajo > 0) {
                        String usuario = nombreCompleto.substring(0, indiceGuionBajo);
                        String nombreOriginalArchivo = nombreCompleto.substring(indiceGuionBajo + 1);
                        // Asegurarse de que el nombre del archivo sea único en la lista por usuario
                        archivosCompartidosPorUsuario.computeIfAbsent(usuario, k -> new ArrayList<>()).add(nombreOriginalArchivo);
                    }
                });
                System.out.println("Archivos compartidos cargados al inicio: " + archivosCompartidosPorUsuario.values().stream().mapToLong(List::size).sum() + " archivos de " + archivosCompartidosPorUsuario.keySet().size() + " usuarios.");
            } catch (IOException e) {
                System.out.println("Error al cargar archivos compartidos al inicio: " + e.getMessage());
            }
        }
    }
}



