import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

public class MenuRecursos extends JFrame {

    public static class RecursosBD implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Object[]> hoteis = new ArrayList<>();
        List<Object[]> centros = new ArrayList<>();
        List<Object[]> estadios = new ArrayList<>();
        List<Object[]> viagens = new ArrayList<>();

        public RecursosBD() {
            hoteis.add(new Object[]{"Copacabana Palace", "Rio de Janeiro", "250", "-", "-"});
            hoteis.add(new Object[]{"Fasano", "Rio de Janeiro", "80", "-", "-"});
            centros.add(new Object[]{"CT Granja Comary", "Teresópolis", "-"});
            estadios.add(new Object[]{"Maracanã", "45000", "28000", "5000", "78000"});
            estadios.add(new Object[]{"Arena Corinthians", "30000", "16205", "3000", "49205"});
        }
    }

    private RecursosBD bd;
    private final String BD_FILE = "recursos_dados.dat";
    private DefaultTableModel modeloHoteis, modeloCentros, modeloEstadios, modeloViagens;

    public MenuRecursos() {
        carregarDados();

        setTitle("Mundial 2026 - Gestão de Recursos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1300, 850);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salvarDados(); }
        });

        JPanel contentorPrincipal = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 20), getWidth(), getHeight(), new Color(10, 140, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 30, 10, 30));

        JLabel titulo = new JLabel("RECURSOS E ESPAÇOS", SwingConstants.LEFT);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(0, 70, 30));
        header.add(titulo, BorderLayout.WEST);

        UIManager.put("TabbedPane.contentOpaque", false);
        JTabbedPane abas = new JTabbedPane();
        abas.setOpaque(false);
        abas.setBackground(Color.WHITE);
        abas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        abas.setBorder(new EmptyBorder(10, 20, 20, 20));

        abas.addTab("HOTÉIS", criarPainelHoteisGeral());
        abas.addTab("CENTROS DE TREINO", criarPainelCentrosGeral());
        abas.addTab("ESTÁDIOS", criarPainelEstadiosGeral());
        abas.addTab("AGENDAMENTO DE VIAGENS", criarPainelViagensGeral());

        contentorPrincipal.add(header, BorderLayout.NORTH);
        contentorPrincipal.add(abas, BorderLayout.CENTER);

        setContentPane(contentorPrincipal);
        setLocationRelativeTo(null);

        abas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
            @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
            @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? new Color(45, 105, 45) : new Color(85, 145, 60));
                g2.fillRoundRect(x, y + 2, w - 10, h - 2, 8, 8);
                g2.dispose();
            }
            @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setColor(Color.WHITE);
                Rectangle textRectAjustado = new Rectangle(textRect); textRectAjustado.x -= 5;
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRectAjustado, isSelected);
            }
            @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}
            @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) { return new Insets(8, 20, 8, 30); }
        });

        sincronizarEstadiosGlobais();
    }

    private void carregarDados() {
        File file = new File(BD_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                bd = (RecursosBD) ois.readObject();
                return;
            } catch (Exception e) { e.printStackTrace(); }
        }
        bd = new RecursosBD();
    }

    private void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BD_FILE))) {
            oos.writeObject(bd);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private List<String> getEquipasDoTorneio() {
        List<String> equipas = new ArrayList<>();
        File file = new File("matchcenter_dados.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object matchBd = ois.readObject();
                List<?> grupos = (List<?>) matchBd.getClass().getDeclaredField("grupos").get(matchBd);
                for (Object g : grupos) equipas.addAll((List<String>) g);
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (equipas.isEmpty()) equipas.add("A Definir");
        return equipas;
    }

    private void setupDynamicEquipas(JComboBox<String> cb) {
        cb.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                String selected = (String) cb.getSelectedItem();
                cb.removeAllItems();
                for (String eq : getEquipasDoTorneio()) cb.addItem(eq);
                if (selected != null) cb.setSelectedItem(selected);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        for (String eq : getEquipasDoTorneio()) cb.addItem(eq);
    }

    private void sincronizarEstadiosGlobais() {
        try {
            for (Object[] row : bd.estadios) {
                GestorDados.getInstance().estadios.put((String) row[0], new int[]{
                        Integer.parseInt((String) row[1]), Integer.parseInt((String) row[2]), Integer.parseInt((String) row[3]), Integer.parseInt((String) row[4])
                });
            }
            GestorDados.getInstance().salvarDados();
        } catch (Throwable ignore) {}
    }

    private JPanel criarPainelHoteisGeral() {
        JPanel painel = new JPanel(new BorderLayout()); painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0)); cardLargo.setOpaque(false);

        String[] colunas = {"Hotel", "Cidade", "Quartos", "Equipa", "Datas"};
        modeloHoteis = new DefaultTableModel(null, colunas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : bd.hoteis) modeloHoteis.addRow(row);

        JTable tabelaHoteis = new JTable(modeloHoteis); estilizarTabela(tabelaHoteis);

        JPanel cardTabela = criarCardBase("HOTÉIS CADASTRADOS", null);
        cardTabela.add(embrulharTabela(tabelaHoteis));
        cardTabela.add(Box.createVerticalStrut(15));
        cardTabela.add(criarBotaoAcao("REMOVER HOTEL", e -> removerLinha(tabelaHoteis, bd.hoteis, "hotel")));

        JPanel cardForm = criarCardBase("GESTÃO DE HOTEL", null);
        JTextField txtNome = campoPlaceholder("Ex: Hotel Marriott");
        JTextField txtCidade = campoPlaceholder("Ex: São Paulo");
        JTextField txtQuartos = campoPlaceholder("Ex: 150");

        JComboBox<String> cbEquipa = criarDropdown(new String[0]);
        setupDynamicEquipas(cbEquipa);

        JTextField txtDatas = campoPlaceholder("DD/MM/AAAA - DD/MM/AAAA");

        cardForm.add(criarLabelCampo("Nome do Hotel:")); cardForm.add(txtNome); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Cidade:")); cardForm.add(txtCidade); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Quartos Disponíveis:")); cardForm.add(txtQuartos); cardForm.add(Box.createVerticalStrut(10));

        cardForm.add(criarBotaoAcao("ADICIONAR HOTEL", e -> {
            String nome = valor(txtNome), cidade = valor(txtCidade), quartos = valor(txtQuartos);
            if (nome.isEmpty() || cidade.isEmpty() || quartos.isEmpty()) { aviso("Preencha todos os campos do hotel."); return; }
            if (!quartos.matches("\\d+")) { aviso("O número de quartos deve ser numérico."); return; }
            Object[] newRow = new Object[]{nome, cidade, quartos, "-", "-"};
            modeloHoteis.addRow(newRow); bd.hoteis.add(newRow); salvarDados();
            limpar(txtNome, txtCidade, txtQuartos);
        }));

        cardForm.add(Box.createVerticalStrut(15)); cardForm.add(new JSeparator()); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Atribuir a Equipa:")); cardForm.add(cbEquipa); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Datas de Estadia:")); cardForm.add(txtDatas); cardForm.add(Box.createVerticalStrut(10));

        cardForm.add(criarBotaoAcao("VINCULAR ESTADIA", e -> {
            int row = tabelaHoteis.getSelectedRow();
            if (row < 0) { aviso("Selecione um hotel na tabela."); return; }
            String equipa = (String) cbEquipa.getSelectedItem();
            String datas = valor(txtDatas);

            if (!"-".equals(modeloHoteis.getValueAt(row, 3))) { aviso("Hotel já ocupado."); return; }
            for (int i = 0; i < modeloHoteis.getRowCount(); i++) {
                if (equipa.equals(modeloHoteis.getValueAt(i, 3))) { aviso("Equipa já possui hotel."); return; }
            }

            modeloHoteis.setValueAt(equipa, row, 3); modeloHoteis.setValueAt(datas, row, 4);
            bd.hoteis.get(row)[3] = equipa; bd.hoteis.get(row)[4] = datas;
            salvarDados(); info("Estadia vinculada!");
        }));
        cardForm.add(Box.createVerticalGlue());

        cardLargo.add(cardTabela); cardLargo.add(cardForm); painel.add(cardLargo, BorderLayout.CENTER); return painel;
    }

    private JPanel criarPainelCentrosGeral() {
        JPanel painel = new JPanel(new BorderLayout()); painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0)); cardLargo.setOpaque(false);

        String[] colunas = {"Centro", "Localização", "Equipa Vinculada"};
        modeloCentros = new DefaultTableModel(null, colunas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : bd.centros) modeloCentros.addRow(row);

        JTable tabelaCentros = new JTable(modeloCentros); estilizarTabela(tabelaCentros);

        JPanel cardTabela = criarCardBase("CENTROS DE TREINO", null);
        cardTabela.add(embrulharTabela(tabelaCentros));
        cardTabela.add(Box.createVerticalStrut(15));
        cardTabela.add(criarBotaoAcao("REMOVER CENTRO", e -> removerLinha(tabelaCentros, bd.centros, "centro")));

        JPanel cardForm = criarCardBase("GESTÃO DE CENTRO", null);
        JTextField txtNome = campoPlaceholder("Ex: CT do Caju");
        JTextField txtLocal = campoPlaceholder("Ex: Curitiba");

        JComboBox<String> cbEquipa = criarDropdown(new String[0]);
        setupDynamicEquipas(cbEquipa);

        cardForm.add(criarLabelCampo("Nome do Centro:")); cardForm.add(txtNome); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Localização:")); cardForm.add(txtLocal); cardForm.add(Box.createVerticalStrut(10));

        cardForm.add(criarBotaoAcao("ADICIONAR CENTRO", e -> {
            String nome = valor(txtNome), local = valor(txtLocal);
            if (nome.isEmpty() || local.isEmpty()) { aviso("Preencha todos os campos."); return; }
            Object[] newRow = new Object[]{nome, local, "-"};
            modeloCentros.addRow(newRow); bd.centros.add(newRow); salvarDados();
            limpar(txtNome, txtLocal);
        }));

        cardForm.add(Box.createVerticalStrut(25)); cardForm.add(new JSeparator()); cardForm.add(Box.createVerticalStrut(15));
        cardForm.add(criarLabelCampo("Atribuir a Equipa:")); cardForm.add(cbEquipa); cardForm.add(Box.createVerticalStrut(10));

        cardForm.add(criarBotaoAcao("VINCULAR TREINO", e -> {
            int row = tabelaCentros.getSelectedRow();
            if (row < 0) { aviso("Selecione um centro na tabela."); return; }
            String equipa = (String) cbEquipa.getSelectedItem();

            if (!"-".equals(modeloCentros.getValueAt(row, 2))) { aviso("Centro já ocupado."); return; }
            for (int i = 0; i < modeloCentros.getRowCount(); i++) {
                if (equipa.equals(modeloCentros.getValueAt(i, 2))) { aviso("Equipa já possui centro."); return; }
            }

            modeloCentros.setValueAt(equipa, row, 2);
            bd.centros.get(row)[2] = equipa; salvarDados(); info("Treino vinculado!");
        }));
        cardForm.add(Box.createVerticalGlue());

        cardLargo.add(cardTabela); cardLargo.add(cardForm); painel.add(cardLargo, BorderLayout.CENTER); return painel;
    }

    private JPanel criarPainelEstadiosGeral() {
        JPanel painel = new JPanel(new BorderLayout()); painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0)); cardLargo.setOpaque(false);

        String[] colunas = {"Estádio", "Central", "Topos", "VIP", "Total"};
        modeloEstadios = new DefaultTableModel(null, colunas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : bd.estadios) modeloEstadios.addRow(row);

        JTable tabelaEstadios = new JTable(modeloEstadios); estilizarTabela(tabelaEstadios);

        JPanel cardTabela = criarCardBase("ESTÁDIOS CADASTRADOS", null);
        cardTabela.add(embrulharTabela(tabelaEstadios));
        cardTabela.add(Box.createVerticalStrut(15));
        cardTabela.add(criarBotaoAcao("REMOVER ESTÁDIO", e -> removerLinha(tabelaEstadios, bd.estadios, "estádio")));

        JPanel cardForm = criarCardBase("ADICIONAR ESTÁDIO", null);
        JTextField txtNome = campoPlaceholder("Ex: Santiago Bernabéu");
        JTextField txtCentral = campoPlaceholder("Ex: 30000");
        JTextField txtTopos = campoPlaceholder("Ex: 40000");
        JTextField txtVip = campoPlaceholder("Ex: 5000");

        cardForm.add(criarLabelCampo("Nome do Estádio:")); cardForm.add(txtNome); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Capacidade Bancada Central:")); cardForm.add(txtCentral); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Capacidade Topos:")); cardForm.add(txtTopos); cardForm.add(Box.createVerticalStrut(10));
        cardForm.add(criarLabelCampo("Capacidade Zona VIP:")); cardForm.add(txtVip); cardForm.add(Box.createVerticalGlue());

        cardForm.add(criarBotaoAcao("REGISTAR ESTÁDIO", e -> {
            String nome = valor(txtNome), c = valor(txtCentral), t = valor(txtTopos), v = valor(txtVip);
            if (nome.isEmpty() || c.isEmpty() || t.isEmpty() || v.isEmpty()) { aviso("Preencha todas as capacidades."); return; }
            if (!c.matches("\\d+") || !t.matches("\\d+") || !v.matches("\\d+")) { aviso("Apenas valores numéricos."); return; }
            int total = Integer.parseInt(c) + Integer.parseInt(t) + Integer.parseInt(v);

            Object[] newRow = new Object[]{nome, c, t, v, String.valueOf(total)};
            modeloEstadios.addRow(newRow); bd.estadios.add(newRow); salvarDados();
            sincronizarEstadiosGlobais();

            limpar(txtNome, txtCentral, txtTopos, txtVip);
            info("Estádio registado!");
        }));

        cardLargo.add(cardTabela); cardLargo.add(cardForm); painel.add(cardLargo, BorderLayout.CENTER); return painel;
    }

    private JPanel criarPainelViagensGeral() {
        JPanel painel = new JPanel(new BorderLayout()); painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JComboBox<String> cbSelecao = criarDropdown(new String[0]);
        setupDynamicEquipas(cbSelecao);

        JPanel topoSelecao = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); topoSelecao.setOpaque(false);
        topoSelecao.add(criarLabelCampo("Configurar Viagem para:")); topoSelecao.add(cbSelecao);

        String[] colunas = {"Seleção", "Veículo", "Rota", "Data/Hora"};
        modeloViagens = new DefaultTableModel(null, colunas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for(Object[] row : bd.viagens) modeloViagens.addRow(row);

        JTable tabelaViagens = new JTable(modeloViagens); estilizarTabela(tabelaViagens);

        JComboBox<String> cbVeiculo = criarDropdown(new String[]{"Autocarro Oficial Seleção", "Voo Charter Privado", "Comboio de Alta Velocidade"});
        JTextField txtRota = campoPlaceholder("Ex: Hotel → Estádio");
        JTextField txtData = campoPlaceholder("DD/MM HH:MM");

        JPanel cardForm = criarCardBase("DESLOCAÇÕES", null); cardForm.setPreferredSize(new Dimension(400, 360));
        cardForm.add(criarLabelCampo("Alocação de Veículo:")); cardForm.add(cbVeiculo); cardForm.add(Box.createVerticalStrut(15));
        cardForm.add(criarLabelCampo("Rotas de Dia de Jogo:")); cardForm.add(txtRota); cardForm.add(Box.createVerticalStrut(15));
        cardForm.add(criarLabelCampo("Registo de Viagem (Data/Hora):")); cardForm.add(txtData); cardForm.add(Box.createVerticalStrut(15));

        cardForm.add(criarBotaoAcao("AGENDAR VIAGEM", e -> {
            String rota = valor(txtRota), data = valor(txtData);
            if (rota.isEmpty() || data.isEmpty()) { aviso("Preencha a rota e a data."); return; }
            String selecao = (String) cbSelecao.getSelectedItem();

            long novosMins = converterDataParaMinutos(data);
            if (novosMins == -1) { aviso("Formato inválido! Use rigorosamente: DD/MM HH:MM"); return; }

            for (int i = 0; i < modeloViagens.getRowCount(); i++) {
                if (selecao.equals(modeloViagens.getValueAt(i, 0))) {
                    long minsExistente = converterDataParaMinutos(modeloViagens.getValueAt(i, 3).toString());
                    if (Math.abs(novosMins - minsExistente) < 120) {
                        JOptionPane.showMessageDialog(this, "BLOQUEADO!\nA equipa selecionada já possui uma deslocação registada que colide em tempo.\nRegra de negócio: A nova deslocação tem de estar a pelo menos 2 horas de intervalo.", "Conflito de Deslocação", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String veiculo = (String) cbVeiculo.getSelectedItem();
            Object[] newRow = new Object[]{selecao, veiculo, rota, data};
            modeloViagens.addRow(newRow); bd.viagens.add(newRow); salvarDados();
            limpar(txtRota, txtData);
            info("Viagem agendada!");
        }));
        cardForm.add(Box.createVerticalGlue());

        JPanel cardLista = criarCardBase("VIAGENS AGENDADAS", null);
        cardLista.add(embrulharTabela(tabelaViagens));
        cardLista.add(Box.createVerticalStrut(15));
        cardLista.add(criarBotaoAcao("CANCELAR VIAGEM", e -> removerLinha(tabelaViagens, bd.viagens, "viagem")));

        JPanel centro = new JPanel(new GridLayout(1, 2, 30, 0)); centro.setOpaque(false);
        centro.add(cardForm); centro.add(cardLista);

        painel.add(topoSelecao, BorderLayout.NORTH); painel.add(centro, BorderLayout.CENTER); return painel;
    }

    private long converterDataParaMinutos(String dataHora) {
        try {
            if (!dataHora.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012]) ([01][0-9]|2[0-3]):[0-5][0-9]$")) return -1;
            String[] parts = dataHora.split(" ");
            String[] data = parts[0].split("/");
            String[] hora = parts[1].split(":");
            return (Long.parseLong(data[1]) * 31 * 24 * 60) + (Long.parseLong(data[0]) * 24 * 60) + (Long.parseLong(hora[0]) * 60) + Long.parseLong(hora[1]);
        } catch (Exception e) { return -1; }
    }

    private void removerLinha(JTable tabela, List<Object[]> listaBD, String oQue) {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um " + oQue + " na tabela."); return; }
        if (JOptionPane.showConfirmDialog(this, "Remover " + oQue + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            listaBD.remove(row); ((DefaultTableModel) tabela.getModel()).removeRow(row); salvarDados();
        }
    }

    private JScrollPane embrulharTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela); scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 74, 35))); scroll.setAlignmentX(Component.CENTER_ALIGNMENT); return scroll;
    }
    private void aviso(String msg) { JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE); }
    private void limpar(JTextField... campos) {
        for (JTextField f : campos) { Object ph = f.getClientProperty("placeholder"); f.setText(ph == null ? "" : ph.toString()); f.setForeground(Color.GRAY); }
    }
    private void estilizarTabela(JTable t) {
        final Color verdeEscuro = new Color(0, 74, 35);
        final Color textoEscuro = new Color(30, 40, 30);
        final Color zebra = new Color(244, 250, 244);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(30);
        t.setGridColor(new Color(214, 228, 214));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(214, 238, 218));
        t.setSelectionForeground(verdeEscuro);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tab, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tab, val, sel, foc, row, col);
                if (!sel) { c.setBackground(row % 2 == 0 ? Color.WHITE : zebra); c.setForeground(textoEscuro); }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(cell);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tab, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tab, val, sel, foc, row, col);
                l.setOpaque(true); l.setBackground(verdeEscuro); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13)); l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(new EmptyBorder(6, 8, 6, 8));
                return l;
            }
        });
    }
    private JPanel criarCardBase(String titulo, String sub) {
        JPanel card = new JPanel() { @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 200)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(255, 255, 255, 150)); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        } };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setOpaque(false); card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lblT = new JLabel(titulo); lblT.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblT.setForeground(new Color(0, 74, 35)); lblT.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(lblT);
        card.add(Box.createVerticalStrut(20)); return card;
    }
    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto); lbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); lbl.setForeground(new Color(0, 50, 0)); lbl.setAlignmentX(Component.CENTER_ALIGNMENT); return lbl;
    }
    private JComboBox<String> criarDropdown(String[] opcoes) {
        JComboBox<String> combo = new JComboBox<>(opcoes);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(30, 40, 30));
        combo.setBorder(new LineBorder(new Color(180, 200, 180), 1, true));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setMaximumSize(new Dimension(300, 35)); combo.setAlignmentX(Component.CENTER_ALIGNMENT); return combo;
    }
    private JTextField campoPlaceholder(String placeholder) {
        JTextField txt = new JTextField(); txt.setMaximumSize(new Dimension(300, 35)); txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(180, 200, 180), 1, true), new EmptyBorder(4, 8, 4, 8)));
        txt.putClientProperty("placeholder", placeholder); txt.setForeground(Color.GRAY); txt.setText(placeholder);
        txt.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(Color.BLACK); } }
            @Override public void focusLost(FocusEvent e) { if (txt.getText().isBlank()) { txt.setForeground(Color.GRAY); txt.setText(placeholder); } }
        }); return txt;
    }
    private String valor(JTextField f) { String t = f.getText().trim(); Object ph = f.getClientProperty("placeholder"); return (ph != null && t.equals(ph.toString())) ? "" : t; }
    private JButton criarBotaoAcao(String texto, ActionListener acao) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? new Color(0, 50, 20)
                        : getModel().isRollover() ? new Color(0, 96, 48) : new Color(0, 74, 35);
                g2.setColor(fill); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE); btn.setFont(new Font("Segoe UI", Font.BOLD, 15)); btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(250, 45)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (acao != null) btn.addActionListener(acao); return btn;
    }
}