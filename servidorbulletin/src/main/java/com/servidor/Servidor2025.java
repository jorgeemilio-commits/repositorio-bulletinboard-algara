
/**
 *
 * @author Jorge Emilio
 */

package com.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor2025 {

    private static final String ARCHIVO_USUARIOS = "usuarios.txt";
    private static AtomicInteger ultimoId = new AtomicInteger(0);

    public static void main(String[] args) {
        cargarUltimoId();

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

    // Carga el último ID del archivo si ya existe
    private static void cargarUltimoId() {
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int maxId = 0;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 1) {
                    int id = Integer.parseInt(partes[0]);
                    if (id > maxId) maxId = id;
                }
            }
            ultimoId.set(maxId);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de usuarios.");
        }
    }

    private static void manejarCliente(Socket cliente) {
        try (
                PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {
            String opcion;
            while ((opcion = lectorSocket.readLine()) != null) {
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

                } else if (opcion.equalsIgnoreCase("Registrar")) {
                    escritor.println("Ingrese un nombre de usuario para registrar:");
                    String nuevoUsuario = lectorSocket.readLine();
                    escritor.println("Ingrese una contraseña:");
                    String nuevoPassword = lectorSocket.readLine();

                    if (usuarioExiste(nuevoUsuario)) {
                        escritor.println("El usuario ya existe. Intente con otro nombre.");
                    } else {
                        registrarUsuario(nuevoUsuario, nuevoPassword);
                        escritor.println("Usuario " + nuevoUsuario + " registrado exitosamente.");
                    }

                } else if (opcion.equalsIgnoreCase("Salir")) {
                    escritor.println("FIN");
                    break;
                } else {
                    escritor.println("Opción no válida. Escriba Inicio, Registrar o Salir.");
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

    private static boolean validarUsuario(String usuario, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 3) {
                    String nombre = partes[1];
                    String pass = partes[2];
                    if (nombre.equalsIgnoreCase(usuario) && pass.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de usuarios.");
        }
        return false;
    }

    private static boolean usuarioExiste(String usuario) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 2 && partes[1].equalsIgnoreCase(usuario)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Si no existe el archivo aún, no hay usuarios
        }
        return false;
    }

    private static void registrarUsuario(String usuario, String password) {
        int nuevoId = ultimoId.incrementAndGet();
        try (FileWriter fw = new FileWriter(ARCHIVO_USUARIOS, true)) {
            fw.write(nuevoId + "|" + usuario + "|" + password + "\n");
        } catch (IOException e) {
            System.out.println("No se pudo escribir en el archivo de usuarios.");
        }
    }
}