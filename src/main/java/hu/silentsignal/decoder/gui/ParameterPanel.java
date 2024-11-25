package hu.silentsignal.decoder.gui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.AuditConfiguration;
import burp.api.montoya.scanner.audit.Audit;
import hu.silentsignal.decoder.DummyAuditInsertionPointProvider;
import hu.silentsignal.decoder.encodings.EncodingTree;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static burp.api.montoya.scanner.BuiltInAuditConfiguration.LEGACY_ACTIVE_AUDIT_CHECKS;

public class ParameterPanel extends JPanel {
    private HttpRequest request;
    private HttpRequestResponse newHrr;
    private ParsedHttpParameter parameter;
    private JTree tree;
    private EncodingTree root;
    private JButton sendPayloadButton;
    private JButton startActiveScanButton;
    private JTextField payloadTextField;
    private final JPanel panel;
    private MontoyaApi api;
    private JPanel bottomPanel;
    private Registration ippRegistration;
    private CustomScannerView mainPanel;
    private JToggleButton replaceButton;

    public ParameterPanel(HttpRequest request, ParsedHttpParameter parameter, EncodingTree root,
                          CustomScannerView mainPanel, MontoyaApi api) {
        this.request = request;
        this.parameter = parameter;
        this.root = root;
        this.api = api;
        this.mainPanel = mainPanel;
        sendPayloadButton = new JButton("Send request with payload");
        startActiveScanButton = new JButton("Start active scan on the selected values");
        payloadTextField = new JTextField("Insert payload here");
        replaceButton = new JToggleButton("Replace value with the payload");
        replaceButton.setSelected(true);
        panel = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        buildPanel();
        this.add(panel, BorderLayout.CENTER);

        sendPayloadButton.addActionListener(e -> {
            try {
                sendRequestWithPayload();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        startActiveScanButton.addActionListener(e -> {
            startActiveScan();
        });

        replaceButton.addActionListener(e -> {
            if (replaceButton.isSelected()) {
                replaceButton.setText("Replace value with the payload");
            } else{
                replaceButton.setText("Append the payload to the value");
            }
        });
    }

    public void buildPanel(){
        tree = new JTree(root);
        panel.add(new JScrollPane(tree), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(payloadTextField, BorderLayout.CENTER);
        bottomPanel.add(sendPayloadButton, BorderLayout.EAST);
        bottomPanel.add(replaceButton, BorderLayout.WEST);
        bottomPanel.add(startActiveScanButton, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void sendRequestWithPayload() throws InterruptedException {
        EncodingTree selectedNode = (EncodingTree)tree.getLastSelectedPathComponent();
        EncodingTree temp = selectedNode.updateNode(selectedNode.getNode().getValue(), payloadTextField.getText(), true, replaceButton.isSelected());
        temp = temp.findRoot(temp);
        String newParamValue = api.utilities().urlUtils().encode(temp.getNode().getValue());
        HttpParameter updatedParam = HttpParameter.parameter(parameter.name(), newParamValue, parameter.type());
        // HTTP requests can only be sent from a different thread
        Thread sendRequestThread = new Thread(() -> {
            newHrr = api.http().sendRequest(request.withParameter(updatedParam));
            api.logging().logToOutput(newHrr.request().toString());
        });
        sendRequestThread.start();
        sendRequestThread.join();
        // Updating the editors' content
        mainPanel.setRequestContent(newHrr.request());
        mainPanel.setResponseContent(newHrr);
    }

    public void startActiveScan(){
        // Unregistering the previous insertion points
        if (ippRegistration != null){
            ippRegistration.deregister();
        }

        // Get the selected nodes
        int[] selectedNodes = tree.getSelectionRows();

        if (selectedNodes == null || selectedNodes.length == 0){
            JOptionPane.showMessageDialog(mainPanel, "At least one node has to be selected!");
            return;
        }
        List<EncodingTree> nodes = new ArrayList<>();

        for (int selectedNode : selectedNodes) {
            nodes.add((EncodingTree) tree.getPathForRow(selectedNode).getLastPathComponent());
        }

        // Registering a new Insertion Point Provider instance
        // It's basically a dummy ipp, since we already have the desired insertion points
        ippRegistration = api.scanner().registerInsertionPointProvider(new DummyAuditInsertionPointProvider(api, nodes, parameter, replaceButton.isSelected()));
        // Starting an audit with the default config, custom configs are not supported yet in the API
        Audit audit = api.scanner().startAudit(AuditConfiguration.auditConfiguration(LEGACY_ACTIVE_AUDIT_CHECKS));
        audit.addRequest(request);
        JOptionPane.showMessageDialog(mainPanel, "Active Scan has started, head over to the Active Scan tab!");
    }


}
