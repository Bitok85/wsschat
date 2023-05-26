package socketserver.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class PropertiesReader {

    private static final String CLIENTS_IDS_PATH = "clients_ids_list.txt";

    public static Set<String> getClientsSet() throws IOException {
        Path file = Paths.get(CLIENTS_IDS_PATH);
        return new HashSet<>(Files.readAllLines(file));
    }
}
