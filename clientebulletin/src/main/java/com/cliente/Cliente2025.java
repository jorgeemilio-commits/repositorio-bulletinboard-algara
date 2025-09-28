/**
 *
 * @author Jorge Emilio
 */

package com.cliente;

import java.io.*;
import java.net.Socket;

public class Cliente2025 {

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
                            // Opción para ver todos los usuarios registrados
                            escritor.println("VerUsuarios");
                            String linea;
                            while ((linea = lector.readLine()) != null) {
                                if (linea.equals("FIN_USUARIOS")) break;
                                System.out.println(linea);
                            }
                            break;
                        case "2":
                            // Opción para ver el buzón de mensajes del usuario actual
                            escritor.println("VerBuzon");
                            escritor.println(usuarioActual);
                            String mensaje;
                            while ((mensaje = lector.readLine()) != null) {
                                if (mensaje.equals("FIN_BUZON")) break;
                                System.out.println(mensaje);
                            }
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
                            break;
                        case "6":
                            // Opción para borrar un mensaje específico enviado por el usuario actual
                            System.out.print("Ingrese el nombre del usuario al que enviaste el mensaje a borrar: ");
                            String usuarioObjetivo = teclado.readLine();

                            escritor.println("BorrarMensaje");
                            escritor.println(usuarioActual);
                            escritor.println(usuarioObjetivo);

                            String respuestaServidor;
                            while ((respuestaServidor = lector.readLine()) != null) {
                                System.out.println(respuestaServidor);
                                if (respuestaServidor.startsWith("FIN_MENSAJES")) {
                                    System.out.print("Selecciona un mensaje para borrar (o 0 para salir): ");
                                    String seleccionMensaje = teclado.readLine();
                                    escritor.println(seleccionMensaje);

                                    String resultadoOperacion = lector.readLine();
                                    System.out.println(resultadoOperacion);
                                    break;
                                } else if (respuestaServidor.startsWith("Error:") || respuestaServidor.startsWith("No tienes")) {
                                    break;
                                }
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
}

