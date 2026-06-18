import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MenuAcessos extends JPanel {

    // Paleta de cores do Mundial
    private final Color verdeBrasil = new Color(0, 156, 59);
    private final Color azulBrasil = new Color(0, 39, 118);
    private final Color amareloBrasil = new Color(255, 223, 0);

    private int precoAtualUnitario = 50;
    private final String[] nomesSetores = {"Bancada Central", "Zona VIP", "Topos"};

    // UI Elements
    private JComboBox<String> cbGrupo;
    private JComboBox<JogoSimples> cbJogos;
    private JComboBox<String> cbSetor;
    private JSpinner spQuantidade;
    private JButton btnEmitir;
    private JLabel lblPreco;
    private JLabel lblEstadio;
    private final JProgressBar[] barras = new JProgressBar[3];

    // ==========================================
    // PERSISTÊNCIA E DADOS (Ficheiros Binários)
    // ==========================================
    private static final String FILE_BILHETES = "bilhetes_dados.dat";
    private static final String FILE_MATCHCENTER = "matchcenter_dados.dat";
    private static final String FILE_MUNDIAL = "mundial_dados.dat";

    // Mapeia o ID do Jogo para os bilhetes vendidos [Central, VIP, Topos]
    private Map<String, int[]> bilhetesVendidos = new HashMap<>();
    private Map<String, int[]> estadiosGlobais = new HashMap<>();
    private List<JogoSimples> todosJogos = new ArrayList<>();

    // Classe auxiliar para isolar os dados lidos do MenuMatchCenter
    private static class JogoSimples {
        String id, partida, estadio; int grupo;
        JogoSimples(String i, int g, String p, String e) { id = i; grupo = g; partida = p; estadio = e; }
        @Override public String toString() { return partida; }
    }

    public MenuAcessos() {
        carregarDados();

        setLayout(new GridLayout(1, 2, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        Color corPaineis = new Color(255, 255, 255, 230);

        // ==========================================
        // LADO ESQUERDO: Emissão de Bilhetes
        // ==========================================
        JPanel painelEsquerdo = new JPanel(new GridLayout(10, 1, 5, 5)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(corPaineis); g.fillRect(0, 0, getWidth(), getHeight()); super.paintComponent(g);
            }
        };
        painelEsquerdo.setOpaque(false);
        painelEsquerdo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(verdeBrasil, 2), "Emissão de Bilhetes", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), azulBrasil));

        String[] fases = { "Selecione a Fase...", "Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H", "Oitavos","Quartos","Meia-Final","Final" };
        cbGrupo = new JComboBox<>(fases);
        cbJogos = new JComboBox<>();
        cbSetor = new JComboBox<>(nomesSetores);

        lblPreco = new JLabel("Preço Unitário: -  |  TOTAL: -");
        lblPreco.setFont(new Font("Arial", Font.BOLD, 14)); lblPreco.setForeground(azulBrasil);

        spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        // REQ: Caixas de texto inalteráveis (Bloqueia a caixa de texto embutida no JSpinner para que só as setas funcionem)
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spQuantidade.getEditor();
        spinnerEditor.getTextField().setEditable(false);

        btnEmitir = new JButton("EMITIR BILHETE");
        btnEmitir.setBackground(verdeBrasil); btnEmitir.setForeground(Color.WHITE);
        btnEmitir.setFont(new Font("Arial", Font.BOLD, 14));
        btnEmitir.setFocusPainted(false);
        btnEmitir.setOpaque(true);
        btnEmitir.setContentAreaFilled(true);

        painelEsquerdo.add(new JLabel("1. Selecionar Fase/Grupo:")); painelEsquerdo.add(cbGrupo);
        painelEsquerdo.add(new JLabel("2. Selecionar Jogo:")); painelEsquerdo.add(cbJogos);
        painelEsquerdo.add(new JLabel("3. Selecionar Setor:")); painelEsquerdo.add(cbSetor);
        painelEsquerdo.add(lblPreco);
        painelEsquerdo.add(new JLabel("4. Quantidade:")); painelEsquerdo.add(spQuantidade);
        painelEsquerdo.add(btnEmitir);

        // ==========================================
        // LADO DIREITO: Controlo de Lotação
        // ==========================================
        JPanel painelDireito = new JPanel(new GridLayout(8, 1, 5, 5)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(corPaineis); g.fillRect(0, 0, getWidth(), getHeight()); super.paintComponent(g);
            }
        };
        painelDireito.setOpaque(false);
        painelDireito.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(amareloBrasil, 2), "Controlo de Lotação (Tempo Real)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), azulBrasil));

        lblEstadio = new JLabel("Estádio: (selecione um jogo)");
        lblEstadio.setFont(new Font("Arial", Font.BOLD, 16)); lblEstadio.setForeground(verdeBrasil);
        painelDireito.add(lblEstadio); painelDireito.add(new JLabel(" "));

        for (int i = 0; i < 3; i++) {
            barras[i] = new JProgressBar(0, 100);
            barras[i].setStringPainted(true);
            painelDireito.add(barras[i]);
            if (i < 2) painelDireito.add(new JLabel(" "));
        }

        // ==========================================
        // EVENTOS E LÓGICA DE NEGÓCIO
        // ==========================================

        cbGrupo.addActionListener(e -> {
            cbJogos.removeAllItems();
            int idxFase = cbGrupo.getSelectedIndex() - 1;
            if (idxFase >= 0) {
                for (JogoSimples j : todosJogos) {
                    // REQ: Validação de Existência - Apenas popula os jogos com instâncias reais trazidas da memória do MatchCenter
                    if (j.grupo == idxFase) cbJogos.addItem(j);
                }
            }
            atualizarUI();
        });

        cbJogos.addActionListener(e -> atualizarUI());
        cbSetor.addActionListener(e -> atualizarUI());
        spQuantidade.addChangeListener(e -> atualizarUI());

        btnEmitir.addActionListener(e -> {
            JogoSimples jogo = (JogoSimples) cbJogos.getSelectedItem();
            if (jogo == null) return;

            int idxSetor = cbSetor.getSelectedIndex();
            int idxCapacidade = (idxSetor == 1) ? 2 : (idxSetor == 2) ? 1 : 0;

            int qtd = (Integer) spQuantidade.getValue();
            int[] lotacaoEstadio = estadiosGlobais.get(jogo.estadio);
            int[] vendasDoJogo = bilhetesVendidos.computeIfAbsent(jogo.id, k -> new int[3]);

            int capacidadeSetor = (lotacaoEstadio != null) ? lotacaoEstadio[idxCapacidade] : 0;
            int disponivel = capacidadeSetor - vendasDoJogo[idxSetor];

            if (qtd > disponivel) {
                JOptionPane.showMessageDialog(this, "Lugares insuficientes no setor selecionado.\nDisponíveis: " + disponivel, "Stock Esgotado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            vendasDoJogo[idxSetor] += qtd;
            salvarBilhetes();
            atualizarUI();

            int totalPago = precoAtualUnitario * qtd;
            JOptionPane.showMessageDialog(this, "RESUMO DA COMPRA:\nJogo: " + jogo.partida + "\nSetor: " + cbSetor.getSelectedItem() + "\nQuantidade: " + qtd + " bilhete(s)\nTOTAL PAGO: " + totalPago + "€", "Transação Concluída", JOptionPane.INFORMATION_MESSAGE);
        });

        add(painelEsquerdo);
        add(painelDireito);
    }

    private void atualizarUI() {
        JogoSimples jogo = (JogoSimples) cbJogos.getSelectedItem();
        if (jogo == null) {
            lblEstadio.setText("Estádio: (selecione um jogo)");
            for (JProgressBar pb : barras) { pb.setValue(0); pb.setMaximum(100); pb.setString("-"); }
            btnEmitir.setEnabled(false);
            return;
        }

        lblEstadio.setText("Estádio do jogo: " + jogo.estadio);
        int[] lotacaoEstadio = estadiosGlobais.getOrDefault(jogo.estadio, new int[]{1000, 1000, 200, 2200});
        int[] vendasDoJogo = bilhetesVendidos.computeIfAbsent(jogo.id, k -> new int[3]);

        int[] mapCaps = { lotacaoEstadio[0], lotacaoEstadio[2], lotacaoEstadio[1] };

        for (int i = 0; i < 3; i++) {
            JProgressBar pb = barras[i];
            int max = mapCaps[i];
            int vendidos = vendasDoJogo[i];
            int disponivel = max - vendidos;

            pb.setMaximum(max);
            pb.setValue(vendidos);

            if (disponivel <= 0) {
                pb.setString(nomesSetores[i] + ": ESGOTADO");
                pb.setForeground(Color.RED);
            } else {
                pb.setString(nomesSetores[i] + ": " + vendidos + " / " + max);
                pb.setForeground(disponivel < max * 0.15 ? new Color(200, 120, 0) : azulBrasil);
            }
        }

        String fase = (String) cbGrupo.getSelectedItem();
        int idxSetor = cbSetor.getSelectedIndex();
        int base = (idxSetor == 1) ? 150 : (idxSetor == 2) ? 30 : 50;
        double mult = ("Meia-Final".equals(fase)) ? 2.0 : ("Final".equals(fase)) ? 3.0 : 1.0;
        precoAtualUnitario = (int) Math.round(base * mult);

        int qtd = (Integer) spQuantidade.getValue();
        int dispAtual = mapCaps[idxSetor] - vendasDoJogo[idxSetor];

        if (dispAtual <= 0) {
            btnEmitir.setEnabled(false); btnEmitir.setText("ESGOTADO"); btnEmitir.setBackground(Color.GRAY);
        } else if (qtd > dispAtual) {
            btnEmitir.setEnabled(false); btnEmitir.setText("Só restam " + dispAtual); btnEmitir.setBackground(new Color(200, 120, 0));
        } else {
            btnEmitir.setEnabled(true); btnEmitir.setText("EMITIR BILHETE"); btnEmitir.setBackground(verdeBrasil);
        }

        lblPreco.setText("Preço Unitário: " + precoAtualUnitario + "€  |  TOTAL: " + (precoAtualUnitario * qtd) + "€");
    }

    // ==========================================
    // LÓGICA DE PERSISTÊNCIA EM FICHEIROS BINÁRIOS
    // ==========================================
    @SuppressWarnings("unchecked")
    private void carregarDados() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_BILHETES))) {
            bilhetesVendidos = (Map<String, int[]>) ois.readObject();
        } catch (Exception e) { bilhetesVendidos = new HashMap<>(); }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_MUNDIAL))) {
            Object dbObj = ois.readObject();
            estadiosGlobais = (Map<String, int[]>) dbObj.getClass().getField("estadios").get(dbObj);
        } catch (Exception e) { System.out.println("Aviso: Ficheiro mundial_dados.dat não encontrado."); }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_MATCHCENTER))) {
            Object bdMatch = ois.readObject();
            List<?> jogosCarregados = (List<?>) bdMatch.getClass().getDeclaredField("jogosRegistados").get(bdMatch);
            for (Object jObj : jogosCarregados) {
                String id = (String) jObj.getClass().getDeclaredField("id").get(jObj);
                int grp = (int) jObj.getClass().getDeclaredField("grupo").get(jObj);
                String eq1 = (String) jObj.getClass().getDeclaredField("eq1").get(jObj);
                String eq2 = (String) jObj.getClass().getDeclaredField("eq2").get(jObj);
                String est = (String) jObj.getClass().getDeclaredField("estadio").get(jObj);
                todosJogos.add(new JogoSimples(id, grp, eq1 + " vs " + eq2, est));
            }
        } catch (Exception e) { System.out.println("Aviso: Ficheiro matchcenter_dados.dat não encontrado."); }
    }

    private void salvarBilhetes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_BILHETES))) {
            oos.writeObject(bilhetesVendidos);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setPaint(new GradientPaint(0, 0, new Color(180, 230, 50), getWidth(), getHeight(), new Color(0, 156, 59)));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    public static void abrirJanela() {
        JFrame frame = new JFrame("Módulo de Acessos - Mundial 2026");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 500); frame.setLocationRelativeTo(null);
        frame.add(new MenuAcessos()); frame.setVisible(true);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(MenuAcessos::abrirJanela);
    }
}