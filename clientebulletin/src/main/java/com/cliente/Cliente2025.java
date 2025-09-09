/**
 *
 * @author Jorge Emilio
 */

package com.cliente;

import java.io.*;
import java.net.Socket;

public class Cliente2025 {

    public static void main(String[] args) {
        try (Socket socket = new Socket("10.22.20.145", 8080);
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
            while (true) {
                opcion = teclado.readLine();
                escritor.println(opcion);
                String respuesta = lector.readLine();
                if (respuesta == null) break;

                if (respuesta.equalsIgnoreCase("FIN")) {
                    System.out.println("Conexión cerrada por el servidor.");
                    break;
                }

                // Mostrar mensaje y manejar login/registro
                System.out.println(respuesta);

                if (opcion.equalsIgnoreCase("Inicio") || opcion.equalsIgnoreCase("Registrar")) {
                    String usuario = teclado.readLine();
                    escritor.println(usuario);

                    System.out.println(lector.readLine());
                    String password = teclado.readLine();
                    escritor.println(password);

                    System.out.println(lector.readLine());
                }

                System.out.println("-------------------------------------");
                System.out.println("Escriba otra opción: Inicio, Registrar o Salir");
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }
}

