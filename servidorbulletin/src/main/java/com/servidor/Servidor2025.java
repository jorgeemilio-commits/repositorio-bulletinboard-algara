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
                    enviarBuzon(usuario, escritor);

                } else if (opcion.equalsIgnoreCase("BorrarBuzon")) {
                    String usuario = lectorSocket.readLine();
                    borrarBuzon(usuario, escritor);

                } else if (opcion.equalsIgnoreCase("BorrarMensaje")) {
                    String usuarioActual = lectorSocket.readLine(); // quien borra
                    String usuarioB = lectorSocket.readLine(); // a quien se lo borra
                    borrarMensaje(usuarioActual, usuarioB, lectorSocket, escritor);    

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

    //codigo para enviar el buzon al usuario que lo solicito
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
     
// codigo para borrar un mensaje especifico enviado a otro usuario
private static void borrarMensaje(String usuarioActual, String usuarioB, BufferedReader lectorSocket, PrintWriter escritor) throws IOException {
    File archivoBuzon = new File("buzon_" + usuarioB + ".txt");
    if (!archivoBuzon.exists()) {
        escritor.println("El usuario " + usuarioB + " no tiene buzón.");
        escritor.println("FIN_BORRAR_MENSAJE"); // Nueva señal para indicar el fin de la operación
        return;
    }

    List<String> todosLosMensajes = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(archivoBuzon))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            todosLosMensajes.add(linea);
        }
    }

    List<String> mensajesDelRemitente = new ArrayList<>();
    List<Integer> indicesOriginales = new ArrayList<>(); // Para mapear de vuelta a los índices originales en todosLosMensajes
    for (int i = 0; i < todosLosMensajes.size(); i++) {
        String msg = todosLosMensajes.get(i);
        if (msg.startsWith("[" + usuarioActual + "]:")) {
            mensajesDelRemitente.add(msg);
            indicesOriginales.add(i);
        }
    }

    if (mensajesDelRemitente.isEmpty()) {
        escritor.println("No tienes mensajes enviados a " + usuarioB);
        escritor.println("FIN_BORRAR_MENSAJE"); // Nueva señal
        return;
    }

    final int MENSAJES_POR_PAGINA = 5;
    int paginaActual = 0;
    int totalMensajes = mensajesDelRemitente.size();
    int totalPaginas = (int) Math.ceil((double) totalMensajes / MENSAJES_POR_PAGINA);

    while (true) {
        int inicio = paginaActual * MENSAJES_POR_PAGINA;
        int fin = Math.min(inicio + MENSAJES_POR_PAGINA, totalMensajes);

        escritor.println("=== Mensajes enviados a " + usuarioB + " (Página " + (paginaActual + 1) + "/" + totalPaginas + ") ===");
        for (int i = inicio; i < fin; i++) {
            // Mostramos el número global del mensaje, no el de la página
            escritor.println((i + 1) + ". " + mensajesDelRemitente.get(i));
        }

        escritor.println("--------------------------------------------------");
        if (paginaActual > 0) {
            escritor.println("P. Página anterior");
        }
        if (paginaActual < totalPaginas - 1) {
            escritor.println("N. Página siguiente");
        }
        escritor.println("1-" + totalMensajes + ". Seleccionar mensaje para borrar");
        escritor.println("0. Salir");
        escritor.println("PROMPT_PAGINATION_CHOICE"); // Señal para que el cliente pida la opción

        String opcionCliente = lectorSocket.readLine();
        if (opcionCliente == null) {
            break; // Cliente desconectado
        }

        if (opcionCliente.equalsIgnoreCase("N")) {
            if (paginaActual < totalPaginas - 1) {
                paginaActual++;
            } else {
                escritor.println("Ya estás en la última página.");
            }
        } else if (opcionCliente.equalsIgnoreCase("P")) {
            if (paginaActual > 0) {
                paginaActual--;
            } else {
                escritor.println("Ya estás en la primera página.");
            }
        } else if (opcionCliente.equals("0")) {
            escritor.println("Regresando al menú principal...");
            break;
        } else {
            try {
                int numeroMensajeSeleccionado = Integer.parseInt(opcionCliente);
                if (numeroMensajeSeleccionado >= 1 && numeroMensajeSeleccionado <= totalMensajes) {
                    // Encontrar el índice original del mensaje a borrar
                    int indexToDeleteInOriginalList = indicesOriginales.get(numeroMensajeSeleccionado - 1);

                    // Eliminar el mensaje de la lista completa
                    todosLosMensajes.remove(indexToDeleteInOriginalList);

                    // Reescribir el archivo con los mensajes restantes
                    try (PrintWriter pw = new PrintWriter(new FileWriter(archivoBuzon))) {
                        for (String m : todosLosMensajes) {
                            pw.println(m);
                        }
                    }
                    escritor.println("Mensaje borrado con éxito.");
                    break; // Salir del bucle de paginación después de borrar
                } else {
                    escritor.println("Número de mensaje inválido. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                escritor.println("Entrada inválida. Por favor, ingrese 'N', 'P', '0' o el número del mensaje a borrar.");
            }
        }
    }
    escritor.println("FIN_BORRAR_MENSAJE"); // Señal de fin de operación
}

}



