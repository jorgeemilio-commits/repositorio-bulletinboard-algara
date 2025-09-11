/**
 *
 * @author Jorge Emilio
 */

package com.cliente;

import java.io.*;
import java.net.Socket;

public class Cliente2025 {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {
            /*
            System.out.println("-------------------------------------");
            System.out.println("Bienvenido al Bulletin Board.");
            System.out.println("Si desea iniciar sesión escriba 'Inicio'");
            System.out.println("Si desea registrar un nuevo usuario escriba 'Registrar'");
            System.out.println("Si desea salir escriba 'Salir'");
            System.out.println("-------------------------------------");
            */
            String opcion;
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
                            escritor.println("Inicio");
                            String respuestaInicio = lector.readLine();
                            if (respuestaInicio == null) break;
                            if (respuestaInicio.equalsIgnoreCase("FIN")) {
                                System.out.println("Conexión cerrada por el servidor.");
                                break;
                            }
                            System.out.println(respuestaInicio);

                            String usuario = teclado.readLine();
                            escritor.println(usuario);

                            System.out.println(lector.readLine());
                            String password = teclado.readLine();
                            escritor.println(password);

                            String resultadoLogin = lector.readLine();
                            System.out.println(resultadoLogin);

                            if (resultadoLogin != null && resultadoLogin.startsWith("Inicio de sesión exitoso")) {
                                sesionIniciada = true;
                                usuarioActual = usuario;
                            }
                            break;

                        case "2":
                            escritor.println("Registrar");
                            String respuestaRegistrar = lector.readLine();
                            if (respuestaRegistrar == null) break;
                            if (respuestaRegistrar.equalsIgnoreCase("FIN")) {
                                System.out.println("Conexión cerrada por el servidor.");
                                break;
                            }
                            System.out.println(respuestaRegistrar);

                            String nuevoUsuario = teclado.readLine();
                            escritor.println(nuevoUsuario);

                            System.out.println(lector.readLine());
                            String nuevoPassword = teclado.readLine();
                            escritor.println(nuevoPassword);

                            System.out.println(lector.readLine());
                            break;

                        case "3":
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
                    // Menu de navegacion tras login
                    System.out.println("------ Menú de usuario "+ usuarioActual +" ------");
                    System.out.println("1. Ver todos los usuarios registrados");
                    System.out.println("2. Ver tu buzón de mensajes");
                    System.out.println("3. Enviar un mensaje a un usuario");
                    System.out.println("4. Cerrar sesión");
                    System.out.println("--------------------------------------------------");
                    System.out.print("Seleccione una opción: ");
                    String opcionUsuario = teclado.readLine();

                    switch (opcionUsuario) {
                        case "1":
                            escritor.println("VerUsuarios");
                            String linea;
                            while ((linea = lector.readLine()) != null) {
                                if (linea.equals("FIN_USUARIOS")) break;
                                System.out.println(linea);
                            }
                            break;
                        case "2":
                            System.out.println("Función placeholder: Mostrando tu buzón de mensajes...");
                            // buzon de mensajes
                            break;
                        case "3":
                            escritor.println("VerBuzon");
                            escritor.println(usuarioActual);
                            String mensaje;
                            while ((mensaje = lector.readLine()) != null) {
                                if (mensaje.equals("FIN_BUZON")) break;
                                System.out.println(mensaje);
                            }
                            break;
                        case "4":
                            escritor.println("EnviarMensaje");
                            System.out.print("Ingrese el nombre del destinatario: ");
                            String destinatario = teclado.readLine();
                            escritor.println(destinatario);
                            System.out.print("Escriba el mensaje: ");
                            String mensajeEnviar = teclado.readLine();
                            escritor.println(mensajeEnviar);
                            System.out.println(lector.readLine()); // confirmación
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
}

