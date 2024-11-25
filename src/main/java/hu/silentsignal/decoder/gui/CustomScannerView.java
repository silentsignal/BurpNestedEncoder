package hu.silentsignal.decoder.gui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import hu.silentsignal.decoder.encodings.EncodingTree;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomScannerView extends JPanel{
    private MontoyaApi api;
    private HttpRequestEditor httpRequestEditor;
    private HttpResponseEditor httpResponseEditor;
    private JPanel topLeftPanel;
    private JPanel bottomLeftPanel;
    private JPanel topRightPanel;
    private EncodingTree root;
    private HttpRequestResponse hrr;
    private JTabbedPane tabbedPane;

    public CustomScannerView(MontoyaApi api) {
        this.api = api;
        initComponents();
    }

    public void initComponents(){
        root = new EncodingTree();
        setLayout(new GridLayout(2, 2));
        JPanel topLeft = new JPanel(new BorderLayout());
        JPanel topRight = new JPanel(new BorderLayout());
        JPanel bottomLeft = new JPanel(new BorderLayout());
        JPanel bottomRight = new JPanel();
        tabbedPane = new JTabbedPane();
        httpRequestEditor = api.userInterface().createHttpRequestEditor();
        topLeft.add(httpRequestEditor.uiComponent(), BorderLayout.CENTER);
        httpResponseEditor = api.userInterface().createHttpResponseEditor();
        topRight.add(httpResponseEditor.uiComponent(), BorderLayout.CENTER);
        bottomLeft.add(tabbedPane, BorderLayout.CENTER);
        this.add(topLeft);
        this.add(topRight);
        this.add(bottomLeft);
        this.add(bottomRight);
        topLeftPanel = topLeft;
        bottomLeftPanel = bottomLeft;
        topRightPanel = topRight;
    }

    public void setRequestContent(HttpRequest request){
        this.removeAll();
        initComponents();
        httpRequestEditor.setRequest(request);

        List<ParsedHttpParameter> parameters = request.parameters();

        for (ParsedHttpParameter parameter : parameters){
            String url_decoded = api.utilities().urlUtils().decode(parameter.value());
            EncodingTree root = new EncodingTree();
            root = root.fillTreeRecursively(root, url_decoded);
            ParameterPanel paramPanel = new ParameterPanel(request, parameter, root, this, api);
            tabbedPane.addTab("Parameter: " + parameter.name(), paramPanel);
        }

        this.revalidate();
        this.repaint();
    }

    public void setResponseContent(HttpRequestResponse hrr){
        httpResponseEditor.setResponse(hrr.response());
        topRightPanel.add(httpResponseEditor.uiComponent(), BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }
}
