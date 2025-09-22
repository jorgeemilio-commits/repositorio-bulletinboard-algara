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
                        //opcion para ver el buzon con paginación
                            int paginaActual = 1;
                            boolean continuarBuzon = true;

                            while (continuarBuzon) {
                                escritor.println("VerBuzon");
                                escritor.println(usuarioActual);
                                escritor.println(String.valueOf(paginaActual)); // Enviar la página solicitada

                                String infoPaginacion = lector.readLine();
                                if (infoPaginacion == null) {
                                    System.out.println("Error al recibir información del servidor.");
                                    break;
                                }

                                // Si no hay mensajes, el servidor envía este mensaje directamente
                                if (infoPaginacion.startsWith("No tienes mensajes en tu buzón.")) {
                                    System.out.println(infoPaginacion);
                                    lector.readLine(); // Consumir la señal FIN_BUZON
                                    continuarBuzon = false;
                                    break;
                                }
                                
                                // Si el servidor envía información de paginación
                                if (infoPaginacion.startsWith("PAGINACION_INFO:")) {
                                    String[] partes = infoPaginacion.substring("PAGINACION_INFO:".length()).split("/");
                                    int paginaMostrada = Integer.parseInt(partes[0]);
                                    int totalPaginas = Integer.parseInt(partes[1]);

                                    System.out.println("--- Buzón de " + usuarioActual + " (Página " + paginaMostrada + " de " + totalPaginas + ") ---");
                                    String mensaje;
                                    while ((mensaje = lector.readLine()) != null) {
                                        if (mensaje.equals("FIN_BUZON")) break; // Señal de fin de mensajes de la página
                                        System.out.println(mensaje);
                                    }

                                    // Preguntar si desea ver la siguiente página
                                    if (paginaMostrada < totalPaginas) {
                                        System.out.print("¿Desea ver la siguiente página? (S/N): ");
                                        String respuesta = teclado.readLine();
                                        if (respuesta != null && respuesta.equalsIgnoreCase("S")) {
                                            paginaActual++;
                                        } else {
                                            continuarBuzon = false; // El usuario no quiere ver más páginas
                                        }
                                    } else {
                                        System.out.println("Has llegado al final de tu buzón.");
                                        continuarBuzon = false; // No hay más páginas
                                    }
                                } else {
                                    // Manejar otros mensajes del servidor (ej. errores)
                                    System.out.println(infoPaginacion); 
                                    String mensaje;
                                    while ((mensaje = lector.readLine()) != null) {
                                        if (mensaje.equals("FIN_BUZON")) break;
                                        System.out.println(mensaje);
                                    }
                                    continuarBuzon = false; // Salir del bucle si no se esperaba paginación
                                }
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
                            System.out.print("Ingrese el nombre del usuario al que enviaste el mensaje a borrar: ");
                            String usuarioB = teclado.readLine();
        
                            int paginaActualBorrar = 1;
                            boolean continuarBorradoMensajes = true;

                            while (continuarBorradoMensajes) {
                                // Solicitar al servidor la lista paginada de mensajes para borrar
                                escritor.println("BorrarMensaje"); 
                                escritor.println(usuarioActual);    
                                escritor.println(usuarioB);          
                                escritor.println(String.valueOf(paginaActualBorrar)); // Enviar la página solicitada

                                String infoPaginacion = lector.readLine();
                                if (infoPaginacion == null) {
                                    System.out.println("Error al recibir información del servidor.");
                                    continuarBorradoMensajes = false;
                                    break;
                                }

                                // Si no hay mensajes o hay un error, el servidor envía un mensaje directo
                                if (infoPaginacion.startsWith("No tienes mensajes enviados a") || infoPaginacion.startsWith("El usuario") || infoPaginacion.startsWith("Error:")) {
                                    System.out.println(infoPaginacion);
                                    lector.readLine(); // Consumir la señal FIN_MENSAJES
                                    continuarBorradoMensajes = false;
                                    break;
                                }
                                
                                // Si el servidor envía información de paginación
                                if (infoPaginacion.startsWith("PAGINACION_INFO:")) {
                                    String[] partes = infoPaginacion.substring("PAGINACION_INFO:".length()).split("/");
                                    int paginaMostrada = Integer.parseInt(partes[0]);
                                    int totalPaginas = Integer.parseInt(partes[1]);

                                    String encabezado = lector.readLine(); // Leer el encabezado "=== Mensajes enviados a..."
                                    System.out.println(encabezado);

                                    String mensaje;
                                    while ((mensaje = lector.readLine()) != null) {
                                        if (mensaje.equals("FIN_MENSAJES")) break; // Señal de fin de mensajes de la página
                                        System.out.println(mensaje);
                                    }

                                    System.out.println("--------------------------------------------------");
                                    System.out.println("Opciones:");
                                    if (paginaMostrada < totalPaginas) System.out.println("S. Siguiente página");
                                    if (paginaMostrada > 1) System.out.println("A. Página anterior");
                                    System.out.println("B [número]. Borrar mensaje (ej. B 1)");
                                    System.out.println("0. Salir");
                                    System.out.print("Seleccione una opción: ");
                                    String seleccion = teclado.readLine();

                                    if (seleccion == null) {
                                        continuarBorradoMensajes = false;
                                        break;
                                    }

                                    if (seleccion.equalsIgnoreCase("S")) {
                                        if (paginaMostrada < totalPaginas) {
                                            paginaActualBorrar++;
                                        } else {
                                            System.out.println("Ya estás en la última página.");
                                        }
                                    } else if (seleccion.equalsIgnoreCase("A")) {
                                        if (paginaMostrada > 1) {
                                            paginaActualBorrar--;
                                        } else {
                                            System.out.println("Ya estás en la primera página.");
                                        }
                                    } else if (seleccion.startsWith("B ")) {
                                        try {
                                            int numeroMensajeABorrar = Integer.parseInt(seleccion.substring(2).trim());
                                            // Enviar el comando de borrado al servidor
                                            escritor.println("EJECUTAR_BORRADO_MENSAJE");
                                            escritor.println(usuarioActual);
                                            escritor.println(usuarioB);
                                            escritor.println(String.valueOf(paginaMostrada)); // La página que se mostró
                                            escritor.println(String.valueOf(numeroMensajeABorrar)); // El número de mensaje en esa página

                                            String resultadoBorrado = lector.readLine();
                                            System.out.println(resultadoBorrado);
                                            continuarBorradoMensajes = false; // Salir después de borrar
                                        } catch (NumberFormatException e) {
                                            System.out.println("Número de mensaje inválido. Intente de nuevo.");
                                        }
                                    } else if (seleccion.equals("0")) {
                                        continuarBorradoMensajes = false; // Salir
                                    } else {
                                        System.out.println("Opción no válida.");
                                    }
                                } else {
                                    // Esto no debería ocurrir si el servidor siempre envía PAGINACION_INFO o un mensaje de error/no hay mensajes
                                    System.out.println("Respuesta inesperada del servidor: " + infoPaginacion);
                                    continuarBorradoMensajes = false;
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

