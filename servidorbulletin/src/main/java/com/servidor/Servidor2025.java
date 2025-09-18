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
        return;
    }

    List<String> mensajes = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(archivoBuzon))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            mensajes.add(linea);
        }
    }

    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < mensajes.size(); i++) {
        String msg = mensajes.get(i);
        if (msg.startsWith("[" + usuarioActual + "]:")) { 
            indices.add(i);
        }
    }

    if (indices.isEmpty()) {
        escritor.println("No tienes mensajes enviados a " + usuarioB);
        return;
    }
    
    // Envia la lista de mensajes al cliente
    escritor.println("=== Mensajes enviados a " + usuarioB + " ===");
    for (int i = 0; i < indices.size(); i++) {
        escritor.println((i + 1) + ". " + mensajes.get(indices.get(i)));
    }
    escritor.println("0. Salir");
    escritor.println("FIN_MENSAJES"); // Señal para que el cliente pida la opción
    
    String opcion = lectorSocket.readLine();
    if (opcion == null || opcion.equals("0")) {
        escritor.println("Regresando al menú principal...");
    } else {
        try {
            int numero = Integer.parseInt(opcion);
            if (numero >= 1 && numero <= indices.size()) {
                int idx = indices.get(numero - 1);
                mensajes.remove(idx);
                
                // Reescribe el archivo con los mensajes restantes
                try (PrintWriter pw = new PrintWriter(new FileWriter(archivoBuzon))) {
                    for (String m : mensajes) pw.println(m);
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

}



