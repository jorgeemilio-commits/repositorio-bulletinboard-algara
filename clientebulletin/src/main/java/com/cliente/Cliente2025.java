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
                    System.out.println("7. Darse de baja (eliminar cuenta)");
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
                        System.out.print("Ingrese el nombre del usuario al que vas a borrar tu mensaje: ");
                        String usuarioB = teclado.readLine();
    
                        // Envia el comando y los usuarios al servidor
                        escritor.println("BorrarMensaje"); 
                        escritor.println(usuarioActual);    
                        escritor.println(usuarioB);          

                        String respuesta;
                        // Lee la respuesta inicial del servidor (la lista o un mensaje de error)
                        while ((respuesta = lector.readLine()) != null) {
                            System.out.println(respuesta);
                            if (respuesta.startsWith("FIN_MENSAJES")) {
                                // El servidor ha enviado la lista completa, ahora esperamos la seleccion
                                System.out.print("Selecciona un mensaje para borrar (o 0 para salir): ");
                                String seleccion = teclado.readLine();
                                escritor.println(seleccion); // Envia la seleccion al servidor
            
                                // Lee la respuesta final del servidor (confirmacion o error)
                                String resultado = lector.readLine();
                                System.out.println(resultado);
                                break; // Salir del bucle
                           } else if (respuesta.startsWith("Error:") || respuesta.startsWith("No tienes")) {
                        // El servidor ha enviado un error, no hay mensajes para borrar
                        break; // Salir del bucle
                        }
                    }
                    break;
                    case "7":
                    //opcion para darse de baja
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

