import java.io.*;
import java.util.*;

public class GestorDados implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_PATH = "mundial_dados.dat";

    private static GestorDados instance;

    public List<String> selecoes = new ArrayList<>(Arrays.asList(
            "Portugal", "Espanha", "França", "Alemanha", "Itália", "Inglaterra", "Países Baixos", "Bélgica",
            "Croácia", "Suíça", "Dinamarca", "Polónia", "Áustria", "Ucrânia", "Suécia", "Sérvia",
            "País de Gales", "Escócia", "Noruega", "Chéquia", "Turquia", "Rússia", "Hungria", "Irlanda",
            "Roménia", "Grécia", "Eslováquia", "Eslovénia", "Finlândia", "Islândia", "Irlanda do Norte",
            "Bósnia e Herzegovina", "Bulgária", "Montenegro", "Macedónia do Norte", "Albânia", "Geórgia",
            "Israel", "Luxemburgo", "Arménia", "Azerbaijão", "Cazaquistão", "Chipre", "Estónia", "Letónia",
            "Lituânia", "Bielorrússia", "Kosovo", "Andorra", "Malta", "Moldávia", "Listenstaine", "Gibraltar",
            "Ilhas Faroé", "São Marino",
            "Brasil", "Argentina", "Uruguai", "Colômbia", "Chile", "Peru", "Equador", "Paraguai", "Venezuela",
            "Bolívia",
            "México", "Estados Unidos", "Canadá", "Costa Rica", "Jamaica", "Panamá", "Honduras", "El Salvador",
            "Guatemala", "Trindade e Tobago", "Haiti", "Curaçao", "Nicarágua", "Suriname", "Cuba", "Granada",
            "Antígua e Barbuda", "São Cristóvão e Neves", "Belize", "República Dominicana", "Barbados",
            "Bermudas", "Guiana", "Santa Lúcia", "São Vicente e Granadinas", "Porto Rico", "Dominica",
            "Bahamas", "Aruba", "Montserrat",
            "Camarões", "Gana", "Nigéria", "Senegal", "Egito", "Argélia", "Marrocos", "Tunísia",
            "Costa do Marfim", "Mali", "Burquina Faso", "África do Sul", "RD Congo", "Cabo Verde", "Guiné",
            "Zâmbia", "Quénia", "Uganda", "Angola", "Gabão", "Benim", "Togo", "Madagáscar", "Mauritânia",
            "Guiné Equatorial", "Congo", "Moçambique", "Namíbia", "Zimbabué", "Serra Leoa", "Líbia", "Sudão",
            "Etiópia", "Tanzânia", "Maláui", "Níger", "Guiné-Bissau", "Comores", "Gâmbia", "Ruanda", "Libéria",
            "Botsuana", "Essuatíni", "Lesoto", "Maurícia", "Chade", "República Centro-Africana", "Burundi",
            "Sudão do Sul", "São Tomé e Príncipe", "Seicheles", "Eritreia", "Somália", "Djibuti",
            "Japão", "Coreia do Sul", "Irão", "Austrália", "Arábia Saudita", "Catar", "Iraque",
            "Emirados Árabes Unidos", "China", "Usbequistão", "Omã", "Jordânia", "Barém", "Síria", "Líbano",
            "Índia", "Vietname", "Tailândia", "Coreia do Norte", "Palestina", "Quirguistão", "Tajiquistão",
            "Turquemenistão", "Hong Kong", "Filipinas", "Indonésia", "Malásia", "Myanmar", "Singapura",
            "Iémen", "Afeganistão", "Maldivas", "Kuwait", "Nepal", "Bangladesh", "Camboja", "Butão", "Brunei",
            "Laos", "Mongólia", "Macau", "Paquistão", "Sri Lanka", "Timor-Leste", "Guame", "Taipé Chinesa",
            "Nova Zelândia", "Fiji", "Papua-Nova Guiné", "Ilhas Salomão", "Vanuatu", "Taiti", "Nova Caledónia",
            "Samoa", "Tonga", "Ilhas Cook", "Samoa Americana"
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