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
                        //opcion para iniciar sesion
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
                        //opcion para registrar usuario nuevo
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
                        //opcion para salir
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
                    System.out.println("5. Borrar buzón de mensajes");
                    System.out.println("6. Borrar un mensaje específico");
                    System.out.println("--------------------------------------------------");
                    System.out.print("Seleccione una opción: ");
                    String opcionUsuario = teclado.readLine();

                    switch (opcionUsuario) {
                        case "1":
                        //opcion para ver todos los usuarios
                            escritor.println("VerUsuarios");
                            String linea;
                            while ((linea = lector.readLine()) != null) {
                                if (linea.equals("FIN_USUARIOS")) break;
                                System.out.println(linea);
                            }
                            break;
                        case "2":
                        //opcion para ver el buzon
                            escritor.println("VerBuzon");
                            escritor.println(usuarioActual);
                            String mensaje;
                            while ((mensaje = lector.readLine()) != null) {
                                if (mensaje.equals("FIN_BUZON")) break;
                                System.out.println(mensaje);
                            }
                            break;
                       case "3":
                       //opcion para enviar un mensaje
                            escritor.println("EnviarMensaje");
                            //quien envia el mensaje
                            escritor.println(usuarioActual); 
                            System.out.print("Ingrese el nombre del destinatario: ");
                            String destinatario = teclado.readLine();
                            //a quien va dirigido el mensaje
                            escritor.println(destinatario);
                            System.out.print("Escriba el mensaje: ");
                            String mensajeEnviar = teclado.readLine();
                            //el mensaje a enviar
                            escritor.println(mensajeEnviar);
                            System.out.println(lector.readLine()); 
                            break;
                        case "4":
                        //opcion para cerrar sesion y regresar al menu principal
                            sesionIniciada = false;
                            usuarioActual = "";
                            System.out.println("Sesión cerrada. Regresando al menú principal.");
                            break;
                        case "5":
                         //opcion para borrar buzon
                            escritor.println("BorrarBuzon");
                            escritor.println(usuarioActual);
                            System.out.println(lector.readLine());
                        break;
                        // opcion para borrar un mensaje especifico
                        case "6":
                            escritor.println("BorrarMensaje");
                            escritor.println(usuarioActual);

                            System.out.print("Ingrese el nombre del usuario al que le enviaste el mensaje a borrar: ");
                            String usuarioB = teclado.readLine();
                            escritor.println(usuarioB);

                            String respuestaServidor;
                            boolean paginando = true;
                            while (paginando) {
                                StringBuilder mensajesPagina = new StringBuilder();
                                while ((respuestaServidor = lector.readLine()) != null) {
                                    if (respuestaServidor.equals("PROMPT_PAGINATION_CHOICE")) {
                                        break; // Fin de los mensajes de la página actual y opciones
                                    }
                                    if (respuestaServidor.equals("FIN_BORRAR_MENSAJE")) {
                                        paginando = false; // El servidor ha terminado la operación
                                        break;
                                    }
                                    mensajesPagina.append(respuestaServidor).append("\n");
                                }

                                if (!paginando) {
                                    // Si se recibió FIN_BORRAR_MENSAJE, imprime cualquier salida restante y sale del bucle
                                    if (mensajesPagina.length() > 0) {
                                        System.out.print(mensajesPagina.toString());
                                    }
                                    break;
                                }

                                System.out.print(mensajesPagina.toString());
                                System.out.print("Seleccione una opción (N/P/0/número): ");
                                String seleccion = teclado.readLine();
                                escritor.println(seleccion);

                                // El servidor enviará una respuesta (ej. "Mensaje borrado con éxito.", "Error:", "Regresando...")
                                // y luego FIN_BORRAR_MENSAJE si la operación ha terminado.
                                // La siguiente iteración del bucle capturará esa respuesta o la señal de fin.
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

