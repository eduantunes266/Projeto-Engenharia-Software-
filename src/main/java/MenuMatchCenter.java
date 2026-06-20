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

    private final Color DARK_GREEN    = new Color(20, 70, 35);
    private final Color MEDIUM_GREEN  = new Color(0, 110, 50);
    private final Color ACCENT_YELLOW = new Color(255, 204, 0);
    private final Color PANEL_WHITE   = Color.WHITE;
    private final Color TEXT_DARK     = new Color(30, 40, 30);
    private final Color TABLE_GRID    = new Color(210, 225, 210);

    private final Font MAIN_FONT  = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font BOLD_FONT  = new Font("Segoe UI", Font.BOLD,  13);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD,  15);
    private final Font NAV_FONT   = new Font("Segoe UI", Font.BOLD,  13);

    private MatchCenterBD bd;
    private final String BD_FILE = "matchcenter_dados.dat";

    private final DefaultTableModel[] modelosCalendario   = new DefaultTableModel[8];
    private final DefaultTableModel[] modelosClassificacao = new DefaultTableModel[8];

    private final JTextField[] txtOitavos = new JTextField[16];
    private final JTextField[] txtQuartos = new JTextField[8];
    private final JTextField[] txtMeias   = new JTextField[4];
    private final JTextField[] txtTerceiroLugar = new JTextField[2];
    private final JTextField[] txtFinal   = new JTextField[2];
    private final JTextField txtCampeao   = new JTextField();

    {
        for (int i = 0; i < 16; i++) txtOitavos[i] = new JTextField("A Definir");
        for (int i = 0; i < 8; i++) txtQuartos[i] = new JTextField("A Definir");
        for (int i = 0; i < 4; i++) txtMeias[i] = new JTextField("A Definir");
        for (int i = 0; i < 2; i++) txtTerceiroLugar[i] = new JTextField("A Definir");
        for (int i = 0; i < 2; i++) txtFinal[i] = new JTextField("A Definir");
        txtCampeao.setText("A Definir");
    }

    private DefaultTableModel modeloArbitros;
    private DefaultTableModel modeloAlocacoes;

    private JLabel lblValGolos, lblValAmarelos, lblValVermelhos, lblValEspectadores;
    private JPanel sidebarPanel, contentArea;
    private CardLayout cardLayout;
    private JButton[] navButtons;
    private int[] navTargets;
    private final boolean isAdmin;

    public static class MatchCenterBD implements Serializable {
        private static final long serialVersionUID = 1L;
        List<List<String>> grupos = new ArrayList<>();
        List<Arbitro> arbitros = new ArrayList<>();
        List<Jogo> jogosRegistados = new ArrayList<>();
        LinkedHashMap<String, Resultado> resultados = new LinkedHashMap<>();
        LinkedHashMap<String, String> alocacoes = new LinkedHashMap<>();
        List<String> equipasArbitragem = new ArrayList<>();

        public MatchCenterBD() {
            for (int i = 0; i < 8; i++) grupos.add(new ArrayList<>());
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

    public MenuMatchCenter() { this(true); }

    public MenuMatchCenter(boolean isAdmin) {
        this.isAdmin = isAdmin;
        carregarDados();
        semearFaseGruposSeVazio();
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

    private void semearFaseGruposSeVazio() {
        if (!bd.jogosRegistados.isEmpty()) return;
        String[] estadiosPreDefinidos = {"Maracanã", "Arena Corinthians", "Mineirão", "Castelão"};
        Random rnd = new Random();
        for (int gi = 0; gi < 8; gi++) {
            List<String> equipas = bd.grupos.get(gi);
            if (equipas.size() < 4) continue;
            String[][] confrontos = {
                    {equipas.get(0), equipas.get(1)}, {equipas.get(2), equipas.get(3)},
                    {equipas.get(0), equipas.get(2)}, {equipas.get(1), equipas.get(3)},
                    {equipas.get(0), equipas.get(3)}, {equipas.get(1), equipas.get(2)}
            };
            int dia = 10;
            for (String[] conf : confrontos) {
                Jogo j = new Jogo(gi, conf[0], conf[1], String.format("%02d/06/2026", dia++), "16:00", estadiosPreDefinidos[rnd.nextInt(estadiosPreDefinidos.length)]);
                bd.jogosRegistados.add(j);
                int g1 = rnd.nextInt(4); int g2 = rnd.nextInt(4);
                if (g1 == g2 && rnd.nextBoolean()) g1++;
                bd.resultados.put(j.id, new Resultado(gi, conf[0], conf[1], g1, g2, rnd.nextInt(4), 0, 45000 + rnd.nextInt(15000)));
            }
        }
        salvarDados();
    }

    private void inicializarModelosCalendario() {
        String[] calCols = {"Partida", "Data", "Hora", "Estádio"};
        for (int i = 0; i < 8; i++) {
            modelosCalendario[i] = new DefaultTableModel(calCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
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

    private String posDoGrupo(int gi, int pos) {
        DefaultTableModel m = modelosClassificacao[gi];
        if (m != null && m.getRowCount() > pos) return String.valueOf(m.getValueAt(pos, 1));
        return (pos == 0 ? "1.º " : "2.º ") + (char) ('A' + gi);
    }

    private void atualizarMataMata() {
        if (txtOitavos[0] == null) return;

        txtOitavos[0].setText(posDoGrupo(0, 0)); txtOitavos[1].setText(posDoGrupo(1, 1));
        txtOitavos[2].setText(posDoGrupo(2, 0)); txtOitavos[3].setText(posDoGrupo(3, 1));
        txtOitavos[4].setText(posDoGrupo(4, 0)); txtOitavos[5].setText(posDoGrupo(5, 1));
        txtOitavos[6].setText(posDoGrupo(6, 0)); txtOitavos[7].setText(posDoGrupo(7, 1));

        txtOitavos[8].setText(posDoGrupo(1, 0)); txtOitavos[9].setText(posDoGrupo(0, 1));
        txtOitavos[10].setText(posDoGrupo(3, 0)); txtOitavos[11].setText(posDoGrupo(2, 1));
        txtOitavos[12].setText(posDoGrupo(5, 0)); txtOitavos[13].setText(posDoGrupo(4, 1));
        txtOitavos[14].setText(posDoGrupo(7, 0)); txtOitavos[15].setText(posDoGrupo(6, 1));

        txtQuartos[0].setText(getVencedor(txtOitavos[0].getText(), txtOitavos[1].getText(), 8));
        txtQuartos[1].setText(getVencedor(txtOitavos[2].getText(), txtOitavos[3].getText(), 8));
        txtQuartos[2].setText(getVencedor(txtOitavos[4].getText(), txtOitavos[5].getText(), 8));
        txtQuartos[3].setText(getVencedor(txtOitavos[6].getText(), txtOitavos[7].getText(), 8));
        txtQuartos[4].setText(getVencedor(txtOitavos[8].getText(), txtOitavos[9].getText(), 8));
        txtQuartos[5].setText(getVencedor(txtOitavos[10].getText(), txtOitavos[11].getText(), 8));
        txtQuartos[6].setText(getVencedor(txtOitavos[12].getText(), txtOitavos[13].getText(), 8));
        txtQuartos[7].setText(getVencedor(txtOitavos[14].getText(), txtOitavos[15].getText(), 8));

        txtMeias[0].setText(getVencedor(txtQuartos[0].getText(), txtQuartos[1].getText(), 9));
        txtMeias[1].setText(getVencedor(txtQuartos[2].getText(), txtQuartos[3].getText(), 9));
        txtMeias[2].setText(getVencedor(txtQuartos[4].getText(), txtQuartos[5].getText(), 9));
        txtMeias[3].setText(getVencedor(txtQuartos[6].getText(), txtQuartos[7].getText(), 9));

        txtFinal[0].setText(getVencedor(txtMeias[0].getText(), txtMeias[1].getText(), 10));
        txtFinal[1].setText(getVencedor(txtMeias[2].getText(), txtMeias[3].getText(), 10));

        txtTerceiroLugar[0].setText(getDerrotado(txtMeias[0].getText(), txtMeias[1].getText(), 10));
        txtTerceiroLugar[1].setText(getDerrotado(txtMeias[2].getText(), txtMeias[3].getText(), 10));

        txtCampeao.setText(getVencedor(txtFinal[0].getText(), txtFinal[1].getText(), 12));
    }

    private String getVencedor(String t1, String t2, int fase) {
        if (t1.isEmpty() || t2.isEmpty() || t1.contains("1.º") || t2.contains("2.º") || t1.equals("A Definir") || t2.equals("A Definir")) {
            return "A Definir";
        }
        for (Resultado r : bd.resultados.values()) {
            if (r.grupo == fase) {
                if ((r.eq1.equals(t1) && r.eq2.equals(t2)) || (r.eq1.equals(t2) && r.eq2.equals(t1))) {
                    return r.g1 > r.g2 ? r.eq1 : r.eq2;
                }
            }
        }
        return "A Definir";
    }

    private String getDerrotado(String t1, String t2, int fase) {
        if (t1.isEmpty() || t2.isEmpty() || t1.contains("1.º") || t2.contains("2.º") || t1.equals("A Definir") || t2.equals("A Definir")) {
            return "A Definir";
        }
        for (Resultado r : bd.resultados.values()) {
            if (r.grupo == fase) {
                if ((r.eq1.equals(t1) && r.eq2.equals(t2)) || (r.eq1.equals(t2) && r.eq2.equals(t1))) {
                    return r.g1 < r.g2 ? r.eq1 : r.eq2;
                }
            }
        }
        return "A Definir";
    }

    private List<String> getEquipasElegiveis(int fase) {
        if (fase < 8) return bd.grupos.get(fase);
        List<String> elegiveis = new ArrayList<>();
        JTextField[] source = null;

        if (fase == 8) source = txtOitavos;
        else if (fase == 9) source = txtQuartos;
        else if (fase == 10) source = txtMeias;
        else if (fase == 11) source = txtTerceiroLugar;
        else if (fase == 12) source = txtFinal;

        if (source != null) {
            for (JTextField t : source) {
                String txt = t.getText();
                if (!txt.isEmpty() && !txt.contains("1.º") && !txt.contains("2.º") && !txt.equals("A Definir")) {
                    if (!elegiveis.contains(txt)) elegiveis.add(txt);
                }
            }
        }
        if (elegiveis.isEmpty()) elegiveis.add("A Definir");
        return elegiveis;
    }

    private JPanel buildTitleAndNav() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)); wrapper.setOpaque(false);
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14)); titleBar.setOpaque(false);
        JLabel lbl = new JLabel("MATCH CENTER"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 26)); lbl.setForeground(DARK_GREEN);
        JButton btnVoltar = buildBackButton();
        btnVoltar.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        titleBar.add(btnVoltar); titleBar.add(lbl); wrapper.add(titleBar, BorderLayout.NORTH);

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

    private JButton buildBackButton() {
        JButton btn = new JButton("← Voltar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(20, 70, 35, 240));
                else if (getModel().isRollover()) g2.setColor(new Color(0, 100, 40, 200));
                else g2.setColor(new Color(0, 80, 30, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(NAV_FONT); btn.setForeground(Color.WHITE); btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 40)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
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
                g2.setColor(new Color(236, 252, 236, 215)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, 160)); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            }
        };
        p.setOpaque(false); return p;
    }

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

        String[] fases = { "Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H", "Oitavos","Quartos","Meia-Final","Terceiro Lugar","Final" };
        JComboBox<String> cbFase = styledCombo(fases);

        JComboBox<String> cbEq1 = styledCombo(new String[]{});
        JComboBox<String> cbEq2 = styledCombo(new String[]{});
        JPanel pnlEquipas = new JPanel(new GridLayout(1, 2, 10, 0)); pnlEquipas.setOpaque(false);
        pnlEquipas.add(cbEq1); pnlEquipas.add(cbEq2);

        Runnable refreshEquipas = () -> {
            int idx = cbFase.getSelectedIndex();
            Object sel1 = cbEq1.getSelectedItem();
            Object sel2 = cbEq2.getSelectedItem();
            cbEq1.removeAllItems(); cbEq2.removeAllItems();

            List<String> elegiveis = getEquipasElegiveis(idx);
            if (elegiveis.isEmpty() || elegiveis.contains("1.º A")) {
                cbEq1.addItem("A Definir"); cbEq2.addItem("A Definir");
            } else {
                for (String t : elegiveis) { cbEq1.addItem(t); cbEq2.addItem(t); }
            }
            if(sel1 != null) cbEq1.setSelectedItem(sel1);
            if(sel2 != null) cbEq2.setSelectedItem(sel2);
        };
        cbFase.addActionListener(e -> refreshEquipas.run());

        cbEq1.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { refreshEquipas.run(); }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JTextField txtData = styledField(); txtData.setToolTipText("DD/MM/AAAA");
        JTextField txtHora = styledField(); txtHora.setToolTipText("HH:MM");

        JComboBox<String> cbEstadio = styledCombo(new String[0]);
        for (String est : GestorDados.getInstance().estadios.keySet()) cbEstadio.addItem(est);

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

            if (!data.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(19[3-9][0-9]|[2-9][0-9]{3})$")) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use DD/MM/AAAA e ano >= 1930.", "Erro Regex", JOptionPane.ERROR_MESSAGE); return;
            }
            if (!hora.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
                JOptionPane.showMessageDialog(this, "Hora inválida! Use HH:MM.", "Erro Regex", JOptionPane.ERROR_MESSAGE); return;
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
            } else {
                boolean matchupValid = false;
                JTextField[] source = null;
                if (idxFase == 8) source = txtOitavos;
                else if (idxFase == 9) source = txtQuartos;
                else if (idxFase == 10) source = txtMeias;
                else if (idxFase == 11) source = txtTerceiroLugar;
                else if (idxFase == 12) source = txtFinal;

                if (source != null && !eq1.equals("A Definir")) {
                    for (int i = 0; i < source.length; i += 2) {
                        String t1 = source[i].getText();
                        String t2 = source[i+1].getText();
                        if ((t1.equals(eq1) && t2.equals(eq2)) || (t1.equals(eq2) && t2.equals(eq1))) {
                            matchupValid = true;
                            break;
                        }
                    }
                    if (!matchupValid) {
                        JOptionPane.showMessageDialog(this, "ERRO DE REGISTO: O confronto selecionado (" + eq1 + " vs " + eq2 + ") não existe como uma partida válida e pendente no esquema da fase a eliminar.\nPor favor, verifique a árvore do 'Caminho para a Final'.", "Confronto Inexistente", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                boolean jaJogouFase = bd.jogosRegistados.stream().anyMatch(j ->
                        j.grupo == idxFase && (j.eq1.equals(eq1) || j.eq2.equals(eq1) || j.eq1.equals(eq2) || j.eq2.equals(eq2))
                );
                if (jaJogouFase && !eq1.equals("A Definir")) {
                    JOptionPane.showMessageDialog(this, "Uma das equipas já está registada num jogo desta eliminatória.", "Atenção", JOptionPane.WARNING_MESSAGE); return;
                }
            }

            Jogo novoJogo = new Jogo(idxFase, eq1, eq2, data, hora, estadio);
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

    private JPanel buildSection2() {
        JPanel outer = new JPanel(new BorderLayout(0, 0)); outer.setOpaque(false);
        JPanel card = createCard(); card.setLayout(new BorderLayout(10, 14));
        card.setBorder(titledCardBorder("2. Gestão de Arbitragem"));

        DefaultComboBoxModel<String> modelEquipas = new DefaultComboBoxModel<>();
        for(String eq : bd.equipasArbitragem) modelEquipas.addElement(eq);

        JPanel pnlReg = new JPanel(new GridLayout(4, 2, 8, 8)); pnlReg.setOpaque(false);
        pnlReg.setBorder(subBorder("Registar Novo Árbitro"));

        JTextField txtNome = styledField();
        JComboBox<String> cbFuncao = styledCombo(new String[]{"Árbitro Principal","Assistente 1","Assistente 2","Quarto Árbitro"});

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
        cbEquipa.setBorder(new LineBorder(new Color(180, 200, 180), 1, true)); cbEquipa.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

            long contaFuncao = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals(func)).count();
            if(contaFuncao > 0) { JOptionPane.showMessageDialog(this, "A equipa '" + equipa + "' já tem um " + func + " registado."); return; }

            Arbitro a = new Arbitro(nome, func, pais, equipa);
            bd.arbitros.add(a); salvarDados();
            modeloArbitros.addRow(new Object[]{nome, func, pais, equipa});
            txtNome.setText("");
        });

        pnlReg.add(label("Nome:")); pnlReg.add(txtNome);
        pnlReg.add(label("Função / País Nasc.:")); pnlReg.add(pnlFN);
        pnlReg.add(label("Equipa:")); pnlReg.add(pnlEqWrap);
        pnlReg.add(new JLabel()); pnlReg.add(btnRegArb);

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

        cbJogo.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                Object selected = cbJogo.getSelectedItem();
                cbJogo.removeAllItems();
                for(Jogo j : bd.jogosRegistados) cbJogo.addItem(j);
                if (selected != null) cbJogo.setSelectedItem(selected);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JComboBox<String> cbEqAloc = new JComboBox<>(modelEquipas);
        cbEqAloc.setFont(MAIN_FONT); cbEqAloc.setBackground(PANEL_WHITE);
        cbEqAloc.setBorder(new LineBorder(new Color(180, 200, 180), 1, true)); cbEqAloc.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnAlocar = makeButton("Alocar Equipa", ACCENT_YELLOW, DARK_GREEN);
        btnAlocar.addActionListener(e -> {
            Jogo j = (Jogo) cbJogo.getSelectedItem();
            String equipa = (String) cbEqAloc.getSelectedItem();
            if (j == null || equipa == null) return;

            if (bd.alocacoes.containsKey(j.id)) {
                JOptionPane.showMessageDialog(this, "ERRO: Este jogo já possui uma equipa de arbitragem alocada!", "Alocação Única", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (Map.Entry<String, String> aloc : bd.alocacoes.entrySet()) {
                if (aloc.getValue().equals(equipa)) {
                    Jogo jogoExistente = bd.jogosRegistados.stream()
                            .filter(x -> x.id.equals(aloc.getKey()))
                            .findFirst().orElse(null);
                    if (jogoExistente != null && jogoExistente.data.equals(j.data)) {
                        JOptionPane.showMessageDialog(this,
                                "BLOQUEADO — Gestão de Disponibilidade!\nA equipa " + equipa +
                                        " já está alocada ao jogo \"" + jogoExistente.getPartida() +
                                        "\" no dia " + j.data + ".\nNão é permitido arbitrar mais do que uma partida por dia.",
                                "Sem Descanso", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            long p = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Árbitro Principal")).count();
            long a1 = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Assistente 1")).count();
            long a2 = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Assistente 2")).count();
            long q = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa) && a.funcao.equals("Quarto Árbitro")).count();
            long totalElementos = bd.arbitros.stream().filter(a -> a.equipa.equals(equipa)).count();

            if (totalElementos != 4 || p != 1 || a1 != 1 || a2 != 1 || q != 1) {
                JOptionPane.showMessageDialog(this, "ERRO: A equipa de arbitragem é inelegível!\nA equipa tem de ser formada por exatos 4 membros, cada um com uma função única.", "Inelegível", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
        pnlAlocar.add(new JLabel()); pnlAlocar.add(new JLabel());
        pnlAlocar.add(new JLabel()); pnlAlocar.add(btnAlocar);

        JPanel forms = new JPanel(new GridLayout(1, 2, 14, 0)); forms.setOpaque(false);
        forms.add(pnlReg); forms.add(pnlAlocar);

        modeloArbitros = new DefaultTableModel(new Object[]{"Nome","Função","País Nascença","Equipa"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for(Arbitro a : bd.arbitros) modeloArbitros.addRow(new Object[]{a.nome, a.funcao, a.paisNascenca, a.equipa});
        JTable tblArb = new JTable(modeloArbitros); configurarTabelaSimples(tblArb);
        JScrollPane spArb = new JScrollPane(tblArb); spArb.setBorder(subBorder("Árbitros Registados"));

        JButton btnRemoverArb = makeButton("Remover Árbitro", new Color(180, 30, 30), Color.WHITE);
        btnRemoverArb.addActionListener(e -> {
            int row = tblArb.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um árbitro na tabela para remover."); return; }
            String nomeRemover = (String) tblArb.getValueAt(row, 0);
            bd.arbitros.removeIf(a -> a.nome.equals(nomeRemover));
            modeloArbitros.removeRow(row);
            salvarDados();
            JOptionPane.showMessageDialog(this, "Árbitro removido com sucesso!");
        });
        JPanel pnlArbWrap = new JPanel(new BorderLayout()); pnlArbWrap.setOpaque(false);
        pnlArbWrap.add(spArb, BorderLayout.CENTER); pnlArbWrap.add(btnRemoverArb, BorderLayout.SOUTH);

        modeloAlocacoes = new DefaultTableModel(new Object[]{"Partida","Equipa de Arbitragem","Data"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for(Map.Entry<String, String> entry : bd.alocacoes.entrySet()) {
            Jogo jo = bd.jogosRegistados.stream().filter(x -> x.id.equals(entry.getKey())).findFirst().orElse(null);
            if(jo != null) modeloAlocacoes.addRow(new Object[]{jo.getPartida(), entry.getValue(), jo.data});
        }
        JTable tblAlo = new JTable(modeloAlocacoes); configurarTabelaSimples(tblAlo);
        JScrollPane spAlo = new JScrollPane(tblAlo); spAlo.setBorder(subBorder("Alocações"));

        JButton btnRemoverAloc = makeButton("Remover Alocação", new Color(180, 30, 30), Color.WHITE);
        btnRemoverAloc.addActionListener(e -> {
            int row = tblAlo.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma alocação na tabela."); return; }
            String partidaStr = (String) tblAlo.getValueAt(row, 0);
            String jogoId = null;
            for (Jogo jo : bd.jogosRegistados) {
                if (jo.getPartida().equals(partidaStr)) { jogoId = jo.id; break; }
            }
            if (jogoId != null) {
                bd.alocacoes.remove(jogoId);
                modeloAlocacoes.removeRow(row);
                salvarDados();
                JOptionPane.showMessageDialog(this, "Alocação removida do jogo!");
            }
        });
        JPanel pnlAloWrap = new JPanel(new BorderLayout()); pnlAloWrap.setOpaque(false);
        pnlAloWrap.add(spAlo, BorderLayout.CENTER); pnlAloWrap.add(btnRemoverAloc, BorderLayout.SOUTH);

        JPanel tabelas = new JPanel(new GridLayout(1, 2, 14, 0)); tabelas.setOpaque(false);
        tabelas.add(pnlArbWrap); tabelas.add(pnlAloWrap);

        card.add(forms, BorderLayout.NORTH); card.add(tabelas, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER); return outer;
    }

    private JPanel buildSection3() {
        JPanel outer = new JPanel(new BorderLayout(14, 0)); outer.setOpaque(false);
        JPanel centerCard = createCard(); centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(titledCardBorder("3. Quadros Competitivos"));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setFont(BOLD_FONT); tabs.setForeground(DARK_GREEN); tabs.setOpaque(false);
        for (int i = 0; i < 8; i++) tabs.addTab("  Grupo " + (char)('A' + i) + "  ", createGroupTable(i));

        tabs.addTab("  Caminho para a Final  ", createKnockoutPanel());

        centerCard.add(tabs, BorderLayout.CENTER);

        sidebarPanel = createGlobalStatsSidebar();
        outer.add(centerCard, BorderLayout.CENTER); outer.add(sidebarPanel, BorderLayout.EAST);
        return outer;
    }

    private void abrirDialogoResultados() {
        JDialog dlg = new JDialog(this, "Adicionar Resultados de Jogo", true);
        dlg.setSize(500, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JLabel lblAvisoArbitragem = new JLabel(" ");
        lblAvisoArbitragem.setForeground(new Color(180, 30, 30));
        lblAvisoArbitragem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAvisoArbitragem.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvisoArbitragem.setBorder(new EmptyBorder(10, 0, 0, 0));
        dlg.add(lblAvisoArbitragem, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 15));
        form.setBorder(new EmptyBorder(10, 20, 20, 20));

        long jogosGruposFeitos = bd.resultados.values().stream().filter(r -> r.grupo >= 0 && r.grupo < 8).count();
        boolean gruposCompletos = (jogosGruposFeitos >= 48);

        String[] todasAsFases = {
                "Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H",
                "Oitavos-de-Final", "Quartos-de-Final", "Meias-Finais", "Terceiro Lugar", "Final"
        };

        JComboBox<String> cbFase = styledCombo(todasAsFases);
        JComboBox<Jogo> cbPartida = new JComboBox<>();
        cbPartida.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogo) setText(((Jogo)value).getPartida());
                return this;
            }
        });

        JTextField txtGolosEq1 = styledField(), txtGolosEq2 = styledField();
        JTextField txtAmarelos = styledField(), txtVermelhos = styledField(), txtEspectadores = styledField();
        JButton btnGuardar = makeButton("Guardar Resultados", ACCENT_YELLOW, DARK_GREEN);

        Runnable refreshPartidas = () -> {
            cbPartida.removeAllItems();
            int idxFaseSelecionada = cbFase.getSelectedIndex();

            for (Jogo j : bd.jogosRegistados) {
                if (j.grupo == idxFaseSelecionada && !bd.resultados.containsKey(j.id)) {
                    if (idxFaseSelecionada >= 8 && (j.eq1.contains("Definir") || j.eq2.contains("Definir"))) {
                        continue;
                    }
                    cbPartida.addItem(j);
                }
            }

            Jogo jFirst = (Jogo) cbPartida.getSelectedItem();
            boolean temArbitro = (jFirst != null && bd.alocacoes.containsKey(jFirst.id));

            txtGolosEq1.setEnabled(temArbitro); txtGolosEq2.setEnabled(temArbitro);
            txtAmarelos.setEnabled(temArbitro); txtVermelhos.setEnabled(temArbitro); txtEspectadores.setEnabled(temArbitro);
            btnGuardar.setEnabled(temArbitro);

            if (!temArbitro && jFirst != null) {
                txtGolosEq1.setText(""); txtGolosEq2.setText("");
                txtAmarelos.setText(""); txtVermelhos.setText(""); txtEspectadores.setText("");
                lblAvisoArbitragem.setText("⚠️ Ação bloqueada: Partida sem equipa de arbitragem atribuída.");
            } else if (jFirst == null) {
                lblAvisoArbitragem.setText(" ");
            } else {
                lblAvisoArbitragem.setText(" ");
            }
        };

        cbFase.addActionListener(e -> refreshPartidas.run());

        cbPartida.addActionListener(e -> {
            Jogo j = (Jogo) cbPartida.getSelectedItem();
            if (j != null) {
                boolean temArbitro = bd.alocacoes.containsKey(j.id);

                txtGolosEq1.setEnabled(temArbitro); txtGolosEq2.setEnabled(temArbitro);
                txtAmarelos.setEnabled(temArbitro); txtVermelhos.setEnabled(temArbitro); txtEspectadores.setEnabled(temArbitro);
                btnGuardar.setEnabled(temArbitro);

                if (!temArbitro) {
                    txtGolosEq1.setText(""); txtGolosEq2.setText("");
                    txtAmarelos.setText(""); txtVermelhos.setText(""); txtEspectadores.setText("");
                    lblAvisoArbitragem.setText("⚠️ Ação bloqueada: Partida sem equipa de arbitragem atribuída.");
                } else {
                    lblAvisoArbitragem.setText(" ");
                }
            }
        });

        refreshPartidas.run();

        form.add(label("Fase/Grupo:")); form.add(cbFase);
        form.add(label("Partida Pendente:")); form.add(cbPartida);
        form.add(label("Golos Equipa 1:")); form.add(txtGolosEq1);
        form.add(label("Golos Equipa 2:")); form.add(txtGolosEq2);
        form.add(label("Cartões Amarelos:")); form.add(txtAmarelos);
        form.add(label("Cartões Vermelhos:")); form.add(txtVermelhos);
        form.add(label("Espectadores:")); form.add(txtEspectadores);
        dlg.add(form, BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> {
            try {
                Jogo j = (Jogo) cbPartida.getSelectedItem();
                if (j == null) { JOptionPane.showMessageDialog(dlg, "Sem jogos pendentes nesta fase.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }

                if (!bd.alocacoes.containsKey(j.id)) return;

                int g1 = Integer.parseInt(txtGolosEq1.getText().trim());
                int g2 = Integer.parseInt(txtGolosEq2.getText().trim());
                int am = Integer.parseInt(txtAmarelos.getText().trim());
                int ve = Integer.parseInt(txtVermelhos.getText().trim());
                int esp = Integer.parseInt(txtEspectadores.getText().trim());

                if (j.grupo >= 8 && g1 == g2) {
                    JOptionPane.showMessageDialog(dlg, "ERRO: Jogos da fase eliminatória não podem terminar empatados.\nPor favor, insira o resultado final apurado (ex: após penáltis ou prolongamento).", "Empate Inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                bd.resultados.put(j.id, new Resultado(j.grupo, j.eq1, j.eq2, g1, g2, am, ve, esp));
                salvarDados();
                recomputarTudo();

                JOptionPane.showMessageDialog(dlg, "Resultado guardado e jogo bloqueado!\nO vencedor avançou na árvore do torneio.");
                dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Insira números válidos nos campos.", "Erro de input", JOptionPane.ERROR_MESSAGE); }
        });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bot.add(btnGuardar);
        dlg.add(bot, BorderLayout.SOUTH); dlg.setVisible(true);
    }

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

    private JPanel createGroupTable(int gi) {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        String[] cols = {"#","Seleção","PJ","PTS","V","E","D","GM","GS","DG"};
        modelosClassificacao[gi] = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tbl = new JTable(modelosClassificacao[gi]); configurarTabelaSimples(tbl);
        JScrollPane sp = new JScrollPane(tbl); panel.add(sp, BorderLayout.CENTER);

        JTable tblCal = new JTable(modelosCalendario[gi]); configurarTabelaSimples(tblCal);
        JScrollPane spCal = new JScrollPane(tblCal); spCal.setPreferredSize(new Dimension(0, 130));
        panel.add(spCal, BorderLayout.SOUTH); return panel;
    }

    private JPanel createKnockoutPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel lbl = new JLabel("Caminho para a Final - Fase a Eliminar (Progresso Automático)", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(MEDIUM_GREEN);
        lbl.setBorder(new EmptyBorder(10, 0, 15, 0));
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel bracketPanel = new JPanel(new GridBagLayout());
        bracketPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; bracketPanel.add(createMatchBox(txtOitavos[0], txtOitavos[1], "Oitavos 1"), gbc);
        gbc.gridx = 0; gbc.gridy = 2; bracketPanel.add(createMatchBox(txtOitavos[2], txtOitavos[3], "Oitavos 2"), gbc);
        gbc.gridx = 0; gbc.gridy = 4; bracketPanel.add(createMatchBox(txtOitavos[4], txtOitavos[5], "Oitavos 3"), gbc);
        gbc.gridx = 0; gbc.gridy = 6; bracketPanel.add(createMatchBox(txtOitavos[6], txtOitavos[7], "Oitavos 4"), gbc);

        gbc.gridx = 6; gbc.gridy = 0; bracketPanel.add(createMatchBox(txtOitavos[8], txtOitavos[9], "Oitavos 5"), gbc);
        gbc.gridx = 6; gbc.gridy = 2; bracketPanel.add(createMatchBox(txtOitavos[10], txtOitavos[11], "Oitavos 6"), gbc);
        gbc.gridx = 6; gbc.gridy = 4; bracketPanel.add(createMatchBox(txtOitavos[12], txtOitavos[13], "Oitavos 7"), gbc);
        gbc.gridx = 6; gbc.gridy = 6; bracketPanel.add(createMatchBox(txtOitavos[14], txtOitavos[15], "Oitavos 8"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; bracketPanel.add(createMatchBox(txtQuartos[0], txtQuartos[1], "Quartos 1"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; bracketPanel.add(createMatchBox(txtQuartos[2], txtQuartos[3], "Quartos 2"), gbc);
        gbc.gridx = 5; gbc.gridy = 1; bracketPanel.add(createMatchBox(txtQuartos[4], txtQuartos[5], "Quartos 3"), gbc);
        gbc.gridx = 5; gbc.gridy = 5; bracketPanel.add(createMatchBox(txtQuartos[6], txtQuartos[7], "Quartos 4"), gbc);

        gbc.gridx = 2; gbc.gridy = 3; bracketPanel.add(createMatchBox(txtMeias[0], txtMeias[1], "Meia-Final 1"), gbc);
        gbc.gridx = 4; gbc.gridy = 3; bracketPanel.add(createMatchBox(txtMeias[2], txtMeias[3], "Meia-Final 2"), gbc);

        gbc.gridx = 3; gbc.gridy = 3; bracketPanel.add(createMatchBox(txtFinal[0], txtFinal[1], "GRANDE FINAL"), gbc);

        gbc.gridx = 3; gbc.gridy = 5; bracketPanel.add(createMatchBox(txtTerceiroLugar[0], txtTerceiroLugar[1], "3º LUGAR"), gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        JPanel pCamp = new JPanel(new BorderLayout(0, 5));
        pCamp.setOpaque(false);
        JLabel lblC = new JLabel("🏆 CAMPEÃO", SwingConstants.CENTER);
        lblC.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblC.setForeground(new Color(200, 150, 0));

        txtCampeao.setEditable(false); txtCampeao.setHorizontalAlignment(JTextField.CENTER);
        txtCampeao.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtCampeao.setBackground(new Color(255, 240, 150)); txtCampeao.setForeground(DARK_GREEN);
        txtCampeao.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 150, 0), 2), new EmptyBorder(8, 10, 8, 10)));

        pCamp.add(lblC, BorderLayout.NORTH); pCamp.add(txtCampeao, BorderLayout.CENTER);
        bracketPanel.add(pCamp, gbc);

        JScrollPane scroll = new JScrollPane(bracketPanel);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMatchBox(JTextField t1, JTextField t2, String title) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(DARK_GREEN, 1), title, TitledBorder.CENTER, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 10), DARK_GREEN),
                new EmptyBorder(2, 4, 4, 4)
        ));
        p.setOpaque(false);

        t1.setEditable(false); t1.setHorizontalAlignment(JTextField.CENTER); t1.setFont(BOLD_FONT);
        t2.setEditable(false); t2.setHorizontalAlignment(JTextField.CENTER); t2.setFont(BOLD_FONT);
        t1.setBackground(new Color(245, 255, 245)); t1.setForeground(DARK_GREEN);
        t2.setBackground(new Color(245, 255, 245)); t2.setForeground(DARK_GREEN);
        t1.setBorder(new LineBorder(new Color(200, 220, 200)));
        t2.setBorder(new LineBorder(new Color(200, 220, 200)));

        p.add(t1); p.add(t2);
        p.setPreferredSize(new Dimension(130, 60));
        return p;
    }

    private JPanel createGlobalStatsSidebar() {
        JPanel card = createCard(); card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 16, 16, 16));
        lblValGolos = new JLabel("0"); lblValAmarelos = new JLabel("0"); lblValVermelhos = new JLabel("0"); lblValEspectadores = new JLabel("0");

        JLabel tituloSidebar = new JLabel("ESTATÍSTICAS GLOBAIS", SwingConstants.CENTER);
        tituloSidebar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tituloSidebar.setForeground(DARK_GREEN);
        tituloSidebar.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(tituloSidebar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 1, 0, 12)); grid.setOpaque(false);
        grid.add(statBox("GOLOS MARCADOS", lblValGolos, DARK_GREEN));
        grid.add(statBox("CARTÕES AMARELOS", lblValAmarelos, new Color(214, 158, 0)));
        grid.add(statBox("CARTÕES VERMELHOS", lblValVermelhos, new Color(180, 40, 40)));
        grid.add(statBox("MÉDIA DE ESPECTADORES", lblValEspectadores, new Color(20, 70, 120)));
        card.add(grid, BorderLayout.CENTER);

        JButton btnAddResultados = makeButton("Adicionar Resultados", ACCENT_YELLOW, DARK_GREEN);
        btnAddResultados.addActionListener(e -> abrirDialogoResultados());
        btnAddResultados.setVisible(isAdmin); card.add(btnAddResultados, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(260, 0)); return card;
    }

    private JPanel statBox(String titulo, JLabel valor, Color cor) {
        JPanel box = new JPanel(new BorderLayout(0, 2)); box.setOpaque(false);
        JLabel cap = new JLabel(titulo, SwingConstants.CENTER);
        cap.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cap.setForeground(new Color(70, 95, 70));
        valor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valor.setForeground(cor);
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(cap, BorderLayout.NORTH);
        box.add(valor, BorderLayout.CENTER);
        return box;
    }

    private void configurarTabelaSimples(JTable t) {
        t.setFont(MAIN_FONT);
        t.setRowHeight(30);
        t.setGridColor(TABLE_GRID);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(214, 238, 218));
        t.setSelectionForeground(DARK_GREEN);

        final Color zebra = new Color(244, 250, 244);
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tab, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tab, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? PANEL_WHITE : zebra);
                    c.setForeground(TEXT_DARK);
                }
                setHorizontalAlignment(val instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tab, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tab, val, sel, foc, row, col);
                l.setOpaque(true);
                l.setBackground(DARK_GREEN);
                l.setForeground(Color.WHITE);
                l.setFont(BOLD_FONT);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(new EmptyBorder(6, 8, 6, 8));
                return l;
            }
        });
    }
    private TitledBorder titledCardBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(DARK_GREEN, 1, true), "  " + title + "  ");
        b.setTitleFont(TITLE_FONT); b.setTitleColor(DARK_GREEN); return b;
    }
    private TitledBorder subBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(new Color(150, 185, 150), 1, true), " " + title + " ");
        b.setTitleFont(BOLD_FONT); b.setTitleColor(MEDIUM_GREEN); return b;
    }
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getBackground();
                Color fill = getModel().isPressed() ? base.darker()
                        : getModel().isRollover() ? tint(base, 0.14f) : base;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(base.darker());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(BOLD_FONT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    private JLabel label(String text) { JLabel l = new JLabel(text); l.setFont(BOLD_FONT); l.setForeground(DARK_GREEN); return l; }
    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(MAIN_FONT);
        f.setBackground(PANEL_WHITE);
        f.setForeground(TEXT_DARK);
        f.setCaretColor(DARK_GREEN);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 200, 180), 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }
    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(MAIN_FONT);
        cb.setBackground(PANEL_WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setBorder(new LineBorder(new Color(180, 200, 180), 1, true));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }
    private static Color tint(Color c, float f) {
        int r = (int) (c.getRed()   + (255 - c.getRed())   * f);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * f);
        int b = (int) (c.getBlue()  + (255 - c.getBlue())  * f);
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b), c.getAlpha());
    }
}