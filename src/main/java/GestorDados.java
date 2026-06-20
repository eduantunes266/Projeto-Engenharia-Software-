import java.io.*;
import java.util.*;

public class GestorDados implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_PATH = "mundial_dados.dat";

    private static GestorDados instance;

    public List<String> selecoes = new ArrayList<>(Arrays.asList(
            "Brasil", "Croácia", "México", "Camarões", "Portugal", "Alemanha", "Espanha", "Argentina", "França"
    ));

    public Map<String, int[]> estadios = new HashMap<>();

    public Map<String, String> hoteisVinculados = new HashMap<>();
    public Map<String, String> centrosVinculados = new HashMap<>();

    private GestorDados() {}

    public static GestorDados getInstance() {
        if (instance == null) {
            carregarDados();
        }
        return instance;
    }

    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(this);
            System.out.println("Dados guardados com sucesso no ficheiro binário.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarDados() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                instance = (GestorDados) ois.readObject();
                System.out.println("Dados carregados com sucesso da sessão anterior.");
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        instance = new GestorDados();
    }

    public boolean equipaJaTemHotel(String equipa) {
        return hoteisVinculados.containsValue(equipa);
    }

    public boolean equipaJaTemCentro(String equipa) {
        return centrosVinculados.containsValue(equipa);
    }
}