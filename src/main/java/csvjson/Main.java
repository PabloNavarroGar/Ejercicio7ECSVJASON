/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package csvjson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author pablo
 */
public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        //-Mas adelante necesito el formato ya que en el archivo esta ordenado con el formato de aqui
      
        //-Lectura del fichero
        // Fichero a leer con datos de ejemplo
        String idFichero = "RelPerCen.csv";

        // Variables para guardar los datos que se van leyendo
        String[] tokens = null;
        String linea;

        System.out.println("Leyendo el fichero: " + idFichero);

        // Inicialización del flujo "datosFichero" en función del archivo llamado "idFichero"
        // Estructura try-with-resources. Permite cerrar los recursos una vez finalizadas
        // las operaciones con el archivo
        try ( Scanner datosFichero = new Scanner(new File(idFichero), "ISO-8859-1")) {
            // hasNextLine devuelve true mientras haya líneas por leer
            while (datosFichero.hasNextLine()) {
                // Guarda la línea completa en un String
                linea = datosFichero.nextLine();
                // Se guarda en el array de String cada elemento de la
                // línea en función del carácter separador de campos del fichero CSV
                tokens = linea.split(";");
                for (String string : tokens) {
                    System.out.print(string + "\t");
                }
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

       
        //Creo la lista
        //-- ./ es la ruta raiz de proyeto
        List<Pojo> listaProfesores = generarLista("./", "RelPerCen", "csv", ",");
        
        //Aqui va todo el metodo de abajo, directamente invocado con el formato y la ruta
        
        
        //Continuacion la separacion
        //Filtramos los profesores que llevan entre 15 y 10 años trabajando
        List<Pojo> profesoradoFiltrado = listaProfesores.stream()
                .filter(p -> p.getFechaPosesion().isAfter(LocalDate.now().minusYears(15)))
                .filter(p -> p.getFechaCese().isBefore(LocalDate.now().minusYears(10)))
                .toList();
        //-Uso de la libreria del temario
        ObjectMapper mapeador = new ObjectMapper();
        // Permite a mapeador usar fechas según java time
        mapeador.registerModule(new JavaTimeModule());
        // Formato JSON bien formateado. Si se comenta, el fichero queda minificado

        // Creamos el archivo con los profesores
        //Creamos el archivo con los profesores que llevan entre 10 y 15 años trabajando
        generarJSON("./listadoProfesores", profesoradoFiltrado);
        //---

    }

    public static List<Pojo> generarLista(String ruta, String nomFichero, String formato, String separador) {

        //-Creo la lista de la clase pojo
        List<Pojo> listaPojo = new ArrayList<>();
        //-Creo el formate de leer
        String idFichero = ruta + nomFichero + "." + formato;
        
        //- Formato de fecha
          DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
         // Variables para guardar los datos que se van leyendo
        String[] tokens;
        String linea;
        
        
        try ( Scanner datosFichero = new Scanner(new File(idFichero), "ISO-8859-1")) {
            // hasNextLine devuelve true mientras haya líneas por leer
            datosFichero.nextLine();
            while (datosFichero.hasNextLine()) {
                Pojo pojo = new Pojo();
                // Guarda la línea completa en un String
                linea = datosFichero.nextLine();
                // Se guarda en el array de String cada elemento de la
                // línea en función del carácter separador de campos del fichero CSV
                // Convierte en String tokens
                tokens = linea.split(separador);
                //- Eliminamos los caracteres " (las comillas)para evitar fallos al parse
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].replaceAll("\"", "");
                }
                //-Guardamos cada elemento en su token 
                pojo.setApellido(tokens[0]);
                pojo.setNombre(tokens[1]);
                pojo.setDni(tokens[2]);
                pojo.setPuesto(tokens[3]);
                //-bloque try-catch para evitar excepciones para la fecha vacía
                try {
                    pojo.setFechaPosesion(LocalDate.parse(tokens[4], formatoFecha));
                } catch (DateTimeParseException dyf) {
                    pojo.setFechaPosesion(null);
                }
                
                try {
                    pojo.setFechaCese(LocalDate.parse(tokens[5], formatoFecha));
                } catch (DateTimeParseException dyf) {
                    pojo.setFechaCese(null);
                }
                pojo.setTelefono(tokens[6]);
                //-Le pongo el equals para que ignore las tildes
                pojo.setEvaluador(tokens[7].equalsIgnoreCase("Sí"));
                pojo.setCoordinador(tokens[8].equalsIgnoreCase("Sí"));

                //-En este punto se añade el profesor a la lista
                listaPojo.add(pojo);

            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        return listaPojo;
    }

    //Meoto para genear JSons
    public static void generarJSON(String ruta, List<Pojo> lista) throws IOException {
        ObjectMapper mapeador = new ObjectMapper();
        // Permite a mapeador usar fechas según java time
        mapeador.registerModule(new JavaTimeModule());
        // Formato JSON bien formateado. Si se comenta, el fichero queda minificado
        mapeador.configure(SerializationFeature.INDENT_OUTPUT, true);
        //--- Creamos el archivo con todos los profesores
        //mapeador.writeValue(new File("profesorado.json"), profesorado);
        //--- Creamos el archivo con los profesores que llevan entre 10 y 15 años trabajando
        mapeador.writeValue(new File(ruta.concat(".json")), lista);

    }

}
