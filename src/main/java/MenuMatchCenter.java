import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MenuMatchCenter extends JFrame {

    // --- Paleta de Cores ---
    private final Color DARK_GREEN    = new Color(20, 70, 35);
    private final Color MEDIUM_GREEN  = new Color(0, 110, 50);
    private final Color ACCENT_YELLOW = new Color(255, 204, 0);
    private final Color PANEL_WHITE   = Color.WHITE;
    private final Color TEXT_DARK     = new Color(30, 40, 30);
    private final Color TABLE_GRID    = new Color(210, 225, 210);
    private final Color TABLE_ROW_ALT = new Color(242, 250, 242);

    // --- Fontes ---
    private final Font MAIN_FONT  = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font BOLD_FONT  = new Font("Segoe UI", Font.BOLD,  13);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD,  15);
    private final Font NAV_FONT   = new Font("Segoe UI", Font.BOLD,  13);

    // --- DADOS DO DOMÍNIO (Persistência) ---
    private MatchCenterBD bd;
    private final String BD_FILE = "matchcenter_dados.dat";

    // MODELOS DE TABELAS
    private final DefaultTableModel[] modelosCalendario   = new DefaultTableModel[8];
    private final DefaultTableModel[] modelosClassificacao = new DefaultTableModel[8];
    private DefaultTableModel modeloKnockout;
    private DefaultTableModel modeloArbitros;
    private DefaultTableModel modeloAlocacoes;

    private JLabel lblValGolos, lblValAmarelos, lblValVermelhos, lblValEspectadores;
    private JPanel sidebarPanel, contentArea;
    private CardLayout cardLayout;
    private JButton[] navButtons;
    private int[] navTargets;
    private final boolean isAdmin;

    // =========================================================================
    // CLASSES DE DADOS (Serializable para guardar ficheiro binário)
    // =========================================================================
    public static class MatchCenterBD implements Serializable {
        private static final long serialVersionUID = 1L;
        List<List<String>> grupos = new ArrayList<>();
        List<Arbitro> arbitros = new ArrayList<>();
        List<Jogo> jogosRegistados = new ArrayList<>();
        LinkedHashMap<String, Resultado> resultados = new LinkedHashMap<>();
        LinkedHashMap<String, String> alocacoes = new LinkedHashMap<>();
        List<String> equipasArbitragem = new ArrayList<>();

        public MatchCenterBD() {
            String[][] defaults = {
                    {"Brasil", "Croácia", "México", "Camarões"},
                    {"Espanha", "Países Baixos", "Chile", "Austrália"},
                    {"Colômbia", "Grécia", "Costa do Marfim", "Japão"},
                    {"Uruguai", "Costa Rica", "Inglaterra", "Itália"},
                    {"Suíça", "Equador", "França", "Honduras"},
                    {"Argentina", "Bósnia e Herzegovina", "Irão", "Nigéria"},
                    {"Alemanha", "Portugal", "Gana", "Estados Unidos"},
                    {"Bélgica", "Argélia", "Rússia", "Coreia do Sul"}
            };
            for (String[] g : defaults) grupos.add(new ArrayList<>(Arrays.asList(g)));
        }
    }

    private static class Arbitro implements Serializable {
        private static final long serialVersionUID = 1L;
        final String nome, funcao, paisNascenca, equipa;
        Arbitro(String n, String f, String pn, String eq) { nome = n; funcao = f; paisNascenca = pn; equipa = eq; }
    }

    private static class Jogo implements Serializable {
        private static final long serialVersionUID = 1L;
        final String id = UUID.randomUUID().toString();
        final int grupo; final String eq1, eq2, data, hora, estadio;
        Jogo(int g, String e1, String e2, String d, String h, String est) {
            grupo = g; eq1 = e1; eq2 = e2; data = d; hora = h; estadio = est;
        }
        String getPartida() { return eq1 + " vs " + eq2; }
    }

    private static class Resultado implements Serializable {
        private static final long serialVersionUID = 1L;
        final int grupo; final String eq1, eq2; final int g1, g2, amarelos, vermelhos, espectadores;
        Resultado(int gr, String a, String b, int x, int y, int am, int ve, int esp) {
            grupo = gr; eq1 = a; eq2 = b; g1 = x; g2 = y; amarelos = am; vermelhos = ve; espectadores = esp;
        }
    }

    // =========================================================================
    // CONSTRUTOR E PERSISTÊNCIA
    // =========================================================================
    public MenuMatchCenter() { this(true); }

    public MenuMatchCenter(boolean isAdmin) {
        this.isAdmin = isAdmin;
        carregarDados();
        inicializarModelosCalendario();

        setTitle("FIFA World Cup Match Center" + (isAdmin ? "" : "  —  Modo Consulta (Adepto)"));
        setSize(1400, 960);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salvarDados(); }
        });

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);
        root.add(buildTitleAndNav(), BorderLayout.NORTH);
        root.add(buildContentPanel(), BorderLayout.CENTER);

        preencherTabelasIniciais();
        recomputarTudo();
        selectSection(isAdmin ? 0 : 2);
    }

    private void carregarDados() {
        File file = new File(BD_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                bd = (MatchCenterBD) ois.readObject();
                return;
            } catch (Exception e) { e.printStackTrace(); }
        }
        bd = new MatchCenterBD();
    }

    private void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BD_FILE))) {
            oos.writeObject(bd);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void inicializarModelosCalendario() {
        String[] calCols = {"Partida", "Data", "Hora", "Estádio"};
        for (int i = 0; i < 8; i++) {
            modelosCalendario[i] = new DefaultTableModel(calCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; } // REQ: Inalterável
            };
        }
    }

    private void preencherTabelasIniciais() {
        for (Jogo j : bd.jogosRegistados) {
            if (j.grupo >= 0 && j.grupo < 8) {
                modelosCalendario[j.grupo].addRow(new Object[]{j.getPartida(), j.data, j.hora, j.estadio});
            }
        }
    }

    // =========================================================================
    // RECÁLCULO AUTOMÁTICO
    // =========================================================================
    private void recomputarTudo() {
        recomputarClassificacoes();
        recomputarEstatisticas();
        atualizarMataMata();
    }

    private void recomputarClassificacoes() {
        for (int gi = 0; gi < 8; gi++) {
            if (modelosClassificacao[gi] == null) continue;
            List<String> teams = bd.grupos.get(gi);
            LinkedHashMap<String, int[]> stats = new LinkedHashMap<>();
            for (String t : teams) stats.put(t, new int[7]);

            for (Resultado r : bd.resultados.values()) {
                if (r.grupo != gi) continue;
                int[] a = stats.get(r.eq1), b = stats.get(r.eq2);
                if (a == null || b == null) continue;
                a[0]++; b[0]++;
                a[5] += r.g1; a[6] += r.g2;
                b[5] += r.g2; b[6] += r.g1;
                if (r.g1 > r.g2)      { a[1] += 3; a[2]++; b[4]++; }
                else if (r.g1 < r.g2) { b[1] += 3; b[2]++; a[4]++; }
                else                  { a[1]++; b[1]++; a[3]++; b[3]++; }
            }

            List<Map.Entry<String, int[]>> list = new ArrayList<>(stats.entrySet());
            list.sort((e1, e2) -> {
                int[] s1 = e1.getValue(), s2 = e2.getValue();
                if (s2[1] != s1[1]) return s2[1] - s1[1];
                int dg1 = s1[5] - s1[6], dg2 = s2[5] - s2[6];
                if (dg2 != dg1) return dg2 - dg1;
                return s2[5] - s1[5];
            });

            DefaultTableModel m = modelosClassificacao[gi];
            m.setRowCount(0);
            int pos = 1;
            for (Map.Entry<String, int[]> e : list) {
                int[] s = e.getValue();
                m.addRow(new Object[]{pos++, e.getKey(), s[0], s[1], s[2], s[3], s[4], s[5], s[6], (s[5]-s[6] > 0 ? "+" : "") + (s[5]-s[6])});
            }
        }
    }

    private void recomputarEstatisticas() {
        int golos = 0, am = 0, ve = 0, esp = 0, n = 0;
        for (Resultado r : bd.resultados.values()) {
            golos += r.g1 + r.g2; am += r.amarelos; ve += r.vermelhos; esp += r.espectadores; n++;
        }
        if (lblValGolos != null)        lblValGolos.setText(String.valueOf(golos));
        if (lblValAmarelos != null)     lblValAmarelos.setText(String.valueOf(am));
        if (lblValVermelhos != null)    lblValVermelhos.setText(String.valueOf(ve));
        if (lblValEspectadores != null) lblValEspectadores.setText(n > 0 ? String.valueOf(esp / n) : "0");
    }

    private void atualizarMataMata() {
        if (modeloKnockout == null) return;
        int[] grpPrim = {2, 0, 1, 3, 4, 6, 5, 7};
        int[] grpSeg  = {3, 1, 0, 2, 5, 7, 4, 6};
        for (int r = 0; r < modeloKnockout.getRowCount() && r < 8; r++) {
            modeloKnockout.setValueAt(posDoGrupo(grpPrim[r], 0), r, 1);
            modeloKnockout.setValueAt(posDoGrupo(grpSeg[r], 1),  r, 3);
        }
    }

    private String posDoGrupo(int gi, int pos) {
        DefaultTableModel m = modelosClassificacao[gi];
        if (m != null && m.getRowCount() > pos) return String.valueOf(m.getValueAt(pos, 1));
        return (pos == 0 ? "1.º " : "2.º ") + (char) ('A' + gi);
    }

    // =========================================================================
    // BARRA DE TÍTULO + NAVEGAÇÃO HORIZONTAL E FUNDO
    // =========================================================================
    private JPanel buildTitleAndNav() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)); wrapper.setOpaque(false);
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14)); titleBar.setOpaque(false);
        JLabel lbl = new JLabel("MATCH CENTER"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 26)); lbl.setForeground(DARK_GREEN);
        titleBar.add(lbl); wrapper.add(titleBar, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)); nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(0, 14, 6, 14));

        String[] labels = isAdmin ? new String[]{"Torneio e Jogos", "Arbitragem", "Quadros e Estatisticas"} : new String[]{"Calendário e Classificações"};
        navTargets = isAdmin ? new int[]{0, 1, 2} : new int[]{2};
        navButtons = new JButton[labels.length];

        for (int i = 0; i < labels.length; i++) {
            final int target = navTargets[i];
            JButton btn = buildNavButton(labels[i]);
            btn.addActionListener(e -> selectSection(target));
            navButtons[i] = btn; nav.add(btn);
        }
        wrapper.add(nav, BorderLayout.SOUTH); return wrapper;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (Boolean.TRUE.equals(getClientProperty("selected"))) g2.setColor(new Color(20, 70, 35, 230));
                else if (getModel().isRollover()) g2.setColor(new Color(0, 100, 40, 180));
                else g2.setColor(new Color(0, 80, 30, 120));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(NAV_FONT); btn.setForeground(Color.WHITE); btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 44)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    private void selectSection(int idx) {
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].putClientProperty("selected", navTargets[i] == idx); navButtons[i].repaint();
        }
        cardLayout.show(contentArea, "section" + idx);
    }

    private JPanel buildContentPanel() {
        cardLayout  = new CardLayout(); contentArea = new JPanel(cardLayout); contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(10, 0, 14, 14));
        contentArea.add(buildSection1(), "section0"); contentArea.add(buildSection2(), "section1"); contentArea.add(buildSection3(), "section2");
        return contentArea;
    }

    private class GradientPanel extends JPanel {
        GradientPanel() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, new Color(200, 230, 10), getWidth(), getHeight(), new Color(8, 140, 45)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 252, 235, 205)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        p.setOpaque(false); return p;
    }

    // =========================================================================
    // SECÇÃO 1 — Torneio e Jogos
    // =========================================================================
    private JPanel buildSection1() {
        JPanel outer = new JPanel(new BorderLayout(14, 0)); outer.setOpaque(false);
        JPanel card = createCard(); card.setLayout(new BorderLayout(10, 10));
        card.setBorder(titledCardBorder("1. Torneio, Selecoes e Jogos"));

        JButton btnEstrutura = makeButton("Estrutura do Torneio", new Color(20, 50, 120), Color.WHITE);
        btnEstrutura.addActionListener(e -> abrirDialogoEstrutura());
        JPanel topPanel = new JPanel(new BorderLayout()); topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(8, 10, 4, 10)); topPanel.add(btnEstrutura, BorderLayout.CENTER);
        card.add(topPanel, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new BorderLayout(12, 0)); formCard.setOpaque(false);
        formCard.setBorder(subBorder("Registar Jogo (Data, Hora e Estádio)"));

        JPanel colLabels = new JPanel(new GridLayout(6, 1, 0, 10)); colLabels.setOpaque(false);
        JLabel[] labels = { label("Grupo / Fase:"), label("Equipas (1 vs 2):"), label("Data (DD/MM/AAAA):"), label("Hora (HH:MM):"), label("Estádio:"), new JLabel() };
        for (JLabel l : labels) { l.setHorizontalAlignment(SwingConstants.RIGHT); colLabels.add(l); }

        JPanel colControls = new JPanel(new GridLayout(6, 1, 0, 10)); colControls.setOpaque(false);

        String[] fases = { "Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H", "Oitavos","Quartos","Meia-Final","Final" };
        JComboBox<String> cbFase = styledCombo(fases);

        JComboBox<String> cbEq1 = styledCombo(new String[]{});
        JComboBox<String> cbEq2 = styledCombo(new String[]{});
        JPanel pnlEquipas = new JPanel(new GridLayout(1, 2, 10, 0)); pnlEquipas.setOpaque(false);
        pnlEquipas.add(cbEq1); pnlEquipas.add(cbEq2);

        Runnable refreshEquipas = () -> {
            int idx = cbFase.getSelectedIndex();
            cbEq1.removeAllItems(); cbEq2.removeAllItems();
            if (idx < 8) {
                for (String t : bd.grupos.get(idx)) { cbEq1.addItem(t); cbEq2.addItem(t); }
            } else {
                cbEq1.addItem("A Definir"); cbEq2.addItem("A Definir");
            }
        };
        cbFase.addActionListener(e -> refreshEquipas.run()); refreshEquipas.run();

        JTextField txtData = styledField(); txtData.setToolTipText("DD/MM/AAAA");
        JTextField txtHora = styledField(); txtHora.setToolTipText("HH:MM");

        // REQ: Restrição e Atualização Dinâmica de Estádios do GestorDados
        JComboBox<String> cbEstadio = styledCombo(new String[0]);
        // Preenche na inicialização
        for (String est : GestorDados.getInstance().estadios.keySet()) cbEstadio.addItem(est);
        // Atualiza dinamicamente sempre que o menu abrir
        cbEstadio.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                String selected = (String) cbEstadio.getSelectedItem();
                cbEstadio.removeAllItems();
                for (String est : GestorDados.getInstance().estadios.keySet()) cbEstadio.addItem(est);
                if (selected != null) cbEstadio.setSelectedItem(selected);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JButton btnReg = makeButton("Registar Jogo", ACCENT_YELLOW, DARK_GREEN);
        btnReg.addActionListener(e -> {
            int idxFase = cbFase.getSelectedIndex();
            String eq1 = (String) cbEq1.getSelectedItem();
            String eq2 = (String) cbEq2.getSelectedItem();
            String data = txtData.getText().trim();
            String hora = txtHora.getText().trim();
            String estadio = (String) cbEstadio.getSelectedItem();

            if (eq1 == null || eq2 == null || data.isEmpty() || hora.isEmpty() || estadio == null) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Atenção", JOptionPane.WARNING_MESSAGE); return;
            }
            if (eq1.equals(eq2) && !eq1.equals("A Definir")) {
                JOptionPane.showMessageDialog(this, "Uma equipa não pode jogar contra si mesma.", "Atenção", JOptionPane.WARNING_MESSAGE); return;
            }
            // Data: DD/MM/AAAA (ano >= 1930)
            if (!data.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(19[3-9][0-9]|[2-9][0-9]{3})$")) {
                JOptionPane.showMessageDialog(this,
                        "Data inválida! Use DD/MM/AAAA e ano >= 1930.",
                        "Erro Regex",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

// Hora: HH:MM (00:00 até 23:59)
            if (!hora.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
                JOptionPane.showMessageDialog(this,
                        "Hora inválida! Use HH:MM.",
                        "Erro Regex",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (idxFase < 8) {
                boolean jaJogaram = bd.jogosRegistados.stream().anyMatch(j ->
                        (j.eq1.equals(eq1) && j.eq2.equals(eq2)) || (j.eq1.equals(eq2) && j.eq2.equals(eq1))
                );
                if (jaJogaram) { JOptionPane.showMessageDialog(this, "Este confronto direto já foi registado na fase de grupos.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }

                long contEq1 = bd.jogosRegistados.stream().filter(j -> j.grupo == idxFase && (j.eq1.equals(eq1) || j.eq2.equals(eq1))).count();
                long contEq2 = bd.jogosRegistados.stream().filter(j -> j.grupo == idxFase && (j.eq1.equals(eq2) || j.eq2.equals(eq2))).count();
                if (contEq1 >= 3 || contEq2 >= 3) {
                    JOptionPane.showMessageDialog(this, "Pelo menos uma destas equipas já atingiu o limite de 3 jogos na fase de grupos.", "Limite Atingido", JOptionPane.WARNING_MESSAGE); return;
                }
            }

            int grupoIdx = (idxFase < 8) ? idxFase : -1;
            Jogo novoJogo = new Jogo(grupoIdx, eq1, eq2, data, hora, estadio);
            bd.jogosRegistados.add(novoJogo);
            salvarDados();

            if (idxFase < 8) {
                modelosCalendario[idxFase].addRow(new Object[]{novoJogo.getPartida(), data, hora, estadio});
            }
            JOptionPane.showMessageDialog(this, "Jogo registado e guardado com sucesso!");
            txtData.setText(""); txtHora.setText("");
        });

        colControls.add(cbFase); colControls.add(pnlEquipas);
        colControls.add(txtData); colControls.add(txtHora);
        colControls.add(cbEstadio); colControls.add(btnReg);

        formCard.add(colLabels, BorderLayout.WEST); formCard.add(colControls, BorderLayout.CENTER);
        JPanel centerWrapper = new JPanel(new BorderLayout()); centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(4, 10, 10, 10)); centerWrapper.add(formCard, BorderLayout.CENTER);
        card.add(centerWrapper, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER); return outer;
    }

    // =========================================================================
    // SECÇÃO 2 — Arbitragem (REQ: Remoção, Quarto Árbitro, Validação Existência)
    // =========================================================================
    private JPanel buildSection2() {
        JPanel outer = new JPanel(new BorderLayout(0, 0)); outer.setOpaque(false);
        JPanel card = createCard(); card.setLayout(new BorderLayout(10, 14));
        card.setBorder(titledCardBorder("2. Gestão de Arbitragem"));

        DefaultComboBoxModel<String> modelEquipas = new DefaultComboBoxModel<>();
        for(String eq : bd.equipasArbitragem) modelEquipas.addElement(eq);

        JPanel pnlReg = new JPanel(new GridLayout(4, 2, 8, 8)); pnlReg.setOpaque(false);
        pnlReg.setBorder(subBorder("Registar Novo Árbitro"));

        JTextField txtNome = styledField();

        // REQ: Substituição de funções (VAR removido, Quarto Árbitro inserido)
        JComboBox<String> cbFuncao = styledCombo(new String[]{"Árbitro Principal","Assistente 1","Assistente 2","Quarto Árbitro"});

        // REQ: Validação de Existência (Só lê países reais criados no GestorDados)
        JComboBox<String> cbPaisNascenca = styledCombo(GestorDados.getInstance().selecoes.toArray(new String[0]));
        cbPaisNascenca.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                String selected = (String) cbPaisNascenca.getSelectedItem();
                cbPaisNascenca.removeAllItems();
                for (String s : GestorDados.getInstance().selecoes) cbPaisNascenca.addItem(s);
                if (selected != null) cbPaisNascenca.setSelectedItem(selected);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JPanel pnlFN = new JPanel(new GridLayout(1, 2, 6, 0)); pnlFN.setOpaque(false);
        pnlFN.add(cbFuncao); pnlFN.add(cbPaisNascenca);

        JComboBox<String> cbEquipa = new JComboBox<>(modelEquipas);
        cbEquipa.setFont(MAIN_FONT); cbEquipa.setBackground(PANEL_WHITE);

        JButton btnNova = makeButton("+", ACCENT_YELLOW, DARK_GREEN);
        btnNova.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da Nova Equipa:");
            if (nome != null && !nome.isBlank()) {
                bd.equipasArbitragem.add(nome.trim());
                modelEquipas.addElement(nome.trim()); cbEquipa.setSelectedItem(nome.trim());
                salvarDados();
            }
        });
        JPanel pnlEqWrap = new JPanel(new BorderLayout(6, 0)); pnlEqWrap.setOpaque(false);
        pnlEqWrap.add(cbEquipa, BorderLayout.CENTER); pnlEqWrap.add(btnNova, BorderLayout.EAST);

        JButton btnRegArb = makeButton("Registar Árbitro", PANEL_WHITE, DARK_GREEN);
        btnRegArb.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String pais = (String) cbPaisNascenca.getSelectedItem();
            String func = (String) cbFuncao.getSelectedItem();
            String equipa = (String) cbEquipa.getSelectedItem();
            if (nome.isEmpty() || equipa == null) { JOptionPane.showMessageDialog(this, "Indique nome e crie uma equipa."); return; }
            Arbitro a = new Arbitro(nome, func, pais, equipa);
            bd.arbitros.add(a); salvarDados();
            modeloArbitros.addRow(new Object[]{nome, func, pais, equipa});
            txtNome.setText("");
        });

        // REQ: Funcionalidade para remover um árbitro já registado
        JButton btnRemoverArb = makeButton("Remover Selecionado", new Color(180, 30, 30), Color.WHITE);

        pnlReg.add(label("Nome:")); pnlReg.add(txtNome);
        pnlReg.add(label("Função / País Nasc.:")); pnlReg.add(pnlFN);
        pnlReg.add(label("Equipa:")); pnlReg.add(pnlEqWrap);
        pnlReg.add(btnRemoverArb); pnlReg.add(btnRegArb);

        // ---------- Alocar Equipa a Jogo ----------
        JPanel pnlAlocar = new JPanel(new GridLayout(4, 2, 8, 8)); pnlAlocar.setOpaque(false);
        pnlAlocar.setBorder(subBorder("Alocar Equipa a Jogo"));

        JComboBox<Jogo> cbJogo = new JComboBox<>();
        for(Jogo j : bd.jogosRegistados) cbJogo.addItem(j);
        cbJogo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogo) setText(((Jogo)value).getPartida());
                return this;
            }
        });

        JComboBox<String> cbEqAloc = new JComboBox<>(modelEquipas);
        cbEqAloc.setFont(MAIN_FONT); cbEqAloc.setBackground(PANEL_WHITE);

        JButton btnAlocar = makeButton("Alocar Equipa", ACCENT_YELLOW, DARK_GREEN);
        btnAlocar.addActionListener(e -> {
            Jogo j = (Jogo) cbJogo.getSelectedItem();
            String equipa = (String) cbEqAloc.getSelectedItem();
            if (j == null || equipa == null) return;

            // REQ: Um jogo só pode ter UMA equipa de arbitragem alocada
            if (bd.alocacoes.containsKey(j.id)) {
                JOptionPane.showMessageDialog(this, "ERRO: Este jogo já possui uma equipa de arbitragem alocada!", "Alocação Única", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // REQ: Elegibilidade Rígida (1 Principal, 2 Assistentes, 1 Quarto Árbitro)
            long p = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Árbitro Principal")).count();
            long a1 = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Assistente 1")).count();
            long a2 = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Assistente 2")).count();
            long q = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Quarto Árbitro")).count();

            if (p != 1 || a1 != 1 || a2 != 1 || q != 1 || (p + a1 + a2 + q) != 4) {
                JOptionPane.showMessageDialog(this, "ERRO: A equipa de arbitragem é inelegível!\nRequisito: Exatamente 1 Principal, 2 Assistentes e 1 Quarto Árbitro.", "Inelegível", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // REQ: Neutralidade
            for (Arbitro ar : bd.arbitros) {
                if (ar.equipa.equals(equipa)) {
                    if (ar.paisNascenca.equalsIgnoreCase(j.eq1) || ar.paisNascenca.equalsIgnoreCase(j.eq2)) {
                        JOptionPane.showMessageDialog(this, "BLOQUEADO (Regra de Neutralidade)!\nO árbitro " + ar.nome + " nasceu em " + ar.paisNascenca + ".\nA equipa " + equipa + " não pode apitar este jogo.", "Conflito", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            bd.alocacoes.put(j.id, equipa); salvarDados();
            modeloAlocacoes.addRow(new Object[]{j.getPartida(), equipa, j.data});
            JOptionPane.showMessageDialog(this, "Equipa alocada com sucesso!");
        });

        pnlAlocar.add(label("Partida:")); pnlAlocar.add(cbJogo);
        pnlAlocar.add(label("Equipa Arbitragem:")); pnlAlocar.add(cbEqAloc);
        pnlAlocar.add(new JLabel()); pnlAlocar.add(btnAlocar);

        JPanel forms = new JPanel(new GridLayout(1, 2, 14, 0)); forms.setOpaque(false);
        forms.add(pnlReg); forms.add(pnlAlocar);

        modeloArbitros = new DefaultTableModel(new Object[]{"Nome","Função","País Nascença","Equipa"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // REQ: Inalterável
        };
        for(Arbitro a : bd.arbitros) modeloArbitros.addRow(new Object[]{a.nome, a.funcao, a.paisNascenca, a.equipa});
        JTable tblArb = new JTable(modeloArbitros); configurarTabelaSimples(tblArb);
        JScrollPane spArb = new JScrollPane(tblArb); spArb.setBorder(subBorder("Árbitros Registados"));

        btnRemoverArb.addActionListener(e -> {
            int row = tblArb.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um árbitro na tabela para remover."); return; }
            String nomeRemover = (String) tblArb.getValueAt(row, 0);
            bd.arbitros.removeIf(a -> a.nome.equals(nomeRemover));
            modeloArbitros.removeRow(row);
            salvarDados();
            JOptionPane.showMessageDialog(this, "Árbitro removido com sucesso!");
        });

        modeloAlocacoes = new DefaultTableModel(new Object[]{"Partida","Equipa de Arbitragem","Data"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // REQ: Inalterável
        };
        for(Map.Entry<String, String> entry : bd.alocacoes.entrySet()) {
            Jogo jo = bd.jogosRegistados.stream().filter(x -> x.id.equals(entry.getKey())).findFirst().orElse(null);
            if(jo != null) modeloAlocacoes.addRow(new Object[]{jo.getPartida(), entry.getValue(), jo.data});
        }
        JTable tblAlo = new JTable(modeloAlocacoes); configurarTabelaSimples(tblAlo);
        JScrollPane spAlo = new JScrollPane(tblAlo); spAlo.setBorder(subBorder("Alocações"));

        JPanel tabelas = new JPanel(new GridLayout(1, 2, 14, 0)); tabelas.setOpaque(false);
        tabelas.add(spArb); tabelas.add(spAlo);

        card.add(forms, BorderLayout.NORTH); card.add(tabelas, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER); return outer;
    }

    // =========================================================================
    // SECÇÃO 3 E DIÁLOGO RESULTADOS
    // =========================================================================
    private JPanel buildSection3() {
        JPanel outer = new JPanel(new BorderLayout(14, 0)); outer.setOpaque(false);
        JPanel centerCard = createCard(); centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(titledCardBorder("3. Quadros Competitivos"));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setFont(BOLD_FONT); tabs.setForeground(DARK_GREEN); tabs.setOpaque(false);
        for (int i = 0; i < 8; i++) tabs.addTab("  Grupo " + (char)('A' + i) + "  ", createGroupTable(i));
        tabs.addTab("  Oitavos  ", createKnockoutPanel());
        centerCard.add(tabs, BorderLayout.CENTER);

        sidebarPanel = createGlobalStatsSidebar();
        outer.add(centerCard, BorderLayout.CENTER); outer.add(sidebarPanel, BorderLayout.EAST);
        return outer;
    }

    private void abrirDialogoResultados() {
        JDialog dlg = new JDialog(this, "Adicionar Resultados de Jogo", true);
        dlg.setSize(500, 480); dlg.setLocationRelativeTo(this); dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 15)); form.setBorder(new EmptyBorder(20, 20, 20, 20));

        long jogosGruposFeitos = bd.resultados.values().stream().filter(r -> r.grupo >= 0 && r.grupo < 8).count();
        boolean gruposCompletos = (jogosGruposFeitos >= 48);

        String[] fases = gruposCompletos ?
                new String[]{"Oitavos", "Quartos", "Meia-Final", "Final"} :
                new String[]{"Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H"};

        JComboBox<String> cbFase = styledCombo(fases);
        JComboBox<Jogo> cbPartida = new JComboBox<>();
        cbPartida.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogo) setText(((Jogo)value).getPartida());
                return this;
            }
        });

        Runnable refreshPartidas = () -> {
            cbPartida.removeAllItems();
            int offset = gruposCompletos ? 8 : 0;
            int idx = cbFase.getSelectedIndex() + offset;

            for (Jogo j : bd.jogosRegistados) {
                if (j.grupo == idx && !bd.resultados.containsKey(j.id)) {
                    cbPartida.addItem(j);
                }
            }
        };
        cbFase.addActionListener(e -> refreshPartidas.run());
        refreshPartidas.run();

        JTextField txtGolosEq1 = styledField(), txtGolosEq2 = styledField();
        JTextField txtAmarelos = styledField(), txtVermelhos = styledField(), txtEspectadores = styledField();

        form.add(label("Fase/Grupo:")); form.add(cbFase);
        form.add(label("Partida Pendente:")); form.add(cbPartida);
        form.add(label("Golos Equipa 1:")); form.add(txtGolosEq1);
        form.add(label("Golos Equipa 2:")); form.add(txtGolosEq2);
        form.add(label("Cartões Amarelos:")); form.add(txtAmarelos);
        form.add(label("Cartões Vermelhos:")); form.add(txtVermelhos);
        form.add(label("Espectadores:")); form.add(txtEspectadores);
        dlg.add(form, BorderLayout.CENTER);

        JButton btnGuardar = makeButton("Guardar Resultados", ACCENT_YELLOW, DARK_GREEN);
        btnGuardar.addActionListener(e -> {
            try {
                Jogo j = (Jogo) cbPartida.getSelectedItem();
                if (j == null) { JOptionPane.showMessageDialog(dlg, "Sem jogos pendentes nesta fase.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }

                int g1 = Integer.parseInt(txtGolosEq1.getText().trim());
                int g2 = Integer.parseInt(txtGolosEq2.getText().trim());
                int am = Integer.parseInt(txtAmarelos.getText().trim());
                int ve = Integer.parseInt(txtVermelhos.getText().trim());
                int esp = Integer.parseInt(txtEspectadores.getText().trim());

                bd.resultados.put(j.id, new Resultado(j.grupo, j.eq1, j.eq2, g1, g2, am, ve, esp));
                salvarDados();
                recomputarTudo();

                JOptionPane.showMessageDialog(dlg, "Resultado guardado e bloqueado!");
                dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Insira números válidos."); }
        });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bot.add(btnGuardar);
        dlg.add(bot, BorderLayout.SOUTH); dlg.setVisible(true);
    }

    // =========================================================================
    // DIÁLOGO ESTRUTURA DO TORNEIO (REQ: Revertido exatamente para o layout original)
    // =========================================================================
    private void abrirDialogoEstrutura() {
        JDialog dlg = new JDialog(this, "Estrutura do Torneio - Grupos", true);
        dlg.setSize(900, 600);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(PANEL_WHITE);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setBackground(PANEL_WHITE);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 5));

        String[] grupoNomes = {"Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H"};
        JComboBox<String> cbGrupoEd = styledCombo(grupoNomes);
        leftPanel.add(cbGrupoEd, BorderLayout.NORTH);

        DefaultTableModel tblModel = new DefaultTableModel(new Object[]{"#","Seleção"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // REQ: Inalterável
        };
        JTable tblEquipas = new JTable(tblModel);
        configurarTabelaSimples(tblEquipas); tblEquipas.setRowHeight(32);
        tblEquipas.getColumnModel().getColumn(0).setMaxWidth(36);

        Runnable refreshTabela = () -> {
            int gi = cbGrupoEd.getSelectedIndex();
            tblModel.setRowCount(0);
            List<String> eq = bd.grupos.get(gi);
            for (int i = 0; i < eq.size(); i++) tblModel.addRow(new Object[]{i+1, eq.get(i)});
        };
        refreshTabela.run();
        cbGrupoEd.addActionListener(e -> refreshTabela.run());

        JScrollPane spEq = new JScrollPane(tblEquipas);
        spEq.setBorder(new LineBorder(TABLE_GRID));
        leftPanel.add(spEq, BorderLayout.CENTER);

        JPanel btnRemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnRemPanel.setBackground(PANEL_WHITE);
        JButton btnRemover = makeButton("Remover Selecao", PANEL_WHITE, DARK_GREEN);
        btnRemover.addActionListener(e -> {
            int row = tblEquipas.getSelectedRow();
            int gi  = cbGrupoEd.getSelectedIndex();
            if (row < 0) { JOptionPane.showMessageDialog(dlg, "Seleccione uma equipa primeiro."); return; }
            String equipa = (String) tblModel.getValueAt(row, 1);
            bd.grupos.get(gi).remove(equipa);
            salvarDados();
            refreshTabela.run();
        });
        btnRemPanel.add(btnRemover);
        leftPanel.add(btnRemPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setBackground(PANEL_WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN), "Adicionar ao Grupo",
                TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, DARK_GREEN));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        Runnable refreshLista = () -> {
            int gi = cbGrupoEd.getSelectedIndex();
            listModel.clear();
            List<String> noGrupo = bd.grupos.get(gi);
            // REQ: Validação de Existência (Lê do GestorDados)
            for (String s : GestorDados.getInstance().selecoes) {
                if (!noGrupo.contains(s)) listModel.addElement(s);
            }
        };
        refreshLista.run();
        cbGrupoEd.addActionListener(e -> refreshLista.run());

        JList<String> jList = new JList<>(listModel);
        jList.setFont(MAIN_FONT);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spLista = new JScrollPane(jList);
        spLista.setBorder(new LineBorder(TABLE_GRID));
        rightPanel.add(spLista, BorderLayout.CENTER);

        JPanel addCustomPanel = new JPanel(new BorderLayout(6, 0));
        addCustomPanel.setBackground(PANEL_WHITE);
        addCustomPanel.setBorder(new EmptyBorder(6, 4, 4, 4));
        JTextField txtNovaSelecao = styledField();
        txtNovaSelecao.setToolTipText("Nome de nova seleção personalizada");
        JButton btnAddCustom = makeButton("+ Nova Seleção", PANEL_WHITE, DARK_GREEN);
        btnAddCustom.addActionListener(e -> {
            String nome = txtNovaSelecao.getText().trim();
            if (nome.isEmpty()) return;
            if (!GestorDados.getInstance().selecoes.contains(nome)) {
                GestorDados.getInstance().selecoes.add(nome);
                GestorDados.getInstance().salvarDados();
            }
            refreshLista.run();
            txtNovaSelecao.setText("");
        });
        addCustomPanel.add(txtNovaSelecao, BorderLayout.CENTER);
        addCustomPanel.add(btnAddCustom, BorderLayout.EAST);
        rightPanel.add(addCustomPanel, BorderLayout.SOUTH);

        JButton btnAdd = makeButton("Adicionar ao Grupo", ACCENT_YELLOW, DARK_GREEN);
        btnAdd.addActionListener(e -> {
            String sel = jList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(dlg, "Seleccione uma seleção da lista."); return; }
            int gi = cbGrupoEd.getSelectedIndex();
            if (bd.grupos.get(gi).contains(sel)) { JOptionPane.showMessageDialog(dlg, "Já está neste grupo."); return; }
            for (int g = 0; g < 8; g++) {
                if (g != gi && bd.grupos.get(g).contains(sel)) {
                    int resp = JOptionPane.showConfirmDialog(dlg,
                            sel + " está no Grupo " + (char)('A'+g) + ". Mover para " + grupoNomes[gi] + "?",
                            "Mover Seleção", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) bd.grupos.get(g).remove(sel);
                    else return;
                }
            }
            bd.grupos.get(gi).add(sel);
            salvarDados();
            refreshTabela.run();
            refreshLista.run();
        });

        JPanel rightWrapper = new JPanel(new BorderLayout(0, 8));
        rightWrapper.setBackground(PANEL_WHITE);
        rightWrapper.add(rightPanel, BorderLayout.CENTER);
        rightWrapper.add(btnAdd,    BorderLayout.SOUTH);
        rightWrapper.setBorder(new EmptyBorder(10, 5, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightWrapper);
        split.setDividerLocation(420);
        split.setBackground(PANEL_WHITE);
        dlg.add(split, BorderLayout.CENTER);

        JButton btnOk = makeButton("Guardar e Fechar", ACCENT_YELLOW, DARK_GREEN);
        btnOk.addActionListener(e -> { recomputarTudo(); dlg.dispose(); });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setBackground(PANEL_WHITE); bot.add(btnOk);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // UI Helpers (Omitidos por Limite de Carateres, são os mesmos utilitários)
    // =========================================================================
    private JPanel createGroupTable(int gi) {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        String[] cols = {"#","Seleção","PJ","PTS","V","E","D","GM","GS","DG"};
        modelosClassificacao[gi] = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // REQ: Inalterável
        };
        JTable tbl = new JTable(modelosClassificacao[gi]); configurarTabelaSimples(tbl);
        JScrollPane sp = new JScrollPane(tbl); panel.add(sp, BorderLayout.CENTER);

        JTable tblCal = new JTable(modelosCalendario[gi]); configurarTabelaSimples(tblCal);
        JScrollPane spCal = new JScrollPane(tblCal); spCal.setPreferredSize(new Dimension(0, 130));
        panel.add(spCal, BorderLayout.SOUTH); return panel;
    }

    private JPanel createKnockoutPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        modeloKnockout = new DefaultTableModel(new Object[8][5], new String[]{"Jogo","1.º","vs","2.º","Res"}) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // REQ: Inalterável
        };
        JTable tbl = new JTable(modeloKnockout); configurarTabelaSimples(tbl);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER); return p;
    }

    private JPanel createGlobalStatsSidebar() {
        JPanel card = createCard(); card.setLayout(new BorderLayout(0, 12));
        lblValGolos = new JLabel("0"); lblValAmarelos = new JLabel("0"); lblValVermelhos = new JLabel("0"); lblValEspectadores = new JLabel("0");
        JPanel grid = new JPanel(new GridLayout(4, 1)); grid.setOpaque(false);
        grid.add(lblValGolos); grid.add(lblValAmarelos); grid.add(lblValVermelhos); grid.add(lblValEspectadores);
        card.add(grid, BorderLayout.CENTER);

        JButton btnAddResultados = makeButton("Adicionar Resultados", ACCENT_YELLOW, DARK_GREEN);
        btnAddResultados.addActionListener(e -> abrirDialogoResultados());
        btnAddResultados.setVisible(isAdmin); card.add(btnAddResultados, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(260, 0)); return card;
    }

    private void configurarTabelaSimples(JTable t) {
        t.setFont(MAIN_FONT); t.setRowHeight(30); t.setGridColor(TABLE_GRID);
        JTableHeader h = t.getTableHeader(); h.setFont(BOLD_FONT); h.setBackground(new Color(200, 225, 200)); h.setForeground(DARK_GREEN);
    }
    private TitledBorder titledCardBorder(String title) { return BorderFactory.createTitledBorder(new LineBorder(DARK_GREEN, 1, true), "  " + title + "  "); }
    private TitledBorder subBorder(String title) { return BorderFactory.createTitledBorder(new LineBorder(DARK_GREEN, 1), title); }
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(fg); btn.setFocusPainted(false); return btn;
    }
    private JLabel label(String text) { JLabel l = new JLabel(text); l.setFont(BOLD_FONT); l.setForeground(DARK_GREEN); return l; }
    private JTextField styledField() { return new JTextField(); }
    private JComboBox<String> styledCombo(String[] items) { return new JComboBox<>(items); }
}