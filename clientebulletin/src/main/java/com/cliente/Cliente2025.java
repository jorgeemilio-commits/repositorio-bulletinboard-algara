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

            System.out.println("-------------------------------------");
            System.out.println("Bienvenido al Bulletin Board.");
            System.out.println("Si desea iniciar sesión escriba 'Inicio'");
            System.out.println("Si desea registrar un nuevo usuario escriba 'Registrar'");
            System.out.println("Si desea salir escriba 'Salir'");
            System.out.println("-------------------------------------");

            String opcion;
            boolean sesionIniciada = false;
            String usuarioActual = "";

            while (true) {
                if (!sesionIniciada) {
                    opcion = teclado.readLine();
                    escritor.println(opcion);
                    String respuesta = lector.readLine();
                    if (respuesta == null) break;

                    if (respuesta.equalsIgnoreCase("FIN")) {
                        System.out.println("Conexión cerrada por el servidor.");
                        break;
                    }

                    System.out.println(respuesta);

                    if (opcion.equalsIgnoreCase("Inicio")) {
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
                    } else if (opcion.equalsIgnoreCase("Registrar")) {
                        String usuario = teclado.readLine();
                        escritor.println(usuario);

                        System.out.println(lector.readLine());
                        String password = teclado.readLine();
                        escritor.println(password);

                        System.out.println(lector.readLine());
                    }

                    System.out.println("-------------------------------------");
                    System.out.println("Escriba otra opción:");
                    System.out.println("Si desea iniciar sesión escriba 'Inicio'");
                    System.out.println("Si desea registrar un nuevo usuario escriba 'Registrar'");
                    System.out.println("Si desea salir escriba 'Salir'");
                    System.out.println("-------------------------------------");
                } else {
                    // Menu de navegacion tras login
                    System.out.println("------ Menú de usuario ------");
                    System.out.println("1. Ver todos los usuarios registrados");
                    System.out.println("2. Ver tu buzón de mensajes");
                    System.out.println("3. Enviar un mensaje a un usuario");
                    System.out.println("4. Cerrar sesión");
                    System.out.println("-----------------------------");
                    System.out.print("Seleccione una opción: ");
                    String opcionUsuario = teclado.readLine();

                    switch (opcionUsuario) {
                        case "1":
                            System.out.println("Función placeholder: Mostrando usuarios registrados...");
                            // usuuarios registrados
                            break;
                        case "2":
                            System.out.println("Función placeholder: Mostrando tu buzón de mensajes...");
                            // buzon de mensajes
                            break;
                        case "3":
                            System.out.println("Función placeholder: Enviando mensaje a usuario...");
                            // enviar mensaje
                            break;
                        case "4":
                            sesionIniciada = false;
                            usuarioActual = "";
                            System.out.println("Sesión cerrada. Regresando al menú principal.");
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

