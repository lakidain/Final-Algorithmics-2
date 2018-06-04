/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finaleb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Ander
 */
public class GeneradorDeDatosAleatorios {

    /**
     * @param args the command line arguments
     */
    //Ficheros
    private static final String OUTPUT_FILENAME_D = "ADP00_Distance100M100P.txt";
    private static final String OUTPUT_FILENAME_P = "ADP00_Price100M.txt";

    //Datos del problema
    public static int numPacientes = 100;    //numero de pacientes que habra, tambien deberia haberse metido en fichero porque si no cuadrara el problema no tira
    public static int numMedicos = 100;  //numero de medicos que habra
    public static int rangoprecio = 10000;  //definimos un rango en el que los datos se generaran
    public static int rangodistancias = 100;
    public static int[][] distancias = new int[numPacientes][numMedicos]; //i van a ser pacientes y j medicos, matriz de distancias, cuanta hay entre cada medico y cada paciente
    public static int[] precio = new int[numMedicos];   //array de cuanto cuesta cada medico

    public static void main(String[] args) {
        generateData();
        writeMatrixToFile();
        writePricesToFile();
        System.out.println("Fichero creado");
    }

    public static void generateData() { //creacion de los datos para el problema
        Random random = new Random();

        for (int i = 0; i < numPacientes; i++) {    //rellenamos la matriz, eje i seran los pacientes
            for (int j = 0; j < numMedicos; j++) {  //el eje j seran los medicos
                distancias[i][j] = random.nextInt(rangodistancias) + 1;
            }
        }

        for (int i = 0; i < numMedicos; i++) {
            precio[i] = random.nextInt(rangoprecio) + 1;
        }
    }

    public static void writePricesToFile() {    //escritura del array precios en fichero
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(OUTPUT_FILENAME_P));
            for (int i = 0; i < numMedicos; i++) {
                bw.write(precio[i] + " ");
            }
        } catch (IOException e) {
            System.err.println("\tERROR: Input file not found");
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeMatrixToFile() {    //escritura de la matriz distancias en fichero
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(OUTPUT_FILENAME_D));
            for (int i = 0; i < numPacientes; i++) {
                for (int j = 0; j < numMedicos - 1; j++) {  ///paramos cuando <numMedicos-1
                    bw.write(distancias[i][j] + " ");
                }
                bw.write(distancias[i][numMedicos - 1] + "\n"); //volcamos el dato que falta mas un enter
            }
        } catch (IOException e) {
            System.err.println("\tERROR: Input file not found");
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
