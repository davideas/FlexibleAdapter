package eu.davidea.samples.flexibleadapter.services;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Davide Steduto
 * @since 29/04/2016
 */
public class InstagramRandomData {

    public static String getRandomName() {
        return names[new Random().nextInt(names.length)];
    }

    public static String getImageUrl(String place) {
        int index = toList(places).indexOf(place);
        return urls[index];
    }

    public static String getRandomPlace() {
        return places[new Random().nextInt(places.length)];
    }

    private static List<String> toList(String... list) {
        return Arrays.asList(list);
    }

    public static final String[] names = {"Andrea", "Luca", "Marco", "Francesco", "Matteo",
            "Alessandro", "Davide", "Simone", "Federico", "Lorenzo", "Mattia", "Stefano",
            "Giuseppe", "Riccardo", "Daniele", "Michele", "Alessio", "Antonio", "Giovanni",
            "Nicola", "Gabriele", "Fabio", "Alberto", "Giacomo", "Giulio", "Filippo",
            "Gianluca", "Paolo", "Roberto", "Salvatore", "Emanuele", "Edoardo", "Enrico",
            "Vincenzo", "Nicolò", "Leonardo", "Jacopo", "Manuel", "Mirko", "Tommaso", "Pietro",
            "Luigi", "Giorgio", "Angelo", "Dario", "Valerio", "Domenico", "Claudio", "Alex",
            "Christian", "Giulia", "Chiara", "Francesca", "Federica", "Sara", "Martina",
            "Valentina", "Alessia", "Silvia", "Elisa", "Ilaria", "Eleonora", "Giorgia", "Elena",
            "Laura", "Alice", "Alessandra", "Jessica", "Arianna", "Marta", "Veronica", "Roberta",
            "Anna", "Giada", "Claudia", "Beatrice", "Valeria", "Michela", "Serena", "Camilla",
            "Irene", "Cristina", "Simona", "Maria", "Noemi", "Stefania", "Erika", "Sofia", "Lucia",
            "Vanessa", "Greta", "Debora", "Nicole", "Angela", "Paola", "Caterina", "Monica",
            "Erica", "Lisa", "Gaia"
    };

    public static final String[] places = {
            "Akjoujt, Mauritania",
            "Sun Lakes, Arizona, United States",
            "Waddan, Libya",
            "Nouméa, New Caledonia",
            "Paris, France",
            "Pyin Hpyu Gyi, Myanmar",
            "Sanlúcar la Mayor, Spain",
            "Clayton County, United States",
            "Malé, Maldives",
            "Liberty Island, New York City",
            "Colombiers, France",
            "Isla Cristina, Spain",
            "Yellowstone National Park, United States",
            "Grimaud, France",
            "Ayers Rock, Australia",
            "Calabasas, California, United States",
            "Mauritania",
            "Uzbekistan",
            "Tablelands, Australia",
            "Saint John, United States",
            "Xhariep, South Africa",
            "Al Jowf, Saudi Arabia",
            "Kumarina, Australia",
            "Ayamonte, Spain",
            "Haerbin, China",
            "Venice, Italy",
            "Seaweed Farms, Nusa Lembongan, Indonesia",
            "The Palm Jumeirah, Dubai, United Arab Emirates"
    };

    public static final String[] urls = {
            "https://twistedsifter.files.wordpress.com/2015/08/akjoujt.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/sun-lakes-united-states.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/waddan-libya.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/noumea-new-caldeonia.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/paris-france-2.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/pyin-hpyu-gyi-myanmar.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/sanlucar-la-mayor.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/clayton-county-united-states.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/male-maldives.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/liberty-island-new-york.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/columbiers-france.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/isla-cristina-spain.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/yellowstone-national-park-united-states.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/grimaud-france.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/ayers-rock-australia.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/calabasas-united-states.jpg",
            "https://twistedsifter.files.wordpress.com/2015/08/mauritania.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-7.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-6.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-5.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-2.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-4.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-8.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-1.jpg",
            "http://www.thisiscolossal.com/wp-content/uploads/2015/08/google-9.jpg",
            "http://twistedsifter.files.wordpress.com/2014/06/venice-italy-from-above-aerial-satellite.jpg?w=400&h=225",
            "http://twistedsifter.files.wordpress.com/2014/06/seaweed-farm-indonesia-from-above-aerial-satellite.jpg?w=400&h=225",
            "http://twistedsifter.files.wordpress.com/2014/06/the-palm-jumeirah-dubai-uae-from-above-aerial-satellite.jpg?w=400&h=218"
    };

}