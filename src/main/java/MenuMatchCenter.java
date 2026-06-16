import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    // SELEÇÕES
    private final List<String> TODAS_SELECOES = new ArrayList<>(Arrays.asList(
            "Alemanha","Argélia","Argentina","Austrália","Bélgica","Bósnia e Herzegovina",
            "Brasil","Camarões","Chile","Colômbia","Coreia do Sul","Costa do Marfim",
            "Costa Rica","Equador","Espanha","Estados Unidos","França","Gana",
            "Grécia","Honduras","Inglaterra","Irão","Itália","Japão",
            "México","Nigéria","Países Baixos","Portugal","Rússia","Suíça","Uruguai","Croácia"
    ));

    // 8 GRUPOS (editáveis em runtime)
    private final List<List<String>> GRUPOS = new ArrayList<>();

    // MODELOS DE CALENDÁRIO (Um por grupo, atualizados quando se regista um jogo)
    private final DefaultTableModel[] modelosCalendario = new DefaultTableModel[8];

    // --- Variáveis Globais de Estatísticas ---
    private int totalGolos = 0;
    private int totalAmarelos = 0;
    private int totalVermelhos = 0;
    private int totalEspectadores = 0;
    private int totalJogosRegistados = 0;

    private JLabel lblValGolos;
    private JLabel lblValAmarelos;
    private JLabel lblValVermelhos;
    private JLabel lblValEspectadores;

    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentArea;
    private JButton[] navButtons;

    // =========================================================================
    // CONSTRUTOR
    // =========================================================================
    public MenuMatchCenter() {
        inicializarGrupos();
        inicializarModelosCalendario(); // Prepara as tabelas vazias do calendário

        setTitle("FIFA World Cup Match Center");
        setSize(1400, 960);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        root.add(buildTitleAndNav(), BorderLayout.NORTH);
        root.add(buildContentPanel(), BorderLayout.CENTER);

        selectSection(0);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (sidebarPanel != null) {
                    int w     = getWidth();
                    int sideW = Math.max(220, Math.min(320, w / 5));
                    sidebarPanel.setPreferredSize(new Dimension(sideW, 0));
                    sidebarPanel.revalidate();
                    sidebarPanel.repaint();
                }
            }
        });
    }

    private void inicializarGrupos() {
        String[][] defaults = {
                {"Brasil",    "Croácia",             "México",    "Camarões"},
                {"Espanha",   "Países Baixos",        "Chile",     "Austrália"},
                {"Colômbia",  "Grécia",               "Costa do Marfim","Japão"},
                {"Uruguai",   "Costa Rica",           "Inglaterra","Itália"},
                {"Suíça",     "Equador",              "França",    "Honduras"},
                {"Argentina", "Bósnia e Herzegovina", "Irão",      "Nigéria"},
                {"Alemanha",  "Portugal",             "Gana",      "Estados Unidos"},
                {"Bélgica",   "Argélia",              "Rússia",    "Coreia do Sul"}
        };
        for (String[] g : defaults) GRUPOS.add(new ArrayList<>(Arrays.asList(g)));
    }

    private void inicializarModelosCalendario() {
        String[] calCols = {"Partida", "Data", "Hora", "Estádio"};
        for (int i = 0; i < 8; i++) {
            modelosCalendario[i] = new DefaultTableModel(calCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
        }
    }

    // =========================================================================
    // BARRA DE TÍTULO + NAVEGAÇÃO HORIZONTAL (em cima)
    // =========================================================================
    private JPanel buildTitleAndNav() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setOpaque(false);

        // Título
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        titleBar.setOpaque(false);
        JLabel lbl = new JLabel("MATCH CENTER");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setForeground(DARK_GREEN);
        titleBar.add(lbl);
        wrapper.add(titleBar, BorderLayout.NORTH);

        // Botões de navegação em linha horizontal
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(0, 14, 6, 14));

        String[] labels = { "Torneio e Jogos", "Arbitragem", "Quadros e Estatisticas" };
        navButtons = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = buildNavButton(labels[i]);
            btn.addActionListener(e -> selectSection(idx));
            navButtons[i] = btn;
            nav.add(btn);
        }
        wrapper.add(nav, BorderLayout.SOUTH);
        return wrapper;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));
                if (selected) {
                    g2.setColor(new Color(20, 70, 35, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 100, 40, 180));
                } else {
                    g2.setColor(new Color(0, 80, 30, 120));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (selected) {
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(NAV_FONT);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void selectSection(int idx) {
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].putClientProperty("selected", i == idx);
            navButtons[i].repaint();
        }
        cardLayout.show(contentArea, "section" + idx);
    }

    // =========================================================================
    // ÁREA DE CONTEÚDO (CardLayout)
    // =========================================================================
    private JPanel buildContentPanel() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(10, 0, 14, 14));

        contentArea.add(buildSection1(), "section0");
        contentArea.add(buildSection2(), "section1");
        contentArea.add(buildSection3(), "section2");
        return contentArea;
    }

    // =========================================================================
    // FUNDO COM GRADIENTE
    // =========================================================================
    private class GradientPanel extends JPanel {
        GradientPanel() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(
                    0, 0,              new Color(200, 230, 10),
                    getWidth(), getHeight(), new Color(8, 140, 45));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 252, 235, 205));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    // =========================================================================
    // SECÇÃO 1 — Torneio e Jogos
    // =========================================================================
    private JPanel buildSection1() {
        JPanel outer = new JPanel(new BorderLayout(14, 0));
        outer.setOpaque(false);

        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(titledCardBorder("1. Torneio, Selecoes e Jogos"));

        JButton btnEstrutura = new JButton("Estrutura do Torneio") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 50, 120));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btnEstrutura.setFont(BOLD_FONT);
        btnEstrutura.setForeground(Color.WHITE);
        btnEstrutura.setOpaque(false);
        btnEstrutura.setContentAreaFilled(false);
        btnEstrutura.setBorderPainted(false);
        btnEstrutura.setFocusPainted(false);
        btnEstrutura.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEstrutura.setPreferredSize(new Dimension(0, 44));
        btnEstrutura.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnEstrutura.addActionListener(e -> abrirDialogoEstrutura());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(8, 10, 4, 10));
        topPanel.add(btnEstrutura, BorderLayout.CENTER);
        card.add(topPanel, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new BorderLayout(12, 0));
        formCard.setOpaque(false);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                subBorder("Registar Jogo"),
                new EmptyBorder(8, 6, 8, 6)
        ));

        JPanel colLabels = new JPanel(new GridLayout(6, 1, 0, 10));
        colLabels.setOpaque(false);
        JLabel[] labels = {
                label("Grupo / Fase:"), label("Partida:"), label("Data (DD/MM/AAAA):"),
                label("Hora (HH:MM):"), label("Estadio:"), new JLabel()
        };
        for (JLabel l : labels) { l.setHorizontalAlignment(SwingConstants.RIGHT); colLabels.add(l); }

        JPanel colControls = new JPanel(new GridLayout(6, 1, 0, 10));
        colControls.setOpaque(false);

        String[] fases = {
                "Grupo A","Grupo B","Grupo C","Grupo D",
                "Grupo E","Grupo F","Grupo G","Grupo H",
                "Oitavos-de-Final","Quartos-de-Final","Meia-Final","Final"
        };
        JComboBox<String> cbFase    = styledCombo(fases);
        JComboBox<String> cbPartida = new JComboBox<>();
        cbPartida.setFont(MAIN_FONT); cbPartida.setForeground(TEXT_DARK); cbPartida.setBackground(PANEL_WHITE);

        Runnable refreshPartidas = () -> {
            int idx = cbFase.getSelectedIndex();
            if (idx < 8) {
                cbPartida.setModel(new DefaultComboBoxModel<>(getJogosDoGrupo(idx)));
            } else {
                String[] ko = switch (idx) {
                    case 8  -> new String[]{"J1","J2","J3","J4","J5","J6","J7","J8"};
                    case 9  -> new String[]{"QF1","QF2","QF3","QF4"};
                    case 10 -> new String[]{"SF1","SF2"};
                    default -> new String[]{"Final"};
                };
                cbPartida.setModel(new DefaultComboBoxModel<>(ko));
            }
        };
        cbFase.addActionListener(e -> refreshPartidas.run());
        refreshPartidas.run();

        JTextField txtData    = styledField(); txtData.setToolTipText("Formato: DD/MM/AAAA");
        JTextField txtHora    = styledField(); txtHora.setToolTipText("Formato: HH:MM");
        JTextField txtEstadio = styledField(); txtEstadio.setToolTipText("Nome do Estadio");

        JButton btnReg = makeButton("Registar Jogo", ACCENT_YELLOW, DARK_GREEN);

        // --- NOVO LISTENER: Adiciona o jogo registado à tabela do Calendário correspondente ---
        btnReg.addActionListener(e -> {
            int idxFase = cbFase.getSelectedIndex();
            Object partidaObj = cbPartida.getSelectedItem();
            if (partidaObj == null) return;

            String partida = partidaObj.toString();
            String data = txtData.getText().trim();
            String hora = txtHora.getText().trim();
            String estadio = txtEstadio.getText().trim();

            if (data.isEmpty() || hora.isEmpty() || estadio.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos do jogo (Data, Hora, Estádio).", "Campos em falta", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (idxFase < 8) {
                // Adiciona a linha na tabela de calendário do grupo respetivo
                modelosCalendario[idxFase].addRow(new Object[]{partida, data, hora, estadio});
                JOptionPane.showMessageDialog(this, "Jogo registado no calendário do " + cbFase.getSelectedItem() + " com sucesso!");
            } else {
                // Jogos a eliminar não aparecem nas tabelas de grupos
                JOptionPane.showMessageDialog(this, "Jogo da fase a eliminar registado com sucesso!");
            }

            // Limpa os campos após registar
            txtData.setText("");
            txtHora.setText("");
            txtEstadio.setText("");
        });

        colControls.add(cbFase); colControls.add(cbPartida);
        colControls.add(txtData); colControls.add(txtHora);
        colControls.add(txtEstadio);
        colControls.add(btnReg);

        formCard.add(colLabels,   BorderLayout.WEST);
        formCard.add(colControls, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(4, 10, 10, 10));
        centerWrapper.add(formCard, BorderLayout.CENTER);
        card.add(centerWrapper, BorderLayout.CENTER);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // =========================================================================
    // SECÇÃO 2 — Arbitragem
    // =========================================================================
    private JPanel buildSection2() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setOpaque(false);

        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 14));
        card.setBorder(titledCardBorder("2. Gestão de Arbitragem"));

        DefaultComboBoxModel<String> modelRegisto = new DefaultComboBoxModel<>(new String[]{"Sem Equipa","Equipa A","Equipa B"});
        DefaultComboBoxModel<String> modelAlocar  = new DefaultComboBoxModel<>(new String[]{"Equipa A","Equipa B"});

        JPanel pnlReg = new JPanel(new GridLayout(4, 2, 8, 8));
        pnlReg.setOpaque(false);
        pnlReg.setBorder(subBorder("Registar Novo Árbitro"));

        JTextField txtNome = styledField();
        JComboBox<String> cbFuncao = styledCombo(new String[]{"Árbitro Principal","Assistente 1","Assistente 2","Quarto Árbitro","VAR"});
        JTextField txtNac = styledField(); txtNac.setToolTipText("Nacionalidade");
        JPanel pnlFN = new JPanel(new GridLayout(1, 2, 6, 0)); pnlFN.setOpaque(false);
        pnlFN.add(cbFuncao); pnlFN.add(txtNac);

        JComboBox<String> cbEquipa = new JComboBox<>(modelRegisto);
        cbEquipa.setFont(MAIN_FONT); cbEquipa.setForeground(TEXT_DARK); cbEquipa.setBackground(PANEL_WHITE);

        JButton btnNova = makeButton("+", ACCENT_YELLOW, DARK_GREEN);
        btnNova.setToolTipText("Criar Nova Equipa de Arbitragem");
        btnNova.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da Nova Equipa:", "Criar Equipa", JOptionPane.QUESTION_MESSAGE);
            if (nome != null && !nome.isBlank()) {
                modelRegisto.addElement(nome.trim()); modelAlocar.addElement(nome.trim());
                cbEquipa.setSelectedItem(nome.trim());
                JOptionPane.showMessageDialog(this, "Equipa '" + nome.trim() + "' criada!");
            }
        });

        JPanel pnlEqWrap = new JPanel(new BorderLayout(6, 0)); pnlEqWrap.setOpaque(false);
        pnlEqWrap.add(cbEquipa, BorderLayout.CENTER); pnlEqWrap.add(btnNova, BorderLayout.EAST);

        pnlReg.add(label("Nome:"));          pnlReg.add(txtNome);
        pnlReg.add(label("Função / Nac.:")); pnlReg.add(pnlFN);
        pnlReg.add(label("Equipa:"));        pnlReg.add(pnlEqWrap);
        pnlReg.add(new JLabel());            pnlReg.add(makeButton("Registar Árbitro", PANEL_WHITE, DARK_GREEN));

        JPanel pnlAlocar = new JPanel(new GridLayout(3, 2, 8, 8));
        pnlAlocar.setOpaque(false);
        pnlAlocar.setBorder(subBorder("Alocar Equipa a Jogo"));

        List<String> todosJogos = new ArrayList<>();
        for (int g = 0; g < 8; g++) for (String j : getJogosDoGrupo(g)) todosJogos.add(j);
        JComboBox<String> cbJogo   = styledCombo(todosJogos.toArray(new String[0]));
        JComboBox<String> cbEqAloc = new JComboBox<>(modelAlocar);
        cbEqAloc.setFont(MAIN_FONT); cbEqAloc.setForeground(TEXT_DARK); cbEqAloc.setBackground(PANEL_WHITE);

        pnlAlocar.add(label("Partida:"));           pnlAlocar.add(cbJogo);
        pnlAlocar.add(label("Equipa Arbitragem:")); pnlAlocar.add(cbEqAloc);
        pnlAlocar.add(new JLabel());                pnlAlocar.add(makeButton("Alocar Equipa", ACCENT_YELLOW, DARK_GREEN));

        card.add(pnlReg,    BorderLayout.NORTH);
        card.add(pnlAlocar, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // =========================================================================
    // SECÇÃO 3 — Quadros + Estatísticas
    // =========================================================================
    private JPanel buildSection3() {
        JPanel outer = new JPanel(new BorderLayout(14, 0));
        outer.setOpaque(false);

        JPanel centerCard = createCard();
        centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(titledCardBorder("3. Quadros Competitivos - 8 Grupos"));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setFont(BOLD_FONT); tabs.setForeground(DARK_GREEN);
        tabs.setOpaque(false);

        for (int i = 0; i < 8; i++)
            tabs.addTab("  Grupo " + (char)('A' + i) + "  ", createGroupTable(i));
        tabs.addTab("  Oitavos  ", createKnockoutPanel());
        centerCard.add(tabs, BorderLayout.CENTER);

        sidebarPanel = createGlobalStatsSidebar();

        outer.add(centerCard,   BorderLayout.CENTER);
        outer.add(sidebarPanel, BorderLayout.EAST);
        return outer;
    }

    private JPanel createGroupTable(int gi) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lbl = new JLabel("  Grupo " + (char)('A' + gi) + "  -  Fase de Grupos", SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(DARK_GREEN);
        panel.add(lbl, BorderLayout.NORTH);

        // --- Tabela da Classificação (Metade Superior) ---
        String[] cols = {"#","Seleção","PJ","PTS","V","E","D","GM","GS","DG"};
        List<String> teams = GRUPOS.get(gi);
        Object[][] data = new Object[Math.min(teams.size(), 4)][10];
        for (int i = 0; i < Math.min(teams.size(), 4); i++)
            data[i] = new Object[]{i+1, teams.get(i),0,0,0,0,0,0,0,"0"};

        DefaultTableModel mdl = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(mdl);
        tbl.setFont(MAIN_FONT); tbl.setRowHeight(38);
        tbl.setFillsViewportHeight(false); tbl.setShowVerticalLines(false);
        tbl.setGridColor(TABLE_GRID); tbl.setForeground(TEXT_DARK);
        tbl.setBackground(new Color(242, 255, 242));
        tbl.setSelectionBackground(new Color(210, 240, 210));
        tbl.setSelectionForeground(DARK_GREEN);
        tbl.getColumnModel().getColumn(0).setMaxWidth(32);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(160);

        JTableHeader hdr = tbl.getTableHeader();
        hdr.setFont(BOLD_FONT);
        hdr.setBackground(new Color(190, 225, 190));
        hdr.setForeground(DARK_GREEN);
        hdr.setOpaque(true);
        hdr.setPreferredSize(new Dimension(0, 36));

        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? new Color(245,255,245) : TABLE_ROW_ALT);
                setFont(col == 3 ? BOLD_FONT : MAIN_FONT);
                setHorizontalAlignment(col == 1 ? JLabel.LEFT : JLabel.CENTER);
                c.setForeground(col == 3 ? MEDIUM_GREEN : TEXT_DARK);
                return c;
            }
        });

        JPanel tblWrapper = new JPanel(new BorderLayout());
        tblWrapper.setOpaque(false);
        tblWrapper.add(tbl.getTableHeader(), BorderLayout.NORTH);
        tblWrapper.add(tbl, BorderLayout.CENTER);
        panel.add(tblWrapper, BorderLayout.CENTER);

        // --- Tabela do Calendário (Metade Inferior) ---
        JPanel pnlCalendario = new JPanel(new BorderLayout(0, 4));
        pnlCalendario.setOpaque(false);
        pnlCalendario.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblCal = new JLabel("Calendário de Jogos Registados", SwingConstants.LEFT);
        lblCal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCal.setForeground(MEDIUM_GREEN);
        pnlCalendario.add(lblCal, BorderLayout.NORTH);

        JTable tblCal = new JTable(modelosCalendario[gi]);
        configurarTabelaSimples(tblCal);
        tblCal.setRowHeight(28);
        tblCal.getColumnModel().getColumn(0).setPreferredWidth(200); // Dar mais espaço à coluna da "Partida"

        JScrollPane spCal = new JScrollPane(tblCal);
        spCal.setPreferredSize(new Dimension(0, 130)); // Limite de altura
        spCal.setBorder(new LineBorder(TABLE_GRID));
        spCal.getViewport().setBackground(PANEL_WHITE);

        pnlCalendario.add(spCal, BorderLayout.CENTER);
        panel.add(pnlCalendario, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createKnockoutPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Avanço automático após conclusão da Fase de Grupos", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lbl.setForeground(MEDIUM_GREEN);
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Jogo","1.º do Grupo","vs","2.º do Grupo","Resultado"};
        Object[][] data = {
                {"1/8 - J1","1.º C","vs","2.º D","—"}, {"1/8 - J2","1.º A","vs","2.º B","—"},
                {"1/8 - J3","1.º B","vs","2.º A","—"}, {"1/8 - J4","1.º D","vs","2.º C","—"},
                {"1/8 - J5","1.º E","vs","2.º F","—"}, {"1/8 - J6","1.º G","vs","2.º H","—"},
                {"1/8 - J7","1.º F","vs","2.º E","—"}, {"1/8 - J8","1.º H","vs","2.º G","—"}
        };
        DefaultTableModel mdl = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(mdl);
        configurarTabelaSimples(tbl); tbl.setRowHeight(38);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.setBorder(new LineBorder(TABLE_GRID));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    // SIDEBAR ESTATÍSTICAS
    // =========================================================================
    private JPanel createGlobalStatsSidebar() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(titledCardBorder("4. Estatísticas"));

        lblValGolos = new JLabel("0", SwingConstants.RIGHT);
        lblValAmarelos = new JLabel("0", SwingConstants.RIGHT);
        lblValVermelhos = new JLabel("0", SwingConstants.RIGHT);
        lblValEspectadores = new JLabel("0", SwingConstants.RIGHT);

        lblValGolos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValAmarelos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValVermelhos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValEspectadores.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel grid = new JPanel(new GridLayout(4, 1, 0, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 6, 8, 6));
        grid.add(createStatCard("Total de Golos",     lblValGolos, new Color(0, 120, 60)));
        grid.add(createStatCard("Cartões Amarelos",   lblValAmarelos, new Color(180, 140, 0)));
        grid.add(createStatCard("Cartões Vermelhos",  lblValVermelhos, new Color(180, 30, 30)));
        grid.add(createStatCard("Média Espectadores", lblValEspectadores, new Color(50, 80, 160)));
        card.add(grid, BorderLayout.CENTER);

        JPanel south = new JPanel(new GridLayout(1, 1, 0, 8));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(0, 6, 6, 6));

        JButton btnAddResultados = makeButton("Adicionar Resultados", ACCENT_YELLOW, DARK_GREEN);
        btnAddResultados.addActionListener(e -> abrirDialogoResultados());
        south.add(btnAddResultados);

        card.add(south, BorderLayout.SOUTH);

        card.setPreferredSize(new Dimension(260, 0));
        return card;
    }

    private JPanel createStatCard(String title, JLabel lblV, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 195));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRect(0, 0, 5, getHeight());
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 200, 160), 1, true),
                new EmptyBorder(8, 14, 8, 10)));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(DARK_GREEN);

        lblV.setForeground(accent);

        card.add(lblT, BorderLayout.CENTER);
        card.add(lblV, BorderLayout.EAST);
        return card;
    }

    // =========================================================================
    // JOGOS POR GRUPO
    // =========================================================================
    private String[] getJogosDoGrupo(int gi) {
        List<String> t = GRUPOS.get(gi);
        if (t.size() < 4) return new String[]{"(grupo incompleto)"};
        return new String[]{
                t.get(0)+" vs "+t.get(1), t.get(2)+" vs "+t.get(3),
                t.get(0)+" vs "+t.get(2), t.get(1)+" vs "+t.get(3),
                t.get(0)+" vs "+t.get(3), t.get(1)+" vs "+t.get(2)
        };
    }

    // =========================================================================
    // DIÁLOGO — ADICIONAR RESULTADOS
    // =========================================================================
    private void abrirDialogoResultados() {
        JDialog dlg = new JDialog(this, "Adicionar Resultados de Jogo", true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(PANEL_WHITE);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 15));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setOpaque(false);

        String[] fases = {
                "Grupo A","Grupo B","Grupo C","Grupo D",
                "Grupo E","Grupo F","Grupo G","Grupo H"
        };
        JComboBox<String> cbFase = styledCombo(fases);
        JComboBox<String> cbPartida = styledCombo(new String[]{});

        Runnable refreshPartidas = () -> {
            int idx = cbFase.getSelectedIndex();
            cbPartida.setModel(new DefaultComboBoxModel<>(getJogosDoGrupo(idx)));
        };
        cbFase.addActionListener(e -> refreshPartidas.run());
        refreshPartidas.run();

        JTextField txtGolosEq1 = styledField();
        JTextField txtGolosEq2 = styledField();
        JTextField txtAmarelos = styledField();
        JTextField txtVermelhos = styledField();
        JTextField txtEspectadores = styledField();

        form.add(label("Fase/Grupo:")); form.add(cbFase);
        form.add(label("Partida:")); form.add(cbPartida);
        form.add(label("Golos Equipa 1:")); form.add(txtGolosEq1);
        form.add(label("Golos Equipa 2:")); form.add(txtGolosEq2);
        form.add(label("Cartões Amarelos:")); form.add(txtAmarelos);
        form.add(label("Cartões Vermelhos:")); form.add(txtVermelhos);
        form.add(label("Espectadores:")); form.add(txtEspectadores);

        dlg.add(form, BorderLayout.CENTER);

        JButton btnGuardar = makeButton("Guardar Resultados", ACCENT_YELLOW, DARK_GREEN);
        btnGuardar.addActionListener(e -> {
            try {
                int g1 = Integer.parseInt(txtGolosEq1.getText().trim());
                int g2 = Integer.parseInt(txtGolosEq2.getText().trim());
                int am = Integer.parseInt(txtAmarelos.getText().trim());
                int ve = Integer.parseInt(txtVermelhos.getText().trim());
                int esp = Integer.parseInt(txtEspectadores.getText().trim());

                // Atualizar globais
                totalGolos += (g1 + g2);
                totalAmarelos += am;
                totalVermelhos += ve;
                totalEspectadores += esp;
                totalJogosRegistados++;

                // Atualizar a Sidebar Visualmente
                lblValGolos.setText(String.valueOf(totalGolos));
                lblValAmarelos.setText(String.valueOf(totalAmarelos));
                lblValVermelhos.setText(String.valueOf(totalVermelhos));
                lblValEspectadores.setText(String.valueOf(totalEspectadores / totalJogosRegistados));

                JOptionPane.showMessageDialog(dlg, "Estatísticas atualizadas com sucesso!");
                dlg.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Por favor, insere apenas números válidos nos campos de estatísticas.", "Erro de Formatação", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(0, 0, 10, 15));
        bot.add(btnGuardar);
        dlg.add(bot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // =========================================================================
    // DIÁLOGO — ESTRUTURA DO TORNEIO
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
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblEquipas = new JTable(tblModel);
        configurarTabelaSimples(tblEquipas); tblEquipas.setRowHeight(32);
        tblEquipas.getColumnModel().getColumn(0).setMaxWidth(36);

        Runnable refreshTabela = () -> {
            int gi = cbGrupoEd.getSelectedIndex();
            tblModel.setRowCount(0);
            List<String> eq = GRUPOS.get(gi);
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
            GRUPOS.get(gi).remove(equipa);
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
            List<String> noGrupo = GRUPOS.get(gi);
            for (String s : TODAS_SELECOES) if (!noGrupo.contains(s)) listModel.addElement(s);
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
            if (!TODAS_SELECOES.contains(nome)) TODAS_SELECOES.add(nome);
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
            if (GRUPOS.get(gi).contains(sel)) { JOptionPane.showMessageDialog(dlg, "Já está neste grupo."); return; }
            for (int g = 0; g < 8; g++) {
                if (g != gi && GRUPOS.get(g).contains(sel)) {
                    int resp = JOptionPane.showConfirmDialog(dlg,
                            sel + " está no Grupo " + (char)('A'+g) + ". Mover para " + grupoNomes[gi] + "?",
                            "Mover Seleção", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) GRUPOS.get(g).remove(sel);
                    else return;
                }
            }
            GRUPOS.get(gi).add(sel);
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
        btnOk.addActionListener(e -> dlg.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setBackground(PANEL_WHITE); bot.add(btnOk);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // UTILITÁRIOS DE UI
    // =========================================================================
    private void configurarTabelaSimples(JTable t) {
        t.setFont(MAIN_FONT); t.setRowHeight(30);
        t.setGridColor(TABLE_GRID); t.setForeground(TEXT_DARK); t.setBackground(PANEL_WHITE);
        t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD_FONT);
        h.setBackground(new Color(200, 225, 200));
        h.setForeground(DARK_GREEN);
        h.setPreferredSize(new Dimension(0, 36));
    }

    private TitledBorder titledCardBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1, true), "  " + title + "  ");
        b.setTitleFont(TITLE_FONT); b.setTitleColor(DARK_GREEN);
        return b;
    }

    private TitledBorder subBorder(String title) {
        return BorderFactory.createTitledBorder(
                new LineBorder(new Color(100, 160, 100), 1), title,
                TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, DARK_GREEN);
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setOpaque(true); btn.setContentAreaFilled(true);
        btn.setFont(BOLD_FONT); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DARK_GREEN, 1, true), new EmptyBorder(9, 14, 9, 14)));
        return btn;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD_FONT); l.setForeground(DARK_GREEN); return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(MAIN_FONT); f.setForeground(TEXT_DARK); return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(MAIN_FONT); cb.setForeground(TEXT_DARK); cb.setBackground(PANEL_WHITE); return cb;
    }

    // =========================================================================
    // MAIN
    // =========================================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MenuMatchCenter().setVisible(true));
    }
}