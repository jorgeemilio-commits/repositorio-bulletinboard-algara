package com.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor2025 {

    private static final String ARCHIVO_USUARIOS = "usuarios.txt";

    public static void main(String[] args) {
        try (ServerSocket socketEspecial = new ServerSocket(8080)) {
            System.out.println("Servidor de Bulletin Board iniciado en puerto 8080...");

            while (true) {
                Socket cliente = socketEspecial.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress());
                new Thread(() -> manejarCliente(cliente)).start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket cliente) {
        try (
            PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true);
            BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {
            String opcion;
            while ((opcion = lectorSocket.readLine()) != null) {
                //procesa la opcion recibida
                if (opcion.equalsIgnoreCase("Inicio")) {
                    escritor.println("Ingrese su nombre de usuario:");
                    String usuario = lectorSocket.readLine();
                    escritor.println("Ingrese su contraseña:");
                    String password = lectorSocket.readLine();

                    if (validarUsuario(usuario, password)) {
                        escritor.println("Inicio de sesión exitoso. Bienvenido " + usuario + "!");
                    } else {
                        escritor.println("Usuario o contraseña incorrectos.");
                    }
                }
                else if (opcion.equalsIgnoreCase("Registrar")) {
                    escritor.println("Ingrese un nombre de usuario para registrar:");
                     String nuevoUsuario = lectorSocket.readLine();

                     boolean existeUsuario = false;
                     List<String> usuarios = leerArchivoUsuarios();
                     for (String linea : usuarios) {
                     String[] partes = linea.split("\\|");
                     if (partes.length >= 2 && partes[1].equalsIgnoreCase(nuevoUsuario)) {
                     existeUsuario = true;
                     break;
                        }
                    }

                    if (existeUsuario) {
                    escritor.println("Error: El usuario '" + nuevoUsuario + "' ya existe. Intente con otro nombre.");
                    } else {
                    escritor.println("Ingrese una contraseña:");
                    String nuevoPassword = lectorSocket.readLine();

                    int nuevoId = obtenerSiguienteId();
                    registrarUsuario(nuevoId, nuevoUsuario, nuevoPassword);

                    escritor.println("Usuario " + nuevoUsuario + " registrado exitosamente con ID: " + nuevoId);
                 }

                } else if (opcion.equalsIgnoreCase("VerUsuarios")) {
                    List<String> usuarios = leerArchivoUsuarios();
                    if (usuarios.isEmpty()) {
                        escritor.println("No hay usuarios registrados.");
                    } else {
                        escritor.println("Usuarios registrados:");
                        for (String linea : usuarios) {
                            String[] partes = linea.split("\\|");
                            if (partes.length >= 2) {
                                escritor.println("- " + partes[1]);
                            }
                        }
                        escritor.println("FIN_USUARIOS");
                    }

                } else if (opcion.equalsIgnoreCase("EnviarMensaje")) {
                   String remitente = lectorSocket.readLine();
                   String destinatario = lectorSocket.readLine();
                   String mensaje = lectorSocket.readLine();

                   boolean existeDestinatario = false;
                   List<String> usuarios = leerArchivoUsuarios();
                   for (String linea : usuarios) {
                   String[] partes = linea.split("\\|");
                   if (partes.length >= 2 && partes[1].equalsIgnoreCase(destinatario)) {
                   existeDestinatario = true;
                   break;
        }
    }

    if (!existeDestinatario) {
        escritor.println("Error: El usuario '" + destinatario + "' no existe.");
    } else {
        guardarMensaje(remitente, destinatario, mensaje);
        escritor.println("Mensaje enviado a " + destinatario);
    }


                } else if (opcion.equalsIgnoreCase("VerBuzon")) {
                    String usuario = lectorSocket.readLine();
                    // El cliente ahora enviará la página solicitada
                    String paginaStr = lectorSocket.readLine();
                    int paginaActual = 1; // Página por defecto
                    try {
                        paginaActual = Integer.parseInt(paginaStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Página inválida recibida del cliente, usando página 1.");
                    }
                    enviarBuzon(usuario, paginaActual, escritor); // Llamada actualizada
                } else if (opcion.equalsIgnoreCase("BorrarBuzon")) {
                    String usuario = lectorSocket.readLine();
                    borrarBuzon(usuario, escritor);

                } else if (opcion.equalsIgnoreCase("BorrarUsuario")) {
                    String usuarioABorrar = lectorSocket.readLine();
                    borrarUsuario(usuarioABorrar, escritor);

                } else if (opcion.equalsIgnoreCase("BorrarMensaje")) { // Este ahora solo lista mensajes para borrar
                    String usuarioActual = lectorSocket.readLine(); // quien borra
                    String usuarioB = lectorSocket.readLine(); // a quien se lo borra
                    String paginaStr = lectorSocket.readLine(); // Recibe la página solicitada
                    int paginaActual = 1;
                    try {
                        paginaActual = Integer.parseInt(paginaStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Página inválida recibida para listar mensajes a borrar, usando página 1.");
                    }
                    listarMensajesParaBorrar(usuarioActual, usuarioB, paginaActual, escritor); // Nuevo método para listar
                } else if (opcion.equalsIgnoreCase("EJECUTAR_BORRADO_MENSAJE")) { // Nuevo comando para borrar un mensaje específico
                    String usuarioActual = lectorSocket.readLine();
                    String usuarioB = lectorSocket.readLine();
                    String paginaMostradaStr = lectorSocket.readLine(); // La página que el cliente estaba viendo
                    String indiceEnPaginaStr = lectorSocket.readLine(); // El número de mensaje en esa página (1-basado)
                    
                    int paginaMostrada = 1;
                    int indiceEnPagina = -1;
                    try {
                        paginaMostrada = Integer.parseInt(paginaMostradaStr);
                        indiceEnPagina = Integer.parseInt(indiceEnPaginaStr);
                    } catch (NumberFormatException e) {
                        escritor.println("Error: Datos de borrado inválidos.");
                        System.out.println("Error al parsear datos de borrado: " + e.getMessage());
                        continue; // Continuar esperando otra opción del cliente
                    }
                    ejecutarBorradoMensaje(usuarioActual, usuarioB, paginaMostrada, indiceEnPagina, escritor);

                } else if (opcion.equalsIgnoreCase("Salir")) {
                    escritor.println("FIN");
                    break;

                } else {
                    escritor.println("Opción no válida. Intente de nuevo.");
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

    //codigo para validar usuario
    private static boolean validarUsuario(String usuario, String password) {
        List<String> usuarios = leerArchivoUsuarios();
        for (String linea : usuarios) {
            String[] partes = linea.split("\\|");
            if (partes.length == 3) {
                String nombre = partes[1];
                String pass = partes[2];
                if (nombre.equalsIgnoreCase(usuario) && pass.equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    //codigo para registrar usuario nuevo
    private static void registrarUsuario(int id, String nombre, String password) {
        try (FileWriter fw = new FileWriter(ARCHIVO_USUARIOS, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(id + "|" + nombre + "|" + password);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    //codigo para obtener el siguiente ID disponible
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

    //codigo para leer archivo de usuarios
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

    //codigo para guardar mensaje en el buzon del destinatario
    private static void guardarMensaje(String remitente, String destinatario, String mensaje) {
        File archivo = new File("buzon_" + destinatario + ".txt");
        try (FileWriter fw = new FileWriter(archivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("[" + remitente + "]: " + mensaje);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar mensaje: " + e.getMessage());
        }
    }

    //codigo para enviar el buzon al usuario que lo solicito con paginación
    private static void enviarBuzon(String usuario, int paginaActual, PrintWriter escritor) {
        File archivo = new File("buzon_" + usuario + ".txt");
        if (!archivo.exists()) {
            escritor.println("No tienes mensajes en tu buzón.");
            escritor.println("FIN_BUZON"); // Señal de fin
            return;
        }

        List<String> todosLosMensajes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                todosLosMensajes.add(linea);
            }
        } catch (IOException e) {
            escritor.println("Error al leer tu buzón.");
            escritor.println("FIN_BUZON"); // Señal de fin
            return;
        }

        if (todosLosMensajes.isEmpty()) {
            escritor.println("No tienes mensajes en tu buzón.");
            escritor.println("FIN_BUZON"); // Señal de fin
            return;
        }

        // Mostrar los mensajes más recientes primero
        java.util.Collections.reverse(todosLosMensajes);

        int mensajesPorPagina = 10;
        int totalMensajes = todosLosMensajes.size();
        int totalPaginas = (int) Math.ceil((double) totalMensajes / mensajesPorPagina);

        // Asegurarse de que la página actual esté dentro de los límites
        if (paginaActual < 1) paginaActual = 1;
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;

        int indiceInicio = (paginaActual - 1) * mensajesPorPagina;
        int indiceFin = Math.min(indiceInicio + mensajesPorPagina, totalMensajes);

        // Enviar información de paginación al cliente
        escritor.println("PAGINACION_INFO:" + paginaActual + "/" + totalPaginas); 

        // Enviar los mensajes de la página actual
        for (int i = indiceInicio; i < indiceFin; i++) {
            escritor.println(todosLosMensajes.get(i));
        }

        escritor.println("FIN_BUZON"); // Señal de fin de mensajes de la página
    }

    //codigo para borrar el buzon del usuario que lo solicito
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
     
    // Nuevo método para listar mensajes enviados por el usuario actual a otro usuario, con paginación
    private static void listarMensajesParaBorrar(String usuarioActual, String usuarioB, int paginaActual, PrintWriter escritor) {
        File archivoBuzon = new File("buzon_" + usuarioB + ".txt");
        if (!archivoBuzon.exists()) {
            escritor.println("El usuario " + usuarioB + " no tiene buzón.");
            escritor.println("FIN_MENSAJES"); // Señal de fin
            return;
        }

        List<String> todosLosMensajesEnBuzon = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoBuzon))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                todosLosMensajesEnBuzon.add(linea);
            }
        } catch (IOException e) {
            escritor.println("Error al leer el buzón de " + usuarioB + ".");
            escritor.println("FIN_MENSAJES"); // Señal de fin
            return;
        }

        // Filtrar solo los mensajes enviados por el usuarioActual
        List<String> mensajesDelRemitente = new ArrayList<>();
        // Guardar los índices originales para poder borrar el mensaje correcto más tarde
        List<Integer> indicesOriginales = new ArrayList<>(); 
        for (int i = 0; i < todosLosMensajesEnBuzon.size(); i++) {
            String msg = todosLosMensajesEnBuzon.get(i);
            if (msg.startsWith("[" + usuarioActual + "]:")) { 
                mensajesDelRemitente.add(msg);
                indicesOriginales.add(i);
            }
        }

        if (mensajesDelRemitente.isEmpty()) {
            escritor.println("No tienes mensajes enviados a " + usuarioB + ".");
            escritor.println("FIN_MENSAJES"); // Señal de fin
            return;
        }

        int mensajesPorPagina = 10;
        int totalMensajesFiltrados = mensajesDelRemitente.size();
        int totalPaginas = (int) Math.ceil((double) totalMensajesFiltrados / mensajesPorPagina);

        // Asegurarse de que la página actual esté dentro de los límites
        if (paginaActual < 1) paginaActual = 1;
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;

        int indiceInicio = (paginaActual - 1) * mensajesPorPagina;
        int indiceFin = Math.min(indiceInicio + mensajesPorPagina, totalMensajesFiltrados);

        // Enviar información de paginación al cliente
        escritor.println("PAGINACION_INFO:" + paginaActual + "/" + totalPaginas); 
        escritor.println("=== Mensajes enviados a " + usuarioB + " (Página " + paginaActual + " de " + totalPaginas + ") ===");

        // Enviar los mensajes de la página actual
        for (int i = indiceInicio; i < indiceFin; i++) {
            // El número que ve el cliente es 1-basado y relativo a la página
            escritor.println((i - indiceInicio + 1) + ". " + mensajesDelRemitente.get(i));
        }
        escritor.println("FIN_MENSAJES"); // Señal de fin de mensajes de la página
    }

    // Nuevo método para ejecutar el borrado de un mensaje específico
    private static void ejecutarBorradoMensaje(String usuarioActual, String usuarioB, int paginaMostrada, int indiceEnPagina, PrintWriter escritor) {
        File archivoBuzon = new File("buzon_" + usuarioB + ".txt");
        if (!archivoBuzon.exists()) {
            escritor.println("Error: El buzón de " + usuarioB + " no existe.");
            return;
        }

        List<String> todosLosMensajesEnBuzon = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoBuzon))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                todosLosMensajesEnBuzon.add(linea);
            }
        } catch (IOException e) {
            escritor.println("Error al leer el buzón para borrar el mensaje.");
            return;
        }

        // Volver a filtrar los mensajes para encontrar el índice correcto
        List<String> mensajesDelRemitente = new ArrayList<>();
        List<Integer> indicesOriginales = new ArrayList<>();
        for (int i = 0; i < todosLosMensajesEnBuzon.size(); i++) {
            String msg = todosLosMensajesEnBuzon.get(i);
            if (msg.startsWith("[" + usuarioActual + "]:")) {
                mensajesDelRemitente.add(msg);
                indicesOriginales.add(i);
            }
        }

        if (mensajesDelRemitente.isEmpty()) {
            escritor.println("Error: No se encontraron mensajes tuyos para borrar.");
            return;
        }

        int mensajesPorPagina = 10;
        int totalMensajesFiltrados = mensajesDelRemitente.size();
        
        // Calcular el índice absoluto en la lista filtrada (mensajesDelRemitente)
        // El cliente envía un índice 1-basado, por eso (indiceEnPagina - 1)
        int indiceAbsolutoFiltrado = (paginaMostrada - 1) * mensajesPorPagina + (indiceEnPagina - 1);

        if (indiceAbsolutoFiltrado < 0 || indiceAbsolutoFiltrado >= totalMensajesFiltrados) {
            escritor.println("Error: Número de mensaje inválido para borrar.");
            return;
        }

        // Obtener el índice real en la lista completa de mensajes del buzón
        int indiceRealEnBuzon = indicesOriginales.get(indiceAbsolutoFiltrado);

        // Borrar el mensaje de la lista completa
        todosLosMensajesEnBuzon.remove(indiceRealEnBuzon);

        // Reescribir el archivo del buzón sin el mensaje borrado
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoBuzon))) {
            for (String m : todosLosMensajesEnBuzon) {
                pw.println(m);
            }
            escritor.println("Mensaje borrado con éxito.");
        } catch (IOException e) {
            escritor.println("Error al reescribir el buzón después de borrar el mensaje.");
        }
    }

// codigo para borrar un usuario y su buzón
private static void borrarUsuario(String usuario, PrintWriter escritor) {
    // borra el usuario del archivo de usuarios
    File archivoUsuarios = new File(ARCHIVO_USUARIOS);
    File archivoTemp = new File("temp_usuarios.txt");

    try (BufferedReader br = new BufferedReader(new FileReader(archivoUsuarios));
         BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemp))) {
        String linea;
        boolean usuarioEncontrado = false;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split("\\|");
            if (partes.length >= 2 && partes[1].equalsIgnoreCase(usuario)) {
                usuarioEncontrado = true;
            } else {
                bw.write(linea);
                bw.newLine();
            }
        }
        
        if (!usuarioEncontrado) {
            escritor.println("Error: El usuario no fue encontrado.");
            return;
        }

    } catch (IOException e) {
        escritor.println("Error al procesar la solicitud de borrado del usuario.");
        return;
    }

    // reescribe el archivo original
    if (!archivoUsuarios.delete()) {
        escritor.println("Error al borrar el archivo de usuarios original.");
        return;
    }
    if (!archivoTemp.renameTo(archivoUsuarios)) {
        escritor.println("Error al renombrar el archivo temporal.");
        return;
    }

    // borra el buzón del usuario
    File archivoBuzon = new File("buzon_" + usuario + ".txt");
    if (archivoBuzon.exists()) {
        if (archivoBuzon.delete()) {
            System.out.println("Buzón de " + usuario + " borrado.");
        } else {
            System.out.println("Error al borrar el buzón de " + usuario + ".");
        }
    }

    escritor.println("Cuenta de usuario y buzón borrados exitosamente.");
}

}



